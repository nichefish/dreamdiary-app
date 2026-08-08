import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

/**
 * journalCategoryMaps
 * 앱 세션 태그·메타 categoryMap SSOT.
 * 로그인·마운트 시 preload 1회, 모달 오픈은 미적재 시에만 HTTP, 저장 성공 시 rsltMap 교체.
 * 무효화·모달 오픈 재조회는 하지 않는다.
 */

export const CATEGORY_MAP_URL_DAY_TAG = "/api/journal/day/tag/categories";
export const CATEGORY_MAP_URL_DAY_META = "/api/journal/day/meta/categories";
export const CATEGORY_MAP_URL_ENTRY_DIARY = "/api/journal/entry/tag/categories?type=DIARY";
export const CATEGORY_MAP_URL_ENTRY_DREAM = "/api/journal/entry/tag/categories?type=DREAM";

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
      console.error("[journalCategoryMaps] categoryMap 조회 rslt=false:", url);
      return {};
    }
    const raw = res.data.rsltMap ?? res.data.rsltObj;
    return normalizeCategoryMap(raw);
  } catch {
    console.error("[journalCategoryMaps] categoryMap 조회 실패:", url);
    return {};
  }
}

/** 엔트리 contentType 에 대응하는 categoryMap URL. 태그 없는 유형은 null. */
export function resolveEntryCategoryMapUrl(contentType: string | undefined): string | null {
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
  await useJournalCategoryMapStore().preloadAll();
}

/** 태그·메타 categoryMap 을 서버에서 다시 조회한다. (레거시 태그 카테고리 동기화) */
export async function syncCategoryMaps(): Promise<void> {
  await useJournalCategoryMapStore().syncAll();
}

export const useJournalCategoryMapStore = defineStore("journalCategoryMaps", () => {
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
  async function ensure(url: string): Promise<Record<string, string[]>> {
    const mapRef = categoryMapRefForUrl(url);
    const loadedRef = categoryMapLoadedRefForUrl(url);
    if (!mapRef || !loadedRef) return {};
    if (loadedRef.value) return mapRef.value;
    mapRef.value = await fetchCategoryMap(url);
    loadedRef.value = true;
    return mapRef.value;
  }

  async function preloadAll(): Promise<void> {
    await Promise.all([
      ensure(CATEGORY_MAP_URL_DAY_TAG),
      ensure(CATEGORY_MAP_URL_DAY_META),
      ensure(CATEGORY_MAP_URL_ENTRY_DREAM),
      ensure(CATEGORY_MAP_URL_ENTRY_DIARY),
    ]);
  }

  /**
   * 앱 세션 categoryMap 을 서버 기준으로 다시 적재한다.
   * 레거시 상단 「태그 카테고리 동기화」와 동일 목적.
   */
  async function syncAll(): Promise<void> {
    reset();
    await preloadAll();
  }

  /** 로그아웃·세션 만료 시 앱 categoryMap 을 비운다 (다음 사용자 preload 용). */
  function reset(): void {
    dayTagCategoryMap.value = {};
    dayTagCategoryMapLoaded.value = false;
    dayMetaCategoryMap.value = {};
    dayMetaCategoryMapLoaded.value = false;
    entryDiaryCategoryMap.value = {};
    entryDiaryCategoryMapLoaded.value = false;
    entryDreamCategoryMap.value = {};
    entryDreamCategoryMapLoaded.value = false;
  }

  /**
   * 저장 성공 응답 rsltMap 으로 앱 세션 categoryMap 을 교체한다.
   * 서버가 evict 후 조회한 전역 map — 추가 GET 없이 삭제 포함 동기화.
   */
  function applyFromSaveResponse(
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

  /**
   * 엔트리 모달 Tagify 용 세션 map.
   * DIARY/DREAM 만 반환한다. NOTE 는 태그 UI 가 없어 null.
   */
  function mapForEntryContentType(contentType: string | undefined): Record<string, string[]> | null {
    if (!contentType) return null;
    if (contentType === "JOURNAL_DREAM" || contentType === "DREAM") {
      return entryDreamCategoryMap.value;
    }
    if (contentType === "JOURNAL_DIARY" || contentType === "DIARY") {
      return entryDiaryCategoryMap.value;
    }
    return null;
  }

  return {
    dayTagCategoryMap,
    dayMetaCategoryMap,
    entryDiaryCategoryMap,
    entryDreamCategoryMap,
    ensure,
    preloadAll,
    syncAll,
    reset,
    applyFromSaveResponse,
    mapForEntryContentType,
  };
});