import { useCallback, useEffect, useRef, useState } from "react";
import { ChatStompClient, isAssistantChatMessage } from "../api/chatStomp";
import { getAccessToken, subscribeAccessToken } from "../auth/accessToken";
import type { ChatMessage, ChatSession } from "../types/chat";

export type UseChatStompOptions = {
  activeSessionId: number | null;
  onIncomingMessage: (message: ChatMessage) => void;
  onSessionInvalid?: () => void;
  bumpSession?: (message: ChatMessage) => void;
};

export type UseChatStompResult = {
  connected: boolean;
  waitingResponse: boolean;
  connectionError: string | null;
  sendMessage: (content: string) => boolean;
  cancelMessage: () => void;
  clearConnectionError: () => void;
};

/**
 * AI 채팅 STOMP WebSocket 연결·세션 구독·전송.
 * access JWT 확보(hydrate/login/refresh) 후 토큰이 생기면 재연결한다.
 */
export function useChatStomp({
  activeSessionId,
  onIncomingMessage,
  onSessionInvalid,
  bumpSession
}: UseChatStompOptions): UseChatStompResult {
  const clientRef = useRef<ChatStompClient | null>(null);
  const onIncomingRef = useRef(onIncomingMessage);
  const onInvalidRef = useRef(onSessionInvalid);
  const bumpRef = useRef(bumpSession);
  const activeSessionIdRef = useRef(activeSessionId);

  const [connected, setConnected] = useState(false);
  const [waitingResponse, setWaitingResponse] = useState(false);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  /** 토큰 준비·갱신 시 WebSocket 재연결 트리거 */
  const [connectGeneration, setConnectGeneration] = useState(0);

  onIncomingRef.current = onIncomingMessage;
  onInvalidRef.current = onSessionInvalid;
  bumpRef.current = bumpSession;
  activeSessionIdRef.current = activeSessionId;

  useEffect(() => {
    return subscribeAccessToken(() => {
      if (getAccessToken()) {
        setConnectGeneration((g) => g + 1);
      }
    });
  }, []);

  useEffect(() => {
    const client = new ChatStompClient({
      onConnected: () => {
        setConnected(true);
        setConnectionError(null);
        const sessionId = activeSessionIdRef.current;
        if (sessionId != null) client.subscribeSession(sessionId);
      },
      onDisconnected: () => {
        setConnected(false);
        setWaitingResponse(false);
      },
      onMessage: (message) => {
        onIncomingRef.current(message);
        bumpRef.current?.(message);
        if (isAssistantChatMessage(message) || message.isCreatedBy === false) {
          setWaitingResponse(false);
        }
      },
      onSessionInvalid: () => {
        setWaitingResponse(false);
        onInvalidRef.current?.();
      },
      onError: (msg) => {
        setConnectionError(msg);
        setWaitingResponse(false);
      }
    });

    client.connect();
    clientRef.current = client;

    return () => {
      client.disconnect();
      clientRef.current = null;
      setConnected(false);
    };
  }, [connectGeneration]);

  useEffect(() => {
    clientRef.current?.subscribeSession(activeSessionId);
    setWaitingResponse(false);
  }, [activeSessionId, connected]);

  const sendMessage = useCallback(
    (content: string) => {
      const trimmed = content.trim();
      if (!trimmed || waitingResponse || activeSessionId == null) return false;

      setWaitingResponse(true);
      const sent = clientRef.current?.sendChatMessage(activeSessionId, trimmed) ?? false;
      if (!sent) {
        setWaitingResponse(false);
        setConnectionError("채팅 연결이 준비되지 않았습니다.");
      }
      return sent;
    },
    [activeSessionId, waitingResponse]
  );

  const cancelMessage = useCallback(() => {
    if (activeSessionId == null) return;
    clientRef.current?.cancelChatMessage(activeSessionId);
    setWaitingResponse(false);
  }, [activeSessionId]);

  const clearConnectionError = useCallback(() => setConnectionError(null), []);

  return {
    connected,
    waitingResponse,
    connectionError,
    sendMessage,
    cancelMessage,
    clearConnectionError
  };
}

/** 수신 메시지로 세션 목록 정렬·제목 갱신 (웹 chat store bumpActiveSession 동일) */
export function bumpChatSessionFromMessage(
  sessions: ChatSession[],
  activeSessionId: number,
  message: ChatMessage
): ChatSession[] {
  const session = sessions.find((s) => s.id === activeSessionId);
  if (!session) return sessions;

  const updated: ChatSession = {
    ...session,
    lastMessageAt: message.createdAt ?? session.lastMessageAt
  };

  if ((!updated.title || updated.title === "New chat") && message.role === "USER") {
    const compact = (message.content ?? "").replace(/\s+/g, " ").trim();
    updated.title = compact.length > 28 ? `${compact.slice(0, 28)}...` : compact;
  }

  return [updated, ...sessions.filter((s) => s.id !== activeSessionId)];
}
