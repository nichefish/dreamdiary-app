import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { swalConfirm, swalAlert } from "@/utils/swal";

// ---- 타입 정의 ----

/** TagItem — 태그 항목 */
export interface AnnualTagItem {
  id?: number | string;
  tagId: number | string;
  name: string;
  ctgr?: string;
  contentSize?: number;
  tagClass?: string;
  textClass?: string;
}

/** TagCmpstn — 태그 컴포지션 */
export interface AnnualTagCmpstn {
  list?: AnnualTagItem[];
  /** 태그 문자열 (tagify 초기값) */
  tagListStrWithCtgr?: string;
}

/** 결산 리뷰 Dto */
export interface JournalAnnualReviewDto {
  id?: number;
  contentType?: string;
  journalAnnualId?: number;
  yy?: number;
  categoryCode?: string;
  title?: string;
  content?: string;
  markdownContent?: string;
  tag?: AnnualTagCmpstn;
  comment?: { list?: Array<{ id?: number; markdownContent?: string }> };
}

/** 저널 결산 Dto */
export interface JournalAnnualDto {
  id?: number;
  /** 컨텐츠 타입 */
  contentType?: string;
  /** 결산 연도 */
  yy?: number;
  /** 제목 */
  title?: string;
  /** 내용 (raw) */
  content?: string;
  /** 마크다운 변환 내용 */
  markdownContent?: string;
  /** 꿈 일수 */
  dreamDayCnt?: number;
  /** 꿈 갯수 */
  dreamCnt?: number;
  /** 꿈 기록 완료 여부 (Y/N) */
  dreamComptYn?: string;
  tag?: AnnualTagCmpstn;
  journalAnnualReviewList?: JournalAnnualReviewDto[];
}

/** 결산 엔트리 항목 (DIARY / DREAM) */
export interface AnnualEntryDto {
  id?: number;
  contentType?: string;
  title?: string;
  content?: string;
  markdownContent?: string;
  stdrdDt?: string;
  journalDateWeekDay?: string;
  sortOrder?: number;
  elseDreamYn?: string;
  tag?: AnnualTagCmpstn;
  state?: { list?: Array<{ stateKey?: string }> };
}

/** 결산 등록/수정 폼 모델 */
export interface JournalAnnualRegistModel {
  id?: number;
  yy?: number;
  title?: string;
  content?: string;
  tag?: { tagListStrWithCtgr?: string };
}

/** 결산 리뷰 등록/수정 폼 모델 */
export interface JournalAnnualReviewRegistModel {
  id?: number;
  journalAnnualId?: number;
  yy?: number;
  categoryCode?: string;
  content?: string;
  tag?: { tagListStrWithCtgr?: string };
}

/** 상세 탭 섹션 */
export type AnnualSection = "DIARY" | "DREAM";

// ---- 스토어 ----

export const useJournalAnnualStore = defineStore("journalAnnual", () => {
  // ---- 목록 ----

  /** 결산 목록 원본 */
  const annualSourceList = ref<JournalAnnualDto[]>([]);
  /** 결산 목록 */
  const annualList = computed(() => {
    if (filterYy.value != null) {
      return annualSourceList.value.filter((annual) => annual.yy === filterYy.value);
    }

    const keyword = normalizeListKeyword(listKeyword.value);
    if (!keyword) return annualSourceList.value;
    return annualSourceList.value.filter((annual) => annualMatchesKeyword(annual, keyword));
  });
  /** 목록 로딩 상태 */
  const loading = ref(false);
  /** 목록 에러 */
  const error = ref<string | null>(null);

  /** 총 집계 (전체 기간 dreamDayCnt / dreamCnt) */
  const totalAnnual = ref<JournalAnnualDto | null>(null);
  /** 총 집계 로딩 상태 */
  const totalLoading = ref(false);

  /** 연도 필터 (null = 전체) */
  const filterYy = ref<number | null>(null);
  const listKeyword = ref("");

  // ---- 결산 등록/수정 모달 ----

  /** 등록/수정 모달 오픈 여부 */
  const registOpen = ref(false);
  /** 등록/수정 모달 로딩 여부 */
  const registLoading = ref(false);
  /** 등록/수정 폼 모델 */
  const registModel = ref<JournalAnnualRegistModel | null>(null);
  /** 등록/수정 처리 중 여부 */
  const submitting = ref(false);

  // ---- 상세 페이지 ----

  /** 결산 상세 DTO */
  const annualDetail = ref<JournalAnnualDto | null>(null);
  /** 상세 로딩 여부 */
  const detailLoading = ref(false);

  /** 현재 탭 섹션 */
  const activeSection = ref<AnnualSection>("DIARY");
  const showTagCloud = ref(true);
  const diaryKeyword = ref("");
  const dreamKeyword = ref("");

  /** 중요 일기/꿈 토글 */
  const showImprtc = ref(true);
  /** 참조 일기/꿈 토글 */
  const showRefrnc = ref(false);

  /** 태그 행 (DAY / DIARY / DREAM) */
  const tagRows = ref<Record<"DAY" | "DIARY" | "DREAM", AnnualTagItem[]>>({
    DAY: [],
    DIARY: [],
    DREAM: [],
  });

  /** DIARY 엔트리 목록 */
  const diaryEntries = ref<AnnualEntryDto[]>([]);
  /** DREAM 엔트리 목록 */
  const dreamEntries = ref<AnnualEntryDto[]>([]);
  /** 엔트리 목록 로딩 여부 */
  const entriesLoading = ref(false);

  // ---- 결산 리뷰 등록/수정 모달 ----

  /** 리뷰 등록/수정 모달 오픈 여부 */
  const reviewRegistOpen = ref(false);
  /** 리뷰 등록/수정 모달 로딩 여부 */
  const reviewRegistLoading = ref(false);
  /** 리뷰 등록/수정 폼 모델 */
  const reviewRegistModel = ref<JournalAnnualReviewRegistModel | null>(null);
  /** 리뷰 처리 중 여부 */
  const reviewSubmitting = ref(false);

  // ---- 목록 액션 ----

  /**
   * 결산 목록 조회.
   * GET /api/journal/annuals
   */
  async function fetchList() {
    loading.value = true;
    error.value = null;
    try {
      const res = await axios.get("/api/journal/annuals");
      annualSourceList.value = res.data?.rsltList ?? [];
    } catch {
      error.value = "결산 목록을 불러오지 못했습니다.";
      annualSourceList.value = [];
    } finally {
      loading.value = false;
    }
  }

  /**
   * 총 집계 조회.
   * GET /api/journal/annual/total
   */
  async function fetchTotal() {
    totalLoading.value = true;
    try {
      const res = await axios.get("/api/journal/annual/total");
      totalAnnual.value = res.data?.rsltObj ?? null;
    } catch {
      totalAnnual.value = null;
    } finally {
      totalLoading.value = false;
    }
  }

  /**
   * 연도 필터를 변경한다.
   * @param yy - 연도 (null = 전체)
   */
  function setFilterYy(yy: number | null) {
    filterYy.value = yy;
  }

  function applyListFilters() {
    // 목록 필터는 클라이언트에서 즉시 반영된다.
  }

  function clearListFilters() {
    filterYy.value = null;
    listKeyword.value = "";
  }

  function normalizeListKeyword(value?: string) {
    return (value ?? "").trim().toLowerCase();
  }

  function annualMatchesKeyword(annual: JournalAnnualDto, keyword: string) {
    const tagText = annual.tag?.list
      ?.map((tag) => `${tag.ctgr ?? ""} ${tag.name ?? ""}`)
      .join(" ") ?? "";
    const haystack = [
      annual.title,
      annual.content,
      annual.markdownContent,
      tagText,
    ].join(" ").toLowerCase();
    return haystack.includes(keyword);
  }

  // ---- 결산 등록/수정 액션 ----

  /** 결산 등록 모달을 연다 (신규). */
  function openRegist() {
    registModel.value = {
      yy: new Date().getFullYear(),
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
    };
    registOpen.value = true;
  }

  /**
   * 결산 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param yy - 결산 연도
   */
  async function openModify(yy: number) {
    registOpen.value = true;
    registLoading.value = true;
    registModel.value = null;
    try {
      const res = await axios.get(`/api/journal/annual/${yy}`);
      const dto: JournalAnnualDto = res.data?.rsltObj ?? {};
      registModel.value = {
        id: dto.id,
        yy: dto.yy,
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
   * 결산 등록/수정 처리.
   * POST /api/journal/annual/{yy}
   */
  async function submitRegist(): Promise<boolean> {
    if (!registModel.value?.yy) return false;
    submitting.value = true;
    try {
      const fd = new FormData();
      if (registModel.value.id != null) fd.append("id", String(registModel.value.id));
      fd.append("yy", String(registModel.value.yy));
      fd.append("contentType", "JOURNAL_ANNUAL");
      fd.append("title", registModel.value.title ?? "");
      fd.append("content", registModel.value.content ?? "");
      fd.append("tag.tagListStr", registModel.value.tag?.tagListStrWithCtgr ?? "");

      const res = await axios.post(
        `/api/journal/annual/${registModel.value.yy}`,
        fd,
        { headers: { "Content-Type": "multipart/form-data" } }
      );
      if (res.data?.rslt) {
        closeRegist();
        void fetchList();
        return true;
      }
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
      return false;
    } catch {
      void swalAlert("요청 처리 중 오류가 발생했습니다.");
      return false;
    } finally {
      submitting.value = false;
    }
  }

  // ---- 상세 액션 ----

  /**
   * 결산 상세 조회.
   * GET /api/journal/annual/{yy}
   * @param yy - 결산 연도
   */
  async function fetchDetail(yy: number) {
    detailLoading.value = true;
    annualDetail.value = null;
    try {
      const res = await axios.get(`/api/journal/annual/${yy}`);
      annualDetail.value = res.data?.rsltObj ?? null;
    } catch {
      annualDetail.value = null;
    } finally {
      detailLoading.value = false;
    }
  }

  /**
   * 탭 섹션을 변경하고 엔트리 + 태그 행을 재조회한다.
   * @param section - DIARY | DREAM
   * @param yy - 결산 연도
   */
  async function setSection(section: AnnualSection, yy: number) {
    activeSection.value = section;
    await fetchEntries(yy, section);
    await fetchTagRows(yy, section);
  }

  /**
   * 엔트리 목록 조회.
   * GET /api/journal/annual/{yy}/diaries or /dreams
   * @param yy - 결산 연도
   * @param section - DIARY | DREAM
   */
  async function fetchEntries(yy: number, section: AnnualSection) {
    entriesLoading.value = true;
    try {
      const path = section === "DIARY"
        ? `/api/journal/annual/${yy}/diaries`
        : `/api/journal/annual/${yy}/dreams`;
      const res = await axios.get(path, {
        params: {
          showImprtc: showImprtc.value,
          showRefrnc: showRefrnc.value,
          ...getEntryFilterParams(section),
        },
      });
      if (section === "DIARY") diaryEntries.value = res.data?.rsltList ?? [];
      else dreamEntries.value = res.data?.rsltList ?? [];
    } catch {
      if (section === "DIARY") diaryEntries.value = [];
      else dreamEntries.value = [];
    } finally {
      entriesLoading.value = false;
    }
  }

  function getEntryFilterParams(section: AnnualSection): Record<string, string> {
    const keyword = section === "DIARY" ? diaryKeyword.value : dreamKeyword.value;
    return keyword.trim() ? { searchKeywords: keyword.trim() } : {};
  }

  /**
   * 태그 행 조회.
   * GET /api/journal/annual/{yy}/tags?type=DAY|DIARY|DREAM
   * @param yy - 결산 연도
   * @param section - DIARY (→ DAY + DIARY 행) | DREAM (→ DREAM 행)
   */
  async function fetchTagRows(yy: number, section: AnnualSection) {
    try {
      if (section === "DIARY") {
        const [dayRes, diaryRes] = await Promise.all([
          axios.get(`/api/journal/annual/${yy}/tags`, { params: { type: "DAY" } }),
          axios.get(`/api/journal/annual/${yy}/tags`, { params: { type: "DIARY" } }),
        ]);
        tagRows.value.DAY = dayRes.data?.rsltList ?? [];
        tagRows.value.DIARY = diaryRes.data?.rsltList ?? [];
      } else {
        const dreamRes = await axios.get(`/api/journal/annual/${yy}/tags`, { params: { type: "DREAM" } });
        tagRows.value.DREAM = dreamRes.data?.rsltList ?? [];
      }
    } catch {
      /* 태그 행 실패는 조용히 무시 (목록 렌더에 영향 없음) */
    }
  }

  /**
   * 표시 토글(IMPORTANT/REFERENCE)을 변경하고 현재 섹션 엔트리를 재조회한다.
   * @param yy - 결산 연도
   */
  async function toggleImprtc(yy: number) {
    showImprtc.value = !showImprtc.value;
    await fetchEntries(yy, activeSection.value);
  }

  async function toggleRefrnc(yy: number) {
    showRefrnc.value = !showRefrnc.value;
    await fetchEntries(yy, activeSection.value);
  }

  async function toggleTagCloud(yy: number) {
    showTagCloud.value = !showTagCloud.value;
    if (showTagCloud.value) {
      await fetchTagRows(yy, activeSection.value);
    }
  }

  async function applyEntryFilters(yy: number) {
    await fetchEntries(yy, activeSection.value);
  }

  async function clearEntryFilters(yy: number) {
    diaryKeyword.value = "";
    dreamKeyword.value = "";
    await fetchEntries(yy, activeSection.value);
  }

  // ---- 결산 리뷰 액션 ----

  /**
   * 리뷰 등록 모달을 연다.
   * @param journalAnnualId - 결산 ID
   * @param yy - 결산 연도 (refetch 시 사용)
   */
  function openReviewRegist(journalAnnualId: number, yy?: number) {
    reviewRegistModel.value = {
      journalAnnualId,
      yy,
      categoryCode: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
    };
    reviewRegistOpen.value = true;
  }

  /**
   * 리뷰 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param id - 리뷰 ID
   */
  async function openReviewModify(id: number) {
    reviewRegistOpen.value = true;
    reviewRegistLoading.value = true;
    reviewRegistModel.value = null;
    try {
      const res = await axios.get(`/api/journal/annual/review/${id}`);
      const dto: JournalAnnualReviewDto = res.data?.rsltObj ?? {};
      reviewRegistModel.value = {
        id: dto.id,
        journalAnnualId: dto.journalAnnualId,
        yy: dto.yy,
        categoryCode: dto.categoryCode ?? "",
        content: dto.content ?? "",
        tag: { tagListStrWithCtgr: dto.tag?.tagListStrWithCtgr ?? "" },
      };
    } catch {
      reviewRegistModel.value = null;
      reviewRegistOpen.value = false;
    } finally {
      reviewRegistLoading.value = false;
    }
  }

  /** 리뷰 등록/수정 모달을 닫는다. */
  function closeReviewRegist() {
    reviewRegistOpen.value = false;
    reviewRegistModel.value = null;
  }

  /**
   * 리뷰 등록/수정 처리.
   * POST /api/journal/annual/reviews (신규) | POST /api/journal/annual/review/{id} (수정)
   */
  async function submitReviewRegist(): Promise<boolean> {
    if (!reviewRegistModel.value?.journalAnnualId) return false;
    const model = reviewRegistModel.value;
    reviewSubmitting.value = true;
    try {
      const fd = new FormData();
      if (model.id != null) fd.append("id", String(model.id));
      fd.append("journalAnnualId", String(model.journalAnnualId));
      fd.append("contentType", "JOURNAL_ANNUAL_REVIEW");
      if (model.categoryCode) fd.append("categoryCode", model.categoryCode);
      fd.append("content", model.content ?? "");
      fd.append("tag.tagListStr", model.tag?.tagListStrWithCtgr ?? "");

      const url = model.id != null
        ? `/api/journal/annual/review/${model.id}`
        : "/api/journal/annual/reviews";
      const res = await axios.post(url, fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data?.rslt) {
        closeReviewRegist();
        /* 상세 재조회 — 리뷰 목록이 detail DTO 에 포함되어 있어 refetch 로 갱신한다. */
        if (model.yy) void fetchDetail(model.yy);
        return true;
      }
      void swalAlert(res.data?.message ?? "처리에 실패했습니다.");
      return false;
    } catch {
      void swalAlert("요청 처리 중 오류가 발생했습니다.");
      return false;
    } finally {
      reviewSubmitting.value = false;
    }
  }

  /**
   * 리뷰 삭제.
   * DELETE /api/journal/annual/review/{id}
   * @param id - 리뷰 ID
   * @param yy - 상세 재조회용 연도
   */
  async function deleteReview(id: number, yy?: number) {
    const confirmed = await swalConfirm("리뷰를 삭제하시겠습니까?");
    if (!confirmed) return;
    try {
      const res = await axios.delete(`/api/journal/annual/review/${id}`);
      if (res.data?.rslt) {
        if (yy) void fetchDetail(yy);
      } else {
        void swalAlert(res.data?.message ?? "삭제에 실패했습니다.");
      }
    } catch {
      void swalAlert("요청 처리 중 오류가 발생했습니다.");
    }
  }

  return {
    // 목록
    annualList,
    loading,
    error,
    totalAnnual,
    totalLoading,
    filterYy,
    listKeyword,
    fetchList,
    fetchTotal,
    setFilterYy,
    applyListFilters,
    clearListFilters,
    // 결산 등록/수정
    registOpen,
    registLoading,
    registModel,
    submitting,
    openRegist,
    openModify,
    closeRegist,
    submitRegist,
    // 상세
    annualDetail,
    detailLoading,
    activeSection,
    showTagCloud,
    diaryKeyword,
    dreamKeyword,
    showImprtc,
    showRefrnc,
    tagRows,
    diaryEntries,
    dreamEntries,
    entriesLoading,
    fetchDetail,
    setSection,
    fetchEntries,
    fetchTagRows,
    toggleImprtc,
    toggleRefrnc,
    toggleTagCloud,
    applyEntryFilters,
    clearEntryFilters,
    // 리뷰 등록/수정
    reviewRegistOpen,
    reviewRegistLoading,
    reviewRegistModel,
    reviewSubmitting,
    openReviewRegist,
    openReviewModify,
    closeReviewRegist,
    submitReviewRegist,
    deleteReview,
  };
});
