import { computed, ref } from "vue";
import axios from "axios";
import type {
  JournalPeriodThreadSummaryItem,
  JournalPeriodThreadSummaryQuery,
  JournalThreadDto,
  ThreadPrefix,
} from "@/features/journal/stores/journalThread.types";

export interface JournalThreadListDeps {
  t: (key: string) => string;
  personalPrefixOptionsStore: {
    optionsFor: (contentType: string) => unknown[];
    fetchOptions: (contentType: string, force?: boolean) => Promise<boolean>;
  };
}

export function createJournalThreadList(deps: JournalThreadListDeps) {
  const { t, personalPrefixOptionsStore } = deps;

  // ---- 목록 ----

  /** 스레드 목록 */
  const threadList = ref<JournalThreadDto[]>([]);
  /** 전체 레코드 수 */
  const totalElements = ref(0);
  /** 전체 페이지 수 */
  const totalPages = ref(0);
  /** 현재 페이지 (0-based) */
  const currentPage = ref(0);
  /** 페이지 크기 */
  const pageSize = ref(10);
  /** 목록 로딩 상태 */
  const loading = ref(false);
  /** 목록 에러 */
  const error = ref<string | null>(null);
  /** 검색 키워드 필터 */
  const filterKeyword = ref("");
  /** 단일 말머리 필터 */
  const filterPrefixId = ref("");
  /** 라이프사이클 필터 (OPEN/PENDING/RESOLVED, 빈 값=전체) */
  const filterLifecycleKey = ref("");
  /**
   * 소속 엔트리 태그 필터 (멀티, AND).
   * Spring 배열 바인딩을 위해 문자열 ID 를 유지하고 fetchList 에서 tagIds 반복 파라미터로 보낸다.
   */
  const filterTagIds = ref<string[]>([]);
  /** filterTagIds 표시용 이름 캐시 (tagId → name) */
  const filterTagLabelMap = ref<Record<string, string>>({});
  /** tagId → 태그 카테고리(ctgr). 배지에 `[ctgr]` 로 표시한다. */
  const filterTagCtgrMap = ref<Record<string, string>>({});
  /** 목록 검색·등록·수정이 공유하는 JOURNAL_THREAD 활성 개인 Prefix 선택지. */
  const prefixOptions = computed<ThreadPrefix[]>(() =>
    personalPrefixOptionsStore.optionsFor("JOURNAL_THREAD") as ThreadPrefix[]
  );
  /** 말머리 선택지 조회 또는 편집 폼 빠른 추가 오류 */
  const prefixError = ref("");

  // ---- 기간별 요약 ----

  /** 현재 월간·주간 조회 기간에 등장한 스레드 집계 */
  const periodSummary = ref<JournalPeriodThreadSummaryItem[]>([]);
  /** 기간별 스레드 집계 로딩 여부 */
  const periodSummaryLoading = ref(false);
  /** 기간별 스레드 집계 오류 */
  const periodSummaryError = ref("");
  /** 늦게 끝난 이전 기간 응답이 현재 기간을 덮지 못하게 하는 요청 순번 */
  let periodSummaryRequestToken = 0;
  /** 기간 요약이 마지막으로 조회한 조건. 소속 변경 후 재조회와 화면 이탈 시 비활성 판단에 쓴다. */
  const lastPeriodSummaryQuery = ref<JournalPeriodThreadSummaryQuery | null>(null);

  // ---- 목록 액션 ----

  /**
   * 스레드 목록 조회.
   * GET /api/journal/threads — 페이지네이션 + 검색 파라미터 적용.
   * @param page - 페이지 번호 (0-based, 생략 시 currentPage 유지)
   */
  async function fetchList(page?: number) {
    loading.value = true;
    error.value = null;
    const targetPage = page ?? currentPage.value;
    try {
      const params = new URLSearchParams();
      params.set("page", String(targetPage));
      params.set("size", String(pageSize.value));
      if (filterKeyword.value) {
        params.set("searchType", "title");
        params.set("searchKeyword", filterKeyword.value);
      }
      if (filterPrefixId.value) params.set("prefixId", filterPrefixId.value);
      if (filterLifecycleKey.value) params.set("lifecycleKey", filterLifecycleKey.value);
      // tagIds=1&tagIds=2 — Spring List<Integer> 바인딩 계약 (엔트리 검색과 동일)
      filterTagIds.value.forEach((tagId) => params.append("tagIds", tagId));
      const res = await axios.get("/api/journal/threads", { params });
      if (!res.data?.rslt) {
        console.error("[journalThread] fetchList soft-fail", { page, message: res.data?.message });
        error.value = res.data?.message ?? t("journal.thread.list.load.failure");
        return;
      }
      /* Spring Page<T> → { content, totalElements, totalPages, number, size } */
      const pageResult = res.data?.rsltObj;
      threadList.value = pageResult?.content ?? [];
      totalElements.value = pageResult?.totalElements ?? 0;
      totalPages.value = pageResult?.totalPages ?? 0;
      currentPage.value = pageResult?.number ?? 0;
    } catch (e: unknown) {
      console.error("[journalThread] fetchList failed", { page }, e);
      error.value = t("journal.thread.list.load.failure");
    } finally {
      loading.value = false;
    }
  }

  /**
   * 태그 필터에 표시 이름·카테고리를 캐시한다.
   * 카테고리는 일자 필터/엔트리 태그와 동일하게 배지에서 `[ctgr]` 로 쓴다.
   */
  function cacheFilterTagLabel(tagId?: number | string, name?: string, ctgr?: string): void {
    if (tagId === undefined || tagId === null || !name) return;
    const key = String(tagId);
    filterTagLabelMap.value[key] = name;
    if (ctgr !== undefined) {
      filterTagCtgrMap.value[key] = ctgr;
    }
  }

  /**
   * 태그 필터에 tagId 를 추가한다 (중복 무시).
   * @returns 새로 추가됐으면 true
   */
  function addFilterTag(tagId: number | string, name?: string, ctgr?: string): boolean {
    const next = String(tagId);
    cacheFilterTagLabel(next, name, ctgr);
    if (filterTagIds.value.includes(next)) return false;
    filterTagIds.value = [...filterTagIds.value, next];
    return true;
  }

  /** 태그 필터에서 tagId 를 제거한다. */
  function removeFilterTag(tagId: string): void {
    filterTagIds.value = filterTagIds.value.filter((id) => id !== tagId);
    delete filterTagLabelMap.value[tagId];
    delete filterTagCtgrMap.value[tagId];
  }

  /** 현재 사용자의 활성 말머리 선택지를 조회한다. */
  async function fetchPrefixOptions(): Promise<void> {
    const loaded = await personalPrefixOptionsStore.fetchOptions("JOURNAL_THREAD", true);
    prefixError.value = loaded ? "" : t("journal.thread.prefix.load.failure");
  }

  /**
   * 스레드 기능 진입 시 목록 검색·등록·수정이 공유할 분류 목록을 최초 한 번 조회한다.
   * 실패한 조회는 완료로 캐시하지 않아 다음 스레드 route 동기화에서 재시도한다.
   */
  async function ensurePrefixOptions(): Promise<void> {
    const loaded = await personalPrefixOptionsStore.fetchOptions("JOURNAL_THREAD");
    prefixError.value = loaded ? "" : t("journal.thread.prefix.load.failure");
  }

  /** 검색 조건을 비우고 첫 페이지를 조회한다. */
  async function resetFilters(): Promise<void> {
    filterKeyword.value = "";
    filterPrefixId.value = "";
    filterLifecycleKey.value = "";
    filterTagIds.value = [];
    filterTagLabelMap.value = {};
    filterTagCtgrMap.value = {};
    await fetchList(0);
  }

  /**
   * 현재 월간·주간에 등장한 스레드를 서버 집계 결과로 조회한다.
   * <p>
   * 일기/꿈/키워드 필터가 적용된 dayList를 재집계하지 않고 기간 전체를 조회한다.
   * 기간 전환 중 이전 요청이 늦게 끝나면 응답을 폐기해 다른 기간의 요약이 노출되지 않게 한다.
   */
  async function fetchPeriodSummary(query: JournalPeriodThreadSummaryQuery): Promise<void> {
    lastPeriodSummaryQuery.value = query;
    const requestToken = ++periodSummaryRequestToken;
    periodSummaryLoading.value = true;
    periodSummaryError.value = "";
    periodSummary.value = [];

    try {
      const res = await axios.get("/api/journal/threads/period-summary", { params: query });
      if (requestToken !== periodSummaryRequestToken) {
        console.info("[journalThread] fetchPeriodSummary discarded stale response", {
          query,
          requestToken,
          currentToken: periodSummaryRequestToken,
        });
        return;
      }
      periodSummary.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e: unknown) {
      if (requestToken !== periodSummaryRequestToken) {
        console.info("[journalThread] fetchPeriodSummary discarded stale failure", {
          query,
          requestToken,
          currentToken: periodSummaryRequestToken,
        });
        return;
      }
      console.error("[journalThread] fetchPeriodSummary failed", { query }, e);
      periodSummaryError.value = t("journal.thread.period-summary.load.failure");
    } finally {
      if (requestToken === periodSummaryRequestToken) {
        periodSummaryLoading.value = false;
      }
    }
  }

  /** 기간 요약이 활성일 때만, 마지막 조회 조건으로 다시 조회한다. 소속 변경 후 호출한다. */
  async function refreshPeriodSummary(): Promise<void> {
    const query = lastPeriodSummaryQuery.value;
    if (!query) return;
    await fetchPeriodSummary(query);
  }

  /** 기간 요약 화면 이탈 시 마지막 조회 조건을 비워 비활성 상태의 무의미한 재조회를 막는다. */
  function clearPeriodSummaryQuery(): void {
    lastPeriodSummaryQuery.value = null;
  }

  return {
    threadList,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    filterKeyword,
    filterPrefixId,
    filterLifecycleKey,
    filterTagIds,
    filterTagLabelMap,
    filterTagCtgrMap,
    prefixOptions,
    prefixError,
    periodSummary,
    periodSummaryLoading,
    periodSummaryError,
    fetchList,
    fetchPrefixOptions,
    ensurePrefixOptions,
    fetchPeriodSummary,
    refreshPeriodSummary,
    clearPeriodSummaryQuery,
    cacheFilterTagLabel,
    addFilterTag,
    removeFilterTag,
    resetFilters,
  };
}
