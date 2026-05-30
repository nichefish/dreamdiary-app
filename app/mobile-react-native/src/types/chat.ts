/** 백엔드 ChatSessionDto 대응 */
export interface ChatSession {
  id: number;
  title?: string;
  status?: string;
  model?: string;
  /** 마지막 메시지 일시 */
  lastMessageAt?: string;
}

/** 백엔드 ChatMessageDto 대응 */
export interface ChatMessage {
  id: number;
  /** "USER" | "ASSISTANT" */
  role?: string;
  content?: string;
  markdownContent?: string;
  /** true = 현재 사용자가 보낸 메시지 */
  isCreatedBy?: boolean;
  sessionId?: number;
  seq?: number;
  createdAt?: string;
}
