import type { HistoryCmpstn, LifecycleCmpstn } from "@/features/journal/stores/journal";

// ---- 타입 정의 ----

/** 태그 항목 */
export interface ThreadTagItem {
  tagId: number | string;
  name: string;
  ctgr?: string;
}

/** 목록 검색 카드의 태그 클라우드 항목 */
export interface ThreadPrefix {
  id: number;
  name: string;
  color?: string | null;
  sortOrder?: number;
  activeYn?: "Y" | "N";
}

/** 태그 컴포지션 */
export interface ThreadTagCmpstn {
  list?: ThreadTagItem[];
  /** 태그 문자열 (tagify 초기값) */
  tagListStrWithCtgr?: string;
}

/** 저널 스레드 Dto */
export interface JournalThreadDto {
  id?: number;
  rnum?: number;
  contentType?: string;
  prefix?: ThreadPrefix | null;
  prefixId?: number | null;
  /** 활성 소속 엔트리 수 (목록 enrich). 없으면 0으로 취급해 숨긴다. */
  membershipCount?: number;
  /** 활성 소속 엔트리 기준일 min (YYYY-MM-DD). 목록 enrich. */
  firstEntryDate?: string;
  /** 활성 소속 엔트리 기준일 max (YYYY-MM-DD). 목록 enrich. */
  lastEntryDate?: string;
  lifecycle?: LifecycleCmpstn;
  /** 마지막 본문 변경 이력 트리거 정보. */
  history?: HistoryCmpstn;
  title?: string;
  content?: string;
  markdownContent?: string;
  tag?: ThreadTagCmpstn;
  comment?: {
    cnt?: number;
    list?: Array<{ id?: number; content?: string; markdownContent?: string }>;
  };
  file?: { fileGroupId?: number };
  hasFiles?: boolean;
  createdByNm?: string;
  createdDt?: string;
}

/** 저널 스레드 등록/수정 폼 모델 */
export interface JournalThreadRegistModel {
  id?: number;
  contentType?: string;
  prefixId?: number | null;
  title?: string;
  content?: string;
}

/** 월간·주간 저널 화면에 표시할 기간별 스레드 집계 */
export interface JournalPeriodThreadSummaryItem {
  threadId: number;
  title: string;
  /** 스레드에 선택된 개인 말머리. 선택하지 않았으면 null. */
  prefix?: ThreadPrefix | null;
  /** 조회 기간 안에서 이 스레드에 속한 엔트리 수 */
  entryCount: number;
  /** 조회 기간 안에서 스레드가 처음 등장한 일자 */
  firstEntryDate: string;
}

/** 기간별 스레드 집계 API가 지원하는 조회 계약 */
export type JournalPeriodThreadSummaryQuery =
  | { viewType: "WEEKLY"; weekStartDt: string }
  | { viewType: "LIST"; yy: number; mnth: number }
  | { viewType: "ANNUAL"; yy: number };
