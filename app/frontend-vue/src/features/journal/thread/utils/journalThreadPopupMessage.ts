/** 스레드 수정 팝업이 원래 창에 보내는 동일 출처 메시지 유형. */
export const JOURNAL_THREAD_UPDATED_MESSAGE_TYPE = "journal-thread-updated";

export interface JournalThreadUpdatedMessage {
  type: typeof JOURNAL_THREAD_UPDATED_MESSAGE_TYPE;
  threadId: number;
}

/** 수정 완료 메시지를 생성한다. */
export function createJournalThreadUpdatedMessage(threadId: number): JournalThreadUpdatedMessage {
  return {
    type: JOURNAL_THREAD_UPDATED_MESSAGE_TYPE,
    threadId,
  };
}

/**
 * 외부 창에서 받은 값을 검증해 수정된 스레드 ID를 반환한다.
 * 출처(origin) 검사는 브라우저 MessageEvent를 소유한 호출자가 별도로 수행한다.
 */
export function parseJournalThreadUpdatedMessage(data: unknown): number | null {
  if (typeof data !== "object" || data == null) return null;
  const candidate = data as Partial<JournalThreadUpdatedMessage>;
  if (candidate.type !== JOURNAL_THREAD_UPDATED_MESSAGE_TYPE) return null;
  const threadId = Number(candidate.threadId);
  return Number.isInteger(threadId) && threadId > 0 ? threadId : null;
}
