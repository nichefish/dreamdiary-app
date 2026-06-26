/** 백엔드 JournalEntryDto 최소 필드 */
export interface JournalEntry {
  id: number;
  contentType: string;
  title?: string;
  content?: string;
  /** 기록이 속한 날짜 (YYYY-MM-DD) — 검색 결과 카드에서 날짜 표시에 사용 */
  stdrdDt?: string;
}

/** 백엔드 JournalDreamSectionDto */
export interface JournalDreamSection {
  sectionKey: string;
  title: string;
  dreamerName?: string | null;
  entries?: JournalEntry[];
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
  /** 일자 화면용 꿈 가상 섹션 */
  journalDreamSectionList?: JournalDreamSection[];
}

/**
 * 일자 DTO 에서 모든 꿈 엔트리를 펼친다.
 * @param day 조회된 일자 DTO. 일자 데이터가 없는 null/undefined이면 빈 목록을 반환한다.
 */
export function dreamEntriesFromDay(day: JournalDay | null | undefined): JournalEntry[] {
  return (day?.journalDreamSectionList ?? []).flatMap((section) => section.entries ?? []);
}
