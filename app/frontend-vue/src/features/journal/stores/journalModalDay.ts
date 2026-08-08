import { ref } from "vue";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { JournalDayDto, MetaDto } from "@/features/journal/stores/journal";
import type {
  DayFilterSeedType,
  JournalDayFilterPayload,
  JournalDayRegistModel,
} from "@/features/journal/stores/journalModal.types";
import {
  CATEGORY_MAP_URL_DAY_META,
  CATEGORY_MAP_URL_DAY_TAG,
} from "@/features/journal/stores/journalCategoryMaps";
import { formatLocalDateStr } from "@/features/journal/utils/journalDate";
import { requireApiPathSegment } from "@/shared/utils/appPath";
import { swalRequestError } from "@/shared/utils/swal";

export interface JournalModalDayDeps {
  t: (key: string) => string;
  ensureCategoryMap: (url: string) => Promise<Record<string, string[]>>;
}

/**
 * 저널 일자 관련 모달 surface.
 * 등록·상세·필터(메타/태그)·메타 프로필 open/close 상태를 관리한다.
 */
export function createJournalModalDay(deps: JournalModalDayDeps) {
  const { t, ensureCategoryMap } = deps;

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

  return {
    dayRegistOpen,
    dayRegistModel,
    openDayRegist,
    closeDayRegist,
    dayDetailOpen,
    dayDetailLoading,
    dayDetailData,
    openDayDetail,
    closeDayDetail,
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
  };
}