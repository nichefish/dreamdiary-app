import type {
  JournalDayDto,
  JournalPrefixDto,
} from "@/features/journal/stores/journal";

/** 저널 일자 등록/수정 폼 모델 */
export interface JournalDayRegistModel {
  id?: number;
  journalDate?: string;
  journalDatePrecision?: string;
  diaryResolvedYn?: string;
  dreamResolvedYn?: string;
  weather?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStr?: string; tagListStrWithCtgr?: string };
  /** 메타 컴포지션 */
  meta?: { metaListStr?: string };
}

/** 저널 챕터 등록/수정 폼 모델 */
export interface JournalChapterRegistModel {
  id?: number;
  journalDayId?: number;
  stdrdDt?: string;
  chapterType?: "DIARY" | "NOTE" | "DREAM";
  /** 일반 챕터에 선택할 개인 말머리. 시스템 요약·DREAM에는 적용하지 않는다. */
  prefixId?: number | null;
  prefix?: JournalPrefixDto | null;
  /** 서버가 관리하는 시스템 요약 챕터 여부 */
  summaryYn?: string;
  title?: string;
  sortOrder?: number;
}

/** 저널 Reflection 등록/수정 폼 모델 (entry 경로) */
export interface JournalReflectionRegistModel {
  id?: number;
  /** target 대상 엔트리 ID */
  refId?: number;
  /** target 대상 콘텐츠 타입 */
  refContentType?: string;
  /** 소속 일자 — 독립 Reflection 챕터 선택 옵션 조회에 사용 */
  journalDayId?: number;
  /** 소속 chapter (target 묶기 시 기본은 target 의 chapter, 챕터 직속 시 필수) */
  journalChapterId?: number;
  stdrdDt?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
  /** 태그 — 일기 축과 동일 categoryMap(DIARY). 저장 시 ref_content_type=JOURNAL_REFLECTION */
  tag?: { tagListStrWithCtgr?: string; list?: unknown[] };
  /** 독립 Reflection 전용: 같은 일자의 DIARY·NOTE 챕터 선택지 */
  chapterList?: JournalChapterOption[];
}

/** 일자 필터 모달 시드 타입 */
export type DayFilterSeedType = "meta" | "tag";

/** 일자 필터 모달 페이로드 (메타/태그 다중 AND 검색) */
export interface JournalDayFilterPayload {
  /** 초기 시드 타입 */
  seedType: DayFilterSeedType;
  /** 초기 시드 ID */
  seedId: string;
  /** 초기 시드 이름 */
  seedName: string;
  /** 초기 시드 카테고리 */
  seedCtgr?: string;
  yy: string;
  yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
  list: JournalDayDto[];
}

/** 저널 할일 등록/수정 폼 모델 */
export interface JournalTodoRegistModel {
  id?: number;
  yy?: number | string;
  mnth?: number | string;
  categoryCode?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStrWithCtgr?: string };
}

/** 챕터 선택 옵션 (entry 등록 시 챕터 선택) */
export interface JournalChapterOption {
  id: number | string;
  title?: string;
  sortOrder?: number;
  prefix?: JournalPrefixDto | null;
  prefixId?: number | null;
  /** 서버가 관리하는 시스템 요약 챕터 여부 */
  summaryYn?: string;
  chapterType?: string;
}

/** 저널 엔트리(일기/꿈/노트) 등록/수정 폼 모델 */
export interface JournalEntryRegistModel {
  id?: number;
  /** 컨텐츠 유형: 'JOURNAL_DIARY' | 'JOURNAL_DREAM' | 'JOURNAL_NOTE' */
  contentType: string;
  journalDayId?: number;
  journalChapterId?: number | string;
  stdrdDt?: string;
  /** 소속 챕터 유형으로 해석한 개인 Prefix 목록 content_type */
  prefixContentType?: "JOURNAL_DIARY" | "JOURNAL_DREAM" | "JOURNAL_NOTE";
  prefixId?: number | null;
  prefix?: JournalPrefixDto | null;
  title?: string;
  sortOrder?: number;
  content?: string;
  elseDreamYn?: string;
  elseDreamerNm?: string;
  collapsedYn?: string;
  imprtcYn?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStrWithCtgr?: string };
  /** 챕터 목록 (DIARY/NOTE 등록 시 챕터 선택 옵션) */
  chapterList?: JournalChapterOption[];
}