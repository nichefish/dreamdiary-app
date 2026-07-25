import { describe, expect, it } from "vitest";
import {
  createJournalThreadUpdatedMessage,
  parseJournalThreadUpdatedMessage,
} from "./journalThreadPopupMessage";

describe("스레드 수정 팝업 메시지 계약", () => {
  it("양의 정수 스레드 ID를 수정 완료 메시지로 왕복한다", () => {
    const message = createJournalThreadUpdatedMessage(42);

    expect(parseJournalThreadUpdatedMessage(message)).toBe(42);
  });

  it("다른 메시지 유형과 유효하지 않은 ID를 거부한다", () => {
    expect(parseJournalThreadUpdatedMessage({ type: "other", threadId: 42 })).toBeNull();
    expect(parseJournalThreadUpdatedMessage({ type: "journal-thread-updated", threadId: 0 })).toBeNull();
    expect(parseJournalThreadUpdatedMessage({ type: "journal-thread-updated", threadId: "invalid" })).toBeNull();
  });
});
