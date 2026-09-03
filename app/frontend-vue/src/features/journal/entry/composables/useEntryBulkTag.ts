import { computed, ref, type Ref } from "vue";
import axios from "axios";
import { swalAlert, swalConfirm, swalFire, swalRequestError } from "@/shared/utils/swal";
import type { JournalEntryDto } from "@/features/journal/stores/journal";

/** 검색 태그 카탈로그 항목. id|tagId·name·ctgr(카테고리)로 태그를 식별한다. */
export interface SearchTagDto {
  id?: number | string;
  tagId?: number | string;
  name?: string;
  ctgr?: string;
}

/** useEntryBulkTag 가 검색 페이지(JournalEntrySearchPage)로부터 주입받는 의존. */
export interface EntryBulkTagDeps {
  /** i18n 번역 함수. */
  t: (key: string) => string;
  /** 현재 검색 결과 엔트리 목록. 전체 선택·적용 대상 판정에 쓴다. */
  entries: Ref<JournalEntryDto[]>;
  /** 검색 유형 축("DIARY" | "DREAM"). 일괄 API contentType 결정에 쓴다. */
  type: Ref<string>;
  /** 태그 카탈로그(이름·카테고리 → tagId 해석). */
  tagCatalog: Ref<SearchTagDto[]>;
  /** 태그 이름 → 카테고리 목록. 다중 카테고리 판정에 쓴다. */
  tagCategoryMap: Ref<Record<string, string[]>>;
  /** tagId → 카테고리 라벨 캐시. 일괄 태그 배지 카테고리 표시에 쓴다. */
  tagCategoryLabelMap: Ref<Record<string, string>>;
  /** 태그 셀렉터 데이터(카탈로그·카테고리맵)를 보장 로드한다. */
  ensureTagSelectorData: () => Promise<void>;
  /** 입력 문자열을 알려진 태그 이름으로 정규화한다(없으면 빈 문자열). */
  findKnownTagName: (input: string) => string;
  /** tagId 표시명을 로컬 캐시에 채운다. */
  cacheTagName: (tagId?: number | string, name?: string) => void;
  /** tagId 카테고리를 로컬 캐시에 채운다. */
  cacheTagCategory: (tagId?: number | string, ctgr?: string) => void;
  /** 일괄 적용/Undo 성공 후 현재 검색 조건으로 결과를 재조회한다. */
  reloadEntries: () => Promise<void>;
}

/**
 * 일괄 태그 (검색 결과에서 선택한 엔트리에 기존 태그 ADD/REMOVE).
 * <pre>
 *  선택 상태·일괄 태그 목록·카테고리 확정·적용/Undo 를 관리한다.
 *  화면 DOM/템플릿은 JournalEntrySearchPage 에 그대로 두고, 이 컴포저블은 상태·동작만 제공한다.
 * </pre>
 *
 * @param deps 검색 페이지가 주입하는 의존 {@link EntryBulkTagDeps}
 */
export function useEntryBulkTag(deps: EntryBulkTagDeps) {
  const {
    t,
    entries,
    type,
    tagCatalog,
    tagCategoryMap,
    tagCategoryLabelMap,
    ensureTagSelectorData,
    findKnownTagName,
    cacheTagName,
    cacheTagCategory,
    reloadEntries,
  } = deps;
  // ===== 일괄 태그 (검색 결과에서 선택한 엔트리에 기존 태그 ADD/REMOVE) =====
  /** 선택된 엔트리 ID 집합. 검색 재조회·조건 변경과 무관하게 사용자가 명시적으로 고른다. */
  const selectedEntryIds = ref<Set<number>>(new Set());
  /** 일괄 작업에 적용할 태그 ID 목록(검색 조건 tagIds 와 별개). */
  const bulkTagIds = ref<number[]>([]);
  /** 일괄 태그 입력창(기존 태그 자동완성 재사용). */
  const bulkTagInput = ref("");
  /** 일괄 태그 입력에서 카테고리 확정을 대기 중인 태그 이름(다중 카테고리일 때만 세팅). */
  const bulkPendingTagName = ref("");
  /** 일괄 태그 입력에서 선택 가능한 카테고리 목록(2개 이상일 때만 선택 UI 표시). */
  const bulkTagCategoryChoices = ref<string[]>([]);
  /** 일괄 API 진행 중 플래그(중복 제출 방지). */
  const bulkActionInProgress = ref(false);
  /** 되돌리기 대상 = 같은 검색 화면 세션에서 마지막으로 성공한 일괄 작업 1건(메모리 전용). */
  interface LastBulkAction { operation: string; contentType: string; pairs: { entryId: number; tagId: number }[]; }
  const lastBulkAction = ref<LastBulkAction | null>(null);

  const selectedCount = computed(() => selectedEntryIds.value.size);
  const allSelected = computed(() => entries.value.length > 0
    && entries.value.every((e) => e.id != null && selectedEntryIds.value.has(Number(e.id))));
  const canApplyBulk = computed(() => selectedEntryIds.value.size > 0
    && bulkTagIds.value.length > 0 && !bulkActionInProgress.value);
  /** 일괄 태그 입력이 카테고리 선택 대기 중인지 여부(선택 중엔 입력·추가 버튼 잠금). */
  const isBulkTagCategoryChoicePending = computed(() => bulkTagCategoryChoices.value.length > 0);

  /** 엔트리가 선택되었는지 여부. */
  function isEntrySelected(id: number | string | undefined): boolean {
    return id != null && selectedEntryIds.value.has(Number(id));
  }

  /** 엔트리 선택 토글. Set 반응성을 위해 새 Set 으로 재할당한다. */
  function toggleEntrySelection(id: number | string | undefined): void {
    if (id == null) return;
    const next = new Set(selectedEntryIds.value);
    const numId = Number(id);
    if (next.has(numId)) next.delete(numId); else next.add(numId);
    selectedEntryIds.value = next;
  }

  /** 현재 검색 결과 전체 선택/해제 토글. */
  function toggleSelectAll(): void {
    if (allSelected.value) {
      selectedEntryIds.value = new Set();
      return;
    }
    const next = new Set<number>();
    entries.value.forEach((e) => { if (e.id != null) next.add(Number(e.id)); });
    selectedEntryIds.value = next;
  }

  /** 일괄 태그 ID 의 표시 이름을 태그 카탈로그에서 해석한다. */
  function bulkTagLabel(tagId: number): string {
    const matched = tagCatalog.value.find((tg) => Number(tg.id ?? tg.tagId) === tagId);
    return String(matched?.name ?? tagId);
  }

  /** 일괄 태그 배지에 표시할 카테고리(ctgr). 미분류(빈 문자열)면 빈 문자열을 반환해 라벨을 숨긴다. */
  function bulkTagCategory(tagId: number): string {
    return tagCategoryLabelMap.value[String(tagId)] ?? "";
  }

  /**
   * 일괄 태그 입력의 태그 이름을 카테고리까지 확정해 일괄 태그 목록에 추가한다.
   * 검색 조건 입력(addTagFromInput)과 동일 계약: 이름이 다중 카테고리면 카테고리 선택 단계로 넘긴다.
   */
  async function addBulkTagFromInput(): Promise<void> {
    await ensureTagSelectorData();
    const tagName = findKnownTagName(bulkTagInput.value);
    const categories = tagCategoryMap.value[tagName] ?? [];
    if (!tagName || categories.length === 0) {
      void swalAlert(t("journal.entry.search.tag.select-existing"));
      return;
    }
    if (categories.length === 1) {
      addBulkTagByNameAndCategory(tagName, categories[0]);
      return;
    }
    bulkPendingTagName.value = tagName;
    bulkTagCategoryChoices.value = categories;
  }

  /** 일괄 태그: 다중 카테고리 중 하나를 골라 확정한다. */
  function selectBulkTagCategory(ctgr: string): void {
    addBulkTagByNameAndCategory(bulkPendingTagName.value, ctgr);
  }

  /** 일괄 태그: 카테고리 선택 대기 상태를 취소한다. */
  function cancelBulkTagCategoryChoice(): void {
    bulkPendingTagName.value = "";
    bulkTagCategoryChoices.value = [];
  }

  /**
   * 일괄 태그: 이름+카테고리로 tagId 를 확정해 일괄 태그 목록에 추가한다.
   * 확정 시 이름·카테고리를 캐시해 배지에 카테고리 라벨을 표시한다.
   */
  function addBulkTagByNameAndCategory(tagName: string, ctgr: string): void {
    const matched = tagCatalog.value.find((tg) =>
      String(tg.name ?? "") === tagName && String(tg.ctgr ?? "") === ctgr
    );
    const tagId = matched?.id ?? matched?.tagId;
    if (tagId == null) { void swalAlert(t("journal.entry.search.tag.not-found")); return; }
    const numId = Number(tagId);
    if (bulkTagIds.value.includes(numId)) { void swalAlert(t("journal.entry.search.tag.duplicate")); return; }
    cacheTagName(numId, tagName);
    cacheTagCategory(numId, ctgr);
    bulkTagIds.value = [...bulkTagIds.value, numId];
    bulkTagInput.value = "";
    cancelBulkTagCategoryChoice();
  }

  /** 선택한 일괄 태그를 목록에서 제거한다. */
  function removeBulkTag(tagId: number): void {
    bulkTagIds.value = bulkTagIds.value.filter((id) => id !== tagId);
  }

  /**
   * 선택 엔트리들에 일괄 태그를 추가·제거한다.
   * 확인 후 POST /api/journal/entries/tags/bulk 를 호출하고, 성공 시 결과를 알린 뒤
   * 선택·일괄 태그를 비우고 현재 검색 조건으로 결과를 재조회한다.
   */
  async function applyBulkTag(operation: "ADD" | "REMOVE"): Promise<void> {
    if (!canApplyBulk.value) return;
    const entryIds = [...selectedEntryIds.value];
    const tagIdList = [...bulkTagIds.value];
    const opLabel = operation === "ADD"
      ? t("journal.entry.bulk-tag.op.add")
      : t("journal.entry.bulk-tag.op.remove");
    const confirmText = t("journal.entry.bulk-tag.confirm")
      .replace("{op}", opLabel)
      .replace("{entries}", String(entryIds.length))
      .replace("{tags}", String(tagIdList.length));
    if (!await swalConfirm(confirmText)) return;

    bulkActionInProgress.value = true;
    try {
      const res = await axios.post("/api/journal/entries/tags/bulk", {
        operation,
        contentType: type.value === "DREAM" ? "JOURNAL_DREAM" : "JOURNAL_DIARY",
        entryIds,
        tagIds: tagIdList,
      });
      const rslt = res.data?.rsltObj;
      const changedPairs = Array.isArray(rslt?.changedPairs) ? rslt.changedPairs : [];
      // 마지막 성공 작업의 실제 변경분만 Undo 대상으로 메모리에 둔다(변경이 없으면 폐기).
      lastBulkAction.value = changedPairs.length > 0
        ? {
            operation,
            contentType: type.value === "DREAM" ? "JOURNAL_DREAM" : "JOURNAL_DIARY",
            pairs: changedPairs,
          }
        : null;
      void swalFire({
        icon: "success",
        text: t("journal.entry.bulk-tag.result")
          .replace("{changed}", String(rslt?.changedLinkCount ?? 0))
          .replace("{entries}", String(rslt?.affectedEntryCount ?? 0)),
      });
      selectedEntryIds.value = new Set();
      bulkTagIds.value = [];
      cancelBulkTagCategoryChoice();
      await reloadEntries();
    } catch (e: unknown) {
      void swalRequestError(e);
    } finally {
      bulkActionInProgress.value = false;
    }
  }

  /**
   * 마지막 성공한 일괄 태그 작업을 되돌린다.
   * 서버가 응답한 실제 변경 연결 쌍(pairs)만 역연산한다(원 ADD -> 제거, 원 REMOVE -> 복원).
   * 성공 시 결과를 알리고 Undo 상태를 비운 뒤 현재 검색 조건으로 재조회한다.
   */
  async function undoLastBulk(): Promise<void> {
    const action = lastBulkAction.value;
    if (action == null || bulkActionInProgress.value) return;
    bulkActionInProgress.value = true;
    try {
      const res = await axios.post("/api/journal/entries/tags/bulk/undo", {
        operation: action.operation,
        contentType: action.contentType,
        pairs: action.pairs,
      });
      const rslt = res.data?.rsltObj;
      void swalFire({
        icon: "success",
        text: t("journal.entry.bulk-tag.undo.result").replace("{reverted}", String(rslt?.changedLinkCount ?? 0)),
      });
      lastBulkAction.value = null;
      await reloadEntries();
    } catch (e: unknown) {
      void swalRequestError(e);
    } finally {
      bulkActionInProgress.value = false;
    }
  }
  return {
    selectedEntryIds,
    bulkTagIds,
    bulkTagInput,
    bulkTagCategoryChoices,
    bulkActionInProgress,
    lastBulkAction,
    selectedCount,
    allSelected,
    canApplyBulk,
    isBulkTagCategoryChoicePending,
    isEntrySelected,
    toggleEntrySelection,
    toggleSelectAll,
    bulkTagLabel,
    bulkTagCategory,
    addBulkTagFromInput,
    selectBulkTagCategory,
    cancelBulkTagCategoryChoice,
    addBulkTagByNameAndCategory,
    removeBulkTag,
    applyBulkTag,
    undoLastBulk,
  };
}
