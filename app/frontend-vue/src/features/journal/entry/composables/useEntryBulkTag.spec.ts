import { beforeEach, describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import axios from "axios";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { useEntryBulkTag, type SearchTagDto } from "./useEntryBulkTag";

vi.mock("axios", () => ({
  default: { post: vi.fn() },
}));

vi.mock("@/shared/utils/swal", () => ({
  swalAlert: vi.fn().mockResolvedValue(undefined),
  swalConfirm: vi.fn().mockResolvedValue(true),
  swalFire: vi.fn().mockResolvedValue(undefined),
  swalRequestError: vi.fn().mockResolvedValue(undefined),
}));

// 가상 픽스처 (개인정보 금지 §9)
const FIXTURE_TAG_NAME = "민수";      // 다중 카테고리 태그 이름
const FIXTURE_CTGR_A = "인물";        // tagId 10
const FIXTURE_CTGR_B = "장소";        // tagId 11
const FIXTURE_TAG_SINGLE = "회의";    // 단일(미분류) 태그, tagId 12

function makeDeps(overrides: Record<string, unknown> = {}) {
  const entries = ref<JournalEntryDto[]>([
    { id: 1 } as JournalEntryDto,
    { id: 2 } as JournalEntryDto,
    { id: 3 } as JournalEntryDto,
  ]);
  const tagCatalog = ref<SearchTagDto[]>([
    { id: 10, name: FIXTURE_TAG_NAME, ctgr: FIXTURE_CTGR_A },
    { id: 11, name: FIXTURE_TAG_NAME, ctgr: FIXTURE_CTGR_B },
    { id: 12, name: FIXTURE_TAG_SINGLE, ctgr: "" },
  ]);
  const tagCategoryMap = ref<Record<string, string[]>>({
    [FIXTURE_TAG_NAME]: [FIXTURE_CTGR_A, FIXTURE_CTGR_B],
    [FIXTURE_TAG_SINGLE]: [""],
  });
  return {
    t: (k: string) => k,
    entries,
    type: ref("DIARY"),
    tagCatalog,
    tagCategoryMap,
    tagCategoryLabelMap: ref<Record<string, string>>({}),
    ensureTagSelectorData: vi.fn().mockResolvedValue(undefined),
    findKnownTagName: (s: string) => s.trim(),
    cacheTagName: vi.fn(),
    cacheTagCategory: vi.fn(),
    reloadEntries: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  };
}

describe("useEntryBulkTag :: 선택 상태", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("엔트리 선택 토글·isEntrySelected·selectedCount 를 반영한다", () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    expect(bulk.isEntrySelected(1)).toBe(false);
    bulk.toggleEntrySelection(1);
    bulk.toggleEntrySelection(2);
    expect(bulk.isEntrySelected(1)).toBe(true);
    expect(bulk.selectedCount.value).toBe(2);
    bulk.toggleEntrySelection(1);
    expect(bulk.isEntrySelected(1)).toBe(false);
    expect(bulk.selectedCount.value).toBe(1);
  });

  it("toggleSelectAll 은 전체 선택/해제하며 allSelected 를 반영한다", () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    expect(bulk.allSelected.value).toBe(false);
    bulk.toggleSelectAll();
    expect(bulk.selectedCount.value).toBe(3);
    expect(bulk.allSelected.value).toBe(true);
    bulk.toggleSelectAll();
    expect(bulk.selectedCount.value).toBe(0);
  });
});

describe("useEntryBulkTag :: 일괄 태그 입력", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("단일 카테고리 태그는 입력 즉시 일괄 태그 목록에 추가된다", async () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    bulk.bulkTagInput.value = FIXTURE_TAG_SINGLE;
    await bulk.addBulkTagFromInput();
    expect(bulk.bulkTagIds.value).toEqual([12]);
    expect(bulk.isBulkTagCategoryChoicePending.value).toBe(false);
    expect(bulk.bulkTagInput.value).toBe("");
  });

  it("다중 카테고리 태그는 카테고리 선택 단계로 넘어가고 선택 시 확정된다", async () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    bulk.bulkTagInput.value = FIXTURE_TAG_NAME;
    await bulk.addBulkTagFromInput();
    expect(bulk.bulkTagIds.value).toEqual([]);
    expect(bulk.isBulkTagCategoryChoicePending.value).toBe(true);
    expect(bulk.bulkTagCategoryChoices.value).toEqual([FIXTURE_CTGR_A, FIXTURE_CTGR_B]);

    bulk.selectBulkTagCategory(FIXTURE_CTGR_A);
    expect(bulk.bulkTagIds.value).toEqual([10]);
    expect(bulk.isBulkTagCategoryChoicePending.value).toBe(false);
  });

  it("removeBulkTag 는 일괄 태그를 제거한다", async () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    bulk.bulkTagInput.value = FIXTURE_TAG_SINGLE;
    await bulk.addBulkTagFromInput();
    expect(bulk.bulkTagIds.value).toEqual([12]);
    bulk.removeBulkTag(12);
    expect(bulk.bulkTagIds.value).toEqual([]);
  });
});

describe("useEntryBulkTag :: 적용/Undo", () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it("canApplyBulk 는 선택 엔트리와 일괄 태그가 모두 있을 때만 참", async () => {
    const bulk = useEntryBulkTag(makeDeps() as never);
    expect(bulk.canApplyBulk.value).toBe(false);
    bulk.toggleEntrySelection(1);
    bulk.bulkTagInput.value = FIXTURE_TAG_SINGLE;
    await bulk.addBulkTagFromInput();
    expect(bulk.canApplyBulk.value).toBe(true);
  });

  it("applyBulkTag 는 올바른 payload 로 POST 하고 선택을 비운 뒤 재조회한다", async () => {
    const deps = makeDeps();
    vi.mocked(axios.post).mockResolvedValue({
      data: { rsltObj: { changedPairs: [{ entryId: 1, tagId: 12 }], changedLinkCount: 1, affectedEntryCount: 1 } },
    });
    const bulk = useEntryBulkTag(deps as never);
    bulk.toggleEntrySelection(1);
    bulk.toggleEntrySelection(2);
    bulk.bulkTagInput.value = FIXTURE_TAG_SINGLE;
    await bulk.addBulkTagFromInput();

    await bulk.applyBulkTag("ADD");

    expect(axios.post).toHaveBeenCalledWith("/api/journal/entries/tags/bulk", {
      operation: "ADD",
      contentType: "JOURNAL_DIARY",
      entryIds: [1, 2],
      tagIds: [12],
    });
    expect(bulk.selectedCount.value).toBe(0);
    expect(bulk.bulkTagIds.value).toEqual([]);
    expect((deps.reloadEntries as ReturnType<typeof vi.fn>)).toHaveBeenCalled();
    expect(bulk.lastBulkAction.value).toEqual({
      operation: "ADD",
      contentType: "JOURNAL_DIARY",
      pairs: [{ entryId: 1, tagId: 12 }],
    });
  });

  it("undoLastBulk 는 마지막 작업의 pairs 를 undo POST 하고 상태를 비운다", async () => {
    const deps = makeDeps();
    vi.mocked(axios.post)
      .mockResolvedValueOnce({
        data: { rsltObj: { changedPairs: [{ entryId: 1, tagId: 12 }], changedLinkCount: 1, affectedEntryCount: 1 } },
      })
      .mockResolvedValueOnce({ data: { rsltObj: { changedLinkCount: 1 } } });
    const bulk = useEntryBulkTag(deps as never);
    bulk.toggleEntrySelection(1);
    bulk.bulkTagInput.value = FIXTURE_TAG_SINGLE;
    await bulk.addBulkTagFromInput();
    await bulk.applyBulkTag("ADD");

    await bulk.undoLastBulk();

    expect(axios.post).toHaveBeenLastCalledWith("/api/journal/entries/tags/bulk/undo", {
      operation: "ADD",
      contentType: "JOURNAL_DIARY",
      pairs: [{ entryId: 1, tagId: 12 }],
    });
    expect(bulk.lastBulkAction.value).toBeNull();
  });
});
