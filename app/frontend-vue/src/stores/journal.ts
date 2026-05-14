import { ref, computed } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

// ---- 타입 정의 ----

export type JournalViewType = "LIST" | "CAL" | "DAILY" | "WEEKLY" | "SEARCH";

/** TagContentDto — 백엔드 TagContentDto 직렬화 구조 */
export interface TagItem {
  tagId: number;
  /** 태그명 */
  name: string;
  /** 태그 카테고리 코드 */
  ctgr?: string;
}

/** TagCmpstn 공통 컴포지션 — 백엔드 TagCmpstn 직렬화 구조 */
export interface TagCmpstn {
  list?: TagItem[];
}

/** StateDto — 백엔드 StateDto 직렬화 구조 */
export interface StateItem {
  stateKey: string;
}

/** StateCmpstn 공통 컴포지션 — 백엔드 StateCmpstn 직렬화 구조 */
export interface StateCmpstn {
  list?: StateItem[];
}

/** LifecycleCmpstn 공통 컴포지션 — 백엔드 LifecycleCmpstn 직렬화 구조 */
export interface LifecycleCmpstn {
  lifecycleKey?: string;
  lifecycleDesc?: string;
}

/** 댓글 항목 */
export interface CommentItem {
  id: number;
  content: string;
  regDt?: string;
}

/** CommentCmpstn 공통 컴포지션 — 백엔드 CommentCmpstn 직렬화 구조 */
export interface CommentCmpstn {
  list?: CommentItem[];
  cnt?: number;
  hasComment?: boolean;
}

/** 관련글 항목 */
export interface RelatedContentItem {
  id?: number;
  contentType?: string;
  refContentNo?: number;
  refTitle?: string;
  relReason?: string;
}

/** 해석 항목 */
export interface InterpretationItem {
  id: number;
  content?: string;
}

/** 저널 엔트리 (일기 / 꿈 / 노트) */
export interface JournalEntryDto {
  id: number;
  contentType?: string;
  title?: string;
  content?: string;
  markdownContent?: string;
  journalDayId?: number;
  journalChapterId?: number;
  stdrdDt?: string;
  sortOrder?: number;
  elseDreamYn?: string;
  elseDreamerNm?: string;
  tag?: TagCmpstn;
  state?: StateCmpstn;
  lifecycle?: LifecycleCmpstn;
  comment?: CommentCmpstn;
  relatedContentList?: RelatedContentItem[];
  journalInterpretationList?: InterpretationItem[];
}

/** 저널 챕터 */
export interface JournalChapterDto {
  id: number;
  chapterType?: "DIARY" | "NOTE" | "DREAM";
  title?: string;
  categoryCode?: string;
  categoryName?: string;
  journalDayId?: number;
  stdrdDt?: string;
  sortOrder?: number;
  journalEntryList?: JournalEntryDto[];
  tag?: TagCmpstn;
  state?: StateCmpstn;
}

/** MetaContentDto — 백엔드 MetaContentDto 직렬화 구조 */
export interface MetaContentItem {
  id?: number;
  metaId?: number;
  /** 메타 이름 */
  name?: string;
  /** 메타 카테고리 */
  ctgr?: string;
  /** 메타 값 */
  value?: string;
  /** 메타 단위 */
  unit?: string;
}

/** MetaCmpstn 공통 컴포지션 — 백엔드 MetaCmpstn 직렬화 구조 */
export interface MetaCmpstn {
  list?: MetaContentItem[];
}

/** MetaDto — 백엔드 MetaDto 직렬화 구조 */
export interface MetaDto {
  id?: number;
  /** 메타 카테고리 */
  ctgr?: string;
  /** 메타 이름 */
  name?: string;
  /** 메타 값 */
  value?: string;
  /** 메타 단위 */
  unit?: string;
  /** 컨텐츠 개수 */
  contentSize?: number;
  /** 글자 크기 클래스 */
  metaClass?: string;
}

/** 챕터 필터로 숨겨진 카테고리 힌트 — 백엔드 JournalChapterCtgrHintDto 직렬화 구조 */
export interface JournalChapterCtgrHintDto {
  categoryCode?: string;
  categoryName?: string;
}

/** 저널 일자 */
export interface JournalDayDto {
  id: number;
  journalDate?: string;
  /** 기준일자 — 백엔드 getStdrdDt() getter 직렬화 (journalDate 와 동일값) */
  stdrdDt?: string;
  journalDateWeekDay?: string;
  journalDatePrecision?: string;
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
  isHolyday?: boolean;
  holydayNm?: string;
  weather?: string;
  journalChapterList?: JournalChapterDto[];
  journalDreamList?: JournalEntryDto[];
  journalElseDreamList?: JournalEntryDto[];
  /** 꿈 목록 보유 여부 — 백엔드 getHasDream() getter 직렬화 */
  hasDream?: boolean;
  /** 챕터 필터로 숨겨진 카테고리 목록 */
  hiddenChapterCtgrList?: JournalChapterCtgrHintDto[];
  tag?: TagCmpstn;
  meta?: MetaCmpstn;
}

/** 목록 조회 파라미터 */
export interface JournalDaySearchParam {
  viewType: JournalViewType;
  yy?: number;
  mnth?: number;
  stdrdDt?: string;
  weekStartDt?: string;
  showDiaries?: boolean;
  showDreams?: boolean;
  showTagCloud?: boolean;
  diaryKeyword?: string;
  dreamKeyword?: string;
  chapterCtgrCds?: string[];
}

// ---- 스토어 ----

export const useJournalStore = defineStore("journal", () => {
  const now = new Date();

  /** 현재 보기 타입 */
  const viewType = ref<JournalViewType>("LIST");

  /** 현재 조회 기준 연도 */
  const yy = ref<number>(now.getFullYear());

  /** 현재 조회 기준 월 (1-based) */
  const mnth = ref<number>(now.getMonth() + 1);

  /** 주간 뷰 기준 주 시작일 (YYYY-MM-DD) */
  const weekStartDt = ref<string>("");

  /** 조회된 일자 목록 */
  const dayList = ref<JournalDayDto[]>([]);

  /** 로딩 상태 */
  const loading = ref<boolean>(false);

  /** 에러 메시지 */
  const error = ref<string | null>(null);

  // 필터 상태
  const showDiaries = ref<boolean>(true);
  const showDreams = ref<boolean>(true);
  const showTagCloud = ref<boolean>(false);
  const diaryKeyword = ref<string>("");
  const dreamKeyword = ref<string>("");
  const chapterCtgrCds = ref<string[]>([]);

  /** 메타 목록 */
  const metaList = ref<MetaDto[]>([]);
  /** 선택된 메타 */
  const selectedMeta = ref<MetaDto | null>(null);
  /** 메타 목록 로딩 상태 */
  const metaLoading = ref<boolean>(false);
  /** 메타 조회 에러 메시지 */
  const metaError = ref<string | null>(null);

  /** 현재 "년-월" 표시 라벨 */
  const yyMnthLabel = computed(() =>
    `${yy.value}년 ${String(mnth.value).padStart(2, "0")}월`
  );

  /**
   * 일자 목록 조회.
   * viewType에 따라 파라미터 구성이 달라진다.
   */
  async function fetchDays(params?: Partial<JournalDaySearchParam>) {
    loading.value = true;
    error.value = null;
    try {
      const query: Record<string, unknown> = {
        viewType: params?.viewType ?? viewType.value,
        yy: params?.yy ?? yy.value,
        mnth: params?.mnth ?? mnth.value,
        showDiaries: params?.showDiaries ?? showDiaries.value,
        showDreams: params?.showDreams ?? showDreams.value,
        showTagCloud: params?.showTagCloud ?? showTagCloud.value,
        ...(diaryKeyword.value ? { diaryKeyword: diaryKeyword.value } : {}),
        ...(dreamKeyword.value ? { dreamKeyword: dreamKeyword.value } : {}),
        ...(chapterCtgrCds.value.length > 0 ? { chapterCtgrCds: chapterCtgrCds.value } : {}),
        ...(params?.weekStartDt ? { weekStartDt: params.weekStartDt } : {}),
        ...(params?.stdrdDt ? { stdrdDt: params.stdrdDt } : {}),
      };
      const res = await axios.get("/api/journal/days", { params: query });
      // 변경: 백엔드 AjaxResponse.rsltList 필드명으로 수정 (기존: res.data?.list)
      dayList.value = res.data?.rsltList ?? [];
    } catch (e: unknown) {
      error.value = "저널 목록을 불러오지 못했습니다.";
      dayList.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * 메타 목록 조회.
   * GET /api/journal/day/metas
   */
  async function fetchMetas() {
    metaLoading.value = true;
    metaError.value = null;
    try {
      const res = await axios.get("/api/journal/day/metas");
      metaList.value = res.data?.rsltList ?? [];
    } catch (e: unknown) {
      metaError.value = "메타 목록을 불러오지 못했습니다.";
      metaList.value = [];
    } finally {
      metaLoading.value = false;
    }
  }

  /**
   * 선택된 메타를 갱신한다.
   */
  function selectMeta(meta: MetaDto | null) {
    selectedMeta.value = meta;
  }

  /**
   * 월 이동 (delta: -1 이전월, +1 다음월).
   * 연도 경계에서 자동으로 넘어간다.
   */
  function navigateMonth(delta: number) {
    let m = mnth.value + delta;
    let y = yy.value;
    if (m < 1) { m = 12; y -= 1; }
    if (m > 12) { m = 1; y += 1; }
    yy.value = y;
    mnth.value = m;
    fetchDays();
  }

  /**
   * 오늘이 속한 월로 이동한다.
   */
  function gotoToday() {
    const today = new Date();
    yy.value = today.getFullYear();
    mnth.value = today.getMonth() + 1;
    fetchDays();
  }

  /**
   * 년도·월을 직접 지정해 이동한다.
   */
  function gotoYyMnth(newYy: number, newMnth: number) {
    yy.value = newYy;
    mnth.value = newMnth;
    fetchDays();
  }

  /**
   * 보기 타입을 전환한다.
   * 화면에서 이미 이동(라우팅)이 완료된 뒤 호출한다.
   */
  function setViewType(vt: JournalViewType) {
    viewType.value = vt;
  }

  return {
    viewType,
    yy,
    mnth,
    weekStartDt,
    dayList,
    loading,
    error,
    showDiaries,
    showDreams,
    showTagCloud,
    diaryKeyword,
    dreamKeyword,
    chapterCtgrCds,
    metaList,
    selectedMeta,
    metaLoading,
    metaError,
    yyMnthLabel,
    fetchDays,
    fetchMetas,
    selectMeta,
    navigateMonth,
    gotoToday,
    gotoYyMnth,
    setViewType,
  };
});