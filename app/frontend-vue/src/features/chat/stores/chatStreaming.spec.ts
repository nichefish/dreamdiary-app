// @vitest-environment happy-dom
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import { useChatStore } from "@/features/chat/stores/chat";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn(), delete: vi.fn() },
}));

const mockedAxios = vi.mocked(axios, true);

/** 스토어가 연결하는 WebSocket 을 대체하는 제어 가능한 목 */
class MockWebSocket {
  static readonly OPEN = 1;
  static readonly CLOSED = 3;
  static instances: MockWebSocket[] = [];
  readyState = MockWebSocket.OPEN;
  url: string;
  sent: string[] = [];
  onopen: (() => void) | null = null;
  onmessage: ((event: { data: string }) => void) | null = null;
  onerror: (() => void) | null = null;
  onclose: (() => void) | null = null;

  constructor(url: string) {
    this.url = url;
    MockWebSocket.instances.push(this);
  }
  send(data: string): void {
    this.sent.push(data);
  }
  close(): void {
    this.readyState = MockWebSocket.CLOSED;
  }
}

/** STOMP 프레임 텍스트를 만든다. (헤더 블록 뒤 빈 줄로 body 를 구분, 프레임 끝은 널바이트) */
function stompFrame(command: string, headers: Record<string, string> = {}, body = ""): string {
  const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
  const headerBlock = headerLines.length ? `${headerLines.join("\n")}\n` : "";
  return `${command}\n${headerBlock}\n${body}\0`;
}

/** MESSAGE 프레임 body 로 실릴 이벤트 JSON */
function eventBody(payload: unknown): string {
  return JSON.stringify({ rslt: true, rsltObj: payload });
}

/** 가장 최근 생성된 목 소켓 */
function ws(): MockWebSocket {
  return MockWebSocket.instances[MockWebSocket.instances.length - 1];
}

/** connectWebSocket 을 거쳐 소켓이 배선된 스토어를 만든다. */
async function connectedStore() {
  const store = useChatStore();
  await store.initialize();
  return store;
}

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  MockWebSocket.instances = [];
  vi.stubGlobal("WebSocket", MockWebSocket);
  // initialize 의 fetchSetting/fetchSessions 는 빈 성공 응답
  mockedAxios.get.mockResolvedValue({
    data: { rslt: true, rsltList: [], rsltObj: {} },
  } as never);
});

describe("STOMP 연결/구독 (handleFrame)", () => {
  it("onopen 시 CONNECT 프레임을 보낸다", async () => {
    await connectedStore();
    ws().onopen?.();
    expect(ws().sent.some((f) => f.startsWith("CONNECT\n"))).toBe(true);
  });

  it("CONNECTED 수신 시 연결 상태로 전환하고 활성 세션을 구독한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    ws().onmessage?.({ data: stompFrame("CONNECTED") });
    expect(store.isConnected).toBe(true);
    expect(ws().sent.some((f) => f.includes("/topic/chat/session/1"))).toBe(true);
  });
});

describe("메시지 프레임 리듀스 (handleMessageFrame)", () => {
  it("PROGRESS 는 대기 중일 때 응답 단계를 갱신한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    store.isWaitingResponse = true;
    ws().onmessage?.({
      data: stompFrame("MESSAGE", {}, eventBody({ type: "PROGRESS", sessionId: 1, phase: "SEARCHING" })),
    });
    expect(store.responsePhase).toBe("SEARCHING");
  });

  it("DELTA 는 스트리밍 본문을 누적하고 GENERATING 으로 표시한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    store.isWaitingResponse = true;
    ws().onmessage?.({ data: stompFrame("MESSAGE", {}, eventBody({ type: "DELTA", sessionId: 1, delta: "안녕" })) });
    ws().onmessage?.({ data: stompFrame("MESSAGE", {}, eventBody({ type: "DELTA", sessionId: 1, delta: "하세요" })) });
    expect(store.streamingContent).toBe("안녕하세요");
    expect(store.responsePhase).toBe("GENERATING");
  });

  it("완성 assistant 메시지는 목록에 추가하고 대기 상태를 해제한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    store.isWaitingResponse = true;
    store.streamingContent = "부분";
    ws().onmessage?.({
      data: stompFrame("MESSAGE", {}, eventBody({ id: 9, sessionId: 1, role: "ASSISTANT", content: "완성" })),
    });
    expect(store.messages).toHaveLength(1);
    expect(store.isWaitingResponse).toBe(false);
    expect(store.responsePhase).toBeNull();
    expect(store.streamingContent).toBe("");
  });

  it("다른 세션의 프레임은 무시한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    store.isWaitingResponse = true;
    ws().onmessage?.({ data: stompFrame("MESSAGE", {}, eventBody({ type: "DELTA", sessionId: 2, delta: "x" })) });
    expect(store.streamingContent).toBe("");
  });

  it("ERROR 프레임은 오류를 남기고 대기 상태를 해제한다", async () => {
    const store = await connectedStore();
    store.isWaitingResponse = true;
    ws().onmessage?.({ data: stompFrame("ERROR", {}, "서버 오류") });
    expect(store.lastError).toBe("서버 오류");
    expect(store.isWaitingResponse).toBe(false);
  });
});

describe("sendMessage / cancelMessage", () => {
  it("연결되어 있으면 SEND 프레임을 보내고 대기 상태로 전환한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    await store.sendMessage("  안녕  ");
    const frame = ws().sent.find((f) => f.startsWith("SEND\n") && f.includes("/send"));
    expect(frame).toBeTruthy();
    expect(frame).toContain("안녕");
    expect(store.isWaitingResponse).toBe(true);
  });

  it("빈 입력이거나 대기 중이면 보내지 않는다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    await store.sendMessage("   ");
    store.isWaitingResponse = true;
    await store.sendMessage("무시");
    expect(ws().sent.some((f) => f.includes("/send"))).toBe(false);
  });

  it("소켓이 열려 있지 않으면 롤백하고 오류를 남긴다", async () => {
    const store = useChatStore();
    store.activeSessionId = 1;
    await store.sendMessage("안녕");
    expect(store.isWaitingResponse).toBe(false);
    expect(store.lastError).toBe("Chat connection is not ready yet.");
  });

  it("cancelMessage 는 취소 프레임을 보내고 대기 상태를 해제한다", async () => {
    const store = await connectedStore();
    store.activeSessionId = 1;
    store.isWaitingResponse = true;
    store.cancelMessage();
    expect(ws().sent.some((f) => f.includes("/cancel"))).toBe(true);
    expect(store.isWaitingResponse).toBe(false);
  });
});
