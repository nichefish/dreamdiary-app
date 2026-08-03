import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert, swalRequestError, swalFire, swalAjaxResult } from "@/shared/utils/swal";

// ---- 타입 정의 ----

/** 게시판 Prefix Scope의 말머리 선택지 */
export interface BoardPrefix {
  id: number;
  name: string;
  color?: string;
  sortOrder?: number;
  activeYn?: string;
}

/** 태그 클라우드 항목 (`/api/tags` 응답) */
export interface BoardTagCloudItem {
  id: number;
  name: string;
  ctgr?: string;
  /** 해당 태그가 달린 글 수 */
  contentSize?: number;
  tagClass?: string;
  textClass?: string;
}

/** 태그 항목 */
export interface BoardTagItem {
  tagId: number | string;
  name: string;
  ctgr?: string;
}

/** 태그 컴포지션 */
export interface BoardTagCmpstn {
  list?: BoardTagItem[];
  /** 태그 문자열 (tagify 초기값) */
  tagListStrWithCtgr?: string;
}

/** 게시판 게시물 Dto */
export interface BoardPostDto {
  id?: number;
  rnum?: number;
  contentType?: string;
  prefixId?: number;
  prefix?: BoardPrefix;
  title?: string;
  content?: string;
  markdownContent?: string;
  tag?: BoardTagCmpstn;
  comment?: { cnt?: number; list?: Array<{ id?: number; markdownContent?: string }> };
  file?: { fileGroupId?: number };
  hasFiles?: boolean;
  createdByNm?: string;
  createdDt?: string;
  isNew?: boolean;
}

/** 게시판 게시물 등록/수정 폼 모델 */
export interface BoardPostRegistModel {
  id?: number;
  contentType?: string;
  prefixId?: number;
  prefix?: BoardPrefix;
  title?: string;
  content?: string;
  tag?: { tagListStrWithCtgr?: string };
}

// ---- 스토어 ----

export const useBoardPostStore = defineStore("boardPost", () => {
  const { t } = useLocaleStore();
  // ---- 목록 ----

  /** 현재 boardKey (contentType) */
  const boardKey = ref("");
  /** 게시물 목록 */
  const postList = ref<BoardPostDto[]>([]);
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
  /** 게시판 Scope의 활성 말머리. 비어 있으면 화면이 말머리 select를 숨긴다 */
  const prefixOptions = ref<BoardPrefix[]>([]);
  /** 말머리 선택지 조회 오류 */
  const prefixError = ref("");

  /** 선택된 태그 필터 (단일). null 이면 태그 조건 없음 */
  const filterTagId = ref<number | null>(null);
  /** 태그 클라우드 */
  const tagCloud = ref<BoardTagCloudItem[]>([]);
  /** 태그 클라우드 로딩 상태 */
  const tagCloudLoading = ref(false);
  /** 태그 클라우드 조회 오류 */
  const tagCloudError = ref("");

  /** 검색 키워드 필터 */
  const filterKeyword = ref("");
  /** 말머리 필터 */
  const filterPrefixId = ref<number | null>(null);

  // ---- 등록/수정 모달 ----

  /** 등록/수정 모달 오픈 여부 */
  const registOpen = ref(false);
  /** 등록/수정 모달 로딩 여부 */
  const registLoading = ref(false);
  /** 등록/수정 폼 모델 */
  const registModel = ref<BoardPostRegistModel | null>(null);
  /** 등록/수정 처리 중 여부 */
  const submitting = ref(false);

  // ---- 상세 모달 ----

  /** 상세 모달 오픈 여부 */
  const detailOpen = ref(false);
  /** 상세 모달 로딩 여부 */
  const detailLoading = ref(false);
  /** 상세 DTO */
  const detailModel = ref<BoardPostDto | null>(null);

  // ---- 목록 액션 ----

  /**
   * 게시물 목록 조회.
   * GET /api/board/posts?contentType=...&page=...&size=...
   * @param page - 페이지 번호 (0-based, 생략 시 currentPage 유지)
   */
  async function fetchList(page?: number) {
    if (!boardKey.value) return;
    loading.value = true;
    error.value = null;
    const targetPage = page ?? currentPage.value;
    try {
      const params: Record<string, unknown> = {
        contentType: boardKey.value,
        page: targetPage,
        size: pageSize.value,
      };
      if (filterKeyword.value) params.searchKeyword = filterKeyword.value;
      if (filterPrefixId.value != null) params.prefixId = filterPrefixId.value;
      /* 태그 필터는 공통 BaseAttachableSearchParam.tags(List<Integer>) 로 전달한다 (스레드와 동일) */
      if (filterTagId.value != null) params.tags = [filterTagId.value];
      const res = await axios.get("/api/board/posts", { params });
      /* Spring Page<T> → { content, totalElements, totalPages, number, size } */
      const pageResult = res.data?.rsltObj;
      postList.value = pageResult?.content ?? [];
      totalElements.value = pageResult?.totalElements ?? 0;
      totalPages.value = pageResult?.totalPages ?? 0;
      currentPage.value = pageResult?.number ?? 0;
    } catch {
      error.value = t("board.post.list.load.failure");
      postList.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * 게시판 화면 진입 시 boardKey를 확정하고 게시글·말머리·태그를 조회한다.
   * 같은 게시판에 재진입해도 관리 화면 등에서 변경된 최신 보조 데이터를 다시 조회한다.
   * @param key - contentType (boardKey)
   */
  async function setBoard(key: string) {
    const boardChanged = boardKey.value !== key;
    if (boardChanged) {
      boardKey.value = key;
      filterKeyword.value = "";
      filterPrefixId.value = null;
      filterTagId.value = null;
      postList.value = [];
      prefixOptions.value = [];
      tagCloud.value = [];
    }
    console.info("[boardPost] 게시판 데이터 조회", { boardKey: key, boardChanged });
    /* 말머리 Scope·태그는 게시판별 데이터이며, 화면 재진입 시 서버의 최신 상태를 반영한다. */
    await Promise.all([fetchList(0), fetchPrefixOptions(), fetchTagCloud()]);
  }

  /**
   * 현재 게시판의 태그 클라우드를 조회한다.
   * 게시판 태그는 `tag_content.ref_content_type` 에 boardKey 로 저장되므로
   * (ContentType enum 의 `BOARD` 가 아니다) 게시물 목록과 같은 규약으로 boardKey 를 넘긴다.
   */
  async function fetchTagCloud(): Promise<void> {
    if (!boardKey.value) {
      tagCloud.value = [];
      return;
    }
    tagCloudLoading.value = true;
    tagCloudError.value = "";
    try {
      const res = await axios.get("/api/tags", { params: { contentType: boardKey.value } });
      tagCloud.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e: unknown) {
      console.error("[boardPost] fetchTagCloud failed", e);
      tagCloud.value = [];
      tagCloudError.value = t("board.post.tag-cloud.load.failure");
    } finally {
      tagCloudLoading.value = false;
    }
  }

  /** 태그 클라우드 클릭 — 같은 태그를 다시 누르면 해제한다. 조건이 바뀌므로 첫 페이지부터 조회. */
  async function toggleTagFilter(tagId: number): Promise<void> {
    filterTagId.value = filterTagId.value === tagId ? null : tagId;
    await fetchList(0);
  }

  /**
   * 현재 게시판 Scope의 활성 말머리를 조회한다.
   * 활성 말머리가 없으면 화면은 말머리 select를 렌더링하지 않는다.
   */
  async function fetchPrefixOptions(): Promise<void> {
    prefixError.value = "";
    if (!boardKey.value) {
      prefixOptions.value = [];
      return;
    }
    try {
      const res = await axios.get(`/api/board/${encodeURIComponent(boardKey.value)}/prefixes`);
      prefixOptions.value = Array.isArray(res.data?.rsltList) ? res.data.rsltList : [];
    } catch (e: unknown) {
      console.error("[boardPost] fetchPrefixOptions failed", e);
      prefixOptions.value = [];
      prefixError.value = t("board.post.prefix.load.failure");
    }
  }

  /** 검색 조건을 비우고 첫 페이지를 조회한다. */
  async function resetFilters(): Promise<void> {
    filterKeyword.value = "";
    filterPrefixId.value = null;
    filterTagId.value = null;
    await fetchList(0);
  }

  // ---- 등록/수정 액션 ----

  /** 게시물 등록 모달을 연다 (신규). */
  async function openRegist() {
    if (!await assertAuthenticatedBeforeModal()) return;
    registModel.value = {
      contentType: boardKey.value,
      prefixId: undefined,
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
    };
    registOpen.value = true;
  }

  /**
   * 게시물 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param id - 게시물 ID
   */
  async function openModify(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    registLoading.value = true;
    registModel.value = null;
    try {
      registOpen.value = true;
      const res = await axios.get(`/api/board/posts/${id}`);
      const dto: BoardPostDto = res.data?.rsltObj ?? {};
      registModel.value = {
        id: dto.id,
        contentType: dto.contentType ?? boardKey.value,
        prefixId: dto.prefixId ?? dto.prefix?.id,
        prefix: dto.prefix,
        title: dto.title ?? "",
        content: dto.content ?? "",
        tag: { tagListStrWithCtgr: dto.tag?.tagListStrWithCtgr ?? "" },
      };
    } catch {
      registModel.value = null;
      registOpen.value = false;
    } finally {
      registLoading.value = false;
    }
  }

  /** 등록/수정 모달을 닫는다. */
  function closeRegist() {
    registOpen.value = false;
    registModel.value = null;
  }

  /**
   * 게시물 등록/수정 처리.
   * POST /api/board/posts (신규) | POST /api/board/posts/{id} (수정)
   * 변경 전에는 성공 직후 목록을 갱신했다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   */
  async function submitRegist(): Promise<boolean> {
    if (!registModel.value) return false;
    submitting.value = true;
    try {
      const fd = new FormData();
      if (registModel.value.id != null) fd.append("id", String(registModel.value.id));
      fd.append("contentType", registModel.value.contentType ?? boardKey.value);
      if (registModel.value.prefixId != null) fd.append("prefixId", String(registModel.value.prefixId));
      fd.append("title", registModel.value.title ?? "");
      fd.append("content", registModel.value.content ?? "");
      fd.append("tag.tagListStr", registModel.value.tag?.tagListStrWithCtgr ?? "");

      const url = registModel.value.id != null
        ? `/api/board/posts/${registModel.value.id}`
        : "/api/board/posts";
      const res = await axios.post(url, fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data?.rslt) {
        closeRegist();
        await swalAjaxResult({
          rslt: true,
          message: res.data?.message,
          successFallback: t("common.result.saved"),
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
   * 게시물 삭제.
   * DELETE /api/board/posts/{id}
   * 변경 전에는 성공 직후 목록을 갱신했다.
   * 변경 후에는 성공 알림 OK 이후 목록을 갱신한다.
   * @param id - 게시물 ID
   */
  async function deletePost(id: number) {
    const confirmed = await swalConfirm(t("board.post.delete.confirm"));
    if (!confirmed) return;
    try {
      const res = await axios.delete(`/api/board/posts/${id}`);
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
          failureFallback: t("board.post.delete.failure"),
        });
      }
    } catch (e: unknown) {
      void swalRequestError(e);
    }
  }

  // ---- 상세 액션 ----

  /**
   * 게시물 상세 모달을 연다. API 에서 상세 데이터를 조회한다.
   * @param id - 게시물 ID
   */
  async function openDetail(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    detailOpen.value = true;
    detailLoading.value = true;
    detailModel.value = null;
    try {
      const res = await axios.get(`/api/board/posts/${id}`);
      detailModel.value = res.data?.rsltObj ?? null;
    } catch {
      detailModel.value = null;
      detailOpen.value = false;
    } finally {
      detailLoading.value = false;
    }
  }

  /** 상세 모달을 닫는다. */
  function closeDetail() {
    detailOpen.value = false;
    detailModel.value = null;
  }

  return {
    // 목록
    boardKey,
    postList,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    filterKeyword,
    filterPrefixId,
    prefixOptions,
    prefixError,
    filterTagId,
    tagCloud,
    tagCloudLoading,
    tagCloudError,
    fetchList,
    fetchPrefixOptions,
    fetchTagCloud,
    toggleTagFilter,
    resetFilters,
    setBoard,
    // 등록/수정
    registOpen,
    registLoading,
    registModel,
    submitting,
    openRegist,
    openModify,
    closeRegist,
    submitRegist,
    deletePost,
    // 상세
    detailOpen,
    detailLoading,
    detailModel,
    openDetail,
    closeDetail,
  };
});
