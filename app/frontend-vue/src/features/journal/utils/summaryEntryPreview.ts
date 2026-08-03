/**
 * summaryEntryPreview.ts
 * 일자 필터 모달 등에서 시스템 요약 챕터 첫 엔트리를 SEARCH 응답에서 고른다.
 *
 * 전용 summaryPreview 필드는 없다. journalChapterList/journalEntryList 를 파생한다.
 * 표시 HTML은 `markdownContent` 우선 전체 문자열이다.
 * 일자 필터 모달은 UI에서 collapse-3(최대 3줄)·클릭 펼침으로 미리보기한다.
 * 접힘 중에는 빈 문단을 제거한 HTML을 쓰고, 펼침 시 원문 HTML을 그대로 쓴다.
 */
import type { JournalChapterDto, JournalDayDto, JournalEntryDto } from "@/features/journal/stores/journal";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";

/**
 * 일자 DTO 에서 시스템 요약 챕터의 첫 non-empty 엔트리를 반환한다.
 *
 * @param day 필터 목록의 일자 DTO
 * @return 엔트리. 요약 챕터/본문이 없으면 undefined
 */
export function summaryEntryOf(
  day: JournalDayDto | null | undefined,
): JournalEntryDto | undefined {
  const chapter = findSummaryChapter(day?.journalChapterList);
  if (!chapter) return undefined;
  return findFirstNonEmptyEntry(chapter.journalEntryList);
}

/**
 * 표시용 HTML. markdownContent(서버 MarkdownUtils 처리본)를 우선하고,
 * 없으면 TinyMCE 원문 content 를 쓴다.
 */
export function summaryEntryHtmlOf(day: JournalDayDto | null | undefined): string {
  const entry = summaryEntryOf(day);
  if (!entry) return "";
  return (entry.markdownContent ?? entry.content ?? "").trim();
}

/**
 * 접힘 미리보기용 HTML. 빈 문단(공백·&nbsp;·br만)을 제거한다.
 * 원본 엔트리/저장본은 변경하지 않으며, 펼침 시에는 {@link summaryEntryHtmlOf}를 쓴다.
 */
export function summaryEntryCollapsedHtmlOf(day: JournalDayDto | null | undefined): string {
  return stripBlankParagraphs(summaryEntryHtmlOf(day));
}

/**
 * HTML 조각에서 텍스트가 비어 있는 p 를 제거한다.
 * TinyMCE/마크다운 빈 줄(`<p></p>`, `<p><br></p>`, `<p>&nbsp;</p>`,
 * `<p><br data-mce-bogus="1"></p>` 등)을 접힘 미리보기에서 없앤다.
 *
 * <p>BEFORE: 공백 문자·NBSP 코드포인트·속성 없는 br 만 정규식으로 지워
 * `&nbsp;` 엔티티·`br` 속성이 있는 빈 문단이 남아 line-clamp 한 줄을 차지했다.</p>
 * <p>AFTER: DOMParser 가 있으면 textContent 기준으로 비어 있는 p 를 제거하고,
 * 없으면 `&nbsp;`/`&#160;`/`br` 속성을 포함하는 정규식 폴백을 쓴다.</p>
 */
export function stripBlankParagraphs(html: string): string {
  const source = (html ?? "").trim();
  if (!source) return "";

  if (typeof DOMParser !== "undefined") {
    const doc = new DOMParser().parseFromString(
      `<div id="dd-strip-blank-root">${source}</div>`,
      "text/html",
    );
    const root = doc.getElementById("dd-strip-blank-root");
    if (root) {
      for (const el of Array.from(root.querySelectorAll("p"))) {
        const plain = (el.textContent ?? "")
          .replace(/\u00a0/g, " ")
          .replace(/\s+/g, " ")
          .trim();
        if (!plain) el.remove();
      }
      return root.innerHTML.trim();
    }
  }

  // Node/vitest 등 DOMParser 미동작 시 폴백
  return source
    .replace(
      /<p(?:\s[^>]*)?>(?:\s|&nbsp;|&#160;|&#xA0;|<br\b[^>]*>)*<\/p>/gi,
      "",
    )
    .trim();
}

/**
 * {@code summaryYn=Y} 시스템 요약 챕터를 고른다. 없으면 첫 non-DREAM 챕터를 fallback 한다.
 * (백엔드가 DREAM 을 journalChapterList 에서 분리하지만, 방어적으로 제외한다.)
 */
export function findSummaryChapter(
  chapters: JournalChapterDto[] | null | undefined,
): JournalChapterDto | undefined {
  if (!Array.isArray(chapters) || chapters.length === 0) return undefined;
  const nonDream = chapters.filter((chapter) => chapter?.chapterType !== "DREAM");
  if (nonDream.length === 0) return undefined;
  return nonDream.find((chapter) => chapter.summaryYn === "Y") ?? nonDream[0];
}

/** sortOrder → id 순으로 첫 non-empty 엔트리. */
export function findFirstNonEmptyEntry(
  entries: JournalEntryDto[] | null | undefined,
): JournalEntryDto | undefined {
  if (!Array.isArray(entries) || entries.length === 0) return undefined;
  const sorted = [...entries].sort((a, b) => {
    const orderA = typeof a.sortOrder === "number" ? a.sortOrder : Number.MAX_SAFE_INTEGER;
    const orderB = typeof b.sortOrder === "number" ? b.sortOrder : Number.MAX_SAFE_INTEGER;
    if (orderA !== orderB) return orderA - orderB;
    return (a.id ?? 0) - (b.id ?? 0);
  });
  return sorted.find((entry) => {
    const plain = htmlToPlainText(entry.content ?? entry.markdownContent ?? "").trim();
    return plain.length > 0;
  });
}
