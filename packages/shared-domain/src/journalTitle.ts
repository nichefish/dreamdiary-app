import { JOURNAL_CONTENT_TYPES, type ContentType } from "@dreamdiary/shared-types";

function compactDateLabel(dateText: string | undefined): string {
  return dateText?.trim() || new Date().toISOString().slice(0, 10);
}

export function createDefaultJournalTitle(contentType: ContentType | undefined, dateText?: string): string {
  const dateLabel = compactDateLabel(dateText);

  if (contentType === JOURNAL_CONTENT_TYPES.DREAM) {
    return `${dateLabel} 꿈`;
  }

  if (contentType === JOURNAL_CONTENT_TYPES.DIARY) {
    return `${dateLabel} 일기`;
  }

  if (contentType === JOURNAL_CONTENT_TYPES.NOTE) {
    return `${dateLabel} 노트`;
  }

  return `${dateLabel} 기록`;
}

