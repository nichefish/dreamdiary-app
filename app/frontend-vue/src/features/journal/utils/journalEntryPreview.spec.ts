// @vitest-environment happy-dom
import { afterEach, describe, expect, it, vi } from "vitest";
import {
  isPreviewId,
  previewPopupName,
  previewStorageKey,
  readPreviewView,
  resolveJournalEntryPreviewWidth,
} from "./journalEntryPreview";

const FIXTURE_PREVIEW_ID = "11111111-2222-3333-4444-555555555555";

describe("journalEntryPreview", () => {
  afterEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it("미리보기 ID로 localStorage 키를 만든다", () => {
    expect(previewStorageKey(FIXTURE_PREVIEW_ID)).toBe(
      `dreamdiary.journal.entry.preview.${FIXTURE_PREVIEW_ID}`,
    );
  });

  it("콘텐츠 유형별 팝업 창 이름을 소문자로 고정한다", () => {
    expect(previewPopupName("JOURNAL_DIARY")).toBe("journal_entry_preview_journal_diary");
    expect(previewPopupName("JOURNAL_REFLECTION")).toBe("journal_entry_preview_journal_reflection");
  });

  it("목록 본문 셸 폭이 있으면 그 너비를 쓴다", () => {
    const shell = document.createElement("div");
    shell.className = "journal-day-monthly-page";
    const card = document.createElement("div");
    card.className = "card post";
    shell.appendChild(card);
    document.body.appendChild(shell);
    vi.spyOn(card, "getBoundingClientRect").mockReturnValue({
      width: 960,
      height: 0,
      top: 0,
      left: 0,
      bottom: 0,
      right: 0,
      x: 0,
      y: 0,
      toJSON() {
        return {};
      },
    });

    expect(resolveJournalEntryPreviewWidth(document)).toBe(960);
    shell.remove();
  });

  it("저장된 미리보기 JSON을 읽고 깨진 값은 버린다", () => {
    localStorage.setItem(
      previewStorageKey(FIXTURE_PREVIEW_ID),
      JSON.stringify({ contentType: "JOURNAL_DIARY", markdownContent: "<p>ok</p>" }),
    );
    expect(readPreviewView(FIXTURE_PREVIEW_ID)?.markdownContent).toBe("<p>ok</p>");

    localStorage.setItem(previewStorageKey(FIXTURE_PREVIEW_ID), "{not-json");
    expect(readPreviewView(FIXTURE_PREVIEW_ID)).toBeNull();
  });

  it("미리보기 ID 형식을 검증한다", () => {
    expect(isPreviewId(FIXTURE_PREVIEW_ID)).toBe(true);
    expect(isPreviewId("short")).toBe(false);
    expect(isPreviewId("../x")).toBe(false);
  });
});
