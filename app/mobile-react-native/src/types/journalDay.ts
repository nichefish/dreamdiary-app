/** 백엔드 JournalEntryDto 최소 필드 */
export interface JournalEntry {
  id: number;
  contentType: string;
  title?: string;
  content?: string;
  /** 기록이 속한 날짜 (YYYY-MM-DD) — 검색 결과 카드에서 날짜 표시에 사용 */
  stdrdDt?: string;
}

/** 백엔드 JournalChapterDto 최소 필드 */
export interface JournalChapter {
  id: number;
  contentType?: string;
  title?: string;
  categoryCode?: string;
  categoryName?: string;
  sortOrder?: number;
  /** 일기 엔트리 목록 */
  journalDiaryList?: JournalEntry[];
  /** 꿈 엔트리 목록 */
  journalDreamList?: JournalEntry[];
  /** 노트 엔트리 목록 */
  journalNoteList?: JournalEntry[];
}

/** 백엔드 JournalDayDto 최소 필드 */
export interface JournalDay {
  id: number;
  stdrdDt: string;
  journalChapterList?: JournalChapter[];
  /** 챕터 외부 꿈 목록 */
  journalDreamList?: JournalEntry[];
  journalElseDreamList?: JournalEntry[];
}