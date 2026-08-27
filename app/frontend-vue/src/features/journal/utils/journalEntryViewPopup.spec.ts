// @vitest-environment happy-dom
import { afterEach, describe, expect, it, vi } from "vitest";
import {
  journalEntryViewPopupName,
  journalEntryViewPopupPath,
  openJournalEntryViewPopup,
} from "./journalEntryViewPopup";

const FIXTURE_ENTRY_ID = 101;

afterEach(() => {
  vi.restoreAllMocks();
});

describe("저장된 저널 엔트리 읽기 전용 팝업", () => {
  it("엔트리 ID를 route와 재사용 창 이름에 반영한다", () => {
    expect(journalEntryViewPopupPath(FIXTURE_ENTRY_ID)).toContain(
      `/journal/entry/view-pop?entryId=${FIXTURE_ENTRY_ID}`,
    );
    expect(journalEntryViewPopupName(FIXTURE_ENTRY_ID)).toBe(
      `journal_entry_view_${FIXTURE_ENTRY_ID}`,
    );
  });

  it("팝업을 열고 포커스한다", () => {
    const focus = vi.fn();
    const open = vi.spyOn(window, "open").mockReturnValue({ focus } as unknown as Window);

    expect(openJournalEntryViewPopup(FIXTURE_ENTRY_ID)).toBe(true);
    expect(open).toHaveBeenCalledWith(
      expect.stringContaining(`/journal/entry/view-pop?entryId=${FIXTURE_ENTRY_ID}`),
      `journal_entry_view_${FIXTURE_ENTRY_ID}`,
      expect.stringContaining("scrollbars=yes"),
    );
    expect(focus).toHaveBeenCalledOnce();
  });

  it("팝업이 차단되면 false를 반환한다", () => {
    vi.spyOn(window, "open").mockReturnValue(null);

    expect(openJournalEntryViewPopup(FIXTURE_ENTRY_ID)).toBe(false);
  });
});
