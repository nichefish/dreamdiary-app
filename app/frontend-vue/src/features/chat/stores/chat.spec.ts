import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import axios from "axios";
import {
  DEFAULT_MEMORY_LIMIT,
  normalizeRecentMessageLimit,
  useChatStore,
  type ChatMessage,
  type ChatSession,
} from "@/features/chat/stores/chat";

vi.mock("axios", () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn(), delete: vi.fn() },
}));

const mockedAxios = vi.mocked(axios, true);

/**
 * 성공 AjaxResponse 형태의 axios 응답을 만든다.
 * 스토어는 `.data` 만 읽으므로 AxiosResponse 의 나머지 필드는 생략하고 반환을 any 로 둔다.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function ok(payload: Record<string, unknown> = {}): any {
  return { data: { rslt: true, ...payload } };
}

const sessions = (...list: ChatSession[]): ChatSession[] => list;
const message = (id: number): ChatMessage => ({ id, role: "user" }) as ChatMessage;

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  // selectSession → fetchMessages 기본값(빈 목록)
  mockedAxios.get.mockResolvedValue(ok({ rsltList: [] }));
});

describe("normalizeRecentMessageLimit", () => {
  it("허용 옵션은 유지, 그 외는 기본값으로 정규화한다", () => {
    expect(normalizeRecentMessageLimit(100)).toBe(100);
    expect(normalizeRecentMessageLimit("25")).toBe(25);
    expect(normalizeRecentMessageLimit(999)).toBe(DEFAULT_MEMORY_LIMIT);
    expect(normalizeRecentMessageLimit(null)).toBe(DEFAULT_MEMORY_LIMIT);
    expect(normalizeRecentMessageLimit(undefined)).toBe(DEFAULT_MEMORY_LIMIT);
  });
});

describe("updateSetting", () => {
  it("서버 응답을 정규화해 적용하고 저장 플래그를 되돌린다", async () => {
    const store = useChatStore();
    mockedAxios.patch.mockResolvedValue(ok({ rsltObj: { recentMessageLimit: 999 } }));

    await store.updateSetting({ recentMessageLimit: 100 });

    expect(mockedAxios.patch).toHaveBeenCalledWith("/chat/settings", {
      recentMessageLimit: 100,
    });
    // 999 는 허용 옵션이 아니므로 기본값으로 정규화된다.
    expect(store.setting.recentMessageLimit).toBe(DEFAULT_MEMORY_LIMIT);
    expect(store.isSettingSaving).toBe(false);
  });
});

describe("renameSession", () => {
  it("성공 시 세션 목록을 갱신하고 업데이트된 세션을 반환한다", async () => {
    const store = useChatStore();
    store.sessions = sessions({ id: 1, title: "old" }, { id: 2, title: "keep" });
    mockedAxios.patch.mockResolvedValue(ok({ rsltObj: { id: 1, title: "new" } }));

    const result = await store.renameSession(1, "  new  ");

    expect(mockedAxios.patch).toHaveBeenCalledWith("/chat/sessions/1", { title: "new" });
    expect(result).toEqual({ id: 1, title: "new" });
    expect(store.sessions.find((s) => s.id === 1)?.title).toBe("new");
    expect(store.sessions.find((s) => s.id === 2)?.title).toBe("keep");
  });

  it("빈 제목이거나 세션ID 가 없으면 요청 없이 null 을 반환한다", async () => {
    const store = useChatStore();
    expect(await store.renameSession(1, "   ")).toBeNull();
    expect(await store.renameSession(0, "x")).toBeNull();
    expect(mockedAxios.patch).not.toHaveBeenCalled();
  });

  it("응답에 세션이 없으면 null 을 반환한다", async () => {
    const store = useChatStore();
    mockedAxios.patch.mockResolvedValue(ok({}));
    expect(await store.renameSession(1, "x")).toBeNull();
  });
});

describe("createSession", () => {
  it("새 세션을 맨 앞에 추가(중복 제거)하고 선택한다", async () => {
    const store = useChatStore();
    store.sessions = sessions({ id: 1, title: "a" });
    mockedAxios.post.mockResolvedValue(ok({ rsltObj: { id: 2, title: "new" } }));

    const created = await store.createSession();

    expect(created).toEqual({ id: 2, title: "new" });
    expect(store.sessions[0].id).toBe(2);
    expect(store.activeSessionId).toBe(2);
    expect(store.isSessionLoading).toBe(false);
  });

  it("응답에 세션이 없으면 null 을 반환한다", async () => {
    const store = useChatStore();
    mockedAxios.post.mockResolvedValue(ok({}));
    expect(await store.createSession()).toBeNull();
  });
});

describe("deleteSession", () => {
  it("세션을 제거하고 활성 세션이면 다음 세션으로 이동한다", async () => {
    const store = useChatStore();
    store.sessions = sessions({ id: 1 }, { id: 2 });
    store.activeSessionId = 1;
    mockedAxios.delete.mockResolvedValue(ok({}));

    await store.deleteSession(1);

    expect(mockedAxios.delete).toHaveBeenCalledWith("/chat/sessions/1");
    expect(store.sessions.map((s) => s.id)).toEqual([2]);
    expect(store.activeSessionId).toBe(2);
  });

  it("sessionId 가 없으면 아무 요청도 보내지 않는다", async () => {
    const store = useChatStore();
    await store.deleteSession(0);
    expect(mockedAxios.delete).not.toHaveBeenCalled();
  });
});

describe("selectSession / reset / close", () => {
  it("selectSession 은 활성 세션을 바꾸고 메시지를 로드한다", async () => {
    const store = useChatStore();
    mockedAxios.get.mockResolvedValue(ok({ rsltList: [message(10)] }));

    await store.selectSession(7);

    expect(store.activeSessionId).toBe(7);
    expect(mockedAxios.get).toHaveBeenCalledWith("/chat/sessions/7/messages");
    expect(store.messages).toHaveLength(1);
  });

  it("reset 은 상태를 초기값으로 되돌린다", () => {
    const store = useChatStore();
    store.isOpen = true;
    store.sessions = sessions({ id: 1 });
    store.activeSessionId = 1;
    store.messages = [message(5)];

    store.reset();

    expect(store.isOpen).toBe(false);
    expect(store.sessions).toEqual([]);
    expect(store.activeSessionId).toBeNull();
    expect(store.messages).toEqual([]);
    expect(store.setting.recentMessageLimit).toBe(DEFAULT_MEMORY_LIMIT);
  });

  it("close 는 패널을 닫는다", () => {
    const store = useChatStore();
    store.isOpen = true;
    store.close();
    expect(store.isOpen).toBe(false);
  });
});
