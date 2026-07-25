import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { swalConfirm, swalAlert, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

// ---- 타입 정의 ----

/** 태그 항목 */
export interface ThreadTagItem {
  tagId: number | string;
  name: string;
  ctgr?: string;
}

/** 목록 검색 카드의 태그 클라우드 항목 */
/** 저널 스레드 분류 선택지 */
export interface ThreadCategoryItem {
  code: string;
  codeName: string;
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
  categoryCode?: string;
  categoryName?: string;
  /** 활성 소속 엔트리 수 (목록 enrich). 없으면 0으로 취급해 숨긴다. */
  membershipCount?: number;
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
  categoryCode?: string;
  title?: string;
  content?: string;
}

/** 월간·주간 저널 화면에 표시할 기간별 스레드 집계 */
export interface JournalPeriodThreadSummaryItem {
  threadId: number;
  title: string;
  /** 조회 기간 안에서 이 스레드에 속한 엔트리 수 */
  entryCount: number;
  /** 조회 기간 안에서 스레드가 처음 등장한 일자 */
  firstEntryDate: string;
}

/** 기간별 스레드 집계 API가 지원하는 조회 계약 */
export type JournalPeriodThreadSummaryQuery =
  | { viewType: "WEEKLY"; weekStartDt: string }
  | { viewType: "LIST"; yy: number; mnth: number };

// ---- 스토어 ----

export const useJournalThreadStore = defineStore("journalThread", () => {
  const { t } = useLocaleStore();

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
  /** 카테고리 필터 */
  const filterCategory = ref("");
  /**
   * 소속 엔트리 태그 필터 (멀티, AND).
   * Spring 배열 바인딩을 위해 문자열 ID 를 유지하고 fetchList 에서 tagIds 반복 파라미터로 보낸다.
   */
  const filterTagIds = ref<string[]>([]);
  /** filterTagIds 표시용 이름 캐시 (tagId → name) */
  const filterTagLabelMap = ref<Record<string, string>>({});
  /** 분류 선택지 */
  const categoryOptions = ref<ThreadCategoryItem[]>([]);
  /** 분류 선택지 조회 오류 */
  const categoryError = ref("");

  // ---- 기간별 요약 ----

  /** 현재 월간·주간 조회 기간에 등장한 스레드 집계 */
  const periodSummary = ref<JournalPeriodThreadSummaryItem[]>([]);
  /** 기간별 스레드 집계 로딩 여부 */
  const periodSummaryLoading = ref(false);
  /** 기간별 스레드 집계 오류 */
  const periodSummaryError = ref("");
  /** 늦게 끝난 이전 기간 응답이 현재 기간을 덮지 못하게 하는 요청 순번 */
  let periodSummaryRequestToken = 0;

  // ---- 등록/수정 (모달/독립 페이지 공용) ----

  /** 등록/수정 표면 활성 여부 */
  const registOpen = ref(false);
  /** 등록/수정 표면 종류. null이면 편집기가 닫힌 상태다. */
  const registSurface = ref<"modal" | "page" | null>(null);
  /** 등록/수정 로딩 여부 */
  const registLoading = ref(false);
  /** 등록/수정 폼 모델 */
  const registModel = ref<JournalThreadRegistModel | null>(null);
  /** 편집 시작 시점의 정규화된 폼 값. 독립 페이지 이탈 시 변경 여부를 판정한다. */
  const registInitialSnapshot = ref("");
  /** 편집 시작 뒤 제목·본문·분류 값이 달라졌는지 여부 */
  const registDirty = computed(() => {
    if (!registOpen.value || !registModel.value) return false;
    return snapshotRegistModel(registModel.value) !== registInitialSnapshot.value;
  });
  /** 등록/수정 처리 중 여부 */
  const submitting = ref(false);
  /** 수정 전환 중 늦게 도착한 이전 응답을 폐기하기 위한 요청 토큰 */
  let registRequestToken = 0;
  /** 문맥형 상세에서 수정에 진입한 동안 보류한 상세 표면 */
  const suspendedDetailSurface = ref<"modal" | "page" | null>(null);
  /** 보류한 상세와 수정 대상이 같은지 검증하기 위한 스레드 ID */
  const suspendedDetailId = ref<number | null>(null);
  /** 수정 종료 뒤 복원할 문맥형 상세가 있는지 여부 */
  const hasSuspendedDetailEdit = computed(() => suspendedDetailSurface.value != null);

  // ---- 상세 (모달/독립 페이지 공용) ----

  /** 상세 표면 활성 여부. 모달과 독립 페이지가 같은 상세 SSOT를 공유한다. */
  const detailOpen = ref(false);
  /** 상세 표면 종류. null이면 상세가 닫힌 상태다. */
  const detailSurface = ref<"modal" | "page" | null>(null);
  /** 상세 로딩 여부 */
  const detailLoading = ref(false);
  /** 상세 DTO */
  const detailModel = ref<JournalThreadDto | null>(null);
  /** 상세의 소속 엔트리 목록 (일자순, 카드 표시용) */
  const detailEntries = ref<JournalEntryDto[]>([]);
  /** 소속 엔트리 로딩 여부 */
  const detailEntriesLoading = ref(false);
  /** 상세 전환 중 늦게 도착한 이전 응답을 폐기하기 위한 요청 토큰 */
  let detailRequestToken = 0;

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
      if (filterCategory.value) params.set("categoryCode", filterCategory.value);
      // tagIds=1&tagIds=2 — Spring List<Integer> 바인딩 계약 (엔트리 검색과 동일)
      filterTagIds.value.forEach((tagId) => params.append("tagIds", tagId));
      const res = await axios.get("/api/journal/threads", { params });
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

  /** 태그 필터에 표시 이름을 캐시한다. */
  function cacheFilterTagLabel(tagId?: number | string, name?: string): void {
    if (tagId === undefined || tagId === null || !name) return;
    filterTagLabelMap.value[String(tagId)] = name;
  }

  /**
   * 태그 필터에 tagId 를 추가한다 (중복 무시).
   * @returns 새로 추가됐으면 true
   */
  function addFilterTag(tagId: number | string, name?: string): boolean {
    const next = String(tagId);
    cacheFilterTagLabel(next, name);
    if (filterTagIds.value.includes(next)) return false;
    filterTagIds.value = [...filterTagIds.value, next];
    return true;
  }

  /** 태그 필터에서 tagId 를 제거한다. */
  function removeFilterTag(tagId: string): void {
    filterTagIds.value = filterTagIds.value.filter((id) => id !== tagId);
    delete filterTagLabelMap.value[tagId];
  }

  /** 현재 locale이 적용된 저널 스레드 분류 선택지를 조회한다. */
  async function fetchCategoryOptions(): Promise<void> {
    categoryError.value = "";
    try {
      const res = await axios.get("/api/journal/threads/categories");
      categoryOptions.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e: unknown) {
      console.error("[journalThread] fetchCategoryOptions failed", e);
      categoryOptions.value = [];
      categoryError.value = t("journal.thread.category.load.failure");
    }
  }

  /** 검색 조건을 비우고 첫 페이지를 조회한다. */
  async function resetFilters(): Promise<void> {
    filterKeyword.value = "";
    filterCategory.value = "";
    filterTagIds.value = [];
    filterTagLabelMap.value = {};
    await fetchList(0);
  }

  /**
   * 현재 월간·주간에 등장한 스레드를 서버 집계 결과로 조회한다.
   * <p>
   * 일기/꿈/키워드 필터가 적용된 dayList를 재집계하지 않고 기간 전체를 조회한다.
   * 기간 전환 중 이전 요청이 늦게 끝나면 응답을 폐기해 다른 기간의 요약이 노출되지 않게 한다.
   */
  async function fetchPeriodSummary(query: JournalPeriodThreadSummaryQuery): Promise<void> {
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

  // ---- 등록/수정 액션 ----

  /** 등록/수정 이탈 판정에 필요한 필드만 같은 순서로 직렬화한다. */
  function snapshotRegistModel(model: JournalThreadRegistModel): string {
    return JSON.stringify({
      id: model.id ?? null,
      contentType: model.contentType ?? "JOURNAL_THREAD",
      categoryCode: model.categoryCode ?? "",
      title: model.title ?? "",
      content: model.content ?? "",
    });
  }

  /** 스레드 등록 모달을 연다 (신규). */
  async function openRegist() {
    if (!await assertAuthenticatedBeforeModal()) return;
    registRequestToken += 1;
    suspendedDetailSurface.value = null;
    suspendedDetailId.value = null;
    registModel.value = {
      contentType: "JOURNAL_THREAD",
      categoryCode: "",
      title: "",
      content: "",
    };
    registInitialSnapshot.value = snapshotRegistModel(registModel.value);
    registSurface.value = "modal";
    registOpen.value = true;
  }

  /**
   * 스레드 수정 폼을 지정한 표면으로 열고 API 에서 기존 데이터를 조회한다.
   * 문맥형 상세에서 진입한 경우 상세 표면만 보류하고 상세 데이터는 유지한다.
   *
   * @param id - 스레드 ID
   * @param surface - 수정 렌더 표면
   * @param returnDetailSurface - 수정 종료 뒤 복원할 상세 표면
   * @return 현재 요청이 유효한 수정 모델을 적용했으면 true
   */
  async function loadModify(
    id: number,
    surface: "modal" | "page",
    returnDetailSurface: "modal" | "page" | null = null,
  ): Promise<boolean> {
    if (!await assertAuthenticatedBeforeModal()) return false;
    const requestToken = ++registRequestToken;
    suspendedDetailSurface.value = returnDetailSurface;
    suspendedDetailId.value = returnDetailSurface ? id : null;
    if (returnDetailSurface) detailSurface.value = null;
    registLoading.value = true;
    registModel.value = null;
    try {
      registSurface.value = surface;
      registOpen.value = true;
      const res = await axios.get(`/api/journal/threads/${id}`);
      if (requestToken !== registRequestToken) {
        console.info("[journalThread] loadModify discarded stale response", {
          id,
          surface,
          requestToken,
          currentToken: registRequestToken,
        });
        return false;
      }
      const dto = res.data?.rsltObj as JournalThreadDto | null | undefined;
      if (!dto?.id || dto.id !== id) {
        console.warn("[journalThread] loadModify rejected invalid detail", {
          requestedId: id,
          responseId: dto?.id,
          surface,
        });
        closeRegist();
        void swalAlert(t("journal.thread.modify.load.failure"));
        return false;
      }
      registModel.value = {
        id: dto.id,
        contentType: dto.contentType ?? "JOURNAL_THREAD",
        categoryCode: dto.categoryCode ?? "",
        title: dto.title ?? "",
        content: dto.content ?? "",
      };
      registInitialSnapshot.value = snapshotRegistModel(registModel.value);
      return true;
    } catch (e: unknown) {
      if (requestToken !== registRequestToken) {
        console.info("[journalThread] loadModify discarded stale failure", {
          id,
          surface,
          requestToken,
          currentToken: registRequestToken,
        });
        return false;
      }
      console.error("[journalThread] loadModify failed", { id, surface }, e);
      closeRegist();
      void swalRequestError(e, t("journal.thread.modify.load.failure"));
      return false;
    } finally {
      if (requestToken === registRequestToken) registLoading.value = false;
    }
  }

  /** 스레드 자체가 주 문맥인 독립 수정 페이지를 연다. */
  async function openModifyPage(id: number): Promise<boolean> {
    return loadModify(id, "page");
  }

  /**
   * 문맥형 상세를 닫지 않고 같은 앱의 수정 모달로 전환한다.
   * 수정 취소·저장 뒤에는 보류한 상세 표면을 같은 ID에만 복원한다.
   */
  async function openModifyFromDetail(id: number): Promise<boolean> {
    if (!detailOpen.value || detailSurface.value !== "modal" || detailModel.value?.id !== id) {
      console.warn("[journalThread] openModifyFromDetail skipped: detail context mismatch", {
        id,
        detailOpen: detailOpen.value,
        detailSurface: detailSurface.value,
        detailId: detailModel.value?.id,
      });
      return false;
    }
    return loadModify(id, "modal", "modal");
  }

  /** 등록/수정 표면을 닫고, 같은 문맥에서 보류한 상세가 있으면 복원한다. */
  function closeRegist() {
    const returnSurface = suspendedDetailSurface.value;
    const returnDetailId = suspendedDetailId.value;
    registRequestToken += 1;
    registOpen.value = false;
    registSurface.value = null;
    registLoading.value = false;
    registModel.value = null;
    registInitialSnapshot.value = "";
    suspendedDetailSurface.value = null;
    suspendedDetailId.value = null;

    if (!returnSurface) return;
    if (detailOpen.value && detailModel.value?.id === returnDetailId) {
      detailSurface.value = returnSurface;
      console.info("[journalThread] restored suspended detail after edit", {
        id: returnDetailId,
        surface: returnSurface,
      });
      return;
    }
    console.warn("[journalThread] suspended detail restore skipped: detail context changed", {
      returnDetailId,
      detailOpen: detailOpen.value,
      currentDetailId: detailModel.value?.id,
    });
  }

  /**
   * 스레드 등록/수정 처리.
   * POST /api/journal/threads (신규) | POST /api/journal/threads/{id} (수정)
   */
  async function submitRegist(): Promise<boolean> {
    if (!registModel.value) return false;
    const wasModify = registModel.value.id != null;
    submitting.value = true;
    try {
      const fd = new FormData();
      if (registModel.value.id != null) fd.append("id", String(registModel.value.id));
      fd.append("contentType", registModel.value.contentType ?? "JOURNAL_THREAD");
      fd.append("categoryCode", registModel.value.categoryCode ?? "");
      fd.append("title", registModel.value.title ?? "");
      fd.append("content", registModel.value.content ?? "");

      const url = registModel.value.id != null
        ? `/api/journal/threads/${registModel.value.id}`
        : "/api/journal/threads";
      const res = await axios.post(url, fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data?.rslt) {
        closeRegist();
        await swalAjaxResult({
          rslt: true,
          message: res.data?.message,
          successFallback: wasModify ? t("common.result.modified") : t("common.result.registered"),
        });
        void fetchList(0);
        return true;
      }
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("common.result.failure"),
      });
      return false;
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    } finally {
      submitting.value = false;
    }
  }

  /**
   * 스레드 삭제.
   * DELETE /api/journal/threads/{id}
   * @param id - 스레드 ID
   */
  async function deleteThread(id: number) {
    const confirmed = await swalConfirm(t("journal.thread.delete.confirm"));
    if (!confirmed) return;
    try {
      const res = await axios.delete(`/api/journal/threads/${id}`);
      if (res.data?.rslt) {
        await swalAjaxResult({
          rslt: true,
          message: res.data?.message,
          successFallback: t("common.result.deleted"),
        });
        void fetchList(0);
      } else {
        void swalAjaxResult({
          rslt: false,
          message: res.data?.message,
          failureFallback: t("journal.thread.delete.failure"),
        });
      }
    } catch (e: unknown) {
      void swalRequestError(e);
    }
  }

  /**
   * 스레드 상세의 단일 데이터를 지정한 표면으로 조회한다.
   * 모달과 독립 페이지는 같은 detailModel·detailEntries를 사용하며 렌더 표면만 구분한다.
   *
   * @param id 스레드 ID
   * @param surface 상세 렌더 표면
   * @return 현재 요청이 유효한 상세를 적용했으면 true
   */
  async function loadDetail(id: number, surface: "modal" | "page"): Promise<boolean> {
    if (!await assertAuthenticatedBeforeModal()) return false;
    const requestToken = ++detailRequestToken;
    detailOpen.value = true;
    detailSurface.value = surface;
    detailLoading.value = true;
    detailModel.value = null;
    try {
      const res = await axios.get(`/api/journal/threads/${id}`);
      if (requestToken !== detailRequestToken) {
        console.info("[journalThread] loadDetail discarded stale response", {
          id,
          surface,
          requestToken,
          currentToken: detailRequestToken,
        });
        return false;
      }
      const loadedDetail = res.data?.rsltObj as JournalThreadDto | null | undefined;
      if (!loadedDetail) {
        console.warn("[journalThread] loadDetail rejected empty detail", { id, surface });
        detailModel.value = null;
        detailOpen.value = false;
        detailSurface.value = null;
        void swalAlert(t("journal.thread.detail.load.failure"));
        return false;
      }
      detailModel.value = loadedDetail;
      void fetchDetailEntries(id, requestToken);
      return true;
    } catch (e: unknown) {
      console.error("[journalThread] loadDetail failed", { id, surface }, e);
      if (requestToken === detailRequestToken) {
        detailModel.value = null;
        detailOpen.value = false;
        detailSurface.value = null;
      }
      void swalRequestError(e, t("journal.thread.detail.load.failure"));
      return false;
    } finally {
      if (requestToken === detailRequestToken) detailLoading.value = false;
    }
  }

  /** 현재 저널 문맥 위에 스레드 상세 모달을 연다. */
  async function openDetail(id: number): Promise<boolean> {
    return loadDetail(id, "modal");
  }

  /** 스레드 자체를 주 문맥으로 삼는 독립 상세 페이지를 연다. */
  async function openDetailPage(id: number): Promise<boolean> {
    return loadDetail(id, "page");
  }

  /** 활성 상세 표면을 닫고 진행 중인 이전 요청을 무효화한다. */
  function closeDetail() {
    detailRequestToken += 1;
    detailOpen.value = false;
    detailSurface.value = null;
    detailLoading.value = false;
    detailModel.value = null;
    detailEntries.value = [];
    detailEntriesLoading.value = false;
  }

  /**
   * 스레드 상세의 소속 엔트리를 조회한다. (GET /api/journal/threads/{id}/entries)
   * 실패해도 상세 표면 자체는 유지한다 — 엔트리 섹션만 비운다.
   * 상세 전환 뒤 늦게 도착한 이전 응답은 현재 페이지나 모달을 덮지 않도록 폐기한다.
   */
  async function fetchDetailEntries(id: number, requestToken: number) {
    detailEntriesLoading.value = true;
    detailEntries.value = [];
    try {
      const res = await axios.get(`/api/journal/threads/${id}/entries`);
      if (requestToken !== detailRequestToken || detailModel.value?.id !== id) {
        console.info("[journalThread] fetchDetailEntries discarded stale response", {
          id,
          requestToken,
          currentToken: detailRequestToken,
          currentDetailId: detailModel.value?.id,
        });
        return;
      }
      detailEntries.value = (res.data?.rsltList ?? []) as JournalEntryDto[];
    } catch (e: unknown) {
      console.error("[journalThread] fetchDetailEntries failed", { id }, e);
      if (requestToken === detailRequestToken) detailEntries.value = [];
    } finally {
      if (requestToken === detailRequestToken) detailEntriesLoading.value = false;
    }
  }

  /**
   * 열린 스레드 상세의 본문·집계 태그와 소속 엔트리를 원자적으로 다시 조회한다.
   * <p>
   * 엔트리 수정·관계·라이프사이클·상태·소속 변경은 원본 엔트리를 바꾸므로,
   * 저널 일자 목록이 아니라 현재 스레드 상세를 갱신해야 한다. 기존 내용을 먼저 비우지 않아
   * 중첩 액션 모달이 닫힌 뒤 스레드의 읽기 위치와 렌더 맥락을 유지한다.
   *
   * @return 열린 상세를 최신 응답으로 교체했으면 true, 갱신 대상이 없거나 실패하면 false
   */
  async function refreshOpenDetail(): Promise<boolean> {
    const id = detailModel.value?.id;
    if (!detailOpen.value || !id) {
      console.warn("[journalThread] refreshOpenDetail skipped: no open detail", {
        detailOpen: detailOpen.value,
        detailId: id,
      });
      return false;
    }

    try {
      const [detailRes, entriesRes] = await Promise.all([
        axios.get(`/api/journal/threads/${id}`),
        axios.get(`/api/journal/threads/${id}/entries`),
      ]);
      const refreshedDetail = detailRes.data?.rsltObj as JournalThreadDto | undefined;
      if (!refreshedDetail) {
        console.warn("[journalThread] refreshOpenDetail rejected empty detail", { id });
        return false;
      }
      if (!detailOpen.value || detailModel.value?.id !== id) {
        console.info("[journalThread] refreshOpenDetail discarded stale response", {
          requestedId: id,
          currentId: detailModel.value?.id,
          detailOpen: detailOpen.value,
        });
        return false;
      }

      detailModel.value = refreshedDetail;
      detailEntries.value = (entriesRes.data?.rsltList ?? []) as JournalEntryDto[];
      return true;
    } catch (e: unknown) {
      console.error("[journalThread] refreshOpenDetail failed", { id }, e);
      void swalRequestError(e, t("journal.thread.detail.load.failure"));
      return false;
    }
  }

  return {
    // 목록
    threadList,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    filterKeyword,
    filterCategory,
    filterTagIds,
    filterTagLabelMap,
    categoryOptions,
    categoryError,
    periodSummary,
    periodSummaryLoading,
    periodSummaryError,
    fetchList,
    fetchCategoryOptions,
    fetchPeriodSummary,
    cacheFilterTagLabel,
    addFilterTag,
    removeFilterTag,
    resetFilters,
    // 등록/수정
    registOpen,
    registSurface,
    registLoading,
    registModel,
    registDirty,
    submitting,
    hasSuspendedDetailEdit,
    openRegist,
    openModifyPage,
    openModifyFromDetail,
    closeRegist,
    submitRegist,
    deleteThread,
    // 상세 (모달/독립 페이지 공용)
    detailOpen,
    detailSurface,
    detailEntries,
    detailEntriesLoading,
    detailLoading,
    detailModel,
    openDetail,
    openDetailPage,
    closeDetail,
    refreshOpenDetail,
  };
});
