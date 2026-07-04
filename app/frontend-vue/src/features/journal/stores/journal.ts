import { ref, computed } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { formatLocalDateStr, resolveWeekStartDt } from "@/features/journal/utils/journalDate";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import type { JournalDreamSectionDto } from "@/features/journal/utils/journalDream";

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
  contentType?: string;
  refContentNo?: number;
  refTitle?: string;
  relReason?: string;
}

/** 이력 컴포지션 — 백엔드 HistoryCmpstn 직렬화 구조 */
export interface HistoryCmpstn {
  historyTriggeredAt?: string;
}

/** 해석 항목 */
export interface InterpretationItem {
  id: number;
  contentType?: string;
  refId?: number;
  refContentType?: string;
  journalDayId?: number;
  title?: string;
  content?: string;
  markdownContent?: string;
  sortOrder?: number;
  stdrdDt?: string;
  state?: StateCmpstn;
  lifecycle?: LifecycleCmpstn;
  history?: HistoryCmpstn;
  comment?: CommentCmpstn;
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
  history?: HistoryCmpstn;
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
  journalDatePrecision?: string;
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
  isHolyday?: boolean;
  holydayNm?: string;
  weather?: string;
  journalChapterList?: JournalChapterDto[];
  journalDreamSectionList?: JournalDreamSectionDto[];
  /** 꿈 목록 보유 여부 — 백엔드 getHasDream() getter 직렬화 */
  hasDream?: boolean;
  /** 챕터 필터로 숨겨진 카테고리 목록 */
  hiddenChapterCtgrList?: JournalChapterCtgrHintDto[];
  tag?: TagCmpstn;
  meta?: MetaCmpstn;
  state?: StateCmpstn;
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
  chapterCtgrCds?: string[];
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
  /** 글자 크기 CSS 클래스 (ts-1~ts-9) */
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
  const chapterCtgrCds = ref<string[]>([]);

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

  /** aside TODO 목록 조회 — 등록/삭제 후에도 호출해 카드를 갱신한다. */
  async function fetchTodos() {
    try {
      const res = await axios.get("/api/journal/todos", { params: { yy: yy.value, mnth: mnth.value } });
      todoList.value = (res.data?.rsltList ?? []) as JournalTodoItem[];
    } catch (e: unknown) {
      console.error("[journal] fetchTodos failed", { yy: yy.value, mnth: mnth.value }, e);
      todoList.value = [];
    }
  }

  const tagCloud = ref<JournalTagCloud>({ dayTagList: [], diaryTagList: [], dreamTagList: [] });
  /** 태그 클라우드 로딩 상태 */
  const tagCloudLoading = ref<boolean>(false);
  const tagCloudRequestSeq: Record<TagCloudSection, number> = { day: 0, diary: 0, dream: 0 };
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
        // axios 1.x 는 배열을 chapterCtgrCds[]=A 형식으로 직렬화해 Spring @ModelAttribute 바인딩이 안 됨.
        // normalizeChapterCtgrCds 가 콤마 구분 단일 문자열을 분리 처리하므로, join(',') 으로 전송.
        ...(chapterCtgrCds.value.length > 0 ? { chapterCtgrCds: chapterCtgrCds.value.join(",") } : {}),
        ...(resolvedViewType === "WEEKLY" && weekStartDt.value
          ? { weekStartDt: weekStartDt.value }
          : {}),
        ...(params?.stdrdDt ? { stdrdDt: params.stdrdDt } : {}),
        sort: params?.sort ?? sortOrder.value,
      };
      const res = await axios.get("/api/journal/days", { params: query });
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
      if ((params?.viewType ?? viewType.value) === "CAL") {
        calEventList.value = [];
      } else {
        dayList.value = [];
      }
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
   */
  async function fetchTagCloud(options: { sections?: TagCloudSection[] } = {}) {
    const sections: TagCloudSection[] = options.sections?.length
      ? options.sections
      : ["day", "diary", "dream"];
    const sectionSeq = Object.fromEntries(
      sections.map((section) => [section, ++tagCloudRequestSeq[section]])
    ) as Record<TagCloudSection, number>;
    tagCloudLoadingCount += 1;
    tagCloudLoading.value = true;
    try {
      const periodParams = getTagPeriodParams();
      await Promise.all(sections.map(async (section) => {
        try {
          if (section === "day") {
            const res = await axios.get("/api/journal/day/tags", { params: periodParams });
            if (sectionSeq.day === tagCloudRequestSeq.day) {
              tagCloud.value = {
                ...tagCloud.value,
                dayTagList: normalizeTagCloudList(res.data?.rsltList),
              };
            }
            return;
          }
          const type = section === "diary" ? "DIARY" : "DREAM";
          const res = await axios.get("/api/journal/entry/tags", { params: { ...periodParams, type } });
          if (section === "diary" && sectionSeq.diary === tagCloudRequestSeq.diary) {
            tagCloud.value = {
              ...tagCloud.value,
              diaryTagList: normalizeTagCloudList(res.data?.rsltList),
            };
          }
          if (section === "dream" && sectionSeq.dream === tagCloudRequestSeq.dream) {
            tagCloud.value = {
              ...tagCloud.value,
              dreamTagList: normalizeTagCloudList(res.data?.rsltList),
            };
          }
        } catch (e: unknown) {
          console.error("[journal] fetchTagCloud failed", { section }, e);
          if (sectionSeq[section] !== tagCloudRequestSeq[section]) return;
          if (section === "day") {
            tagCloud.value = { ...tagCloud.value, dayTagList: [] };
          } else if (section === "diary") {
            tagCloud.value = { ...tagCloud.value, diaryTagList: [] };
          } else {
            tagCloud.value = { ...tagCloud.value, dreamTagList: [] };
          }
        }
      }));
    } finally {
      tagCloudLoadingCount = Math.max(0, tagCloudLoadingCount - 1);
      tagCloudLoading.value = tagCloudLoadingCount > 0;
    }
  }

  function getTagPeriodParams(): Record<string, string | number> {
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
   * FILTER 헤더 SORT 토글. 정렬 방향 변경 후 목록 재조회.
   */
  function toggleSort() {
    sortOrder.value = sortOrder.value === "ASC" ? "DESC" : "ASC";
    localStorage.setItem("journal_day_sort", sortOrder.value);
    void fetchDays();
  }

  return {
    viewType,
    yy,
    mnth,
    weekStartDt,
    dayList,
    calEventList,
    loading,
    error,
    showDiaries,
    showDreams,
    showTagCloud,
    sortOrder,
    diaryKeyword,
    dreamKeyword,
    diaryLifecycleKey,
    dreamLifecycleKey,
    chapterCtgrCds,
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
    toggleSort,
    tagCloud,
    tagCloudLoading,
    fetchTagCloud,
    todoList,
    fetchTodos,
  };
});
