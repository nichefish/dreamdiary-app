import { computed, ref, type Ref } from "vue";
import axios from "axios";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import type { SearchTagDto } from "./useEntryBulkTag";

/** useSearchTagCatalog 가 검색 페이지(JournalEntrySearchPage)로부터 주입받는 의존. */
export interface SearchTagCatalogDeps {
  /** 검색 유형 축("DIARY" | "DREAM"). 태그 셀렉터·미싱 태그명 로드의 조회 타입. */
  type: Ref<string>;
  /** 현재 검색 조건 tagIds. 결과에 없는 태그명을 보강 로드하는 대상. */
  tagIds: Ref<string[]>;
}

/**
 * 태그 카탈로그·라벨·정규화 데이터층.
 * <pre>
 *  검색 태그 카탈로그(이름·카테고리)와 tagId → 표시명/카테고리 라벨 캐시를 로드·보관한다.
 *  검색 조건(tagIds) 갱신·URL 반영은 담당하지 않으며(그 흐름은 검색 페이지에 둔다),
 *  일괄 태그(useEntryBulkTag)와 검색 페이지가 공유하는 태그 데이터의 단일 소스다.
 * </pre>
 *
 * @param deps 검색 페이지가 주입하는 의존 {@link SearchTagCatalogDeps}
 */
export function useSearchTagCatalog(deps: SearchTagCatalogDeps) {
  const { type, tagIds } = deps;
  const tagCategoryMap = ref<Record<string, string[]>>({});
  const tagCatalog = ref<SearchTagDto[]>([]);
  const tagSelectorLoadedType = ref("");
  /** tagId 를 화면 표시명으로 바꾸기 위한 로컬 캐시. URL 검색 조건에는 tagIds 만 사용한다. */
  const tagLabelMap = ref<Record<string, string>>({});
  /**
   * tagId → 카테고리(ctgr) 로컬 캐시. 검색 조건 칩·일괄 태그 배지에 카테고리 라벨을 표시하는 데 쓴다.
   * 빈 문자열은 미분류를 의미하며, 이 경우 배지에 카테고리 라벨을 표시하지 않는다.
   */
  const tagCategoryLabelMap = ref<Record<string, string>>({});

  const tagNameOptions = computed(() => Object.keys(tagCategoryMap.value).sort((a, b) => a.localeCompare(b)));

  /** tagId 의 카테고리(ctgr)를 캐시한다. cacheTagName 과 짝을 이뤄 같은 소스에서 함께 채운다. */
  function cacheTagCategory(tagId?: number | string, ctgr?: string): void {
    if (tagId === undefined || tagId === null) return;
    tagCategoryLabelMap.value[String(tagId)] = String(ctgr ?? "");
  }

  function cacheTagName(tagId?: number | string, name?: string): void {
    if (tagId === undefined || tagId === null || !name) return;
    tagLabelMap.value[String(tagId)] = name;
  }

  function hydrateTagNamesFromEntries(entryList: JournalEntryDto[]): void {
    entryList.forEach((entry) => {
      (entry.tag?.list ?? []).forEach((tag) => {
        cacheTagName(tag.tagId, tag.name);
        cacheTagCategory(tag.tagId, tag.ctgr);
      });
    });
  }

  async function hydrateMissingTagNames(): Promise<void> {
    const missingIds = tagIds.value.filter((tagId) => !tagLabelMap.value[tagId]);
    if (missingIds.length === 0) return;

    const requestedType = type.value;
    try {
      const res = await axios.get("/api/journal/entry/tags", { params: { type: requestedType } });
      if (requestedType !== type.value) return;
      const list = (res.data?.rsltList ?? []) as SearchTagDto[];
      list.forEach((tag) => {
        cacheTagName(tag.id ?? tag.tagId, tag.name);
        cacheTagCategory(tag.id ?? tag.tagId, tag.ctgr);
      });
    } catch {
      // 태그명 표시에 실패해도 tagIds 검색 자체는 유지한다.
    }
  }

  async function ensureTagSelectorData(): Promise<void> {
    const requestedType = type.value;
    if (tagSelectorLoadedType.value === requestedType) return;

    try {
      const [categoryRes, tagRes] = await Promise.all([
        axios.get("/api/journal/entry/tag/categories", { params: { type: requestedType } }),
        axios.get("/api/journal/entry/tags", { params: { type: requestedType } }),
      ]);
      if (requestedType !== type.value) return;
      tagCatalog.value = (tagRes.data?.rsltList ?? []) as SearchTagDto[];
      tagCategoryMap.value = mergeCatalogIntoCategoryMap(
        normalizeCategoryMap(categoryRes.data?.rsltMap ?? categoryRes.data?.rsltObj),
        tagCatalog.value,
      );
      tagCatalog.value.forEach((tag) => {
        cacheTagName(tag.id ?? tag.tagId, tag.name);
        cacheTagCategory(tag.id ?? tag.tagId, tag.ctgr);
      });
      tagSelectorLoadedType.value = requestedType;
    } catch {
      console.warn("[JournalEntrySearchPage] tag selector data load failed.", { type: requestedType });
    }
  }

  function mergeCatalogIntoCategoryMap(baseMap: Record<string, string[]>, catalog: SearchTagDto[]): Record<string, string[]> {
    const next: Record<string, string[]> = {};
    for (const [tagName, categories] of Object.entries(baseMap)) {
      next[tagName] = [...categories];
    }
    catalog.forEach((tag) => {
      const name = String(tag.name ?? "").trim();
      if (!name) return;
      const ctgr = String(tag.ctgr ?? "");
      const categories = next[name] ? [...next[name]] : [];
      if (!categories.includes(ctgr)) categories.push(ctgr);
      next[name] = categories;
    });
    return next;
  }

  function normalizeCategoryMap(raw: unknown): Record<string, string[]> {
    if (!raw || typeof raw !== "object" || Array.isArray(raw)) return {};
    const out: Record<string, string[]> = {};
    for (const [tagName, categories] of Object.entries(raw as Record<string, unknown>)) {
      if (!Array.isArray(categories)) continue;
      out[tagName] = categories.map((c) => String(c ?? "")).filter((c) => c.length > 0);
    }
    return out;
  }

  function normalizeTagName(raw: string): string {
    return raw.trim().replace(/\s+/g, "_");
  }

  function findKnownTagName(input: string): string {
    const normalized = normalizeTagName(input);
    if (tagCategoryMap.value[normalized]) return normalized;
    return tagNameOptions.value.find((name) => name.toLowerCase() === normalized.toLowerCase()) ?? normalized;
  }

  return {
    tagCatalog,
    tagCategoryMap,
    tagLabelMap,
    tagCategoryLabelMap,
    tagNameOptions,
    cacheTagName,
    cacheTagCategory,
    hydrateTagNamesFromEntries,
    hydrateMissingTagNames,
    ensureTagSelectorData,
    findKnownTagName,
  };
}
