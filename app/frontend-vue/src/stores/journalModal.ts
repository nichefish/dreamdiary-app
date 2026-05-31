import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import type { JournalDayDto } from "@/stores/journal";
import { formatLocalDateStr } from "@/utils/journalDate";
import { mergeTagifyListIntoCategoryMap } from "@/utils/tagifyHelper";

// ---- categoryMap (앱 세션 SSOT: 로그인·마운트 시 preload 1회, 저장 시 Tagify JSON 병합 — 무효화·모달 오픈 재조회 없음) ----

const CATEGORY_MAP_URL_DAY_TAG = "/api/journal/day/tag/categories";
const CATEGORY_MAP_URL_DAY_META = "/api/journal/day/meta/categories";
const CATEGORY_MAP_URL_ENTRY_DIARY = "/api/journal/entry/tag/categories?type=DIARY";
const CATEGORY_MAP_URL_ENTRY_DREAM = "/api/journal/entry/tag/categories?type=DREAM";

function normalizeCategoryMap(raw: unknown): Record<string, string[]> {
  if (!raw || typeof raw !== "object" || Array.isArray(raw)) return {};
  const out: Record<string, string[]> = {};
  for (const [tagName, categories] of Object.entries(raw as Record<string, unknown>)) {
    if (!Array.isArray(categories)) continue;
    out[tagName] = categories.map((c) => String(c ?? "")).filter((c) => c.length > 0);
  }
  return out;
}

/** categoryMap 을 서버에서 조회한다. (최초 preload·캐시 미적재 시에만 호출) */
async function fetchCategoryMap(url: string): Promise<Record<string, string[]>> {
  try {
    const res = await axios.get(url);
    if (!res.data?.rslt) {
      console.error("[journalModal] categoryMap 조회 rslt=false:", url);
      return {};
    }
    const raw = res.data.rsltMap ?? res.data.rsltObj;
    return normalizeCategoryMap(raw);
  } catch {
    console.error("[journalModal] categoryMap 조회 실패:", url);
    return {};
  }
}

function resolveEntryCategoryMapUrl(contentType: string | undefined): string | null {
  if (!contentType) return null;
  if (contentType === "JOURNAL_DREAM" || contentType === "DREAM") {
    return CATEGORY_MAP_URL_ENTRY_DREAM;
  }
  if (
    contentType === "JOURNAL_DIARY"
    || contentType === "DIARY"
    || contentType === "JOURNAL_NOTE"
    || contentType === "NOTE"
  ) {
    return CATEGORY_MAP_URL_ENTRY_DIARY;
  }
  return null;
}

/** 앱 마운트·로그인 후 4종 categoryMap 을 1회 적재한다. */
export async function preloadCategoryMaps(): Promise<void> {
  await useJournalModalStore().preloadAllCategoryMaps();
}

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
  /** 메타 이름 (헤더 표시용) */
  metaName?: string;
  yy: string;
  yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
  list: JournalDayDto[];
}


/** 저널 일자 태그 상세 모달 페이로드 */
export interface JournalDayTagDetailPayload {
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
    /** 앱 세션 categoryMap — preload 미적재 시에만 HTTP */
    await Promise.all([
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_TAG),
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_META),
    ]);
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
  /** 챕터 카테고리 옵션 목록 — 일기 전용 (JOURNAL_CHAPTER_DIARY_CTGR_CD) */
  const chapterDiaryCategoryOptions = ref<ChapterCategoryOption[]>([]);
  /** 챕터 카테고리 옵션 목록 — 노트 전용 (JOURNAL_CHAPTER_NOTE_CTGR_CD) */
  const chapterNoteCategoryOptions = ref<ChapterCategoryOption[]>([]);
  /** 프리페치 진행 중 여부 (중복 요청 방지) */
  let chapterCategoryFetching = false;

  function mapCategoryOptions(list: unknown[]): ChapterCategoryOption[] {
    return list.map((item: unknown) => {
      const i = item as Record<string, string>;
      return { code: i.code ?? "", codeName: i.codeName ?? "" };
    });
  }

  /**
   * 챕터 카테고리 옵션을 조회한다. (일기/노트 각각 별도 코드 그룹)
   * 화면 마운트 시점에도 호출하여 세션 캐시에 넣어 두면 모달 오픈 시 로딩 없이 사용 가능하다.
   */
  async function prefetchChapterCategories(): Promise<void> {
    const diaryDone = chapterDiaryCategoryOptions.value.length > 0;
    const noteDone  = chapterNoteCategoryOptions.value.length > 0;
    if (diaryDone && noteDone) return;
    if (chapterCategoryFetching) return;
    chapterCategoryFetching = true;
    try {
      const [diaryRes, noteRes] = await Promise.all([
        diaryDone ? null : axios.get("/api/code/items", { params: { groupCode: "JOURNAL_CHAPTER_DIARY_CTGR_CD" } }),
        noteDone  ? null : axios.get("/api/code/items", { params: { groupCode: "JOURNAL_CHAPTER_NOTE_CTGR_CD"  } }),
      ]);
      if (diaryRes) chapterDiaryCategoryOptions.value = mapCategoryOptions(diaryRes.data?.rsltList ?? []);
      if (noteRes)  chapterNoteCategoryOptions.value  = mapCategoryOptions(noteRes.data?.rsltList  ?? []);
    } catch {
      console.error("[journalModal] 챕터 카테고리 조회 실패");
    } finally {
      chapterCategoryFetching = false;
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
    void prefetchChapterCategories();
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
  async function openMetaModal(metaId: number | string, yy?: string, metaName?: string) {
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
        metaName,
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
  const tagDetailOpen = ref(false);
  /** 태그 상세 모달 로딩 여부 */
  const tagDetailLoading = ref(false);
  /** 태그 상세 모달 페이로드 */
  const tagDetailPayload = ref<JournalDayTagDetailPayload | null>(null);

  /**
   * 태그 상세 모달을 연다. 연도 목록 + 해당 연도 일자 목록을 조회한다.
   * @param tagId - 태그 ID
   * @param name - 태그명
   * @param yy - 조회 연도 (없으면 현재 연도)
   */
  async function openTagDetail(tagId: number | string, name: string, yy?: string) {
    tagDetailOpen.value = true;
    tagDetailLoading.value = true;
    tagDetailPayload.value = null;
    try {
      const yearsRes = await axios.get(`/api/journal/day/tag/${tagId}/years`);
      const yyList: string[] = (yearsRes.data?.rsltList ?? []).map(String);
      /** yy === '' 이면 전체 연도 조회 (연도 필터 미적용) */
      const isAllYears = yy === "";
      const currentYy = isAllYears ? "" : (yy ?? String(new Date().getFullYear()));
      const selectedYy = isAllYears ? "" : (
        yyList.length > 0 ? (yyList.includes(currentYy) ? currentYy : yyList[0]) : currentYy
      );
      const dayParams: Record<string, unknown> = { viewType: "SEARCH", tagId };
      if (selectedYy) dayParams.yy = selectedYy;
      const daysRes = await axios.get("/api/journal/days", { params: dayParams });
      tagDetailPayload.value = {
        tagId,
        name,
        yy: selectedYy,
        yearOptions: [
          { value: "", label: "전체 년도" },
          ...yyList.map((y) => ({ value: y, label: y, selected: y === selectedYy })),
        ],
        list: daysRes.data?.rsltList ?? [],
      };
    } catch {
      tagDetailPayload.value = null;
    } finally {
      tagDetailLoading.value = false;
    }
  }

  /** 태그 상세 모달을 닫는다. */
  function closeTagDetail() {
    tagDetailOpen.value = false;
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
  /** 일자 태그 categoryMap — 앱 세션 SSOT (TagifyEditor prop) */
  const dayTagCategoryMap = ref<Record<string, string[]>>({});
  const dayTagCategoryMapLoaded = ref(false);
  /** 일자 메타 categoryMap — 앱 세션 SSOT */
  const dayMetaCategoryMap = ref<Record<string, string[]>>({});
  const dayMetaCategoryMapLoaded = ref(false);
  /** 엔트리 DIARY 태그 categoryMap */
  const entryDiaryCategoryMap = ref<Record<string, string[]>>({});
  const entryDiaryCategoryMapLoaded = ref(false);
  /** 엔트리 DREAM 태그 categoryMap */
  const entryDreamCategoryMap = ref<Record<string, string[]>>({});
  const entryDreamCategoryMapLoaded = ref(false);
  /** 엔트리 모달용 뷰 (contentType 에 따라 diary/dream map 참조) */
  const entryCategoryMap = computed((): Record<string, string[]> | null => {
    const ct = entryRegModel.value?.contentType;
    if (!ct) return null;
    if (isDreamEntry(ct)) return entryDreamCategoryMap.value;
    if (isDiaryLikeEntry(ct)) return entryDiaryCategoryMap.value;
    return null;
  });
  let dreamEntryRegOpening = false;

  function categoryMapRefForUrl(url: string) {
    switch (url) {
      case CATEGORY_MAP_URL_DAY_TAG: return dayTagCategoryMap;
      case CATEGORY_MAP_URL_DAY_META: return dayMetaCategoryMap;
      case CATEGORY_MAP_URL_ENTRY_DIARY: return entryDiaryCategoryMap;
      case CATEGORY_MAP_URL_ENTRY_DREAM: return entryDreamCategoryMap;
      default: return null;
    }
  }

  function categoryMapLoadedRefForUrl(url: string) {
    switch (url) {
      case CATEGORY_MAP_URL_DAY_TAG: return dayTagCategoryMapLoaded;
      case CATEGORY_MAP_URL_DAY_META: return dayMetaCategoryMapLoaded;
      case CATEGORY_MAP_URL_ENTRY_DIARY: return entryDiaryCategoryMapLoaded;
      case CATEGORY_MAP_URL_ENTRY_DREAM: return entryDreamCategoryMapLoaded;
      default: return null;
    }
  }

  /** URL 별 map 이 아직 적재되지 않았을 때만 서버에서 1회 조회 */
  async function ensureCategoryMap(url: string): Promise<Record<string, string[]>> {
    const mapRef = categoryMapRefForUrl(url);
    const loadedRef = categoryMapLoadedRefForUrl(url);
    if (!mapRef || !loadedRef) return {};
    if (loadedRef.value) return mapRef.value;
    mapRef.value = await fetchCategoryMap(url);
    loadedRef.value = true;
    return mapRef.value;
  }

  async function preloadAllCategoryMaps(): Promise<void> {
    await Promise.all([
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_TAG),
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_META),
      ensureCategoryMap(CATEGORY_MAP_URL_ENTRY_DREAM),
      ensureCategoryMap(CATEGORY_MAP_URL_ENTRY_DIARY),
    ]);
  }

  /**
   * 엔트리 신규 등록 모달을 연다. (DIARY/NOTE: 챕터 목록을 caller 가 전달)
   * categoryMap 과 챕터 옵션을 병렬로 조회한 뒤 모달 로딩을 해제한다.
   * @param payload - contentType, journalDayId, stdrdDt 등 초기값
   */
  async function openEntryReg(payload: JournalEntryRegModel) {
    entryRegOpen.value = true;
    entryRegLoading.value = true;
    entryRegModel.value = null;
    try {
      const model: JournalEntryRegModel = {
        ctgrCd: "",
        title: "",
        content: "",
        tag: { tagListStrWithCtgr: "" },
        chapterList: [],
        ...payload,
      };
      const showTagForType = isDiaryLikeEntry(payload.contentType) || isDreamEntry(payload.contentType);
      const categoryUrl = resolveEntryCategoryMapUrl(payload.contentType);
      await Promise.all([
        showTagForType && categoryUrl ? ensureCategoryMap(categoryUrl) : Promise.resolve(),
        hydrateEntryChapterOptions(model),
      ]);
      entryRegModel.value = model;
    } finally {
      entryRegLoading.value = false;
    }
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
      const [res] = await Promise.all([
        axios.post("/api/journal/chapters/dream-auto", fd, { headers: { "Content-Type": "multipart/form-data" } }),
        ensureCategoryMap(CATEGORY_MAP_URL_ENTRY_DREAM),
      ]);
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
      const showTagForType = isDiaryLikeEntry(merged.contentType) || isDreamEntry(merged.contentType);
      const categoryUrl = resolveEntryCategoryMapUrl(merged.contentType);
      await Promise.all([
        showTagForType && categoryUrl ? ensureCategoryMap(categoryUrl) : Promise.resolve(),
        hydrateEntryChapterOptions(merged),
      ]);
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

  function isDreamEntry(contentType: string): boolean {
    return contentType === "JOURNAL_DREAM" || contentType === "DREAM";
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

  /**
   * 저장 성공 응답 rsltMap 으로 앱 세션 categoryMap 을 교체한다.
   * 변경 전: Tagify JSON 병합만(삭제 미반영).
   * 변경 후: 서버가 evict 후 조회한 전역 map — 추가 GET 없이 삭제 포함 동기화.
   */

  /** 로그아웃·세션 만료 시 앱 categoryMap 을 비운다 (다음 사용자 preload 용). */
  function resetCategoryMaps(): void {
    dayTagCategoryMap.value = {};
    dayTagCategoryMapLoaded.value = false;
    dayMetaCategoryMap.value = {};
    dayMetaCategoryMapLoaded.value = false;
    entryDiaryCategoryMap.value = {};
    entryDiaryCategoryMapLoaded.value = false;
    entryDreamCategoryMap.value = {};
    entryDreamCategoryMapLoaded.value = false;
  }

  function applyCategoryMapsFromSaveResponse(
    rsltMap: unknown,
    entryContentType?: string,
  ): void {
    if (!rsltMap || typeof rsltMap !== "object" || Array.isArray(rsltMap)) return;
    const maps = rsltMap as Record<string, unknown>;
    if (maps.dayTagCategoryMap != null) {
      dayTagCategoryMap.value = normalizeCategoryMap(maps.dayTagCategoryMap);
      dayTagCategoryMapLoaded.value = true;
    }
    if (maps.dayMetaCategoryMap != null) {
      dayMetaCategoryMap.value = normalizeCategoryMap(maps.dayMetaCategoryMap);
      dayMetaCategoryMapLoaded.value = true;
    }
    if (maps.entryTagCategoryMap != null && entryContentType) {
      const entryUrl = resolveEntryCategoryMapUrl(entryContentType);
      if (!entryUrl) return;
      const mapRef = categoryMapRefForUrl(entryUrl);
      const loadedRef = categoryMapLoadedRefForUrl(entryUrl);
      if (mapRef) {
        mapRef.value = normalizeCategoryMap(maps.entryTagCategoryMap);
        if (loadedRef) loadedRef.value = true;
      }
    }
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
    chapterDiaryCategoryOptions,
    chapterNoteCategoryOptions,
    prefetchChapterCategories,
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
    tagDetailOpen,
    tagDetailLoading,
    tagDetailPayload,
    openTagDetail,
    closeTagDetail,
    // 할일 등록/수정
    todoRegOpen,
    todoRegModel,
    openTodoReg,
    closeTodoReg,
    // 엔트리 등록/수정
    entryRegOpen,
    entryRegLoading,
    entryRegModel,
    dayTagCategoryMap,
    dayMetaCategoryMap,
    entryCategoryMap,
    openEntryReg,
    openDreamEntryReg,
    openEntryMdf,
    closeEntryReg,
    preloadAllCategoryMaps,
    applyCategoryMapsFromSaveResponse,
    resetCategoryMaps,
  };
});
