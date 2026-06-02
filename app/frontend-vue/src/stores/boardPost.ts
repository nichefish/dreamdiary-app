import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { swalConfirm, swalAlert, swalRequestError } from "@/utils/swal";

// ---- 타입 정의 ----

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
  categoryCode?: string;
  ctgrNm?: string;
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
  categoryCode?: string;
  title?: string;
  content?: string;
  tag?: { tagListStrWithCtgr?: string };
}

// ---- 스토어 ----

export const useBoardPostStore = defineStore("boardPost", () => {
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
  /** 검색 키워드 필터 */
  const filterKeyword = ref("");
  /** 카테고리 필터 */
  const filterCategory = ref("");

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
      if (filterCategory.value) params.categoryCode = filterCategory.value;
      const res = await axios.get("/api/board/posts", { params });
      /* Spring Page<T> → { content, totalElements, totalPages, number, size } */
      const pageResult = res.data?.rsltObj;
      postList.value = pageResult?.content ?? [];
      totalElements.value = pageResult?.totalElements ?? 0;
      totalPages.value = pageResult?.totalPages ?? 0;
      currentPage.value = pageResult?.number ?? 0;
    } catch {
      error.value = "게시물 목록을 불러오지 못했습니다.";
      postList.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * boardKey 를 세팅하고 목록을 재조회한다.
   * 라우트 파라미터가 변경될 때 호출한다.
   * @param key - contentType (boardKey)
   */
  async function setBoard(key: string) {
    if (boardKey.value === key) return;
    boardKey.value = key;
    filterKeyword.value = "";
    filterCategory.value = "";
    postList.value = [];
    await fetchList(0);
  }

  // ---- 등록/수정 액션 ----

  /** 게시물 등록 모달을 연다 (신규). */
  function openRegist() {
    registModel.value = {
      contentType: boardKey.value,
      categoryCode: "",
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
    registOpen.value = true;
    registLoading.value = true;
    registModel.value = null;
    try {
      const res = await axios.get(`/api/board/posts/${id}`);
      const dto: BoardPostDto = res.data?.rsltObj ?? {};
      registModel.value = {
        id: dto.id,
        contentType: dto.contentType ?? boardKey.value,
        categoryCode: dto.categoryCode ?? "",
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
      fd.append("categoryCode", registModel.value.categoryCode ?? "");
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
        await swalAlert(res.data?.message ?? "저장되었습니다.");
        void fetchList(0);
        return true;
      }
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
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
    const confirmed = await swalConfirm("게시물을 삭제하시겠습니까?");
    if (!confirmed) return;
    try {
      const res = await axios.delete(`/api/board/posts/${id}`);
      if (res.data?.rslt) {
        await swalAlert(res.data?.message ?? "삭제되었습니다.");
        void fetchList(0);
      } else {
        void swalAlert(res.data?.message ?? "삭제에 실패했습니다.");
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
    filterCategory,
    fetchList,
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
