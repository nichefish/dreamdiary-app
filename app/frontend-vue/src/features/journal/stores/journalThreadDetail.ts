import { computed, ref, type Ref, type ComputedRef } from "vue";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { swalConfirm, swalAlert, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import type { JournalEntryDto, RelatedContentItem } from "@/features/journal/stores/journal";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import type {
  JournalThreadDto,
  JournalThreadRegistModel,
  ThreadPrefix,
} from "@/features/journal/stores/journalThread.types";

export interface JournalThreadDetailDeps {
  t: (key: string) => string;
  fetchList: (page?: number) => Promise<void>;
  fetchPrefixOptions: () => Promise<void>;
  prefixOptions: ComputedRef<ThreadPrefix[]>;
  threadList: Ref<JournalThreadDto[]>;
}

export function createJournalThreadDetail(deps: JournalThreadDetailDeps) {
  const { t, fetchList, fetchPrefixOptions, prefixOptions, threadList } = deps;

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
  /** 스레드 상세 소속 엔트리 목록 */
  const detailEntries = ref<JournalEntryDto[]>([]);
  /** 엔트리 목록 로딩 여부 */
  const detailEntriesLoading = ref(false);
  /** 소속 엔트리 목록 조회 실패 메시지 */
  const detailEntriesError = ref<string | null>(null);
  /** 뷰에 합성 중인 연관 스레드 ID 목록 — 행 단위 토글, 화면 임시 (기본 빈 목록) */
  const detailIncludedRelatedThreadIds = ref<number[]>([]);
  /** 직접 연관된 스레드 목록 */
  const detailRelatedThreads = ref<RelatedContentItem[]>([]);
  /** 연관 스레드 목록 로딩 여부 */
  const detailRelatedThreadsLoading = ref(false);
  /** 연관 스레드 목록 조회 실패 메시지 */
  const detailRelatedError = ref<string | null>(null);
  /** 상세 전환 중 늦게 도착한 이전 응답을 폐기하기 위한 요청 토큰 */
  let detailRequestToken = 0;

  /**
   * 스레드 편집 문맥에서 활성 말머리를 빠르게 추가하고 즉시 현재 선택에 반영한다.
   * 관리 기능 전체는 /my/prefixes 탭에 두며 이름·정렬 검증의 SSOT는 관리 API다.
   */
  async function quickAddPrefix(rawName: string): Promise<ThreadPrefix> {
    const name = rawName.trim();
    if (!name) throw new Error(t("user.my.prefixes.name.required"));
    const sortOrder = prefixOptions.value.reduce(
      (maximum, prefix) => Math.max(maximum, prefix.sortOrder ?? 0),
      -1,
    ) + 1;
    try {
      const response = await axios.post("/api/my/prefixes", {
        name,
        color: null,
        sortOrder,
      }, {
        params: { contentType: "JOURNAL_THREAD" },
      });
      const created = response.data?.rsltObj as ThreadPrefix | undefined;
      if (!created?.id) {
        console.error("[journalThread] quickAddPrefix rejected empty response");
        throw new Error(t("journal.thread.prefix.quick-add.failure"));
      }
      await fetchPrefixOptions();
      if (registModel.value) registModel.value.prefixId = created.id;
      return created;
    } catch (error) {
      console.error("[journalThread] quickAddPrefix failed", { name }, error);
      throw error;
    }
  }

  // ---- 등록/수정 액션 ----

  /** 등록/수정 이탈 판정에 필요한 필드만 같은 순서로 직렬화한다. */
  function snapshotRegistModel(model: JournalThreadRegistModel): string {
    return JSON.stringify({
      id: model.id ?? null,
      contentType: model.contentType ?? "JOURNAL_THREAD",
      prefixId: model.prefixId ?? null,
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
      prefixId: null,
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
        prefixId: dto.prefix?.id ?? dto.prefixId ?? null,
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
      if (registModel.value.prefixId != null) {
        fd.append("prefixId", String(registModel.value.prefixId));
      }
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
   * 스레드 라이프사이클을 설정한다.
   * 목록·상세 로컬 모델을 즉시 맞추고, 상세가 열려 있으면 상세를 재조회한다.
   */
  async function setLifecycle(id: number, lifecycleKey: string): Promise<boolean> {
    const attachableStore = useAttachableModalStore();
    try {
      const result = await attachableStore.setLifecycle({
        id,
        contentType: "JOURNAL_THREAD",
        lifecycleKey,
      });
      if (!result.rslt) {
        void swalAjaxResult({
          rslt: false,
          message: result.message,
          failureFallback: t("common.result.failure"),
        });
        return false;
      }
      const patchLifecycle = (thread: JournalThreadDto | null | undefined) => {
        if (!thread || thread.id !== id) return;
        thread.lifecycle = {
          ...(thread.lifecycle ?? {}),
          lifecycleKey,
        };
      };
      threadList.value.forEach((thread) => patchLifecycle(thread));
      patchLifecycle(detailModel.value);
      if (detailOpen.value && detailModel.value?.id === id) {
        const surface = detailSurface.value ?? "modal";
        await loadDetail(id, surface);
      }
      return true;
    } catch (e: unknown) {
      console.error("[journalThread] setLifecycle failed", { id, lifecycleKey }, e);
      void swalRequestError(e);
      return false;
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
   * 스레드 상세 소속 엔트리를 조회한다.
   * relatedThreadIds가 있으면 해당 연관 스레드 엔트리까지 합성하여 조회한다.
   *
   * @param id 스레드 ID
   * @param requestToken 요청 토큰
   * @param relatedThreadIds 뷰에 합성할 연관 스레드 ID 목록
   */
  async function fetchDetailEntries(id: number, requestToken: number, relatedThreadIds: number[] = []) {
    detailEntriesLoading.value = true;
    detailEntriesError.value = null;
    try {
      const params = new URLSearchParams();
      for (const relatedThreadId of relatedThreadIds) {
        params.append("relatedThreadIds", String(relatedThreadId));
      }
      const res = await axios.get(`/api/journal/threads/${id}/entries`, {
        params: params.toString() ? params : undefined,
      });
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
      if (requestToken === detailRequestToken) {
        detailEntriesError.value = t("journal.thread.entries.load.failure");
      }
    } finally {
      if (requestToken === detailRequestToken) detailEntriesLoading.value = false;
    }
  }

  /**
   * 현재 상세 스레드에 직접 연관된 스레드 목록을 조회한다.
   * GET /api/related/JOURNAL_THREAD/{id}
   * 설계 정본: docs/migration/journal/thread-relation.md §3
   *
   * @param id 스레드 ID
   */
  async function fetchRelatedThreads(id: number): Promise<void> {
    detailRelatedThreadsLoading.value = true;
    detailRelatedError.value = null;
    try {
      const res = await axios.get(`/api/related/JOURNAL_THREAD/${id}`);
      detailRelatedThreads.value = (res.data?.rsltList ?? []) as RelatedContentItem[];
      const aliveIds = new Set(
        detailRelatedThreads.value.map((r) => r.targetId).filter((tid): tid is number => tid != null),
      );
      detailIncludedRelatedThreadIds.value = detailIncludedRelatedThreadIds.value.filter((tid) => aliveIds.has(tid));
    } catch (e: unknown) {
      console.error("[journalThread] fetchRelatedThreads failed", { id }, e);
      detailRelatedError.value = t("journal.thread.related.load.failure");
    } finally {
      detailRelatedThreadsLoading.value = false;
    }
  }

  /**
   * 연관 스레드를 추가한다. POST /api/related/JOURNAL_THREAD/{id}
   * 성공 시 연관 목록과 엔트리 목록을 갱신한다.
   *
   * @param baseThreadId base 스레드 ID
   * @param targetThreadId 연관시킬 스레드 ID
   * @returns 처리 성공 여부
   */
  async function addRelatedThread(baseThreadId: number, targetThreadId: number): Promise<boolean> {
    try {
      const res = await axios.post(`/api/related/JOURNAL_THREAD/${baseThreadId}`, {
        srcId: baseThreadId,
        srcContentType: "JOURNAL_THREAD",
        targetId: targetThreadId,
        targetContentType: "JOURNAL_THREAD",
        relationType: "REFERENCE",
      });
      if (res.data?.rslt !== true) {
        void swalAjaxResult({ rslt: false, message: res.data?.message, failureFallback: t("common.result.failure") });
        return false;
      }
      await fetchRelatedThreads(baseThreadId);
      await fetchDetailEntries(baseThreadId, detailRequestToken, detailIncludedRelatedThreadIds.value);
      return true;
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    }
  }

  /**
   * 연관 스레드를 삭제한다. DELETE /api/related/{relatedContentId}
   * 성공 시 연관 목록과 엔트리 목록을 갱신한다.
   *
   * @param baseThreadId base 스레드 ID (목록 갱신용)
   * @param relatedContentId related_content 행 ID
   * @returns 처리 성공 여부
   */
  async function removeRelatedThread(baseThreadId: number, relatedContentId: number): Promise<boolean> {
    try {
      const res = await axios.delete(`/api/related/${relatedContentId}`);
      const rslt = res.data?.rslt === true;
      if (!rslt) {
        void swalAjaxResult({ rslt: false, message: res.data?.message, failureFallback: t("common.result.failure") });
        return false;
      }
      // fetchRelatedThreads가 살아 있는 targetId만 남기도록 합성 선택을 prune한다
      await fetchRelatedThreads(baseThreadId);
      await fetchDetailEntries(baseThreadId, detailRequestToken, detailIncludedRelatedThreadIds.value);
      return rslt;
    } catch (e: unknown) {
      void swalRequestError(e);
      return false;
    }
  }

  /**
   * 연관 스레드 행의 뷰 합성 토글을 전환하고 엔트리 목록을 다시 조회한다.
   * 설계 정본: docs/migration/journal/thread-relation.md §2 결정 3 (토글 상태 = 화면 임시, 행 단위)
   *
   * @param relatedThreadId 토글 대상 연관 스레드 ID
   */
  async function toggleRelatedThreadInclude(relatedThreadId: number): Promise<void> {
    const id = detailModel.value?.id;
    if (!id || relatedThreadId == null) return;
    const current = detailIncludedRelatedThreadIds.value;
    if (current.includes(relatedThreadId)) {
      detailIncludedRelatedThreadIds.value = current.filter((tid) => tid !== relatedThreadId);
    } else {
      detailIncludedRelatedThreadIds.value = [...current, relatedThreadId];
    }
    await fetchDetailEntries(id, detailRequestToken, detailIncludedRelatedThreadIds.value);
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
      detailIncludedRelatedThreadIds.value = [];
      void fetchDetailEntries(id, requestToken, detailIncludedRelatedThreadIds.value);
      void fetchRelatedThreads(id);
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
    detailEntriesError.value = null;
    detailRelatedThreads.value = [];
    detailRelatedThreadsLoading.value = false;
    detailRelatedError.value = null;
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
    quickAddPrefix,
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
    setLifecycle,
    deleteThread,
    detailOpen,
    detailSurface,
    detailEntries,
    detailEntriesLoading,
    detailEntriesError,
    detailLoading,
    detailModel,
    detailIncludedRelatedThreadIds,
    detailRelatedThreads,
    detailRelatedThreadsLoading,
    detailRelatedError,
    openDetail,
    openDetailPage,
    closeDetail,
    refreshOpenDetail,
    fetchRelatedThreads,
    addRelatedThread,
    removeRelatedThread,
    toggleRelatedThreadInclude,
  };
}
