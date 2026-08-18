/**
 * journalEntryPreview.ts
 * 작성 중 저널 엔트리·리플렉션 본문을 목록과 같은 markdownContent로 새 창에 띄운다.
 * 클릭 시점에 팝업을 연 뒤 POST /api/journal/entries/preview 결과를 localStorage로 전달한다.
 */
import { apiPost, unwrapObj } from "@/shared/api/client";
import { joinAppBasePath } from "@/shared/utils/appPath";

/** localStorage 키 접두. 미리보기 ID를 붙여 창 간에 렌더 결과를 전달한다. */
export const JOURNAL_ENTRY_PREVIEW_STORAGE_PREFIX = "dreamdiary.journal.entry.preview.";

/** 미리보기 창에 적용할 콘텐츠 유형. 목록 item/content 클래스 분기와 같다. */
export type JournalEntryPreviewContentType =
  | "JOURNAL_DIARY"
  | "JOURNAL_DREAM"
  | "JOURNAL_NOTE"
  | "JOURNAL_REFLECTION";

/** 등록 모달이 미리보기 창에 넘기는 작성 중 스냅샷. */
export interface JournalEntryPreviewDraft {
  contentType: JournalEntryPreviewContentType;
  title?: string | null;
  sortOrder?: number | null;
  content?: string | null;
  prefixName?: string | null;
  prefixColor?: string | null;
}

/** 미리보기 창이 렌더하는 확정 페이로드. */
export interface JournalEntryPreviewView extends JournalEntryPreviewDraft {
  markdownContent: string;
  error?: string | null;
}

/** 미리보기 ID에 대응하는 localStorage 키. */
export function previewStorageKey(previewId: string): string {
  return `${JOURNAL_ENTRY_PREVIEW_STORAGE_PREFIX}${previewId}`;
}

/** 미리보기 쿼리 ID 형식. UUID 또는 타임스탬프 접두. */
export function isPreviewId(raw: unknown): raw is string {
  return typeof raw === "string" && /^[0-9A-Za-z-]{8,64}$/.test(raw);
}

/**
 * 미리보기 창 너비. 목록 본문 셸 폭을 우선하고, 없으면 콘텐츠 컨테이너·뷰포트 비율로 보정한다.
 */
export function resolveJournalEntryPreviewWidth(root: Document = document): number {
  const contentShell = root.querySelector(
    "#journal_day_list_div, .journal-day-monthly-page .card.post, .journal-day-weekly-page .card.post, .journal-day-calendar-page .card.post, .journal-day-meta-page .card.post, .journal-day-daily-page .card.post",
  );
  if (contentShell) {
    const shellWidth = Math.round(contentShell.getBoundingClientRect().width);
    if (shellWidth > 320) return shellWidth;
  }

  const journalShell = root.querySelector(
    ".journal-day-monthly-page, .journal-day-weekly-page, .journal-day-calendar-page, .journal-day-meta-page, .journal-day-daily-page",
  );
  if (journalShell) {
    const shellWidth = Math.round(journalShell.getBoundingClientRect().width);
    if (shellWidth > 320) return Math.max(480, shellWidth - 64);
  }

  const container = root.querySelector("#kt_app_content_container");
  if (container) {
    const containerWidth = Math.round(container.getBoundingClientRect().width);
    if (containerWidth > 320) return Math.max(480, containerWidth - 64);
  }
  return Math.min(Math.max(Math.round(window.innerWidth * 0.92), 480), 1600);
}

/** 미리보기 창 이름. 같은 콘텐츠 유형은 기존 창을 재사용한다. */
export function previewPopupName(contentType: JournalEntryPreviewContentType): string {
  return `journal_entry_preview_${contentType.toLowerCase()}`;
}

function createPreviewId(): string {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

function writePreviewView(previewId: string, view: JournalEntryPreviewView): void {
  try {
    localStorage.setItem(previewStorageKey(previewId), JSON.stringify(view));
  } catch (error) {
    console.error("[journalEntryPreview] preview payload write failed", { previewId, error });
    throw error;
  }
}

/**
 * 작성 중 본문 미리보기 창을 연다.
 * 팝업 차단 시 blocked를 반환하고 API를 호출하지 않는다.
 *
 * @param draft 모달 폼 스냅샷
 * @param failureMessage API 실패 시 unwrap 문구
 * @return opened | blocked
 */
export async function openJournalEntryPreview(
  draft: JournalEntryPreviewDraft,
  failureMessage: string,
): Promise<"opened" | "blocked"> {
  const previewId = createPreviewId();
  const width = resolveJournalEntryPreviewWidth();
  const height = Math.round(window.innerHeight * 0.9);
  const left = Math.max(0, Math.round((window.screen.availWidth - width) / 2));
  const top = Math.max(0, Math.round((window.screen.availHeight - height) / 2));
  const option = `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`;
  const url = joinAppBasePath(`/journal/entry/preview-pop?previewId=${encodeURIComponent(previewId)}`);
  const popup = window.open(url, previewPopupName(draft.contentType), option);
  if (!popup) {
    console.warn("[journalEntryPreview] popup blocked", { contentType: draft.contentType });
    return "blocked";
  }
  popup.focus();

  try {
    const res = await apiPost<{ markdownContent?: string }>("/api/journal/entries/preview", {
      content: draft.content ?? "",
    });
    const rendered = unwrapObj(res, failureMessage);
    writePreviewView(previewId, {
      ...draft,
      markdownContent: rendered?.markdownContent ?? "",
      error: null,
    });
    console.info("[journalEntryPreview] preview rendered", {
      contentType: draft.contentType,
      contentLength: (draft.content ?? "").length,
    });
    return "opened";
  } catch (error) {
    const message = error instanceof Error ? error.message : failureMessage;
    writePreviewView(previewId, {
      ...draft,
      markdownContent: "",
      error: message,
    });
    console.error("[journalEntryPreview] preview render failed", {
      contentType: draft.contentType,
      message,
    });
    throw error;
  }
}

/** localStorage에 쌓인 미리보기 페이로드를 읽는다. 파싱 실패 시 null. */
export function readPreviewView(previewId: string): JournalEntryPreviewView | null {
  let raw: string | null = null;
  try {
    raw = localStorage.getItem(previewStorageKey(previewId));
  } catch (error) {
    console.error("[journalEntryPreview] preview payload read failed", { previewId, error });
    return null;
  }
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as JournalEntryPreviewView;
    if (!parsed || typeof parsed !== "object") return null;
    return parsed;
  } catch (error) {
    console.error("[journalEntryPreview] preview payload parse failed", { previewId, error });
    return null;
  }
}

/** 전달이 끝난 미리보기 페이로드를 제거한다. */
export function clearPreviewView(previewId: string): void {
  localStorage.removeItem(previewStorageKey(previewId));
}
