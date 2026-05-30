import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import ApiService from "@metronic/core/services/ApiService";

interface AjaxResponse<T = unknown> {
  rslt?: boolean;
  msg?: string;
  message?: string;
  rsltObj?: T;
  rsltList?: T[];
}

export interface ChatSetting {
  id?: number;
  scope?: string;
  scopeKey?: string;
  recentMessageLimit: number;
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

function getResponseMessage(data: AjaxResponse): string {
  return data.message || data.msg || "Request failed.";
}

function assertSuccess<T>(data: AjaxResponse<T>): AjaxResponse<T> {
  if (!data.rslt) {
    throw new Error(getResponseMessage(data));
  }
  return data;
}

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
  const isSessionLoading = ref(false);
  const isSettingSaving = ref(false);
  const lastError = ref("");

  const setting = ref<ChatSetting>({ recentMessageLimit: 20 });
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
    subscribe("/topic/session-invalid", invalidSubscriptionId);
  }

  function websocketUrl(): string {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    return `${protocol}://${window.location.host}/chat`;
  }

  function handleMessageFrame(body: string): void {
    if (!body) return;

    const response = JSON.parse(body) as AjaxResponse<ChatMessage>;
    const nextMessage = response.rsltObj;
    if (!nextMessage || nextMessage.sessionId !== activeSessionId.value) return;

    messages.value.push(nextMessage);
    bumpActiveSession(nextMessage);

    if (isAssistantMessage(nextMessage) || nextMessage.isCreatedBy === false) {
      isWaitingResponse.value = false;
    }
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
      if (frame.headers.destination === "/topic/session-invalid") {
        reset();
        return;
      }

      handleMessageFrame(frame.body);
      return;
    }

    if (frame.command === "ERROR") {
      lastError.value = frame.body || "Chat connection error.";
      isWaitingResponse.value = false;
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

  async function fetchSetting(): Promise<void> {
    const { data } = await ApiService.get("/chat/settings");
    const response = assertSuccess<ChatSetting>(data);
    if (response.rsltObj) setting.value = response.rsltObj;
  }

  async function updateSetting(nextSetting: ChatSetting): Promise<void> {
    isSettingSaving.value = true;
    try {
      const { data } = await axios.patch("/chat/settings", nextSetting);
      const response = assertSuccess<ChatSetting>(data);
      if (response.rsltObj) setting.value = response.rsltObj;
    } finally {
      isSettingSaving.value = false;
    }
  }

  async function fetchSessions(): Promise<void> {
    const { data } = await ApiService.get("/chat/sessions");
    const response = assertSuccess<ChatSession>(data);
    sessions.value = response.rsltList || [];
    if (!activeSessionId.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0].id);
    }
  }

  async function createSession(): Promise<ChatSession | null> {
    isSessionLoading.value = true;
    try {
      const { data } = await ApiService.post("/chat/sessions", {});
      const response = assertSuccess<ChatSession>(data);
      const session = response.rsltObj || null;
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
      const { data } = await ApiService.delete(`/chat/sessions/${sessionId}`);
      assertSuccess(data);

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

    const { data } = await ApiService.get(
      `/chat/sessions/${sessionId}/messages`
    );
    const response = assertSuccess<ChatMessage>(data);
    messages.value = response.rsltList || [];
  }

  async function selectSession(sessionId: number): Promise<void> {
    if (!sessionId || activeSessionId.value === sessionId) return;

    activeSessionId.value = sessionId;
    isWaitingResponse.value = false;
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

  function bumpActiveSession(message: ChatMessage): void {
    const session = sessions.value.find(
      (item) => item.id === activeSessionId.value
    );
    if (!session) return;

    session.lastMessageAt = message.createdAt || session.lastMessageAt;
    if ((!session.title || session.title === "New chat") && message.role === "USER") {
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
      lastError.value = "Chat connection is not ready yet.";
    }
  }

  function cancelMessage(): void {
    if (!activeSessionId.value) return;
    sendFrame("SEND", {
      destination: `/app/chat/session/${activeSessionId.value}/cancel`,
    });
    isWaitingResponse.value = false;
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
    isSessionLoading.value = false;
    isSettingSaving.value = false;
    setting.value = { recentMessageLimit: 20 };
    sessions.value = [];
    activeSessionId.value = null;
    messages.value = [];
  }

  return {
    isOpen,
    isInitialized,
    isConnected,
    isWaitingResponse,
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
    updateSetting,
    sendMessage,
    cancelMessage,
  };
});
