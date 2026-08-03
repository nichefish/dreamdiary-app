import { computed, ref, watch } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type {
  JournalDayDto,
  JournalEntryDto,
  JournalPrefixDto,
  MetaDto,
} from "@/features/journal/stores/journal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { formatLocalDateStr } from "@/features/journal/utils/journalDate";
import { mergeTagifyListIntoCategoryMap } from "@/shared/utils/tagifyHelper";
import { requireApiPathSegment } from "@/shared/utils/appPath";
import { swalRequestError } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";

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

/** 태그·메타 categoryMap 을 서버에서 다시 조회한다. (레거시 태그 카테고리 동기화) */
export async function syncCategoryMaps(): Promise<void> {
  await useJournalModalStore().syncAllCategoryMaps();
}

// ---- 타입 정의 ----

/** 저널 일자 등록/수정 폼 모델 */
export interface JournalDayRegistModel {
  id?: number;
  journalDate?: string;
  journalDatePrecision?: string;
  diaryResolvedYn?: string;
  dreamResolvedYn?: string;
  weather?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStr?: string; tagListStrWithCtgr?: string };
  /** 메타 컴포지션 */
  meta?: { metaListStr?: string };
}

/** 저널 챕터 등록/수정 폼 모델 */
export interface JournalChapterRegistModel {
  id?: number;
  journalDayId?: number;
  stdrdDt?: string;
  chapterType?: "DIARY" | "NOTE" | "DREAM";
  /** 일반 챕터에 선택할 개인 말머리. 시스템 요약·DREAM에는 적용하지 않는다. */
  prefixId?: number | null;
  prefix?: JournalPrefixDto | null;
  /** 서버가 관리하는 시스템 요약 챕터 여부 */
  summaryYn?: string;
  title?: string;
  sortOrder?: number;
}

/** 저널 Reflection 등록/수정 폼 모델 (entry 경로) */
export interface JournalReflectionRegistModel {
  id?: number;
  /** target 대상 엔트리 ID */
  refId?: number;
  /** target 대상 콘텐츠 타입 */
  refContentType?: string;
  /** 소속 일자 — 독립 Reflection 챕터 선택 옵션 조회에 사용 */
  journalDayId?: number;
  /** 소속 chapter (target 묶기 시 기본은 target 의 chapter, 챕터 직속 시 필수) */
  journalChapterId?: number;
  stdrdDt?: string;
  title?: string;
  sortOrder?: number;
  content?: string;
  /** 태그 — 일기 축과 동일 categoryMap(DIARY). 저장 시 ref_content_type=JOURNAL_REFLECTION */
  tag?: { tagListStrWithCtgr?: string; list?: unknown[] };
  /** 독립 Reflection 전용: 같은 일자의 DIARY·NOTE 챕터 선택지 */
  chapterList?: JournalChapterOption[];
}

/** 일자 필터 모달 시드 타입 */
export type DayFilterSeedType = "meta" | "tag";

/** 일자 필터 모달 페이로드 (메타/태그 다중 AND 검색) */
export interface JournalDayFilterPayload {
  /** 초기 시드 타입 */
  seedType: DayFilterSeedType;
  /** 초기 시드 ID */
  seedId: string;
  /** 초기 시드 이름 */
  seedName: string;
  /** 초기 시드 카테고리 */
  seedCtgr?: string;
  yy: string;
  yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
  list: JournalDayDto[];
}

/** 저널 할일 등록/수정 폼 모델 */
export interface JournalTodoRegistModel {
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
  prefix?: JournalPrefixDto | null;
  prefixId?: number | null;
  /** 서버가 관리하는 시스템 요약 챕터 여부 */
  summaryYn?: string;
  chapterType?: string;
}

/** 저널 엔트리(일기/꿈/노트) 등록/수정 폼 모델 */
export interface JournalEntryRegistModel {
  id?: number;
  /** 컨텐츠 유형: 'JOURNAL_DIARY' | 'JOURNAL_DREAM' | 'JOURNAL_NOTE' */
  contentType: string;
  journalDayId?: number;
  journalChapterId?: number | string;
  stdrdDt?: string;
  /** 소속 챕터 유형으로 해석한 개인 Prefix 목록 content_type */
  prefixContentType?: "JOURNAL_DIARY" | "JOURNAL_DREAM" | "JOURNAL_NOTE";
  prefixId?: number | null;
  prefix?: JournalPrefixDto | null;
  title?: string;
  sortOrder?: number;
  content?: string;
  elseDreamYn?: string;
  elseDreamerNm?: string;
  collapsedYn?: string;
  imprtcYn?: string;
  /** 태그 컴포지션 */
  tag?: { tagListStrWithCtgr?: string };
  /** 챕터 목록 (DIARY/NOTE 등록 시 챕터 선택 옵션) */
  chapterList?: JournalChapterOption[];
}
// ---- 스토어 ----

export const useJournalModalStore = defineStore("journalModal", () => {
  const { t } = useLocaleStore();
  const personalPrefixOptionsStore = usePersonalPrefixOptionsStore();

  /**
   * 신규 엔트리 저장 후 펼칠 챕터 ID.
   * 저장된 COLLAPSED 상태는 변경하지 않고, 목록·상세에 마운트된 챕터가 일회성 로컬 펼침을 적용하는 동안만 유지한다.
   */
  const entryCreatedExpandChapterId = ref<number | string | null>(null);

  /** 신규 엔트리가 들어간 챕터를 현재 화면에서 펼침 대상으로 표시한다. */
  function requestEntryCreatedChapterExpand(chapterId: number | string): void {
    entryCreatedExpandChapterId.value = chapterId;
  }

  /** 같은 저장 요청이 표시한 챕터만 해제하여 뒤이은 요청을 지우지 않는다. */
  function clearEntryCreatedChapterExpand(chapterId: number | string): void {
    if (String(entryCreatedExpandChapterId.value) === String(chapterId)) {
      entryCreatedExpandChapterId.value = null;
    }
  }

  // ---- 일자 등록/수정 모달 ----

  /** 등록/수정 모달 오픈 여부 */
  const dayRegistOpen = ref(false);
  /** 등록/수정 폼 모델 */
  const dayRegistModel = ref<JournalDayRegistModel | null>(null);

  /**
   * 일자 등록/수정 모달을 연다.
   * 수정(id 있음) 시 Tagify 초기값(tagListStr/metaListStr)을 API 상세에서 채운다.
   * @param payload - 수정 시 기존 데이터, 신규 시 날짜 등 초기값
   */
  async function openDayRegist(payload?: JournalDayRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    let merged: JournalDayRegistModel = {
      journalDatePrecision: "EXACT",
      diaryResolvedYn: "N",
      dreamResolvedYn: "N",
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
            diaryResolvedYn: dto.diaryResolvedYn ?? merged.diaryResolvedYn,
            dreamResolvedYn: dto.dreamResolvedYn ?? merged.dreamResolvedYn,
            tag: { tagListStr: tagCmpstn?.tagListStrWithCtgr ?? tagCmpstn?.tagListStr ?? "" },
            meta: { metaListStr: metaCmpstn?.metaListStr ?? "" },
          };
        }
      } catch (e: unknown) {
        console.error("[journalModal] openDayRegist 상세 조회 실패 id=", payload.id, e);
        void swalRequestError(e, t("journal.day.modify.load.failure"));
        return;
      }
    }
    dayRegistModel.value = merged;
    /** 앱 세션 categoryMap — preload 미적재 시에만 HTTP */
    await Promise.all([
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_TAG),
      ensureCategoryMap(CATEGORY_MAP_URL_DAY_META),
    ]);
    dayRegistOpen.value = true;
  }

  /** 일자 등록/수정 모달을 닫는다. */
  function closeDayRegist() {
    dayRegistOpen.value = false;
  }

  // ---- 일자 상세 모달 ----

  /** 상세 모달 오픈 여부 */
  const dayDetailOpen = ref(false);
  /** 상세 모달 로딩 여부 */
  const dayDetailLoading = ref(false);
  /** 상세 모달 데이터 */
  const dayDetailData = ref<JournalDayDto | null>(null);

  /**
   * 일자 상세 모달을 연다. API에서 상세 데이터를 조회한다.
   * @param id - 저널 일자 ID
   */
  async function openDayDetail(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    dayDetailData.value = null;
    dayDetailOpen.value = true;
    dayDetailLoading.value = true;
    try {
      const res = await axios.get(`/api/journal/day/${id}`);
      dayDetailData.value = res.data?.rsltObj ?? null;
    } catch (e: unknown) {
      console.error("[journalModal] openDayDetail failed", { id }, e);
      dayDetailData.value = null;
      dayDetailOpen.value = false;
      void swalRequestError(e, t("journal.day.detail.load.failure"));
    } finally {
      dayDetailLoading.value = false;
    }
  }

  /** 일자 상세 모달을 닫는다. */
  function closeDayDetail() {
    dayDetailOpen.value = false;
  }

  // ---- 챕터 등록/수정 모달 ----

  /** 챕터 등록/수정 모달 오픈 여부 */
  const chapterRegistOpen = ref(false);
  /** 챕터 등록/수정 폼 모델 */
  const chapterRegistModel = ref<JournalChapterRegistModel | null>(null);
  /**
   * 챕터 유형을 챕터 말머리 목록의 개인 content_type 으로 변환한다.
   * 챕터의 attachable 정체성은 JOURNAL_CHAPTER 로 불변이지만, 말머리 목록은 일기 챕터
   * (JOURNAL_CHAPTER_DIARY)와 노트 챕터(JOURNAL_CHAPTER_NOTE)가 각각 사용자 정의로 분리된다
   * (백엔드 JournalChapterService.resolveChapterPrefixScopeContentType 과 동일 계약).
   * DREAM 등 사용자 말머리가 없는 유형은 null 을 반환한다.
   * @param chapterType 챕터 유형(DIARY | NOTE | ...)
   * @return 말머리 목록 content_type 또는 null
   */
  function resolveChapterPrefixContentType(chapterType: string | null | undefined): string | null {
    if (chapterType === "DIARY") return "JOURNAL_CHAPTER_DIARY";
    if (chapterType === "NOTE") return "JOURNAL_CHAPTER_NOTE";
    return null;
  }

  /**
   * 지정한 챕터 유형 범위의 활성 개인 말머리 선택지를 반환한다.
   * 변경 전: DIARY·NOTE 가 JOURNAL_CHAPTER 한 목록을 공유했다.
   * 변경 후: 챕터 유형별 목록을 읽고, 사용자 말머리가 없는 유형(DREAM 등)은 빈 목록을 반환한다.
   * @param chapterType 챕터 유형(생략·미대응 유형이면 빈 목록)
   */
  function chapterPrefixOptionsFor(chapterType?: string | null): JournalPrefixDto[] {
    const contentType = resolveChapterPrefixContentType(chapterType);
    if (!contentType) return [];
    return personalPrefixOptionsStore.optionsFor(contentType) as JournalPrefixDto[];
  }
  /**
   * 지정한 챕터 유형 말머리 선택지 조회 실패 여부.
   * @param chapterType 챕터 유형(미대응 유형이면 false)
   */
  function chapterPrefixLoadFailedFor(chapterType?: string | null): boolean {
    const contentType = resolveChapterPrefixContentType(chapterType);
    return contentType ? personalPrefixOptionsStore.hasFailed(contentType) : false;
  }

  /**
   * 챕터 개인 말머리 옵션을 조회한다.
   * 화면 마운트 시점에도 호출하여 콘텐츠 타입 공통 캐시에 넣어 두면 모달 오픈 시 로딩 없이 사용 가능하다.
   * 관리 화면 변경으로 무효화된 경우 다음 호출이 서버 확정 목록을 다시 조회한다.
   * @param chapterType 조회할 챕터 유형. 생략하면 사용자 말머리를 갖는 모든 유형(DIARY·NOTE)을 워밍한다.
   */
  async function prefetchChapterPrefixes(chapterType?: string | null): Promise<void> {
    if (chapterType == null) {
      await Promise.all([
        personalPrefixOptionsStore.fetchOptions("JOURNAL_CHAPTER_DIARY"),
        personalPrefixOptionsStore.fetchOptions("JOURNAL_CHAPTER_NOTE"),
      ]);
      return;
    }
    const contentType = resolveChapterPrefixContentType(chapterType);
    if (!contentType) return;
    await personalPrefixOptionsStore.fetchOptions(contentType);
  }

  /** 등록/수정 폼에서 챕터 유형을 바꾸면 해당 유형의 말머리 목록을 미리 적재한다. */
  watch(
    () => chapterRegistModel.value?.chapterType,
    (chapterType) => {
      if (!chapterRegistOpen.value) return;
      void prefetchChapterPrefixes(chapterType);
    },
  );

  /**
   * 챕터 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 챕터 데이터, 신규 시 journalDayId 등 초기값
   */
  async function openChapterRegist(payload?: JournalChapterRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    chapterRegistModel.value = {
      chapterType: "DIARY",
      prefixId: null,
      summaryYn: "N",
      title: "",
      ...payload,
    };
    chapterRegistOpen.value = true;
    void prefetchChapterPrefixes();
  }

  /** 챕터 등록/수정 모달을 닫는다. */
  function closeChapterRegist() {
    chapterRegistOpen.value = false;
  }


  // ---- Reflection 등록/수정 모달 (entry 경로) ----

  const reflectionRegistOpen = ref(false);
  /** Reflection 등록/수정 폼 모델 */
  const reflectionRegistModel = ref<JournalReflectionRegistModel | null>(null);

  /**
   * Reflection 등록/수정 모달을 연다. Reflection 은 Entry 이므로 entry 상세/등록 경로를 쓴다.
   * 수정(id 있음)은 entry 상세 API로 실제 저장값을 폼 모델로 사용한다.
   * 신규는 target 묶기(refId/refContentType) 또는 챕터 직속(journalChapterId 만) 모두 가능하다.
   *
   * @param payload 신규 시 target·chapter 초기값(독립이면 journalChapterId만), 수정 시 id
   */
  async function openReflectionRegist(payload?: JournalReflectionRegistModel): Promise<void> {
    if (!await assertAuthenticatedBeforeModal()) return;
    /* payload 의 refId/refContentType 을 categoryMap 로드 전에 심어 딸린/독립 판정이 빗나가지 않게 한다. */
    let merged: JournalReflectionRegistModel = {
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      ...payload,
    };
    reflectionRegistModel.value = merged;
    if (payload?.id) {
      try {
        const res = await axios.get(`/api/journal/reflection/${payload.id}`);
        const dto = res.data?.rsltObj as JournalReflectionRegistModel | undefined;
        if (!res.data?.rslt || !dto?.id) {
          console.error("[journalModal] openReflectionRegist 상세 조회 결과 없음 id=", payload.id);
          reflectionRegistModel.value = null;
          return;
        }
        const tagCmpstn = dto.tag as { tagListStrWithCtgr?: string; tagListStr?: string } | undefined;
        /* 수정: 상세의 target 이 있으면 유지. 없으면 payload(호출 의도)를 존중한다. */
        merged = {
          ...merged,
          ...dto,
          refId: dto.refId ?? payload.refId ?? merged.refId,
          refContentType: dto.refContentType ?? payload.refContentType ?? merged.refContentType,
          journalDayId: dto.journalDayId ?? merged.journalDayId,
          journalChapterId: dto.journalChapterId ?? merged.journalChapterId,
          stdrdDt: dto.stdrdDt ?? merged.stdrdDt,
          tag: { tagListStrWithCtgr: tagCmpstn?.tagListStrWithCtgr ?? tagCmpstn?.tagListStr ?? "" },
        };
        reflectionRegistModel.value = merged;
      } catch (e: unknown) {
        console.error("[journalModal] openReflectionRegist 상세 조회 실패 id=", payload.id, e);
        reflectionRegistModel.value = null;
        return;
      }
    }
    reflectionRegistOpen.value = true;
  }




  /** Reflection 등록/수정 모달을 닫는다. 다음 오픈 시 이전 target/태그 상태가 남지 않게 모델을 비운다. */
  function closeReflectionRegist() {
    reflectionRegistOpen.value = false;
    reflectionRegistModel.value = null;
  }

  // ---- 일자 필터 모달 (메타/태그 다중 AND 검색) ----

  /** 일자 필터 모달 오픈 여부 */
  const filterModalOpen = ref(false);
  /** 일자 필터 모달 로딩 여부 */
  const filterModalLoading = ref(false);
  /** 일자 필터 모달 페이로드 */
  const filterModalPayload = ref<JournalDayFilterPayload | null>(null);

  /**
   * 일자 필터 모달을 연다. 메타 또는 태그를 시드로 연도별 일자 목록을 조회한다.
   * @param seed - 시드 타입·ID·이름 (meta 또는 tag)
   * @param yy - 조회 연도 (없으면 현재 연도; 태그 시드에서 "" 이면 전체 연도)
   */
  async function openDayFilterModal(
    seed: { type: DayFilterSeedType; id: number | string; name: string; ctgr?: string },
    yy?: string
  ): Promise<void> {
    if (!await assertAuthenticatedBeforeModal()) return;
    let seedId: string;
    try {
      seedId = requireApiPathSegment(seed.id, "filter seed id");
    } catch (e: unknown) {
      console.error("[journalModal] openDayFilterModal rejected invalid seed id", { seed, yy }, e);
      void swalRequestError(e, t("journal.day.filter.load.failure"));
      return;
    }
    filterModalOpen.value = true;
    filterModalLoading.value = true;
    filterModalPayload.value = null;
    try {
      const yearsUrl = seed.type === "meta"
        ? `/api/journal/day/metas/${seedId}/years`
        : `/api/journal/day/tag/${seedId}/years`;
      const yearsRes = await axios.get(yearsUrl);
      const yyList: string[] = (yearsRes.data?.rsltList ?? []).map(String);
      /** 태그 시드에서 yy === "" 이면 전체 연도 조회 */
      const isAllYears = seed.type === "tag" && yy === "";
      const currentYy = isAllYears ? "" : (yy ?? String(new Date().getFullYear()));
      const selectedYy = isAllYears ? "" : (
        yyList.length > 0 ? (yyList.includes(currentYy) ? currentYy : yyList[0]) : currentYy
      );
      const dayParams: Record<string, unknown> = { viewType: "SEARCH" };
      if (seed.type === "meta") dayParams.metaId = seedId;
      else dayParams.tagId = seedId;
      if (selectedYy) dayParams.yy = selectedYy;
      const daysRes = await axios.get("/api/journal/days", { params: dayParams });
      const yearOptions: Array<{ value: string | number; label: string; selected?: boolean }> =
        seed.type === "tag"
          ? [
              { value: "", label: "" },
              ...yyList.map((y) => ({ value: y, label: y, selected: y === selectedYy })),
            ]
          : yyList.map((y) => ({ value: y, label: y, selected: y === selectedYy }));
      filterModalPayload.value = {
        seedType: seed.type,
        seedId,
        seedName: seed.name,
        seedCtgr: seed.ctgr,
        yy: selectedYy,
        yearOptions,
        list: daysRes.data?.rsltList ?? [],
      };
    } catch (e: unknown) {
      console.error("[journalModal] openDayFilterModal failed", { tagId: seed.id, yy }, e);
      filterModalPayload.value = null;
      filterModalOpen.value = false;
      void swalRequestError(e, t("journal.day.filter.load.failure"));
    } finally {
      filterModalLoading.value = false;
    }
  }

  /** 일자 필터 모달을 닫는다. */
  function closeDayFilterModal(): void {
    filterModalOpen.value = false;
  }

  // ---- 메타 프로필 모달 (메타 VIEW 컨텍스트 메뉴) ----

  /** 메타 프로필 모달 오픈 여부 */
  const metaProfileOpen = ref(false);
  /** 메타 프로필 모달 로딩 여부 */
  const metaProfileLoading = ref(false);
  /** 메타 프로필 조회 결과 */
  const metaProfileModel = ref<MetaDto | null>(null);

  /**
   * 메타 프로필 모달을 연다.
   * GET /api/journal/day/metas/{id}
   */
  async function openMetaProfile(seed: {
    id: number | string;
    name?: string;
    ctgr?: string;
    unit?: string;
    contentSize?: number;
  }): Promise<void> {
    if (!await assertAuthenticatedBeforeModal()) return;
    metaProfileOpen.value = true;
    metaProfileLoading.value = true;
    const seedModel: MetaDto = {
      id: Number(seed.id),
      name: seed.name,
      ctgr: seed.ctgr,
      unit: seed.unit,
      contentSize: seed.contentSize,
    };
    metaProfileModel.value = seedModel;
    try {
      const res = await axios.get(`/api/journal/day/metas/${seed.id}`);
      const fetched = res.data?.rsltObj as MetaDto | undefined;
      if (fetched) {
        metaProfileModel.value = {
          ...seedModel,
          ...fetched,
          contentSize: fetched.contentSize ?? seedModel.contentSize ?? 0,
          unit: fetched.unit || seedModel.unit || "",
        };
      }
    } catch (e: unknown) {
      console.error("[journalModal] openMetaProfile failed", { metaId: seed.id }, e);
    } finally {
      metaProfileLoading.value = false;
    }
  }

  /** 메타 프로필 모달을 닫는다. */
  function closeMetaProfile(): void {
    metaProfileOpen.value = false;
  }

  // ---- 할일 등록/수정 모달 ----

  /** 할일 등록/수정 모달 오픈 여부 */
  const todoRegistOpen = ref(false);
  /** 할일 등록/수정 폼 모델 */
  const todoRegistModel = ref<JournalTodoRegistModel | null>(null);

  /**
   * 할일 등록/수정 모달을 연다.
   * @param payload - 수정 시 기존 데이터, 신규 시 yy/mnth 등 초기값
   */
  async function openTodoRegist(payload?: JournalTodoRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    todoRegistModel.value = {
      categoryCode: "",
      title: "",
      content: "",
      tag: { tagListStrWithCtgr: "" },
      ...payload,
    };
    todoRegistOpen.value = true;
  }

  /** 할일 등록/수정 모달을 닫는다. */
  function closeTodoRegist() {
    todoRegistOpen.value = false;
  }

  // ---- 엔트리(일기/꿈/노트) 등록/수정 모달 ----

  /** 엔트리 등록/수정 모달 오픈 여부 */
  const entryRegistOpen = ref(false);
  /** 엔트리 등록/수정 로딩 여부 */
  const entryRegistLoading = ref(false);
  /** 엔트리 등록/수정 폼 모델 */
  const entryRegistModel = ref<JournalEntryRegistModel | null>(null);
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
  /**
   * 엔트리 유형별 활성 개인 Prefix 선택지는 콘텐츠 타입 공통 캐시에서 읽는다.
   * 빈 목록 캐시·실패 재시도·동시 요청 공유·이전 사용자 응답 폐기도 공통 store가 담당한다.
   */
  /** 엔트리 모달용 뷰 (contentType 에 따라 diary/dream map 참조) */
  const entryCategoryMap = computed((): Record<string, string[]> | null => {
    const ct = entryRegistModel.value?.contentType;
    if (!ct) return null;
    if (isDreamEntry(ct)) return entryDreamCategoryMap.value;
    if (isDiaryLikeEntry(ct)) return entryDiaryCategoryMap.value;
    return null;
  });
  let dreamEntryRegistOpening = false;

  /** 엔트리 유형별 활성 Prefix 선택지를 반환한다. */
  function entryPrefixOptionsFor(contentType?: string): JournalPrefixDto[] {
    if (!contentType) return [];
    return personalPrefixOptionsStore.optionsFor(contentType) as JournalPrefixDto[];
  }

  /** 엔트리 유형별 Prefix 조회 실패 여부를 반환한다. */
  function entryPrefixLoadFailedFor(contentType?: string): boolean {
    if (!contentType) return false;
    return personalPrefixOptionsStore.hasFailed(contentType);
  }

  /**
   * 엔트리 유형별 개인 Prefix 선택지를 조회한다.
   * 빈 목록도 정상 결과로 캐시하고 실패한 요청만 다음 모달 진입에서 재시도한다.
   */
  async function prefetchEntryPrefixes(contentType: string): Promise<void> {
    await personalPrefixOptionsStore.fetchOptions(contentType);
  }

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
   * 앱 세션 categoryMap 을 서버 기준으로 다시 적재한다.
   * 레거시 상단 「태그 카테고리 동기화」와 동일 목적.
   */
  async function syncAllCategoryMaps(): Promise<void> {
    resetCategoryMaps();
    await preloadAllCategoryMaps();
  }

  /**
   * 엔트리 신규 등록 모달을 연다. (DIARY/NOTE: 챕터 목록을 caller 가 전달)
   * categoryMap 과 챕터 옵션을 병렬로 조회한 뒤 모달 로딩을 해제한다.
   * @param payload - contentType, journalDayId, stdrdDt 등 초기값
   */
  async function openEntryRegist(payload: JournalEntryRegistModel) {
    if (!await assertAuthenticatedBeforeModal()) return;
    entryRegistOpen.value = true;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const model: JournalEntryRegistModel = {
        prefixId: null,
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
        hydrateEntryPrefixContext(model),
      ]);
      entryRegistModel.value = model;
    } finally {
      entryRegistLoading.value = false;
    }
  }

  /**
   * 꿈 엔트리 신규 등록 모달을 연다. dream-auto API 로 챕터를 자동 생성/조회한다.
   * @param params - journalDayId, stdrdDt, dreamerName
   */
  async function openDreamEntryRegist(params: {
    journalDayId: number;
    stdrdDt: string;
    dreamerName?: string;
  }) {
    if (dreamEntryRegistOpening) return;
    if (!await assertAuthenticatedBeforeModal()) return;
    dreamEntryRegistOpening = true;
    entryRegistOpen.value = true;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const fd = new FormData();
      fd.append("journalDayId", String(params.journalDayId));
      const [res] = await Promise.all([
        axios.post("/api/journal/chapters/dream-auto", fd, { headers: { "Content-Type": "multipart/form-data" } }),
        ensureCategoryMap(CATEGORY_MAP_URL_ENTRY_DREAM),
        prefetchEntryPrefixes("JOURNAL_DREAM"),
      ]);
      const chapter = res.data?.rsltObj;
      entryRegistModel.value = {
        contentType: "JOURNAL_DREAM",
        prefixContentType: "JOURNAL_DREAM",
        prefixId: null,
        journalDayId: params.journalDayId,
        journalChapterId: chapter?.id ?? "",
        stdrdDt: params.stdrdDt,
        elseDreamerNm: params.dreamerName?.trim() ?? "",
        title: "",
        content: "",
        tag: { tagListStrWithCtgr: "" },
        chapterList: [],
      };
    } catch (e: unknown) {
      console.error("[journalModal] openDreamEntryRegist failed", { journalDayId: params.journalDayId }, e);
      entryRegistModel.value = null;
      entryRegistOpen.value = false;
      void swalRequestError(e, t("journal.entry.dream-regist.load.failure"));
    } finally {
      entryRegistLoading.value = false;
      dreamEntryRegistOpening = false;
    }
  }

  /**
   * 엔트리 수정 모달을 연다. API 에서 기존 데이터를 조회한다.
   * @param id - 엔트리 ID
   */
  async function openEntryModify(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    entryRegistLoading.value = true;
    entryRegistModel.value = null;
    try {
      const res = await axios.get(`/api/journal/entry/${id}`);
      const entry = res.data?.rsltObj;
      if (!entry) {
        entryRegistOpen.value = false;
        return;
      }
      /* JOURNAL_REFLECTION 수정은 태그·제목 계약을 가진 Reflection 전용 모달로 보낸다. */
      if (entry.contentType === "JOURNAL_REFLECTION") {
        entryRegistOpen.value = false;
        await openReflectionRegist({
          id,
          refId: entry.refId,
          refContentType: entry.refContentType,
          journalDayId: entry.journalDayId,
          journalChapterId: entry.journalChapterId,
        });
        return;
      }
      entryRegistOpen.value = true;
      const merged: JournalEntryRegistModel = {
        ...entry,
        tag: { tagListStrWithCtgr: entry.tag?.tagListStrWithCtgr ?? "" },
        chapterList: entry.chapterList ?? [],
      };
      const showTagForType = isDiaryLikeEntry(merged.contentType) || isDreamEntry(merged.contentType);
      const categoryUrl = resolveEntryCategoryMapUrl(merged.contentType);
      await Promise.all([
        showTagForType && categoryUrl ? ensureCategoryMap(categoryUrl) : Promise.resolve(),
        hydrateEntryPrefixContext(merged),
      ]);
      entryRegistModel.value = merged;
    } catch (e: unknown) {
      console.error("[journalModal] openEntryModify failed", { entryId: id }, e);
      entryRegistModel.value = null;
      entryRegistOpen.value = false;
      void swalRequestError(e, t("journal.entry.modify.load.failure"));
    } finally {
      entryRegistLoading.value = false;
    }
  }

  function shouldLoadChapterOptions(model: JournalEntryRegistModel | null): model is JournalEntryRegistModel {
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

  /**
   * 엔트리 챕터 선택 목록에 쓸 chapterType (신규·수정 공통: 노트끼리·일기끼리).
   * 변경 전: contentType 만으로 NOTE/DIARY 분기 → NOTE 챕터의 JOURNAL_DIARY 엔트리가 DIARY 목록으로 잘못 필터됨.
   * 변경 후: journalChapterId 가 NOTE 챕터면 NOTE 목록 유지 (백엔드 JournalEntryTypeResolver 와 동일).
   */
  function resolveExpectedChapterTypeForEntry(
    contentType: string,
    journalChapterId: number | string | undefined,
    rawList: Record<string, unknown>[],
  ): "NOTE" | "DIARY" | "DREAM" {
    if (isDreamEntry(contentType)) return "DREAM";
    if (isNoteLikeEntry(contentType)) return "NOTE";
    if (journalChapterId !== undefined && journalChapterId !== null && String(journalChapterId) !== "") {
      const current = rawList.find((raw) => String(raw.id) === String(journalChapterId));
      const chapterType = String(current?.chapterType ?? "");
      if (chapterType === "NOTE") return "NOTE";
      if (chapterType === "DREAM") return "DREAM";
    }
    return "DIARY";
  }

  function normalizeChapterOptions(
    rawList: unknown,
    contentType: string,
    journalChapterId?: number | string,
  ): JournalChapterOption[] {
    if (!Array.isArray(rawList)) return [];
    const rawRecords = rawList.map((raw) => raw as Record<string, unknown>);
    const expectedChapterType = resolveExpectedChapterTypeForEntry(contentType, journalChapterId, rawRecords);
    return rawRecords
      .filter((raw) => {
        const chapterType = String(raw.chapterType ?? "");
        return chapterType === "" || chapterType === expectedChapterType;
      })
      .map((raw) => ({
        id: raw.id as number | string,
        title: String(raw.title ?? ""),
        sortOrder: raw.sortOrder as number | undefined,
        prefix: (raw.prefix as JournalPrefixDto | null | undefined) ?? null,
        prefixId: raw.prefixId == null ? null : Number(raw.prefixId),
        summaryYn: String(raw.summaryYn ?? "N"),
        chapterType: String(raw.chapterType ?? ""),
      }))
      .filter((chapter) => chapter.id !== undefined && chapter.id !== null && String(chapter.id) !== "");
  }

  async function hydrateEntryChapterOptions(model: JournalEntryRegistModel | null): Promise<void> {
    if (!shouldLoadChapterOptions(model)) return;
    try {
      const res = await axios.get(`/api/journal/day/${model.journalDayId}`);
      const day = res.data?.rsltObj ?? {};
      const rawChapterList = Array.isArray(day.chapterList) && day.chapterList.length > 0
        ? day.chapterList
        : day.journalChapterList;
      const options = normalizeChapterOptions(rawChapterList, model.contentType, model.journalChapterId);
      if (options.length === 0) return;
      model.chapterList = options;
      const chapterStillValid = model.journalChapterId
        && options.some((chapter) => String(chapter.id) === String(model.journalChapterId));
      if (!chapterStillValid) {
        console.warn(
          "[journalModal] 엔트리 챕터 ID가 선택 목록에 없어 첫 챕터로 보정",
          { journalChapterId: model.journalChapterId, contentType: model.contentType, fallbackChapterId: options[0].id },
        );
        model.journalChapterId = options[0].id;
      }
    } catch {
      console.error("[journalModal] 엔트리 챕터 옵션 조회 실패", model.journalDayId);
    }
  }

  /**
   * 엔트리가 사용할 개인 Prefix 목록 키를 소속 챕터 유형으로 결정한다.
   * NOTE 엔트리는 영속 contentType이 JOURNAL_DIARY여도 JOURNAL_NOTE를 반환한다.
   */
  function resolveEntryPrefixContentType(
    model: JournalEntryRegistModel,
  ): "JOURNAL_DIARY" | "JOURNAL_DREAM" | "JOURNAL_NOTE" {
    if (model.prefixContentType) return model.prefixContentType;
    if (isDreamEntry(model.contentType)) return "JOURNAL_DREAM";
    if (isNoteLikeEntry(model.contentType)) return "JOURNAL_NOTE";
    const chapter = model.chapterList?.find(
      (option) => String(option.id) === String(model.journalChapterId ?? ""),
    );
    if (chapter?.chapterType === "NOTE") return "JOURNAL_NOTE";
    if (chapter?.chapterType === "DREAM") return "JOURNAL_DREAM";
    return "JOURNAL_DIARY";
  }

  /** 챕터 선택지와 그 챕터 유형의 개인 Prefix 선택지를 순서대로 적재한다. */
  async function hydrateEntryPrefixContext(model: JournalEntryRegistModel): Promise<void> {
    await hydrateEntryChapterOptions(model);
    const contentType = resolveEntryPrefixContentType(model);
    model.prefixContentType = contentType;
    await prefetchEntryPrefixes(contentType);
  }

  /** 엔트리 등록/수정 모달을 닫는다. */
  function closeEntryRegist() {
    entryRegistOpen.value = false;
    entryRegistModel.value = null;
  }

  // ---- 엔트리 읽기 전용 뷰 모달 (채팅 RAG 원문 등) ----

  /** 엔트리 읽기 전용 모달 오픈 여부 */
  const entryViewOpen = ref(false);
  /** 엔트리 읽기 전용 로딩 여부 */
  const entryViewLoading = ref(false);
  /** 엔트리 읽기 전용 표시 모델 */
  const entryViewModel = ref<JournalEntryDto | null>(null);

  /**
   * 엔트리 읽기 전용 모달을 연다. 수정 폼이 아니라 목록과 동일한 markdownContent 본문을 보여 준다.
   * @param id - 엔트리 ID
   */
  async function openEntryView(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    closeEntryRegist();
    entryViewOpen.value = true;
    entryViewLoading.value = true;
    entryViewModel.value = null;
    try {
      const res = await axios.get(`/api/journal/entry/${id}`);
      const entry = res.data?.rsltObj as JournalEntryDto | undefined;
      if (!entry) {
        entryViewOpen.value = false;
        return;
      }
      entryViewModel.value = entry;
    } catch (e: unknown) {
      console.error("[journalModal] openEntryView failed", { entryId: id }, e);
      entryViewModel.value = null;
      entryViewOpen.value = false;
      void swalRequestError(e, t("journal.entry.view.load.failure"));
    } finally {
      entryViewLoading.value = false;
    }
  }

  /** 엔트리 읽기 전용 모달을 닫는다. */
  function closeEntryView() {
    entryViewOpen.value = false;
    entryViewModel.value = null;
  }

  /**
   * 읽기 전용 모달에서 수정 모달로 전환한다.
   * JOURNAL_REFLECTION 은 Reflection 전용 모달로, 그 외는 openEntryModify 경로를 쓴다.
   */
  async function openEntryModifyFromView() {
    const id = entryViewModel.value?.id;
    const contentType = entryViewModel.value?.contentType;
    const refId = entryViewModel.value?.refId;
    const refContentType = entryViewModel.value?.refContentType;
    const journalDayId = entryViewModel.value?.journalDayId;
    const journalChapterId = entryViewModel.value?.journalChapterId;
    closeEntryView();
    if (id == null) return;
    if (contentType === "JOURNAL_REFLECTION") {
      await openReflectionRegist({ id, refId, refContentType, journalDayId, journalChapterId });
      return;
    }
    await openEntryModify(id);
  }

  /**
   * 저장 성공 응답 rsltMap 으로 앱 세션 categoryMap 을 교체한다.
   * 변경 전: Tagify JSON 병합만(삭제 미반영).
   * 변경 후: 서버가 evict 후 조회한 전역 map — 추가 GET 없이 삭제 포함 동기화.
   */

  /** 로그아웃·세션 만료 시 앱 categoryMap과 개인 Prefix 캐시를 비운다 (다음 사용자 preload 용). */
  function resetCategoryMaps(): void {
    dayTagCategoryMap.value = {};
    dayTagCategoryMapLoaded.value = false;
    dayMetaCategoryMap.value = {};
    dayMetaCategoryMapLoaded.value = false;
    entryDiaryCategoryMap.value = {};
    entryDiaryCategoryMapLoaded.value = false;
    entryDreamCategoryMap.value = {};
    entryDreamCategoryMapLoaded.value = false;
    personalPrefixOptionsStore.resetAll();
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
    dayRegistOpen,
    dayRegistModel,
    openDayRegist,
    closeDayRegist,
    // 일자 상세
    dayDetailOpen,
    dayDetailLoading,
    dayDetailData,
    openDayDetail,
    closeDayDetail,
    // 챕터 등록/수정
    chapterRegistOpen,
    chapterRegistModel,
    chapterPrefixOptionsFor,
    chapterPrefixLoadFailedFor,
    prefetchChapterPrefixes,
    openChapterRegist,
    closeChapterRegist,
    reflectionRegistOpen,
    reflectionRegistModel,
    openReflectionRegist,
    closeReflectionRegist,
    // 일자 필터 모달 (메타/태그 다중 AND 검색)
    filterModalOpen,
    filterModalLoading,
    filterModalPayload,
    openDayFilterModal,
    closeDayFilterModal,
    metaProfileOpen,
    metaProfileLoading,
    metaProfileModel,
    openMetaProfile,
    closeMetaProfile,
    // 할일 등록/수정
    todoRegistOpen,
    todoRegistModel,
    openTodoRegist,
    closeTodoRegist,
    // 엔트리 등록/수정
    entryRegistOpen,
    entryRegistLoading,
    entryRegistModel,
    entryPrefixOptionsFor,
    entryPrefixLoadFailedFor,
    prefetchEntryPrefixes,
    entryCreatedExpandChapterId,
    dayTagCategoryMap,
    dayMetaCategoryMap,
    entryCategoryMap,
    entryDiaryCategoryMap,
    openEntryRegist,
    openDreamEntryRegist,
    openEntryModify,
    closeEntryRegist,
    requestEntryCreatedChapterExpand,
    clearEntryCreatedChapterExpand,
    // 엔트리 읽기 전용
    entryViewOpen,
    entryViewLoading,
    entryViewModel,
    openEntryView,
    closeEntryView,
    openEntryModifyFromView,
    preloadAllCategoryMaps,
    syncAllCategoryMaps,
    applyCategoryMapsFromSaveResponse,
    resetCategoryMaps,
  };
});
