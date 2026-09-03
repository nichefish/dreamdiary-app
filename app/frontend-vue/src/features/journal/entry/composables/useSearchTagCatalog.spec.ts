import { beforeEach, describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import axios from "axios";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { useSearchTagCatalog } from "./useSearchTagCatalog";

vi.mock("axios", () => ({
  default: { get: vi.fn() },
}));

// 가상 픽스처 (개인정보 금지 §9)
const FIXTURE_TAG_NAME = "민수";   // 다중 카테고리
const FIXTURE_CTGR_A = "인물";
const FIXTURE_CTGR_B = "장소";
const FIXTURE_TAG_SINGLE = "회의"; // 미분류

function catalogResponses() {
  vi.mocked(axios.get).mockImplementation((url: string) => {
    if (url.includes("categories")) {
      return Promise.resolve({ data: { rsltMap: { [FIXTURE_TAG_NAME]: [FIXTURE_CTGR_A, FIXTURE_CTGR_B] } } });
    }
    return Promise.resolve({
      data: {
        rsltList: [
          { id: 10, name: FIXTURE_TAG_NAME, ctgr: FIXTURE_CTGR_A },
          { id: 11, name: FIXTURE_TAG_NAME, ctgr: FIXTURE_CTGR_B },
          { id: 12, name: FIXTURE_TAG_SINGLE, ctgr: "" },
        ],
      },
    });
  });
}

function make(tagIds: string[] = []) {
  return useSearchTagCatalog({ type: ref("DIARY"), tagIds: ref(tagIds) });
}

describe("useSearchTagCatalog :: 카탈로그 로드", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("ensureTagSelectorData 는 카탈로그·카테고리맵·라벨 캐시를 채우고 tagNameOptions 를 정렬한다", async () => {
    catalogResponses();
    const cat = make();
    await cat.ensureTagSelectorData();

    expect(cat.tagCatalog.value).toHaveLength(3);
    expect(cat.tagCategoryMap.value[FIXTURE_TAG_NAME]).toEqual([FIXTURE_CTGR_A, FIXTURE_CTGR_B]);
    expect(cat.tagCategoryMap.value[FIXTURE_TAG_SINGLE]).toEqual([""]);
    expect(cat.tagNameOptions.value).toEqual([FIXTURE_TAG_NAME, FIXTURE_TAG_SINGLE].sort((a, b) => a.localeCompare(b)));
    expect(cat.tagLabelMap.value["10"]).toBe(FIXTURE_TAG_NAME);
    expect(cat.tagCategoryLabelMap.value["10"]).toBe(FIXTURE_CTGR_A);
    expect(cat.tagCategoryLabelMap.value["12"]).toBe("");
  });

  it("같은 type 재호출은 재조회하지 않는다(로드 가드)", async () => {
    catalogResponses();
    const cat = make();
    await cat.ensureTagSelectorData();
    expect(vi.mocked(axios.get)).toHaveBeenCalledTimes(2);
    await cat.ensureTagSelectorData();
    expect(vi.mocked(axios.get)).toHaveBeenCalledTimes(2);
  });
});

describe("useSearchTagCatalog :: 태그명 해석·보강", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("findKnownTagName 은 공백 정규화 후 알려진 이름을 찾고, 없으면 정규화값을 반환한다", async () => {
    catalogResponses();
    const cat = make();
    await cat.ensureTagSelectorData();

    expect(cat.findKnownTagName("  " + FIXTURE_TAG_NAME + "  ")).toBe(FIXTURE_TAG_NAME);
    expect(cat.findKnownTagName("없는 태그")).toBe("없는_태그");
  });

  it("hydrateTagNamesFromEntries 는 엔트리 태그로 라벨·카테고리 캐시를 채운다", () => {
    const cat = make();
    const entries = [
      { tag: { list: [{ tagId: 20, name: FIXTURE_TAG_SINGLE, ctgr: "" }] } },
    ] as unknown as JournalEntryDto[];
    cat.hydrateTagNamesFromEntries(entries);
    expect(cat.tagLabelMap.value["20"]).toBe(FIXTURE_TAG_SINGLE);
    expect(cat.tagCategoryLabelMap.value["20"]).toBe("");
  });

  it("hydrateMissingTagNames 는 라벨이 없는 tagIds 만 보강 조회한다", async () => {
    vi.mocked(axios.get).mockResolvedValue({
      data: { rsltList: [{ id: 30, name: FIXTURE_TAG_NAME, ctgr: FIXTURE_CTGR_A }] },
    });
    const cat = make(["30"]);
    await cat.hydrateMissingTagNames();
    expect(vi.mocked(axios.get)).toHaveBeenCalledTimes(1);
    expect(cat.tagLabelMap.value["30"]).toBe(FIXTURE_TAG_NAME);

    // 이미 라벨이 있으면 추가 조회하지 않는다
    vi.mocked(axios.get).mockClear();
    await cat.hydrateMissingTagNames();
    expect(vi.mocked(axios.get)).not.toHaveBeenCalled();
  });
});
