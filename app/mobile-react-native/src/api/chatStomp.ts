import { API_BASE_URL } from "../config/env";
import { getAccessToken } from "../auth/accessToken";
import type { ChatMessage } from "../types/chat";

export type StompHeaders = Record<string, string>;

export type ChatStompMessagePayload = {
  rslt?: boolean;
  rsltObj?: ChatMessage;
};

export type ChatStompClientOptions = {
  onConnected?: () => void;
  onDisconnected?: () => void;
  onMessage?: (message: ChatMessage) => void;
  onSessionInvalid?: () => void;
  onError?: (message: string) => void;
};

/**
 * WebSocket + STOMP 1.2 프레이밍 클라이언트.
 * 웹 `app/frontend-vue/src/stores/chat.ts` 와 동일한 엔드포인트·프레임 규약.
 */
export class ChatStompClient {
  private socket: WebSocket | null = null;
  private activeSubscriptionId = "";
  private invalidSubscriptionId = "";
  private activeSessionId: number | null = null;
  private readonly options: ChatStompClientOptions;

  constructor(options: ChatStompClientOptions = {}) {
    this.options = options;
  }

  get isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN && this.connectedFlag;
  }

  private connectedFlag = false;

  connect(): void {
    if (this.socket && this.socket.readyState <= WebSocket.OPEN) return;

    const token = getAccessToken();
    // RN WebSocket: Authorization 헤더 (실기기에서 HttpOnly 쿠키 미전달 대비)
    const wsInit = token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : undefined;
    type ReactNativeWebSocketCtor = new (
      url: string,
      protocols?: string | string[],
      options?: { headers?: Record<string, string> }
    ) => WebSocket;
    const ReactNativeWebSocket = WebSocket as unknown as ReactNativeWebSocketCtor;
    this.socket = new ReactNativeWebSocket(chatWebSocketUrl(), undefined, wsInit);
    this.socket.onopen = () => {
      this.sendFrame("CONNECT", {
        "accept-version": "1.2",
        "heart-beat": "10000,10000"
      });
    };
    this.socket.onmessage = (event) => {
      const data = typeof event.data === "string" ? event.data : "";
      data.split("\0").forEach((frame) => this.handleFrame(frame));
    };
    this.socket.onerror = () => {
      this.connectedFlag = false;
      this.options.onError?.("채팅 서버에 연결하지 못했습니다.");
    };
    this.socket.onclose = () => {
      this.connectedFlag = false;
      this.activeSubscriptionId = "";
      this.invalidSubscriptionId = "";
      this.options.onDisconnected?.();
    };
  }

  disconnect(): void {
    if (this.activeSubscriptionId) this.unsubscribe(this.activeSubscriptionId);
    if (this.invalidSubscriptionId) this.unsubscribe(this.invalidSubscriptionId);
    this.sendFrame("DISCONNECT");
    this.socket?.close();
    this.socket = null;
    this.connectedFlag = false;
    this.activeSubscriptionId = "";
    this.invalidSubscriptionId = "";
    this.activeSessionId = null;
  }

  subscribeSession(sessionId: number | null): void {
    this.activeSessionId = sessionId;
    if (!this.isConnected || sessionId == null) return;
    if (this.activeSubscriptionId) this.unsubscribe(this.activeSubscriptionId);
    this.activeSubscriptionId = `chat-session-${sessionId}`;
    this.subscribe(`/topic/chat/session/${sessionId}`, this.activeSubscriptionId);
  }

  sendChatMessage(sessionId: number, content: string): boolean {
    return this.sendFrame(
      "SEND",
      {
        destination: `/app/chat/session/${sessionId}/send`,
        "content-type": "text/plain;charset=UTF-8"
      },
      content
    );
  }

  cancelChatMessage(sessionId: number): boolean {
    return this.sendFrame("SEND", {
      destination: `/app/chat/session/${sessionId}/cancel`
    });
  }

  private subscribe(destination: string, id: string): void {
    this.sendFrame("SUBSCRIBE", { id, destination });
  }

  private unsubscribe(id: string): void {
    if (!id) return;
    this.sendFrame("UNSUBSCRIBE", { id });
  }

  private sendFrame(command: string, headers: StompHeaders = {}, body = ""): boolean {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return false;
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
    this.socket.send(`${command}\n${headerLines.join("\n")}\n\n${body}\0`);
    return true;
  }

  private subscribeToSessionInvalid(): void {
    if (this.invalidSubscriptionId) return;
    this.invalidSubscriptionId = "chat-session-invalid";
    this.subscribe("/user/queue/session-invalid", this.invalidSubscriptionId);
  }

  private handleFrame(rawFrame: string): void {
    const frame = parseStompFrame(rawFrame);
    if (!frame) return;

    if (frame.command === "CONNECTED") {
      this.connectedFlag = true;
      this.options.onConnected?.();
      this.subscribeToSessionInvalid();
      if (this.activeSessionId != null) {
        this.subscribeSession(this.activeSessionId);
      }
      return;
    }

    if (frame.command === "MESSAGE") {
      if (frame.headers.subscription === this.invalidSubscriptionId) {
        this.options.onSessionInvalid?.();
        return;
      }
      this.handleMessageBody(frame.body);
      return;
    }

    if (frame.command === "ERROR") {
      this.options.onError?.(frame.body || "채팅 연결 오류");
    }
  }

  private handleMessageBody(body: string): void {
    if (!body) return;
    try {
      const response = JSON.parse(body) as ChatStompMessagePayload;
      const nextMessage = response.rsltObj;
      if (!nextMessage || nextMessage.sessionId !== this.activeSessionId) return;
      this.options.onMessage?.(nextMessage);
    } catch {
      this.options.onError?.("메시지 형식을 해석하지 못했습니다.");
    }
  }
}

/** API_BASE_URL 기준 ws/wss `/chat` URL */
export function chatWebSocketUrl(): string {
  const url = new URL(API_BASE_URL);
  const protocol = url.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${url.host}/chat`;
}

export function isAssistantChatMessage(message: ChatMessage): boolean {
  const role = (message.role ?? "").toUpperCase();
  return role === "ASSISTANT" || role === "AI" || role === "SYSTEM";
}

function parseStompFrame(rawFrame: string) {
  const normalized = rawFrame.replace(/\r/g, "").replace(/^\n+/, "");
  if (!normalized.trim()) return null;

  const separatorIndex = normalized.indexOf("\n\n");
  const headerBlock = separatorIndex >= 0 ? normalized.slice(0, separatorIndex) : normalized;
  const body = separatorIndex >= 0 ? normalized.slice(separatorIndex + 2) : "";
  const lines = headerBlock.split("\n");
  const command = lines.shift() ?? "";
  const headers: StompHeaders = {};

  lines.forEach((line) => {
    const colonIndex = line.indexOf(":");
    if (colonIndex <= 0) return;
    headers[line.slice(0, colonIndex)] = line.slice(colonIndex + 1);
  });

  return { command, headers, body };
}
