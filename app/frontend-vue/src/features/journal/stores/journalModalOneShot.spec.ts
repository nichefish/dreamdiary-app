/**
 * journalModalOneShot — 저장 직후 일회성 펼침/접힘 신호 계약.
 */
import { describe, expect, it } from "vitest";
import { createJournalModalOneShotSignals } from "./journalModalOneShot";

describe("createJournalModalOneShotSignals", () => {
  it("챕터 펼침 요청 후 같은 id clear 만 해제한다", () => {
    const s = createJournalModalOneShotSignals();
    s.requestEntryCreatedChapterExpand(10);
    s.clearEntryCreatedChapterExpand(99);
    expect(s.entryCreatedExpandChapterId.value).toBe(10);
    s.clearEntryCreatedChapterExpand(10);
    expect(s.entryCreatedExpandChapterId.value).toBeNull();
  });

  it("Reflection 접힘 요청 후 같은 id clear 만 해제한다", () => {
    const s = createJournalModalOneShotSignals();
    s.requestReflectionCreatedCollapse("r1");
    s.clearReflectionCreatedCollapse("r2");
    expect(s.reflectionCreatedCollapseId.value).toBe("r1");
    s.clearReflectionCreatedCollapse("r1");
    expect(s.reflectionCreatedCollapseId.value).toBeNull();
  });
});