import { ref } from "vue";
import { defineStore } from "pinia";
import { apiGet, apiPost, apiPatch, apiDelete, assertOk } from "@/shared/api/client";
import type { AjaxResponse } from "@/shared/api/types";

export const MEMORY_LIMIT_OPTIONS = [25, 50, 100, 200] as const;
export const DEFAULT_MEMORY_LIMIT = 50;

export interface ChatSetting {
  id?: number;
  scope?: string;
  scopeKey?: string;
  recentMessageLimit: number;
}

export function normalizeRecentMessageLimit(value: unknown): number {
  const parsed = Number(value);
  if (
    MEMORY_LIMIT_OPTIONS.includes(
      parsed as (typeof MEMORY_LIMIT_OPTIONS)[number]
    )
  ) {
    return parsed;
  }
  return DEFAULT_MEMORY_LIMIT;
}

export interface ChatSession {
  id: number;
  title?: string;
  status?: string;
  model?: string;
  systemPrompt?: string;
  lastMessageAt?: string;
  createdAt?: string;
}

export interface ChatProgressEvent {
  type: "PROGRESS";
  sessionId: number;
  phase: "SEARCHING" | "GENERATING";
}

export interface ChatDeltaEvent {
  type: "DELTA";
  sessionId: number;
  delta: string;
}

export interface ChatMessage {

  id?: number;
  isCreatedBy?: boolean;
  title?: string;
  content?: string;
  markdownContent?: string;
  categoryCode?: string;
  role?: string;
  sessionId?: number;
  seq?: number;
  metadataJson?: string;
  createdAt?: string;
  createdByNm?: string;
}

type StompHeaders = Record<string, string>;


function isAssistantMessage(message: ChatMessage): boolean {
  const role = (message.role || "").toUpperCase();
  return role === "ASSISTANT" || role === "AI" || role === "SYSTEM";
}

function parseStompFrame(rawFrame: string) {
  const normalized = rawFrame.replace(/\r/g, "").replace(/^\n+/, "");
  if (!normalized.trim()) return null;

  const separatorIndex = normalized.indexOf("\n\n");
  const headerBlock =
    separatorIndex >= 0 ? normalized.slice(0, separatorIndex) : normalized;
  const body =
    separatorIndex >= 0 ? normalized.slice(separatorIndex + 2) : "";
  const lines = headerBlock.split("\n");
  const command = lines.shift() || "";
  const headers: StompHeaders = {};

  lines.forEach((line) => {
    const colonIndex = line.indexOf(":");
    if (colonIndex <= 0) return;
    headers[line.slice(0, colonIndex)] = line.slice(colonIndex + 1);
  });

  return { command, headers, body };
}

export const useChatStore = defineStore("chat", () => {
  const isOpen = ref(false);
  const isInitialized = ref(false);
  const isConnected = ref(false);
  const isWaitingResponse = ref(false);
  /** AI 응답 진행 단계: SEARCHING | GENERATING | null */
  const responsePhase = ref<"SEARCHING" | "GENERATING" | null>(null);
  /** 본경로 스트리밍 중 임시 assistant 본문 (완성 메시지 도착 시 비움) */
  const streamingContent = ref("");
  const isSessionLoading = ref(false);
  const isSettingSaving = ref(false);
  const lastError = ref("");

  const setting = ref<ChatSetting>({
    recentMessageLimit: DEFAULT_MEMORY_LIMIT,
  });
  const sessions = ref<ChatSession[]>([]);
  const activeSessionId = ref<number | null>(null);
  const messages = ref<ChatMessage[]>([]);

  let socket: WebSocket | null = null;
  let activeSubscriptionId = "";
  let invalidSubscriptionId = "";

  function sendFrame(
    command: string,
    headers: StompHeaders = {},
    body = ""
  ): boolean {
    if (!socket || socket.readyState !== WebSocket.OPEN) return false;

    const headerLines = Object.entries(headers).map(
      ([key, value]) => `${key}:${value}`
    );
    socket.send(`${command}\n${headerLines.join("\n")}\n\n${body}\0`);
    return true;
  }

  function subscribe(destination: string, id: string): void {
    sendFrame("SUBSCRIBE", { id, destination });
  }

  function unsubscribe(id: string): void {
    if (!id) return;
    sendFrame("UNSUBSCRIBE", { id });
  }

  function subscribeToSession(sessionId: number): void {
    if (!isConnected.value || !sessionId) return;
    if (activeSubscriptionId) unsubscribe(activeSubscriptionId);

    activeSubscriptionId = `chat-session-${sessionId}`;
    subscribe(`/topic/chat/session/${sessionId}`, activeSubscriptionId);
  }

  function subscribeToSessionInvalid(): void {
    if (invalidSubscriptionId) return;
    invalidSubscriptionId = "chat-session-invalid";
    subscribe("/user/queue/session-invalid", invalidSubscriptionId);
  }

  function websocketUrl(): string {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    return `${protocol}://${window.location.host}/chat`;
  }

  function handleMessageFrame(body: string): void {
    if (!body) return;

    const response = JSON.parse(body) as AjaxResponse<
      ChatMessage | ChatProgressEvent | ChatDeltaEvent
    >;
    const nextPayload = response.rsltObj;
    if (!nextPayload || nextPayload.sessionId !== activeSessionId.value) return;

    if (isProgressEvent(nextPayload)) {
      if (isWaitingResponse.value) {
        responsePhase.value = nextPayload.phase;
      }
      return;
    }

    if (isDeltaEvent(nextPayload)) {
      if (!isWaitingResponse.value) return;
      streamingContent.value += nextPayload.delta || "";
      if (responsePhase.value !== "GENERATING") {
        responsePhase.value = "GENERATING";
      }
      return;
    }

    messages.value.push(nextPayload);
    bumpActiveSession(nextPayload);

    if (isAssistantMessage(nextPayload) || nextPayload.isCreatedBy === false) {
      isWaitingResponse.value = false;
      responsePhase.value = null;
      streamingContent.value = "";
    }
  }

  function isProgressEvent(
    payload: ChatMessage | ChatProgressEvent | ChatDeltaEvent
  ): payload is ChatProgressEvent {
    return (
      (payload as ChatProgressEvent).type === "PROGRESS" &&
      ((payload as ChatProgressEvent).phase === "SEARCHING" ||
        (payload as ChatProgressEvent).phase === "GENERATING")
    );
  }

  function isDeltaEvent(
    payload: ChatMessage | ChatProgressEvent | ChatDeltaEvent
  ): payload is ChatDeltaEvent {
    return (payload as ChatDeltaEvent).type === "DELTA";
  }

  function handleFrame(rawFrame: string): void {
    const frame = parseStompFrame(rawFrame);
    if (!frame) return;

    if (frame.command === "CONNECTED") {
      isConnected.value = true;
      subscribeToSessionInvalid();
      if (activeSessionId.value) subscribeToSession(activeSessionId.value);
      return;
    }

    if (frame.command === "MESSAGE") {
      if (frame.headers.subscription === invalidSubscriptionId) {
        reset();
        return;
      }

      handleMessageFrame(frame.body);
      return;
    }

    if (frame.command === "ERROR") {
      lastError.value = frame.body || "Chat connection error.";
      isWaitingResponse.value = false;
      responsePhase.value = null;
      streamingContent.value = "";
    }
  }

  function connectWebSocket(): void {
    if (socket && socket.readyState <= WebSocket.OPEN) return;

    socket = new WebSocket(websocketUrl());
    socket.onopen = () => {
      sendFrame("CONNECT", {
        "accept-version": "1.2",
        "heart-beat": "10000,10000",
      });
    };
    socket.onmessage = (event: MessageEvent<string>) => {
      event.data.split("\0").forEach(handleFrame);
    };
    socket.onerror = () => {
      lastError.value = "Unable to connect to chat server.";
      isWaitingResponse.value = false;
      responsePhase.value = null;
      streamingContent.value = "";
    };
    socket.onclose = () => {
      isConnected.value = false;
      activeSubscriptionId = "";
      invalidSubscriptionId = "";
    };
  }

  function disconnectWebSocket(): void {
    if (activeSubscriptionId) unsubscribe(activeSubscriptionId);
    if (invalidSubscriptionId) unsubscribe(invalidSubscriptionId);
    sendFrame("DISCONNECT");

    socket?.close();
    socket = null;
    isConnected.value = false;
    activeSubscriptionId = "";
    invalidSubscriptionId = "";
  }

  function applySetting(nextSetting: ChatSetting): void {
    setting.value = {
      ...nextSetting,
      recentMessageLimit: normalizeRecentMessageLimit(
        nextSetting.recentMessageLimit
      ),
    };
  }

  async function fetchSetting(): Promise<void> {
    const res = await apiGet<ChatSetting>("/chat/settings");
    assertOk(res, "Request failed.");
    if (res.rsltObj) applySetting(res.rsltObj);
  }

  async function updateSetting(nextSetting: ChatSetting): Promise<void> {
    isSettingSaving.value = true;
    try {
      const res = await apiPatch<ChatSetting>("/chat/settings", nextSetting);
      assertOk(res, "Request failed.");
      if (res.rsltObj) applySetting(res.rsltObj);
    } finally {
      isSettingSaving.value = false;
    }
  }

  async function fetchSessions(): Promise<void> {
    const res = await apiGet<ChatSession>("/chat/sessions");
    assertOk(res, "Request failed.");
    sessions.value = res.rsltList || [];
    if (!activeSessionId.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0].id);
    }
  }

  async function createSession(): Promise<ChatSession | null> {
    isSessionLoading.value = true;
    try {
      const res = await apiPost<ChatSession>("/chat/sessions", {});
      assertOk(res, "Request failed.");
      const session = res.rsltObj || null;
      if (!session) return null;

      sessions.value = [
        session,
        ...sessions.value.filter((item) => item.id !== session.id),
      ];
      await selectSession(session.id);
      return session;
    } finally {
      isSessionLoading.value = false;
    }
  }

  async function deleteSession(sessionId: number): Promise<void> {
    if (!sessionId) return;

    isSessionLoading.value = true;
    try {
      const res = await apiDelete(`/chat/sessions/${sessionId}`);
      assertOk(res, "Request failed.");

      sessions.value = sessions.value.filter((item) => item.id !== sessionId);
      if (activeSessionId.value !== sessionId) return;

      activeSessionId.value = null;
      messages.value = [];
      if (sessions.value.length > 0) {
        await selectSession(sessions.value[0].id);
      }
    } finally {
      isSessionLoading.value = false;
    }
  }

  async function fetchMessages(sessionId: number): Promise<void> {
    if (!sessionId) {
      messages.value = [];
      return;
    }

    const res = await apiGet<ChatMessage>(
      `/chat/sessions/${sessionId}/messages`
    );
    assertOk(res, "Request failed.");
    messages.value = res.rsltList || [];
  }

  async function selectSession(sessionId: number): Promise<void> {
    if (!sessionId || activeSessionId.value === sessionId) return;

    activeSessionId.value = sessionId;
    isWaitingResponse.value = false;
    responsePhase.value = null;
    streamingContent.value = "";
    messages.value = [];
    subscribeToSession(sessionId);
    await fetchMessages(sessionId);
  }

  async function ensureActiveSession(): Promise<void> {
    if (activeSessionId.value) return;
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0].id);
      return;
    }

    await createSession();
  }

  async function open(): Promise<void> {
    isOpen.value = true;
    await ensureActiveSession();
  }

  function close(): void {
    isOpen.value = false;
  }

  async function toggle(): Promise<void> {
    if (isOpen.value) {
      close();
      return;
    }
    await open();
  }


/**
 * 서버 기본 제목(`새 대화`) 및 레거시/카탈로그 `New chat`을 동일한 미설정 제목으로 본다.
 * 수동 제목은 이 조건에 걸리지 않으므로 자동 축약으로 덮어쓰지 않는다.
 */
function isDefaultSessionTitle(title: string | undefined | null): boolean {
  const value = (title || "").trim();
  return !value || value === "New chat" || value === "새 대화";
}

  function bumpActiveSession(message: ChatMessage): void {
    const session = sessions.value.find(
      (item) => item.id === activeSessionId.value
    );
    if (!session) return;

    session.lastMessageAt = message.createdAt || session.lastMessageAt;
    if (isDefaultSessionTitle(session.title) && message.role === "USER") {
      const compact = (message.content || "").replace(/\s+/g, " ").trim();
      session.title = compact.length > 28 ? `${compact.slice(0, 28)}...` : compact;
    }

    sessions.value = [
      session,
      ...sessions.value.filter((item) => item.id !== session.id),
    ];
  }

  async function sendMessage(content: string): Promise<void> {
    const trimmed = content.trim();
    if (!trimmed || isWaitingResponse.value) return;

    await ensureActiveSession();
    if (!activeSessionId.value) return;

    isWaitingResponse.value = true;
    responsePhase.value = null;
    streamingContent.value = "";
    const sent = sendFrame(
      "SEND",
      {
        destination: `/app/chat/session/${activeSessionId.value}/send`,
        "content-type": "text/plain;charset=UTF-8",
      },
      trimmed
    );

    if (!sent) {
      isWaitingResponse.value = false;
      responsePhase.value = null;
      streamingContent.value = "";
      lastError.value = "Chat connection is not ready yet.";
    }
  }

  function cancelMessage(): void {
    if (!activeSessionId.value) return;
    sendFrame("SEND", {
      destination: `/app/chat/session/${activeSessionId.value}/cancel`,
    });
    isWaitingResponse.value = false;
    responsePhase.value = null;
    streamingContent.value = "";
  }

  async function initialize(): Promise<void> {
    if (isInitialized.value) return;

    lastError.value = "";
    connectWebSocket();
    await Promise.all([fetchSetting(), fetchSessions()]);
    isInitialized.value = true;
  }

  function reset(): void {
    disconnectWebSocket();
    isOpen.value = false;
    isInitialized.value = false;
    isWaitingResponse.value = false;
    responsePhase.value = null;
    streamingContent.value = "";
    isSessionLoading.value = false;
    isSettingSaving.value = false;
    setting.value = { recentMessageLimit: DEFAULT_MEMORY_LIMIT };
    sessions.value = [];
    activeSessionId.value = null;
    messages.value = [];
  }


  async function renameSession(sessionId: number, title: string): Promise<ChatSession | null> {
    const trimmed = title.trim();
    if (!sessionId || !trimmed) return null;

    const res = await apiPatch<ChatSession>(`/chat/sessions/${sessionId}`, { title: trimmed });
    assertOk(res, "Request failed.");
    const updated = res.rsltObj || null;
    if (!updated) return null;

    sessions.value = sessions.value.map((item) =>
      item.id === sessionId ? { ...item, ...updated } : item
    );
    return updated;
  }

  return {
    isOpen,
    isInitialized,
    isConnected,
    isWaitingResponse,
    responsePhase,
    streamingContent,
    isSessionLoading,
    isSettingSaving,
    lastError,
    setting,
    sessions,
    activeSessionId,
    messages,
    initialize,
    reset,
    open,
    close,
    toggle,
    selectSession,
    createSession,
    deleteSession,
    renameSession,
    updateSetting,
    sendMessage,
    cancelMessage,
  };
});
