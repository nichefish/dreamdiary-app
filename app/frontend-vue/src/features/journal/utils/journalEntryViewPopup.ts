/**
 * 저장된 저널 엔트리·리플렉션 한 건을 ID 기반 읽기 전용 새 창으로 연다.
 * 같은 ID는 이름 있는 창을 재사용하고, 다른 ID는 서로 독립된 창으로 유지한다.
 */
import { joinAppBasePath } from "@/shared/utils/appPath";
import { resolveJournalEntryPreviewWidth } from "@/features/journal/utils/journalEntryPreview";

/** 저장된 엔트리 읽기 전용 팝업 route를 조립한다. */
export function journalEntryViewPopupPath(entryId: number): string {
  return joinAppBasePath(`/journal/entry/view-pop?entryId=${encodeURIComponent(String(entryId))}`);
}

/** 동일 엔트리 재호출 시 기존 창을 재사용하기 위한 창 이름. */
export function journalEntryViewPopupName(entryId: number): string {
  return `journal_entry_view_${entryId}`;
}

/**
 * 저장된 엔트리 읽기 전용 팝업을 연다.
 * 팝업 차단 시 false를 반환하며 호출자가 현재 locale 오류 안내를 표시한다.
 */
export function openJournalEntryViewPopup(entryId: number): boolean {
  if (!Number.isInteger(entryId) || entryId <= 0) {
    console.warn("[journalEntryViewPopup] invalid entry id", { entryId });
    return false;
  }

  const width = resolveJournalEntryPreviewWidth();
  const height = Math.round(window.innerHeight * 0.9);
  const left = Math.max(0, Math.round((window.screen.availWidth - width) / 2));
  const top = Math.max(0, Math.round((window.screen.availHeight - height) / 2));
  const option = `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`;
  const popup = window.open(
    journalEntryViewPopupPath(entryId),
    journalEntryViewPopupName(entryId),
    option,
  );
  if (!popup) {
    console.warn("[journalEntryViewPopup] popup blocked", { entryId });
    return false;
  }
  popup.focus();
  console.info("[journalEntryViewPopup] popup opened", { entryId });
  return true;
}
