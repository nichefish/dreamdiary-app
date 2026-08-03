import { JOURNAL_CONTENT_TYPES, type ContentType, type JournalEntryKind } from "@dreamdiary/shared-types";

export function isDreamContentType(contentType: ContentType | undefined): boolean {
  return contentType === JOURNAL_CONTENT_TYPES.DREAM;
}

export function isDiaryContentType(contentType: ContentType | undefined): boolean {
  return contentType === JOURNAL_CONTENT_TYPES.DIARY;
}

export function getJournalEntryKind(contentType: ContentType | undefined): JournalEntryKind | undefined {
  if (contentType === JOURNAL_CONTENT_TYPES.DAY) return "DAY";
  if (contentType === JOURNAL_CONTENT_TYPES.DIARY) return "DIARY";
  if (contentType === JOURNAL_CONTENT_TYPES.DREAM) return "DREAM";
  if (contentType === JOURNAL_CONTENT_TYPES.NOTE) return "NOTE";

  return undefined;
}

export function getContentTypeLabel(contentType: ContentType | undefined): string {
  switch (contentType) {
    case JOURNAL_CONTENT_TYPES.DAY:
      return "일지";
    case JOURNAL_CONTENT_TYPES.DIARY:
      return "일기";
    case JOURNAL_CONTENT_TYPES.DREAM:
      return "꿈";
    case JOURNAL_CONTENT_TYPES.NOTE:
      return "노트";
    case JOURNAL_CONTENT_TYPES.THREAD:
      return "흐름";
    case JOURNAL_CONTENT_TYPES.ANNUAL:
      return "연간 기록";
    case JOURNAL_CONTENT_TYPES.ANNUAL_REVIEW:
      return "연간 회고";
    default:
      return contentType ?? "";
  }
}

