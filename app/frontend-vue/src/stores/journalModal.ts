import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import type { JournalDayDto } from "@/stores/journal";
import { formatLocalDateStr } from "@/utils/journalDate";

// ---- 타입 정의 ----

/** 저널 일자 등록/수정 폼 모델 */
export interface JournalDayRegModel {
  id?: number;
  journalDate?: string;
  journalDatePrecision?: string;
  diaryResolvedYn?: string;
  weather?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStr?: string; tagListStrWithCtgr?: string };
  /** 메타 컴포지션 */
  meta?: { metaListStr?: string };
}

/** 저널 챕터 등록/수정 폼 모델 */
export interface JournalChapterRegModel {
  id?: number;
  journalDayId?: number;
  stdrdDt?: string;
  journalDateWeekDay?: string;
  chapterType?: "DIARY" | "NOTE" | "DREAM";
  categoryCode?: string;
  title?: string;
  sortOrder?: number;
}

/** 챕터 카테고리 옵션 항목 */
export interface ChapterCategoryOption {
  code: string;
  codeName: string;
}

/** 저널 해석 등록/수정 폼 모델 */
export interface JournalInterpretationRegModel {
  id?: number;
  refId?: number;
  refContentType?: string;
  stdrdDt?: string;
  journalDateWeekDay?: string;
  ctgrCd?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
}

/** 메타 모달 페이로드 */
export interface JournalDayMetaModalPayload {
  metaId: number | string;
  yy: string;
  yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
  list: JournalDayDto[];
}


/** 저널 일자 태그 상세 모달 페이로드 */
export interface JournalDayTagDtlPayload {
  tagId: number | string;
  name: string;
  yy: string;
  yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
  list: JournalDayDto[];
}

/** 저널 할일 등록/수정 폼 모델 */
export interface JournalTodoRegModel {
  id?: number;
  yy?: number | string;
  mnth?: number | string;
  categoryCode?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStrWithCtgr?: string };
}

/** 챕터 선택 옵션 (entry 등록 시 챕터 선택) */
export interface JournalChapterOption {
  id: number | string;
  title?: string;
  sortOrder?: number;
  categoryCode?: string;
  categoryName?: string;
  chapterType?: string;
}

/** 저널 엔트리(일기/꿈/노트) 등록/수정 폼 모델 */
export interface JournalEntryRegModel {
  id?: number;
  /** 컨텐츠 유형: 'JOURNAL_DIARY' | 'JOURNAL_DREAM' | 'JOURNAL_NOTE' */
  contentType: string;
  journalDayId?: number;
  journalChapterId?: number | string;
  stdrdDt?: string;
  journalDateWeekDay?: string;
  ctgrCd?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
  elseDreamYn?: string;
  collapsedYn?: string;
  imprtcYn?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStrWithCtgr?: string };
  /** 챕터 목록 (DIARY/NOTE 등록 시 챕터 선택 옵션) */
  chapterList?: JournalChapterOption[];
}
// ---- 스토어 ----

export const useJournalModalStore = defineStore("journalModal", () => {
  // ---- 일자 등록/수정 모달 ----

  /** 등록/수정 모달 오픈 여부 */
  const dayRegOpen = ref(false);
  /** 등록/수정 폼 모델 */
  const dayRegModel = ref<JournalDayRegModel | null>(null);

  /**
   * 일자 등록/수정 모달을 연다.
   * 수정(id 있음) 시 Tagify 초기값(tagListStr/metaListStr)을 API 상세에서 채운다.
   * @param payload - 수정 시 기존 데이터, 신규 시 날짜 등 초기값
   */
  async function openDayReg(payload?: JournalDayRegModel) {
    let merged: JournalDayRegModel = {
      journalDatePrecision: "EXACT",
      diaryResolvedYn: "N",
      weather: "",
      tag: { tagListStr: "" },
      meta: { metaListStr: "" },
      ...payload,
    };
    if (!merged.journalDate?.trim()) {
      merged.journalDate = formatLocalDateStr(new Date());
    }
    if (payload?.id) {
      try {
        const res = await axios.get(`/api/journal/day/${payload.id}`);
        const dto = res.data?.rsltObj as JournalDayDto | undefined;
        if (dto) {
          const tagCmpstn = dto.tag as { tagListStrWithCtgr?: string; tagListStr?: string } | undefined;
          const metaCmpstn = dto.meta as { metaListStr?: string } | undefined;
          merged = {
            ...merged,
            journalDate: dto.journalDate ?? dto.stdrdDt ?? merged.journalDate,
            journalDatePrecision: dto.journalDatePrecision ?? merged.journalDatePrecision,
            weather: dto.weather ?? merged.weather,
            diaryResolvedYn: (dto as { diaryResolvedYn?: string }).diaryResolvedYn ?? merged.diaryResolvedYn,
            tag: { tagListStr: tagCmpstn?.tagListStrWithCtgr ?? tagCmpstn?.tagListStr ?? "" },
            meta: { metaListStr: metaCmpstn?.metaListStr ?? "" },
          };
        }
      } catch {
        console.error("[journalModal] openDayReg 상세 조회 실패 id=", payload.id);
      }
    }
    dayRegModel.value = merged;
    dayRegOpen.value = true;
  }

  /** 일자 등록/수정 모달을 닫는다. */
  function closeDayReg() {
    dayRegOpen.value = false;
  }

  // ---- 일자 상세 모달 ----

  /** 상세 모달 오픈 여부 */
  const dayDtlOpen = ref(false);
  /** 상세 모달 로딩 여부 */
  const dayDtlLoading = ref(false);
  /** 상세 모달 데이터 */
  const dayDtlData = ref<JournalDayDto | null>(null);

  /**
   * 일자 상세 모달을 연다. API에서 상세 데이터를 조회한다.
   * @param id - 저널 일자 ID
   */
  async function openDayDtl(id: number) {
    dayDtlData.value = null;
    dayDtlOpen.value = true;
    dayDtlLoading.value = true;
    try {
      const res = await axios.get(`/api/journal/day/${id}`);
      dayDtlData.value = res.data?.rsltObj ?? null;
    } catch {
      dayDtlData.value = null;
    } finally {
      dayDtlLoading.value = false;
    }
  }

  /** 일자 상세 모달을 닫는다. */
  function closeDayDtl() {
    dayDtlOpen.value = false;
  }

  // ---- 챕터 등록/수정 모달 ----

  /** 챕터 등록/수정 모달 오픈 여부 */
  const chapterRegOpen = ref(false);
  /** 챕터 등록/수정 폼 모델 */
  const chapterRegModel = ref<JournalChapterRegModel | null>(null);
  /** 챕터 카테고리 옵션 목록 (JOURNAL_CHAPTER_CTGR_CD) */
  const chapterCategoryOptions = ref<ChapterCategoryOption[]>([]);

  /**
   * 챕터 카테고리 옵션을 조회한다.
   * GET /api/code/items?groupCode=JOURNAL_CHAPTER_CTGR_CD — MNGR 권한 필요.
   */
  async function fetchChapterCategories() {
    if (chapterCategoryOptions.value.length > 0) return;
    try {
      const res = await axios.get("/api/code/items", { params: { groupCode: "JOURNAL_CHAPTER_CTGR_CD" } });
      chapterCategoryOptions.value = (res.data?.rsltList ?? []).map((item: Record<string, string>) => ({
        code: item.code ?? "",
        codeName: item.codeName ?? "",
      }));
    } catch {
      chapterCategoryOptions.value = [];
    }
  }

  /**
   * 챕터 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 챕터 데이터, 신규 시 journalDayId 등 초기값
   */
  function openChapterReg(payload?: JournalChapterRegModel) {
    chapterRegModel.value = {
      chapterType: "DIARY",
      categoryCode: "",
      title: "",
      ...payload,
    };
    chapterRegOpen.value = true;
    void fetchChapterCategories();
  }

  /** 챕터 등록/수정 모달을 닫는다. */
  function closeChapterReg() {
    chapterRegOpen.value = false;
  }

  // ---- 해석 등록/수정 모달 ----

  /** 해석 등록/수정 모달 오픈 여부 */
  const interpretationRegOpen = ref(false);
  /** 해석 등록/수정 폼 모델 */
  const interpretationRegModel = ref<JournalInterpretationRegModel | null>(null);

  /**
   * 해석 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 데이터, 신규 시 refId 등 초기값
   */
  function openInterpretationReg(payload?: JournalInterpretationRegModel) {
    interpretationRegModel.value = {
      ctgrCd: "",
      title: "",
      content: "",
      ...payload,
    };
    interpretationRegOpen.value = true;
  }

  /** 해석 등록/수정 모달을 닫는다. */
  function closeInterpretationReg() {
    interpretationRegOpen.value = false;
  }

  // ---- 메타 모달 ----

  /** 메타 모달 오픈 여부 */
  const metaModalOpen = ref(false);
  /** 메타 모달 로딩 여부 */
  const metaModalLoading = ref(false);
  /** 메타 모달 페이로드 */
  const metaModalPayload = ref<JournalDayMetaModalPayload | null>(null);

  /**
   * 메타 모달을 연다. 연도 목록 + 해당 연도 일자 목록을 조회한다.
   * @param metaId - 메타 ID
   * @param yy - 조회 연도 (없으면 현재 연도)
   */
  async function openMetaModal(metaId: number | string, yy?: string) {
    metaModalOpen.value = true;
    metaModalLoading.value = true;
    metaModalPayload.value = null;
    try {
      const yearsRes = await axios.get(`/api/journal/day/metas/${metaId}/years`);
      const yyList: string[] = (yearsRes.data?.rsltList ?? []).map(String);
      const currentYy = yy ?? String(new Date().getFullYear());
      const selectedYy = yyList.length > 0
        ? (yyList.includes(currentYy) ? currentYy : yyList[0])
        : currentYy;
      const daysRes = await axios.get("/api/journal/days", {
        params: { viewType: "SEARCH", metaId, yy: selectedYy },
      });
      metaModalPayload.value = {
        metaId,
        yy: selectedYy,
        yearOptions: yyList.map((y) => ({ value: y, label: y, selected: y === selectedYy })),
        list: daysRes.data?.rsltList ?? [],
      };
    } catch {
      metaModalPayload.value = null;
    } finally {
      metaModalLoading.value = false;
    }
  }

  /** 메타 모달을 닫는다. */
  function closeMetaModal() {
    metaModalOpen.value = false;
  }


  // ---- 일자 태그 상세 모달 ----

  /** 태그 상세 모달 오픈 여부 */
  const tagDtlOpen = ref(false);
  /** 태그 상세 모달 로딩 여부 */
  const tagDtlLoading = ref(false);
  /** 태그 상세 모달 페이로드 */
  const tagDtlPayload = ref<JournalDayTagDtlPayload | null>(null);

  /**
   * 태그 상세 모달을 연다. 연도 목록 + 해당 연도 일자 목록을 조회한다.
   * @param tagId - 태그 ID
   * @param name - 태그명
   * @param yy - 조회 연도 (없으면 현재 연도)
   */
  async function openTagDtl(tagId: number | string, name: string, yy?: string) {
    tagDtlOpen.value = true;
    tagDtlLoading.value = true;
    tagDtlPayload.value = null;
    try {
      const yearsRes = await axios.get(`/api/journal/day/tag/${tagId}/years`);
      const yyList: string[] = (yearsRes.data?.rsltList ?? []).map(String);
      const currentYy = yy ?? String(new Date().getFullYear());
      const selectedYy = yyList.length > 0
        ? (yyList.includes(currentYy) ? currentYy : yyList[0])
        : currentYy;
      const daysRes = await axios.get("/api/journal/days", {
        params: { viewType: "SEARCH", tagId, yy: selectedYy },
      });
      tagDtlPayload.value = {
        tagId,
        name,
        yy: selectedYy,
        yearOptions: yyList.map((y) => ({ value: y, label: y, selected: y === selectedYy })),
        list: daysRes.data?.rsltList ?? [],
      };
    } catch {
      tagDtlPayload.value = null;
    } finally {
      tagDtlLoading.value = false;
    }
  }

  /** 태그 상세 모달을 닫는다. */
  function closeTagDtl() {
    tagDtlOpen.value = false;
  }

  // ---- 할일 등록/수정 모달 ----

  /** 할일 등록/수정 모달 오픈 여부 */
  const todoRegOpen = ref(false);
  /** 할일 등록/수정 폼 모델 */
  const todoRegModel = ref<JournalTodoRegModel | null>(null);

  /**
   * 할일 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 데이터, 신규 시 yy/mnth 등 초기값
   */
  function openTodoReg(payload?: JournalTodoRegModel) {
    todoRegModel.value = {
      categoryCode: "",
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      ...payload,
    };
    todoRegOpen.value = true;
  }

  /** 할일 등록/수정 모달을 닫는다. */
  function closeTodoReg() {
    todoRegOpen.value = false;
  }

  // ---- 엔트리(일기/꿈/노트) 등록/수정 모달 ----

  /** 엔트리 등록/수정 모달 오픈 여부 */
  const entryRegOpen = ref(false);
  /** 엔트리 등록/수정 로딩 여부 */
  const entryRegLoading = ref(false);
  /** 엔트리 등록/수정 폼 모델 */
  const entryRegModel = ref<JournalEntryRegModel | null>(null);
  let dreamEntryRegOpening = false;

  /**
   * 엔트리 신규 등록 모달을 연다. (DIARY/NOTE: 챕터 목록을 caller 가 전달)
   * @param payload - contentType, journalDayId, stdrdDt 등 초기값
   */
  function openEntryReg(payload: JournalEntryRegModel) {
    entryRegModel.value = {
      ctgrCd: "",
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      chapterList: [],
      ...payload,
    };
    entryRegOpen.value = true;
    void hydrateEntryChapterOptions(entryRegModel.value);
  }

  /**
   * 꿈 엔트리 신규 등록 모달을 연다. dream-auto API 로 챕터를 자동 생성/조회한다.
   * @param params - journalDayId, stdrdDt, journalDateWeekDay
   */
  async function openDreamEntryReg(params: { journalDayId: number; stdrdDt: string; journalDateWeekDay?: string }) {
    if (dreamEntryRegOpening) return;
    dreamEntryRegOpening = true;
    entryRegOpen.value = true;
    entryRegLoading.value = true;
    entryRegModel.value = null;
    try {
      const fd = new FormData();
      fd.append("journalDayId", String(params.journalDayId));
      const res = await axios.post("/api/journal/chapters/dream-auto", fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      const chapter = res.data?.rsltObj;
      entryRegModel.value = {
        contentType: "JOURNAL_DREAM",
        journalDayId: params.journalDayId,
        journalChapterId: chapter?.id ?? "",
        stdrdDt: params.stdrdDt,
        journalDateWeekDay: params.journalDateWeekDay,
        title: "",
        content: "",
        tag: { tagListStrWithCtgr: "" },
        chapterList: [],
      };
    } catch {
      entryRegModel.value = null;
      entryRegOpen.value = false;
    } finally {
      entryRegLoading.value = false;
      dreamEntryRegOpening = false;
    }
  }

  /**
   * 엔트리 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param id - 엔트리 ID
   */
  async function openEntryMdf(id: number) {
    entryRegOpen.value = true;
    entryRegLoading.value = true;
    entryRegModel.value = null;
    try {
      const res = await axios.get(`/api/journal/entry/${id}`);
      const entry = res.data?.rsltObj;
      if (!entry) {
        entryRegOpen.value = false;
        return;
      }
      const merged: JournalEntryRegModel = {
        ...entry,
        tag: { tagListStrWithCtgr: entry.tag?.tagListStrWithCtgr ?? "" },
        chapterList: entry.chapterList ?? [],
      };
      await hydrateEntryChapterOptions(merged);
      entryRegModel.value = merged;
    } catch {
      entryRegModel.value = null;
      entryRegOpen.value = false;
    } finally {
      entryRegLoading.value = false;
    }
  }

  function shouldLoadChapterOptions(model: JournalEntryRegModel | null): model is JournalEntryRegModel {
    if (!model?.journalDayId) return false;
    return isDiaryLikeEntry(model.contentType) || isNoteLikeEntry(model.contentType);
  }

  function isDiaryLikeEntry(contentType: string): boolean {
    return contentType === "JOURNAL_DIARY" || contentType === "DIARY";
  }

  function isNoteLikeEntry(contentType: string): boolean {
    return contentType === "JOURNAL_NOTE" || contentType === "NOTE";
  }

  function normalizeChapterOptions(rawList: unknown, contentType: string): JournalChapterOption[] {
    if (!Array.isArray(rawList)) return [];
    const expectedChapterType = isNoteLikeEntry(contentType) ? "NOTE" : "DIARY";
    return rawList
      .map((raw) => raw as Record<string, unknown>)
      .filter((raw) => {
        const chapterType = String(raw.chapterType ?? "");
        return chapterType === "" || chapterType === expectedChapterType;
      })
      .map((raw) => ({
        id: raw.id as number | string,
        title: String(raw.title ?? ""),
        sortOrder: raw.sortOrder as number | undefined,
        categoryCode: String(raw.categoryCode ?? ""),
        categoryName: String(raw.categoryName ?? ""),
        chapterType: String(raw.chapterType ?? ""),
      }))
      .filter((chapter) => chapter.id !== undefined && chapter.id !== null && String(chapter.id) !== "");
  }

  async function hydrateEntryChapterOptions(model: JournalEntryRegModel | null): Promise<void> {
    if (!shouldLoadChapterOptions(model)) return;
    try {
      const res = await axios.get(`/api/journal/day/${model.journalDayId}`);
      const day = res.data?.rsltObj ?? {};
      const options = normalizeChapterOptions(
        Array.isArray(day.chapterList) && day.chapterList.length > 0
          ? day.chapterList
          : day.journalChapterList,
        model.contentType
      );
      if (options.length === 0) return;
      model.chapterList = options;
      if (!model.journalChapterId || !options.some((chapter) => String(chapter.id) === String(model.journalChapterId))) {
        model.journalChapterId = options[0].id;
      }
    } catch {
      console.error("[journalModal] 엔트리 챕터 옵션 조회 실패", model.journalDayId);
    }
  }

  /** 엔트리 등록/수정 모달을 닫는다. */
  function closeEntryReg() {
    entryRegOpen.value = false;
    entryRegModel.value = null;
  }
  return {
    // 일자 등록/수정
    dayRegOpen,
    dayRegModel,
    openDayReg,
    closeDayReg,
    // 일자 상세
    dayDtlOpen,
    dayDtlLoading,
    dayDtlData,
    openDayDtl,
    closeDayDtl,
    // 챕터 등록/수정
    chapterRegOpen,
    chapterRegModel,
    chapterCategoryOptions,
    openChapterReg,
    closeChapterReg,
    // 해석 등록/수정
    interpretationRegOpen,
    interpretationRegModel,
    openInterpretationReg,
    closeInterpretationReg,
    // 메타 모달
    metaModalOpen,
    metaModalLoading,
    metaModalPayload,
    openMetaModal,
    closeMetaModal,
    // 일자 태그 상세
    tagDtlOpen,
    tagDtlLoading,
    tagDtlPayload,
    openTagDtl,
    closeTagDtl,
    // 할일 등록/수정
    todoRegOpen,
    todoRegModel,
    openTodoReg,
    closeTodoReg,
    // 엔트리 등록/수정
    entryRegOpen,
    entryRegLoading,
    entryRegModel,
    openEntryReg,
    openDreamEntryReg,
    openEntryMdf,
    closeEntryReg,
  };
});
