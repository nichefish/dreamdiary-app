import { ref, computed } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { formatLocalDateStr, resolveWeekStartDt } from "@/features/journal/utils/journalDate";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import type { JournalDreamSectionDto } from "@/features/journal/utils/journalDream";
import {
  readReflectionDefaultCollapsedFromStorage,
  writeReflectionDefaultCollapsedToStorage,
} from "@/features/journal/utils/journalReflectionCollapseMode";

// ---- 타입 정의 ----

export type JournalViewType = "LIST" | "CAL" | "DAILY" | "WEEKLY" | "SEARCH";

/** TagContentDto — 백엔드 TagContentDto 직렬화 구조 */
export interface TagItem {
  tagId: number;
  /** 태그명 */
  name: string;
  /** 태그 카테고리 코드 */
  ctgr?: string;
  /** 태그 프로필 본문 */
  profileContent?: string;
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
  leftId?: number;
  leftContentType?: string;
  rightId?: number;
  rightContentType?: string;
  relationType?: string;
  reason?: string;
  originType?: string;
  targetId?: number;
  targetContentType?: string;
  targetTitle?: string;
}

/**
 * 엔트리가 속한 저널 스레드 소속 항목.
 * 백엔드 JournalThreadEntryDto 와 대응한다. FLOW 를 대체하는 축이다.
 */
export interface JournalThreadEntryDto {
  id: number;
  threadId: number;
  entryId: number;
  sortOrder?: number;
  threadTitle?: string;
  entryContentType?: string;
}

/** 이력 컴포지션 — 백엔드 HistoryCmpstn 직렬화 구조 */
export interface HistoryCmpstn {
  historyTriggeredAt?: string;
}


/** 저널 엔트리 (일기 / 꿈 / 노트 / 리플렉션) */
export interface JournalEntryDto {
  id: number;
  contentType?: string;
  /** target(해석 대상) 엔트리 ID — Reflection 만 사용, nullable */
  refId?: number;
  /** target 엔트리 콘텐츠 타입 — Reflection 만 사용, nullable */
  refContentType?: string;
  title?: string;
  content?: string;
  markdownContent?: string;
  journalDayId?: number;
  journalChapterId?: number;
  stdrdDt?: string;
  /** 공휴일 또는 주말 — 검색 팝업 등 provide 없는 화면 날짜 헤더용 */
  isHolyday?: boolean;
  /** 공휴일명(복수면 콤마 연결). 주말 단독이면 비움 */
  holydayNm?: string;
  sortOrder?: number;
  prefix?: JournalPrefixDto | null;
  prefixId?: number | null;
  /** 소속 챕터 유형으로 해석한 개인 Prefix 목록 content_type */
  prefixContentType?: "JOURNAL_DIARY" | "JOURNAL_DREAM" | "JOURNAL_NOTE";
  elseDreamYn?: string;
  elseDreamerNm?: string;
  tag?: TagCmpstn;
  state?: StateCmpstn;
  lifecycle?: LifecycleCmpstn;
  history?: HistoryCmpstn;
  comment?: CommentCmpstn;
  relatedContentList?: RelatedContentItem[];
  /** 이 엔트리가 속한 스레드 목록. 소속이 없으면 빈 목록. */
  threadList?: JournalThreadEntryDto[];
  /** target 이 이 엔트리인 Reflection 목록 (역참조 교차뷰) */
  reflectionList?: JournalEntryDto[];
  /** 현재 로그인 사용자 소유 여부 (백엔드 BaseAuditRegDto getIsCreatedBy 직렬화) */
  isCreatedBy?: boolean;
  /** 소속 일자 일기 축 완결 Y/N — 검색·상세 등 provide 없는 화면 UI 잠금 */
  diaryResolvedYn?: string;
  /** 소속 일자 꿈 축 완결 Y/N — 검색·상세 등 provide 없는 화면 UI 잠금 */
  dreamResolvedYn?: string;
  /**
   * 뷰 합성(relatedThreadIds) 응답에서 이 엔트리의 출처 스레드 ID.
   * null이면 base 스레드 소속 엔트리. 값이 있으면 연관 스레드에서 빌려온 엔트리.
   * 설계 정본: docs/migration/journal/thread-relation.md §4
   */
  sourceThreadId?: number | null;
}

/** 저널 Prefix — 백엔드 PrefixDto 직렬화 구조 */
export interface JournalPrefixDto {
  id: number;
  name: string;
  color?: string | null;
  sortOrder?: number;
  activeYn?: "Y" | "N";
}

/** 저널 챕터 */
export interface JournalChapterDto {
  id: number;
  chapterType?: "DIARY" | "NOTE" | "DREAM";
  title?: string;
  /** 일반 챕터에 선택된 개인 말머리. 시스템 요약·DREAM에는 존재하지 않는다. */
  prefix?: JournalPrefixDto | null;
  prefixId?: number | null;
  /** 시스템 요약 챕터 여부. 사용자 선택 분류와 독립적이다. */
  summaryYn?: string;
  journalDayId?: number;
  stdrdDt?: string;
  sortOrder?: number;
  /** 등록자 ID (백엔드 BaseAuditRegDto) */
  createdBy?: string;
  /** 등록자 표시명 */
  createdByNm?: string;
  /** 현재 로그인 사용자 소유 여부 (백엔드 getIsCreatedBy 직렬화) */
  isCreatedBy?: boolean;
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

/** 챕터 Prefix 필터로 숨겨진 말머리 힌트 — 백엔드 JournalChapterPrefixHintDto 직렬화 구조 */
export interface JournalChapterPrefixHintDto {
  prefixId?: number;
  prefixName?: string;
  prefixColor?: string;
}

/** 저널 일자에 투영된 현재 사용자 휴가의 시간 범위 상태 */
export type VacationDayStatus = "NONE" | "FULL_DAY" | "AM_HALF" | "PM_HALF" | "UNKNOWN";

/** 저널 일자 */
export interface JournalDayDto {
  id: number;
  journalDate?: string;
  /** 기준일자 — 백엔드 getStdrdDt() getter 직렬화 (journalDate 와 동일값) */
  stdrdDt?: string;
  journalDatePrecision?: string;
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
  isHolyday?: boolean;
  holydayNm?: string;
  /** 현재 사용자 참가 휴가 상태 — 전역 공휴일·주말 축과 별도 */
  vacationDayStatus?: VacationDayStatus;
  /** 휴가 일정 제목 목록 — 일자 헤더 표시용 */
  vacationReasonList?: string[];
  weather?: string;
  journalChapterList?: JournalChapterDto[];
  journalDreamSectionList?: JournalDreamSectionDto[];
  /** 꿈 목록 보유 여부 — 백엔드 getHasDream() getter 직렬화 */
  hasDream?: boolean;
  /** 챕터 Prefix 필터로 숨겨진 말머리 목록 */
  hiddenChapterPrefixList?: JournalChapterPrefixHintDto[];
  tag?: TagCmpstn;
  meta?: MetaCmpstn;
  state?: StateCmpstn;
  /** 일기 축(챕터·노트) 완결 여부 Y/N */
  diaryResolvedYn?: string;
  /** 꿈 축 완결 여부 Y/N */
  dreamResolvedYn?: string;
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
  diaryLifecycleKey?: string;
  dreamLifecycleKey?: string;
  chapterPrefixIds?: number[];
  /** 정렬 (ASC/DESC) — 백엔드 JournalDaySearchParam.sort */
  sort?: "ASC" | "DESC";
}

/** 태그 클라우드 항목 */
export interface TagCloudItem {
  /** 태그 ID */
  id: number | string;
  /** 태그명 */
  name: string;
  /** 태그 카테고리 */
  ctgr?: string;
  /** 연결된 컨텐츠 수 */
  contentSize: number;
  tagClass?: string;
  /** 글자 크기 CSS 클래스 (ts-1~ts-9) — 실제로는 tagClass 가 ts-N */
  textClass?: string;
}

/** 저널 달력(CAL) 이벤트 — 백엔드 BaseCalDto(JournalDayCalDto·JournalEntryCalDto·공휴일) 직렬화 형태 (FullCalendar 이벤트 입력) */
export interface JournalCalEvent {
  id: string | number;
  title: string;
  /** 이벤트 분기 키 (JOURNAL_DAY | JOURNAL_DIARY | JOURNAL_DREAM | 일정 코드) */
  groupId: string;
  start: string;
  end?: string;
  allDay?: boolean;
  display?: string;
  color?: string;
  className?: string;
  textColor?: string;
  /** 아이콘 HTML (레거시 renderEventContent 계약) */
  icon?: string;
  imprtcYn?: string;
  markdownContent?: string;
  /** DIARY/DREAM 이벤트 클릭 시 열 일자 상세 ID */
  journalDayId?: number;
  contentType?: string;
  [key: string]: unknown;
}

/** 저널 할일 항목 — 백엔드 JournalTodoDto 직렬화 (aside TODO 카드 표시분) */
export interface JournalTodoItem {
  id: number;
  title?: string;
}

export type TagCloudSection = "day" | "diary" | "dream";

/** 태그 클라우드 결과 — 일자/일기/꿈 태그 목록 */
export interface JournalTagCloud {
  /** 일자 태그 목록 */
  dayTagList: TagCloudItem[];
  /** 일기 태그 목록 */
  diaryTagList: TagCloudItem[];
  /** 꿈 태그 목록 */
  dreamTagList: TagCloudItem[];
}
// ---- 스토어 ----

export const useJournalStore = defineStore("journal", () => {
  const { t } = useLocaleStore();
  const now = new Date();

  /** 현재 보기 타입 */
  const viewType = ref<JournalViewType>("LIST");

  /** 현재 조회 기준 연도 */
  const yy = ref<number>(now.getFullYear());

  /** 현재 조회 기준 월 (1-based) */
  const mnth = ref<number>(now.getMonth() + 1);

  /** 주간 뷰 기준 주 시작일 (YYYY-MM-DD) */
  const weekStartDt = ref<string>("");

  /** 일간 뷰 태그 집계 기준일 (YYYY-MM-DD) */
  const dailyStdrdDt = ref<string>("");

  /** 조회된 일자 목록 */
  const dayList = ref<JournalDayDto[]>([]);

  /** 달력(CAL) 이벤트 목록 — dayList 와 응답 형태가 달라 별도 상태로 보관 */
  const calEventList = ref<JournalCalEvent[]>([]);

  /** 로딩 상태 */
  const loading = ref<boolean>(false);

  /** 에러 메시지 */
  const error = ref<string | null>(null);

  // 필터 상태
  const showDiaries = ref<boolean>(true);
  const showDreams = ref<boolean>(true);
  /** 리플렉션 기본 접힘 표시 모드. 조회 필터가 아니며 필터 초기화 대상이 아니다. localStorage 로 복원한다. */
  const reflectionDefaultCollapsed = ref<boolean>(readReflectionDefaultCollapsedFromStorage());
  /** 레거시 기본: 태그 클라우드 표시 (aside TAGCLOUD 토글과 연동) */
  const showTagCloud = ref<boolean>(true);
  /** 일자 목록 정렬 — FILTER 헤더 SORT 버튼과 연동. 레거시 기본: DESC, localStorage("journal_day_sort") 로 복원 */
  const sortOrder = ref<"ASC" | "DESC">(
    (localStorage.getItem("journal_day_sort") as "ASC" | "DESC") || "DESC"
  );
  const diaryKeyword = ref<string>("");
  const dreamKeyword = ref<string>("");
  const diaryLifecycleKey = ref<string>("");
  const dreamLifecycleKey = ref<string>("");
  const chapterPrefixIds = ref<number[]>([]);

  /** 메타 목록 */
  const metaList = ref<MetaDto[]>([]);
  /** 메타 VIEW 에서 선택된 메타 (최대 2개, 비교 그래프용) */
  const selectedMetas = ref<MetaDto[]>([]);
  /** 메타 목록 로딩 상태 */
  const metaLoading = ref<boolean>(false);
  /** 메타 조회 에러 메시지 */
  const metaError = ref<string | null>(null);

  /** 태그 클라우드 결과 */
  /** aside TODO 카드 목록 (레거시 journal_todo yyMnthListAjax 등가 — 현재 년/월 기준) */
  const todoList = ref<JournalTodoItem[]>([]);
  /** aside TODO 목록 조회 실패 메시지 */
  const todoError = ref<string | null>(null);

  /** aside TODO 목록 조회 — 등록/삭제 후에도 호출해 카드를 갱신한다. */
  async function fetchTodos() {
    todoError.value = null;
    try {
      const res = await axios.get("/api/journal/todos", { params: { yy: yy.value, mnth: mnth.value } });
      if (!res.data?.rslt) {
        todoError.value = res.data?.message ?? t("journal.todo.list.load.failure");
        return;
      }
      todoList.value = (res.data?.rsltList ?? []) as JournalTodoItem[];
    } catch (e: unknown) {
      console.error("[journal] fetchTodos failed", { yy: yy.value, mnth: mnth.value }, e);
      todoError.value = t("journal.todo.list.load.failure");
    }
  }

  const tagCloud = ref<JournalTagCloud>({ dayTagList: [], diaryTagList: [], dreamTagList: [] });
  /** 태그 클라우드 섹션별 조회 실패 메시지 */
  const tagCloudSectionError = ref<Partial<Record<TagCloudSection, string>>>({});
  /** 태그 클라우드 로딩 상태 */
  const tagCloudLoading = ref<boolean>(false);
  const tagCloudRequestSeq: Record<TagCloudSection, number> = { day: 0, diary: 0, dream: 0 };
  /** 같은 섹션·기간의 동시 조회를 하나의 HTTP 요청으로 합친다. */
  const tagCloudRequests = new Map<string, Promise<void>>();
  /** 로그아웃·세션 만료 전 요청의 상태 반영과 로딩 변경을 차단하는 세대. */
  let tagCloudGeneration = 0;
  let tagCloudLoadingCount = 0;

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
      const resolvedViewType = params?.viewType ?? viewType.value;
      const resolvedYy = params?.yy ?? yy.value;
      const resolvedMnth = params?.mnth ?? mnth.value;

      if (resolvedViewType === "DAILY" && params?.stdrdDt?.trim()) {
        dailyStdrdDt.value = params.stdrdDt.trim();
      }

      if (resolvedViewType === "WEEKLY") {
        const resolvedWeekStart =
          params?.weekStartDt?.trim() ||
          weekStartDt.value ||
          resolveWeekStartDt({
            stdrdDt: params?.stdrdDt,
            yy: resolvedYy,
            mnth: resolvedMnth,
          });
        weekStartDt.value = resolvedWeekStart;
      }

      const query: Record<string, unknown> = {
        viewType: resolvedViewType,
        yy: resolvedYy,
        mnth: resolvedMnth,
        showDiaries: params?.showDiaries ?? showDiaries.value,
        showDreams: params?.showDreams ?? showDreams.value,
        showTagCloud: params?.showTagCloud ?? showTagCloud.value,
        ...(diaryKeyword.value ? { diaryKeyword: diaryKeyword.value } : {}),
        ...(dreamKeyword.value ? { dreamKeyword: dreamKeyword.value } : {}),
        ...(diaryLifecycleKey.value ? { diaryLifecycleKey: diaryLifecycleKey.value } : {}),
        ...(dreamLifecycleKey.value ? { dreamLifecycleKey: dreamLifecycleKey.value } : {}),
        // axios 1.x 는 배열을 chapterPrefixIds[]=1 형식으로 직렬화해 Spring @ModelAttribute 바인딩이 안 됨.
        // Spring 컬렉션 변환기가 처리할 수 있도록 콤마 구분 단일 문자열로 전송한다.
        ...(chapterPrefixIds.value.length > 0 ? { chapterPrefixIds: chapterPrefixIds.value.join(",") } : {}),
        ...(resolvedViewType === "WEEKLY" && weekStartDt.value
          ? { weekStartDt: weekStartDt.value }
          : {}),
        ...(params?.stdrdDt ? { stdrdDt: params.stdrdDt } : {}),
        sort: params?.sort ?? sortOrder.value,
      };
      const res = await axios.get("/api/journal/days", { params: query });
      if (!res.data?.rslt) {
        console.error("[journal] fetchDays soft-fail", { viewType: resolvedViewType, message: res.data?.message });
        error.value = res.data?.message ?? t("journal.day.list.load.failure");
        return;
      }
      if (resolvedViewType === "CAL") {
        // CAL 은 FullCalendar 이벤트(BaseCalDto) 응답 — dayList 와 형태가 달라 별도 상태에 담고 정렬 반전도 하지 않는다.
        calEventList.value = (res.data?.rsltList ?? []) as JournalCalEvent[];
        return;
      }
      // 변경: 백엔드 AjaxResponse.rsltList 필드명으로 수정 (기존: res.data?.list)
      // 레거시 동일: 백엔드는 항상 ASC 반환 → DESC 이면 프론트에서 reverse
      const rslt: JournalDayDto[] = res.data?.rsltList ?? [];
      dayList.value = (query.sort ?? sortOrder.value) !== "ASC" ? [...rslt].reverse() : rslt;
    } catch (e: unknown) {
      const vt = params?.viewType ?? viewType.value;
      console.error("[journal] fetchDays failed", { viewType: vt, weekStartDt: weekStartDt.value }, e);
      error.value = t("journal.day.list.load.failure");
    } finally {
      loading.value = false;
      void reinitMetronicAfterDom();
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
      console.error("[journal] fetchMetas failed", e);
      metaError.value = useLocaleStore().t("journal.meta.list.load.failure");
    } finally {
      metaLoading.value = false;
    }
  }

  /**
   * 메타 VIEW 그래프에 메타를 추가한다. 최대 2개. 이미 있으면 true, 추가 불가(꽉 참)면 false.
   */
  function addMetaToGraph(meta: MetaDto): boolean {
    if (meta.id == null) return false;
    if (selectedMetas.value.some((m) => m.id === meta.id)) return true;
    if (selectedMetas.value.length >= 2) return false;
    selectedMetas.value = [...selectedMetas.value, meta];
    return true;
  }

  /** 메타 VIEW 그래프 선택에서 메타를 제거한다. */
  function removeMetaFromGraph(metaId: number | string): void {
    selectedMetas.value = selectedMetas.value.filter((m) => String(m.id) !== String(metaId));
  }

  /** 메타가 현재 그래프 선택 목록에 포함되는지 여부 */
  function isMetaSelected(meta: MetaDto): boolean {
    return meta.id != null && selectedMetas.value.some((m) => m.id === meta.id);
  }

  /**
   * 태그 클라우드 조회.
   * 같은 섹션·기간의 진행 중 요청은 공유하고, 기간이 다른 요청은 섹션별 최신 응답만 반영한다.
   */
  async function fetchTagCloud(options: { sections?: TagCloudSection[] } = {}) {
    const sections: TagCloudSection[] = options.sections?.length
      ? options.sections
      : ["day", "diary", "dream"];
    const periodParams = getTagPeriodParams();
    const requestGeneration = tagCloudGeneration;
    tagCloudLoadingCount += 1;
    tagCloudLoading.value = true;
    try {
      await Promise.all(
        sections.map((section) => fetchTagCloudSection(section, periodParams, requestGeneration))
      );
    } finally {
      if (requestGeneration !== tagCloudGeneration) return;
      tagCloudLoadingCount = Math.max(0, tagCloudLoadingCount - 1);
      tagCloudLoading.value = tagCloudLoadingCount > 0;
    }
  }

  /** 섹션·기간별 진행 중 태그 클라우드 조회를 공유한다. */
  function fetchTagCloudSection(
    section: TagCloudSection,
    periodParams: Record<string, string | number>,
    requestGeneration: number,
  ): Promise<void> {
    const requestKey = getTagCloudRequestKey(section, periodParams);
    const inFlight = tagCloudRequests.get(requestKey);
    if (inFlight) {
      console.info("[journal] 진행 중 태그 클라우드 조회 공유", { section, periodParams });
      return inFlight;
    }

    const requestSeq = ++tagCloudRequestSeq[section];
    const nextError = { ...tagCloudSectionError.value };
    delete nextError[section];
    tagCloudSectionError.value = nextError;

    let request!: Promise<void>;
    request = requestTagCloudSection(section, periodParams, requestSeq, requestGeneration)
      .finally(() => {
        if (tagCloudRequests.get(requestKey) === request) tagCloudRequests.delete(requestKey);
      });
    tagCloudRequests.set(requestKey, request);
    return request;
  }

  /** 태그 클라우드 섹션 하나를 조회하고 현재 기간 요청일 때만 상태에 반영한다. */
  async function requestTagCloudSection(
    section: TagCloudSection,
    periodParams: Record<string, string | number>,
    requestSeq: number,
    requestGeneration: number,
  ): Promise<void> {
    try {
      const res = section === "day"
        ? await axios.get("/api/journal/day/tags", { params: periodParams })
        : await axios.get("/api/journal/entry/tags", {
          params: { ...periodParams, type: section === "diary" ? "DIARY" : "DREAM" },
        });
      if (
        requestGeneration !== tagCloudGeneration
        || requestSeq !== tagCloudRequestSeq[section]
      ) {
        console.info("[journal] 무효화된 태그 클라우드 응답 폐기", {
          section,
          periodParams,
          requestGeneration,
          activeGeneration: tagCloudGeneration,
          requestSeq,
          activeRequestSeq: tagCloudRequestSeq[section],
        });
        return;
      }

      const tagList = normalizeTagCloudList(res.data?.rsltList);
      if (section === "day") {
        tagCloud.value = { ...tagCloud.value, dayTagList: tagList };
        return;
      }
      if (section === "diary") {
        tagCloud.value = { ...tagCloud.value, diaryTagList: tagList };
        return;
      }
      tagCloud.value = { ...tagCloud.value, dreamTagList: tagList };
    } catch (e: unknown) {
      console.error("[journal] fetchTagCloud failed", { section, periodParams }, e);
      if (
        requestGeneration !== tagCloudGeneration
        || requestSeq !== tagCloudRequestSeq[section]
      ) {
        console.info("[journal] 무효화된 태그 클라우드 실패 응답 폐기", {
          section,
          periodParams,
          requestGeneration,
          activeGeneration: tagCloudGeneration,
          requestSeq,
          activeRequestSeq: tagCloudRequestSeq[section],
        });
        return;
      }
      tagCloudSectionError.value = {
        ...tagCloudSectionError.value,
        [section]: t("journal.tag-cloud.load.failure"),
      };
    }
  }

  /** 섹션과 일간·주간·월간 기간을 진행 중 요청 식별자로 직렬화한다. */
  function getTagCloudRequestKey(
    section: TagCloudSection,
    periodParams: Record<string, string | number>,
  ): string {
    if (periodParams.stdrdDt != null) {
      return `${section}|stdrdDt=${String(periodParams.stdrdDt)}`;
    }
    if (periodParams.weekStartDt != null) {
      return `${section}|weekStartDt=${String(periodParams.weekStartDt)}`;
    }
    return `${section}|yy=${String(periodParams.yy)}&mnth=${String(periodParams.mnth)}`;
  }

  /** 로그아웃·세션 만료 시 태그 클라우드 상태와 이전 세대의 진행 중 요청을 초기화한다. */
  function resetTagCloudState(): void {
    tagCloudGeneration += 1;
    tagCloudRequests.clear();
    tagCloud.value = { dayTagList: [], diaryTagList: [], dreamTagList: [] };
    tagCloudSectionError.value = {};
    tagCloudLoadingCount = 0;
    tagCloudLoading.value = false;
    console.info("[journal] 사용자 세션 태그 클라우드 초기화", { tagCloudGeneration });
  }

  /** 현재 화면 단위에 맞는 일간·주간·월간 태그 집계 파라미터를 반환한다. */
  function getTagPeriodParams(): Record<string, string | number> {
    if (viewType.value === "DAILY" && dailyStdrdDt.value) {
      return { stdrdDt: dailyStdrdDt.value };
    }
    if (viewType.value === "WEEKLY" && weekStartDt.value) {
      return { weekStartDt: weekStartDt.value };
    }
    return { yy: yy.value, mnth: mnth.value };
  }

  function normalizeTagCloudList(rawList: unknown): TagCloudItem[] {
    if (!Array.isArray(rawList)) return [];
    return rawList
      .map((raw) => {
        const item = raw as Record<string, unknown>;
        return {
          id: (item.id as number | string | undefined) ?? "",
          name: String(item.name ?? ""),
          ctgr: String(item.ctgr ?? ""),
          contentSize: Number(item.contentSize ?? 0),
          tagClass: String(item.tagClass ?? ""),
          textClass: String(item.textClass ?? ""),
        };
      })
      .filter((item) => item.id !== "" && item.name !== "");
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
    if (viewType.value === "WEEKLY") {
      weekStartDt.value = resolveWeekStartDt({ yy: y, mnth: m });
    }
    fetchDays();
  }

  /**
   * 오늘이 속한 월로 이동한다.
   */
  function gotoToday() {
    const today = new Date();
    yy.value = today.getFullYear();
    mnth.value = today.getMonth() + 1;
    if (viewType.value === "WEEKLY") {
      weekStartDt.value = resolveWeekStartDt({ yy: yy.value, mnth: mnth.value });
    }
    fetchDays();
  }

  /**
   * 년도·월을 직접 지정해 이동한다.
   */
  function gotoYyMnth(newYy: number, newMnth: number) {
    yy.value = newYy;
    mnth.value = newMnth;
    if (viewType.value === "WEEKLY") {
      weekStartDt.value = resolveWeekStartDt({ yy: newYy, mnth: newMnth });
    }
    fetchDays();
  }

  /**
   * 주 이동 (delta: -1 이전주, +1 다음주).
   * weekStartDt 기준으로 7일 단위로 이동하며 yy/mnth를 갱신한다.
   */
  function navigateWeek(delta: number) {
    const base = new Date(
      (weekStartDt.value || formatLocalDateStr(new Date())) + "T12:00:00"
    );
    base.setDate(base.getDate() + delta * 7);
    weekStartDt.value = formatLocalDateStr(base);
    yy.value = base.getFullYear();
    mnth.value = base.getMonth() + 1;
    fetchDays();
  }

  /**
   * 보기 타입을 전환한다.
   * 화면에서 이미 이동(라우팅)이 완료된 뒤 호출한다.
   */
  function setViewType(vt: JournalViewType) {
    viewType.value = vt;
  }

  /**
   * 리플렉션 기본 접힘 표시 모드 토글. API 재조회 없이 localStorage 만 갱신한다.
   */
  function toggleReflectionDefaultCollapsed() {
    reflectionDefaultCollapsed.value = !reflectionDefaultCollapsed.value;
    writeReflectionDefaultCollapsedToStorage(reflectionDefaultCollapsed.value);
  }

  /**
   * FILTER 헤더 SORT 토글. 정렬 방향 변경 후 목록 재조회.
   */
  function toggleSort() {
    sortOrder.value = sortOrder.value === "ASC" ? "DESC" : "ASC";
    localStorage.setItem("journal_day_sort", sortOrder.value);
    void fetchDays();
  }

  /**
   * dayList 트리 내에서 특정 entry 의 reflectionList 와 lifecycle 을 in-place 교체한다.
   * 리플렉션 등록/수정 응답으로 fetchDays() 전체 재호출 없이 부분 갱신할 때 사용한다.
   *
   * @param targetId 대상 entry ID
   * @param targetContentType 대상 entry contentType
   * @param reflectionList 갱신된 reflectionList
   * @param lifecycleKey 갱신된 lifecycle 키
   */
  function patchEntryReflections(
    targetId: number,
    targetContentType: string,
    reflectionList: JournalEntryDto[],
    lifecycleKey?: string,
  ): void {
    for (const day of dayList.value) {
      if (!day.journalChapterList) continue;
      for (const chapter of day.journalChapterList) {
        if (!chapter.journalEntryList) continue;
        for (const entry of chapter.journalEntryList) {
          if (entry.id === targetId && entry.contentType === targetContentType) {
            entry.reflectionList = reflectionList;
            if (lifecycleKey && entry.lifecycle) {
              entry.lifecycle.lifecycleKey = lifecycleKey;
            } else if (lifecycleKey) {
              entry.lifecycle = { lifecycleKey };
            }
            /* 부분 갱신으로 새로 렌더된 리플렉션 임베드의 ⋯(KTMenu)를 재바인딩한다.
               fetchDays 경로와 달리 여기서 호출하지 않으면 신규 등록 리플렉션의 컨텍스트 메뉴가 바인딩되지 않는다. */
            void reinitMetronicAfterDom();
            return;
          }
        }
      }
    }
  }

  /**
   * dayList 트리에서 특정 엔트리/리플렉션의 lifecycle 을 in-place 로 교체한다.
   * 라이프사이클 변경 성공 직후, 서버 재조회의 캐시 staleness(간헐적으로 옛 값 반환)와 무관하게
   * 화면 접힘/펼침을 즉시·확정 반영하기 위한 낙관적 갱신이다. cascade(리플렉션·챕터 집계)는 재조회가 정합화한다.
   *
   * @param targetId 대상 엔트리/리플렉션 ID
   * @param targetContentType 대상 contentType (JOURNAL_DIARY/DREAM/NOTE/REFLECTION)
   * @param lifecycleKey 새 lifecycle 키
   */
  function patchEntryLifecycle(
    targetId: number,
    targetContentType: string,
    lifecycleKey: string,
  ): void {
    const applyTo = (entry: JournalEntryDto): boolean => {
      if (entry.id !== targetId || entry.contentType !== targetContentType) return false;
      if (entry.lifecycle) entry.lifecycle.lifecycleKey = lifecycleKey;
      else entry.lifecycle = { lifecycleKey };
      return true;
    };
    for (const day of dayList.value) {
      if (!day.journalChapterList) continue;
      for (const chapter of day.journalChapterList) {
        if (!chapter.journalEntryList) continue;
        for (const entry of chapter.journalEntryList) {
          if (applyTo(entry)) return;
          for (const refl of entry.reflectionList ?? []) {
            if (applyTo(refl)) return;
          }
        }
      }
    }
  }

  return {
    viewType,
    yy,
    mnth,
    weekStartDt,
    dailyStdrdDt,
    dayList,
    calEventList,
    loading,
    error,
    showDiaries,
    showDreams,
    reflectionDefaultCollapsed,
    showTagCloud,
    sortOrder,
    diaryKeyword,
    dreamKeyword,
    diaryLifecycleKey,
    dreamLifecycleKey,
    chapterPrefixIds,
    metaList,
    selectedMetas,
    metaLoading,
    metaError,
    yyMnthLabel,
    fetchDays,
    fetchMetas,
    addMetaToGraph,
    removeMetaFromGraph,
    isMetaSelected,
    navigateMonth,
    navigateWeek,
    gotoToday,
    gotoYyMnth,
    setViewType,
    toggleReflectionDefaultCollapsed,
    toggleSort,
    tagCloud,
    tagCloudSectionError,
    tagCloudLoading,
    fetchTagCloud,
    resetTagCloudState,
    todoList,
    todoError,
    fetchTodos,
    patchEntryReflections,
    patchEntryLifecycle,
  };
});
