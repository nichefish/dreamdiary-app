import { beforeEach, describe, expect, it, vi } from "vitest";
import type { JournalDayDto } from "@/features/journal/stores/journal";
import {
  findFirstNonEmptyEntry,
  findSummaryChapter,
  stripBlankParagraphs,
  summaryEntryCollapsedHtmlOf,
  summaryEntryHtmlOf,
  summaryEntryOf,
} from "./summaryEntryPreview";

vi.mock("@/features/journal/utils/htmlToPlainText", () => ({
  htmlToPlainText: (html: string) => String(html ?? "")
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim(),
}));

const FIXTURE_DAY_ID = 10;

function dayWithChapters(chapters: JournalDayDto["journalChapterList"]): JournalDayDto {
  return { id: FIXTURE_DAY_ID, journalChapterList: chapters };
}

describe("summaryEntryPreview", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("시스템 요약 챕터의 첫 non-empty 엔트리 HTML 전체를 반환한다", () => {
    const html = summaryEntryHtmlOf(dayWithChapters([
      {
        id: 1,
        chapterType: "DIARY",
        summaryYn: "Y",
        journalEntryList: [
          { id: 11, sortOrder: 1, content: "" },
          {
            id: 12,
            sortOrder: 2,
            content: "<p>원문</p>",
            markdownContent: "<p>병원 방문 후 <u>경과</u>를 정리한다. 긴 본문도 자르지 않는다.</p>",
          },
        ],
      },
    ]));
    expect(html).toBe("<p>병원 방문 후 <u>경과</u>를 정리한다. 긴 본문도 자르지 않는다.</p>");
  });

  it("시스템 요약이 없으면 첫 non-DREAM 챕터를 fallback 한다", () => {
    const chapter = findSummaryChapter([
      { id: 1, chapterType: "DREAM", summaryYn: "Y", journalEntryList: [] },
      { id: 2, chapterType: "NOTE", summaryYn: "N", journalEntryList: [] },
      { id: 3, chapterType: "DIARY", summaryYn: "N", journalEntryList: [] },
    ]);
    expect(chapter?.id).toBe(2);
  });

  it("SUMMARY 코드만으로는 시스템 요약으로 판정하지 않는다", () => {
    const chapter = findSummaryChapter([
      { id: 1, chapterType: "DIARY", prefixId: 11, summaryYn: "N", journalEntryList: [] },
      { id: 2, chapterType: "NOTE", summaryYn: "Y", journalEntryList: [] },
    ]);
    expect(chapter?.id).toBe(2);
  });

  it("빈 본문만 있으면 엔트리를 고르지 않는다", () => {
    expect(summaryEntryOf(dayWithChapters([
      {
        id: 1,
        summaryYn: "Y",
        journalEntryList: [{ id: 11, content: "<p>  </p>" }],
      },
    ]))).toBeUndefined();
    expect(summaryEntryHtmlOf(dayWithChapters([
      {
        id: 1,
        summaryYn: "Y",
        journalEntryList: [{ id: 11, content: "<p>  </p>" }],
      },
    ]))).toBe("");
  });

  it("sortOrder 가 앞선 non-empty 엔트리를 고른다", () => {
    const entry = findFirstNonEmptyEntry([
      { id: 2, sortOrder: 2, content: "나중" },
      { id: 1, sortOrder: 1, content: "먼저" },
    ]);
    expect(entry?.id).toBe(1);
  });


  it("접힘 미리보기는 빈 문단을 제거한다", () => {
    const collapsed = stripBlankParagraphs(
      '<p>점심</p><p></p><p><br></p><p>&nbsp;</p><p><br data-mce-bogus="1"></p><p>수영</p>',
    );
    expect(collapsed).toContain("점심");
    expect(collapsed).toContain("수영");
    expect(collapsed).not.toMatch(/<p>\s*<\/p>/);
    expect(collapsed).not.toContain("<br");
    expect(collapsed).not.toContain("&nbsp;");
  });

  it("summaryEntryCollapsedHtmlOf 는 원문 HTML 과 달리 빈 문단이 없다", () => {
    const day = dayWithChapters([
      {
        id: 1,
        summaryYn: "Y",
        journalEntryList: [
          {
            id: 1,
            markdownContent: '<p>첫째</p><p><br data-mce-bogus="1"></p><p>&nbsp;</p><p>둘째</p>',
          },
        ],
      },
    ]);
    expect(summaryEntryHtmlOf(day)).toContain("<br");
    expect(summaryEntryCollapsedHtmlOf(day)).not.toContain("<br");
    expect(summaryEntryCollapsedHtmlOf(day)).not.toContain("&nbsp;");
    expect(summaryEntryCollapsedHtmlOf(day)).toContain("첫째");
    expect(summaryEntryCollapsedHtmlOf(day)).toContain("둘째");
  });

  it("markdownContent 가 없으면 content 를 쓴다", () => {
    expect(summaryEntryHtmlOf(dayWithChapters([
      {
        id: 1,
        summaryYn: "Y",
        journalEntryList: [{ id: 1, content: "<p>원문만</p>" }],
      },
    ]))).toBe("<p>원문만</p>");
  });
});
