import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

// ---- 타입 정의 ----

/** 이력 항목 */
export interface HistoryItem {
  id: number | string;
  historyType: string;
  fromHistoryId?: number | string;
  createdAt: string;
  createdByNm: string;
  previewContent: string;
  markdownContent?: string;
}

/** 관련 글 검색 결과 항목 */
export interface RelatedTargetItem {
  id: number;
  contentType: string;
  title: string;
  stdrdDt: string;
  content: string;
}

/** 댓글 목록 항목 */
export interface CommentListItem {
  id: number | string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
  createdByNm?: string;
  createdByInfo?: {
    profileImageUrl?: string;
  };
}

/** TEXT_CLASS_CD 코드 항목 */
export interface TextClassOption {
  code: string;
  codeName: string;
  description: string;
}

/** 태그 목록 항목 */
export interface TagListItem {
  id: number | string;
  name: string;
  ctgr: string;
  contentSize: number;
  textClass?: string;
  tagClass?: string;
}

/** 태그 목록 조회 파라미터 */
export interface TagListParams {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
}

/** 첨부파일 레코드 */
export interface FileRecord {
  id: number;
  fileGroupId: string | number;
  orgnFileNm: string;
  fileSize: number;
}

/** 상태 토글 요청 */
export interface StateTogglePayload {
  id: number | string;
  contentType: string;
  stateKey: string;
  cacheContext?: Record<string, unknown>;
}

/** 라이프사이클 설정 요청 */
export interface LifecycleSetPayload {
  id: number | string;
  contentType: string;
  lifecycleKey: string;
  cacheContext?: Record<string, unknown>;
}

/** 공통 액션 결과 */
export interface AttachableActionResult {
  rslt: boolean;
  message?: string;
  rsltSts?: string;
  rsltObj?: unknown;
}

/** 태그 프로필 폼 모델 */
export interface TagProfileModel {
  id: string;
  categoryProfileId: string;
  tagId: string;
  tagCategoryId: string;
  contentType: string;
  contentTypeLabel: string;
  ctgr: string;
  name: string;
  categoryTextClassCd: string;
  textClassCd: string;
  content: string;
}

// ---- 스토어 ----

export const useAttachableModalStore = defineStore("attachableModal", () => {

  // ---- 댓글 등록/수정 모달 ----

  /** 댓글 등록/수정 모달 오픈 여부 */
  const commentRegOpen = ref(false);
  /** 댓글 등록/수정 로딩 여부 */
  const commentRegLoading = ref(false);
  /** 수정 대상 댓글 번호 (신규 시 undefined) */
  const commentId = ref<number | undefined>(undefined);
  /** 참조 게시물 번호 */
  const commentRefId = ref<number>(0);
  /** 참조 콘텐츠 타입 */
  const commentRefContentType = ref<string>("");
  /** 댓글 내용 */
  const commentContent = ref<string>("");

  /**
   * 댓글 신규 등록 모달을 연다.
   * @param refId - 참조 게시물 번호
   * @param refContentType - 참조 콘텐츠 타입
   */
  function openCommentReg(refId: number, refContentType: string): void {
    commentId.value = undefined;
    commentRefId.value = refId;
    commentRefContentType.value = refContentType;
    commentContent.value = "";
    commentRegOpen.value = true;
  }

  /**
   * 댓글 수정 모달을 연다. API에서 댓글 데이터를 조회한다.
   * @param id - 수정할 댓글 번호
   */
  async function openCommentMdf(id: number): Promise<void> {
    commentRegOpen.value = true;
    commentRegLoading.value = true;
    commentContent.value = "";
    try {
      const res = await axios.get(`/api/comment/${id}`);
      const data = res.data?.rsltObj;
      if (!data) { commentRegOpen.value = false; return; }
      commentId.value = Number(data.id);
      commentRefId.value = Number(data.refId);
      commentRefContentType.value = String(data.refContentType ?? "");
      commentContent.value = String(data.content ?? "");
    } catch {
      commentRegOpen.value = false;
    } finally {
      commentRegLoading.value = false;
    }
  }

  /** 댓글 등록/수정 모달을 닫는다. */
  function closeCommentReg(): void {
    commentRegOpen.value = false;
    commentContent.value = "";
  }

  // ---- 댓글 목록 모달 ----

  /** 댓글 목록 모달 오픈 여부 */
  const commentListOpen = ref(false);
  /** 댓글 목록 로딩 여부 */
  const commentListLoading = ref(false);
  /** 댓글 목록 */
  const commentList = ref<CommentListItem[]>([]);
  /** 댓글 목록 참조 게시물 번호 */
  const commentListRefId = ref<number | string>(0);
  /** 댓글 목록 참조 콘텐츠 타입 */
  const commentListRefContentType = ref<string>("");

  /** 댓글 목록 모달을 연다. */
  async function openCommentList(refId: number | string, refContentType: string): Promise<void> {
    commentListOpen.value = true;
    commentListLoading.value = true;
    commentList.value = [];
    commentListRefId.value = refId;
    commentListRefContentType.value = refContentType;
    try {
      const res = await axios.get("/api/comments", {
        params: { refId, refContentType },
      });
      commentList.value = Array.isArray(res.data?.rsltList)
        ? (res.data.rsltList as CommentListItem[])
        : [];
    } catch {
      commentList.value = [];
    } finally {
      commentListLoading.value = false;
    }
  }

  /** 댓글 목록 모달을 닫는다. */
  function closeCommentList(): void {
    commentListOpen.value = false;
  }

  // ---- 이력 모달 ----

  /** 이력 모달 오픈 여부 */
  const historyOpen = ref(false);
  /** 이력 로딩 여부 */
  const historyLoading = ref(false);
  /** 이력 콘텐츠 타입 */
  const historyContentType = ref<string>("");
  /** 이력 대상 게시물 번호 */
  const historyPostId = ref<number | string>(0);
  /** 이력 최종 수정일자 */
  const historyTriggeredAt = ref<string>("");
  /** 이력 목록 */
  const historyList = ref<HistoryItem[]>([]);

  /**
   * 이력 모달을 연다. API에서 이력 목록을 조회한다.
   * @param contentType - 콘텐츠 타입
   * @param id - 게시물 번호
   */
  async function openHistory(contentType: string, id: number | string): Promise<void> {
    historyOpen.value = true;
    historyLoading.value = true;
    historyList.value = [];
    historyTriggeredAt.value = "";
    try {
      const res = await axios.get(`/api/history/${contentType}/${id}`);
      if (!res.data?.rslt) return;
      const rsltObj: Record<string, unknown> = res.data.rsltObj ?? {};
      historyContentType.value = contentType;
      historyPostId.value = (rsltObj.id as number | string) ?? id;
      historyTriggeredAt.value = String(
        (rsltObj.historyTriggeredAt as string)
        ?? ((rsltObj.history as Record<string, unknown>)?.historyTriggeredAt as string)
        ?? ""
      );
      historyList.value = Array.isArray(rsltObj.historyList)
        ? (rsltObj.historyList as HistoryItem[])
        : [];
    } catch {
      historyList.value = [];
    } finally {
      historyLoading.value = false;
    }
  }

  /** 이력 모달을 닫는다. */
  function closeHistory(): void {
    historyOpen.value = false;
  }

  /**
   * 이력 복원. 성공 여부를 반환한다.
   * @param historyId - 복원할 이력 번호
   */
  async function restoreHistory(historyId: number | string): Promise<boolean> {
    try {
      const res = await axios.post(
        `/api/history/${historyContentType.value}/${historyPostId.value}/${historyId}/restore`
      );
      return res.data?.rslt === true;
    } catch {
      return false;
    }
  }

  /**
   * 이력 단건 삭제. 성공 여부를 반환한다.
   * @param historyId - 삭제할 이력 번호
   */
  async function deleteHistory(historyId: number | string): Promise<boolean> {
    try {
      const res = await axios.delete(
        `/api/history/${historyContentType.value}/${historyPostId.value}/${historyId}`
      );
      return res.data?.rslt === true;
    } catch {
      return false;
    }
  }

  /**
   * 이력 전체 삭제. 성공 여부를 반환한다.
   */
  async function clearHistory(): Promise<boolean> {
    try {
      const res = await axios.delete(
        `/api/history/${historyContentType.value}/${historyPostId.value}/clear`
      );
      return res.data?.rslt === true;
    } catch {
      return false;
    }
  }

  // ---- 관련 글 추가 모달 ----

  /** 관련 글 추가 모달 오픈 여부 */
  const relatedOpen = ref(false);
  /** 출처 콘텐츠 타입 */
  const relatedSrcContentType = ref<string>("");
  /** 출처 게시물 번호 */
  const relatedSrcId = ref<number>(0);
  /** 관련 유형 */
  const relatedRelationType = ref<string>("REFERENCE");
  /** 대상 콘텐츠 타입 */
  const relatedTargetContentType = ref<string>("");
  /** 검색 키워드 */
  const relatedKeyword = ref<string>("");
  /** 검색 결과 목록 */
  const relatedSearchResults = ref<RelatedTargetItem[]>([]);
  /** 선택한 대상 항목 */
  const relatedSelectedTarget = ref<RelatedTargetItem | null>(null);
  /** 연결 이유/메모 */
  const relatedReason = ref<string>("");
  /** 검색 중 여부 */
  const relatedSearching = ref(false);
  /** 검색 시도 여부 */
  const relatedSearchAttempted = ref(false);
  /** 유효성 메시지 */
  const relatedValidationMsg = ref<string>("");

  /**
   * 관련 글 추가 모달을 연다.
   * @param contentType - 출처 콘텐츠 타입
   * @param id - 출처 게시물 번호
   */
  function openRelated(contentType: string, id: number): void {
    relatedSrcContentType.value = contentType;
    relatedSrcId.value = id;
    relatedRelationType.value = "REFERENCE";
    relatedTargetContentType.value = contentType === "JOURNAL_DIARY" ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
    relatedKeyword.value = "";
    relatedSearchResults.value = [];
    relatedSelectedTarget.value = null;
    relatedReason.value = "";
    relatedSearching.value = false;
    relatedSearchAttempted.value = false;
    relatedValidationMsg.value = "";
    relatedOpen.value = true;
  }

  /** 관련 글 추가 모달을 닫는다. */
  function closeRelated(): void {
    relatedOpen.value = false;
  }

  /** 대상 유형 변경 시 검색 상태를 초기화한다. */
  function onRelatedTargetTypeChange(): void {
    relatedSearchResults.value = [];
    relatedSelectedTarget.value = null;
    relatedSearchAttempted.value = false;
  }

  /** 관련 글 검색을 실행한다. */
  async function searchRelatedTargets(): Promise<void> {
    relatedValidationMsg.value = "";
    if (!relatedKeyword.value.trim()) {
      relatedSearchResults.value = [];
      relatedSearchAttempted.value = false;
      return;
    }
    relatedSearching.value = true;
    relatedSearchAttempted.value = true;
    try {
      const urlMap: Record<string, string> = {
        JOURNAL_DIARY: "/api/journal/diaries",
        JOURNAL_DREAM: "/api/journal/dreams",
      };
      const url = urlMap[relatedTargetContentType.value] ?? "";
      if (!url) { relatedSearchResults.value = []; return; }
      const res = await axios.get(url, {
        params: { searchKeywords: relatedKeyword.value, pageSize: 8, sort: "DESC" },
      });
      if (!res.data?.rslt) { relatedSearchResults.value = []; return; }
      relatedSearchResults.value = (
        Array.isArray(res.data.rsltList) ? res.data.rsltList : []
      )
        .map((item: Record<string, unknown>) => ({
          id: Number(item.id ?? 0),
          contentType: String(item.contentType ?? relatedTargetContentType.value),
          title: String(item.title ?? "").trim(),
          stdrdDt: String(item.stdrdDt ?? "").trim(),
          content: String(item.content ?? item.markdownContent ?? "").trim(),
        }))
        .filter((item: RelatedTargetItem) => Number.isInteger(item.id) && item.id > 0);
    } catch {
      relatedSearchResults.value = [];
    } finally {
      relatedSearching.value = false;
    }
  }

  /**
   * 검색 결과 항목을 선택한다.
   * @param item - 선택한 항목
   */
  function selectRelatedTarget(item: RelatedTargetItem): void {
    relatedSelectedTarget.value = item;
  }

  /**
   * 관련 글 연결을 저장한다. 성공 여부와 메시지를 반환한다.
   */
  async function saveRelated(): Promise<{ rslt: boolean; message?: string }> {
    relatedValidationMsg.value = "";
    if (!relatedTargetContentType.value) {
      relatedValidationMsg.value = "대상 글 유형을 선택해 주세요.";
      return { rslt: false };
    }
    if (!relatedSelectedTarget.value) {
      relatedValidationMsg.value = "검색 결과에서 연결할 글을 선택해 주세요.";
      return { rslt: false };
    }
    if (
      relatedTargetContentType.value === relatedSrcContentType.value
      && relatedSelectedTarget.value.id === relatedSrcId.value
    ) {
      relatedValidationMsg.value = "현재 글 자신과는 연결할 수 없습니다.";
      return { rslt: false };
    }
    if (!relatedRelationType.value) {
      relatedValidationMsg.value = "관련 유형을 선택해 주세요.";
      return { rslt: false };
    }
    try {
      const res = await axios.post(
        `/api/related/${relatedSrcContentType.value}/${relatedSrcId.value}`,
        {
          srcId: relatedSrcId.value,
          srcContentType: relatedSrcContentType.value,
          targetId: relatedSelectedTarget.value.id,
          targetContentType: relatedTargetContentType.value,
          relationType: relatedRelationType.value,
          reason: relatedReason.value,
        }
      );
      return { rslt: res.data?.rslt === true, message: res.data?.message as string | undefined };
    } catch {
      return { rslt: false };
    }
  }

  // ---- 상태/라이프사이클 ----

  /** 부착 가능 컨텐츠 상태를 토글한다. */
  async function toggleState(payload: StateTogglePayload): Promise<AttachableActionResult> {
    try {
      const res = await axios.post("/api/states", payload);
      return {
        rslt: res.data?.rslt === true,
        message: res.data?.message as string | undefined,
        rsltSts: res.data?.rsltSts as string | undefined,
        rsltObj: res.data?.rsltObj,
      };
    } catch {
      return { rslt: false };
    }
  }

  /** 부착 가능 컨텐츠 라이프사이클을 설정한다. */
  async function setLifecycle(payload: LifecycleSetPayload): Promise<AttachableActionResult> {
    try {
      const res = await axios.put("/api/lifecycles", payload);
      return {
        rslt: res.data?.rslt === true,
        message: res.data?.message as string | undefined,
        rsltObj: res.data?.rsltObj,
      };
    } catch {
      return { rslt: false };
    }
  }

  // ---- 태그 목록 모달 ----

  /** 태그 목록 모달 오픈 여부 */
  const tagListOpen = ref(false);
  /** 태그 목록 로딩 여부 */
  const tagListLoading = ref(false);
  /** 태그 목록 카테고리 그룹 */
  const tagGroupMap = ref<Record<string, TagListItem[]>>({});
  /** 1개짜리 태그 숨김 여부 */
  const tagListHideSingles = ref(false);

  /** 태그 목록 모달을 연다. */
  async function openTagList(params: TagListParams = {}): Promise<void> {
    tagListOpen.value = true;
    tagListHideSingles.value = false;
    await loadTagList(params);
  }

  /** 태그 목록 모달을 닫는다. */
  function closeTagList(): void {
    tagListOpen.value = false;
  }

  /** 태그 목록을 조회한다. */
  async function loadTagList(params: TagListParams = {}): Promise<void> {
    tagListLoading.value = true;
    try {
      const query = Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== "")
      );
      const res = await axios.get("/api/journal/day/tag/group-list", { params: query });
      if (!res.data?.rslt) {
        tagGroupMap.value = {};
        return;
      }
      tagGroupMap.value = normalizeTagGroupMap(res.data.rsltMap ?? {});
    } catch {
      tagGroupMap.value = {};
    } finally {
      tagListLoading.value = false;
    }
  }

  /** 1개짜리 태그 숨김 상태를 토글한다. */
  function toggleTagListSingles(): void {
    tagListHideSingles.value = !tagListHideSingles.value;
  }

  /** 백엔드 태그 그룹 응답을 화면 모델로 정규화한다. */
  function normalizeTagGroupMap(rawMap: Record<string, unknown>): Record<string, TagListItem[]> {
    return Object.entries(rawMap).reduce<Record<string, TagListItem[]>>((acc, [category, rawList]) => {
      const list = Array.isArray(rawList) ? rawList : [];
      acc[category] = list
        .map((raw) => {
          const item = raw as Record<string, unknown>;
          return {
            id: (item.id as number | string | undefined) ?? "",
            name: String(item.name ?? ""),
            ctgr: String(item.ctgr ?? category ?? ""),
            contentSize: Number(item.contentSize ?? 0),
            textClass: String(item.textClass ?? ""),
            tagClass: String(item.tagClass ?? ""),
          };
        })
        .filter((item) => item.id !== "" && item.name !== "");
      return acc;
    }, {});
  }

  // ---- 태그 프로필 모달 ----

  /** 태그 프로필 모달 오픈 여부 */
  const tagProfileOpen = ref(false);
  /** 태그 프로필 로딩 여부 */
  const tagProfileLoading = ref(false);
  /** TEXT_CLASS_CD 코드 목록 */
  const tagProfileTextClassOptions = ref<TextClassOption[]>([]);
  /** 태그 프로필 폼 모델 */
  const tagProfileModel = ref<TagProfileModel>({
    id: "", categoryProfileId: "", tagId: "", tagCategoryId: "",
    contentType: "", contentTypeLabel: "", ctgr: "", name: "",
    categoryTextClassCd: "", textClassCd: "", content: "",
  });

  /**
   * 태그 프로필 모달을 연다.
   * @param payload - 열 때 채울 모델 데이터
   */
  function openTagProfile(payload: Partial<TagProfileModel>): void {
    tagProfileModel.value = {
      id: String(payload.id ?? ""),
      categoryProfileId: String(payload.categoryProfileId ?? ""),
      tagId: String(payload.tagId ?? ""),
      tagCategoryId: String(payload.tagCategoryId ?? ""),
      contentType: String(payload.contentType ?? ""),
      contentTypeLabel: String(payload.contentTypeLabel ?? ""),
      ctgr: String(payload.ctgr ?? ""),
      name: String(payload.name ?? ""),
      categoryTextClassCd: String(payload.categoryTextClassCd ?? ""),
      textClassCd: String(payload.textClassCd ?? ""),
      content: String(payload.content ?? ""),
    };
    tagProfileOpen.value = true;
  }

  /** 태그 프로필 모달을 닫는다. */
  function closeTagProfile(): void {
    tagProfileOpen.value = false;
  }

  /**
   * TEXT_CLASS_CD 코드 목록을 로드한다 (최초 1회).
   */
  async function loadTagProfileTextClassOptions(): Promise<void> {
    if (tagProfileTextClassOptions.value.length > 0) return;
    try {
      const res = await axios.get("/api/code/items", { params: { groupCode: "TEXT_CLASS_CD" } });
      if (res.data?.rslt && Array.isArray(res.data.rsltList)) {
        tagProfileTextClassOptions.value = (res.data.rsltList as Record<string, unknown>[]).map((item) => ({
          code: String(item.code ?? ""),
          codeName: String(item.codeName ?? ""),
          description: String(item.description ?? ""),
        }));
      }
    } catch {
      console.error("[attachableModal] TEXT_CLASS_CD 로드 실패");
    }
  }

  /**
   * 태그 프로필을 저장한다. 성공 여부와 메시지를 반환한다.
   */
  async function saveTagProfile(): Promise<{ rslt: boolean; message?: string }> {
    const m = tagProfileModel.value;
    if (!m.tagId) return { rslt: false, message: "tagId가 없습니다." };
    try {
      const fd = new FormData();
      if (m.id) fd.append("id", m.id);
      if (m.categoryProfileId) fd.append("categoryProfileId", m.categoryProfileId);
      fd.append("tagId", m.tagId);
      if (m.tagCategoryId) fd.append("tagCategoryId", m.tagCategoryId);
      fd.append("contentType", m.contentType);
      fd.append("categoryTextClassCd", m.categoryTextClassCd);
      fd.append("textClassCd", m.textClassCd);
      fd.append("content", m.content);
      const res = await axios.post(`/api/tags/${m.tagId}/profile`, fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      return { rslt: res.data?.rslt === true, message: res.data?.message as string | undefined };
    } catch {
      return { rslt: false };
    }
  }

  /**
   * 태그 프로필을 삭제한다. 성공 여부와 메시지를 반환한다.
   */
  async function deleteTagProfile(): Promise<{ rslt: boolean; message?: string }> {
    const m = tagProfileModel.value;
    if (!m.tagId) return { rslt: false, message: "tagId가 없습니다." };
    try {
      const res = await axios.delete(`/api/tags/${m.tagId}/profile`, {
        params: { contentType: m.contentType },
      });
      return { rslt: res.data?.rslt === true, message: res.data?.message as string | undefined };
    } catch {
      return { rslt: false };
    }
  }
  // ---- 파일 목록 모달 ----

  /** 파일 목록 모달 오픈 여부 */
  const fileListOpen = ref(false);
  /** 파일 목록 로딩 여부 */
  const fileListLoading = ref(false);
  /** 파일 목록 */
  const fileList = ref<FileRecord[]>([]);

  /**
   * 파일 목록 모달을 연다. API에서 파일 목록을 조회한다.
   * @param fileGroupId - 파일 그룹 번호
   */
  async function openFileList(fileGroupId: string | number): Promise<void> {
    fileListOpen.value = true;
    fileListLoading.value = true;
    fileList.value = [];
    try {
      const res = await axios.get("/api/file/file-account-list", { params: { fileGroupId } });
      if (!res.data?.rslt) return;
      fileList.value = Array.isArray(res.data.rsltList) ? (res.data.rsltList as FileRecord[]) : [];
    } catch {
      fileList.value = [];
    } finally {
      fileListLoading.value = false;
    }
  }

  /** 파일 목록 모달을 닫는다. */
  function closeFileList(): void {
    fileListOpen.value = false;
  }

  return {
    // 댓글 등록/수정
    commentRegOpen,
    commentRegLoading,
    commentId,
    commentRefId,
    commentRefContentType,
    commentContent,
    openCommentReg,
    openCommentMdf,
    closeCommentReg,
    // 댓글 목록
    commentListOpen,
    commentListLoading,
    commentList,
    commentListRefId,
    commentListRefContentType,
    openCommentList,
    closeCommentList,
    // 이력
    historyOpen,
    historyLoading,
    historyContentType,
    historyPostId,
    historyTriggeredAt,
    historyList,
    openHistory,
    closeHistory,
    restoreHistory,
    deleteHistory,
    clearHistory,
    // 관련 글 추가
    relatedOpen,
    relatedSrcContentType,
    relatedSrcId,
    relatedRelationType,
    relatedTargetContentType,
    relatedKeyword,
    relatedSearchResults,
    relatedSelectedTarget,
    relatedReason,
    relatedSearching,
    relatedSearchAttempted,
    relatedValidationMsg,
    openRelated,
    closeRelated,
    onRelatedTargetTypeChange,
    searchRelatedTargets,
    selectRelatedTarget,
    saveRelated,
    // 상태/라이프사이클
    toggleState,
    setLifecycle,
    // 태그 목록 모달
    tagListOpen,
    tagListLoading,
    tagGroupMap,
    tagListHideSingles,
    openTagList,
    closeTagList,
    loadTagList,
    toggleTagListSingles,
    // 태그 프로필 모달
    tagProfileOpen,
    tagProfileLoading,
    tagProfileTextClassOptions,
    tagProfileModel,
    openTagProfile,
    closeTagProfile,
    loadTagProfileTextClassOptions,
    saveTagProfile,
    deleteTagProfile,
    // 파일 목록 모달
    fileListOpen,
    fileListLoading,
    fileList,
    openFileList,
    closeFileList,
  };
});
