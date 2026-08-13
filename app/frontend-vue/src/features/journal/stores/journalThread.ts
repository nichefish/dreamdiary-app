import { defineStore } from "pinia";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";
import { createJournalThreadList } from "@/features/journal/stores/journalThreadList";
import { createJournalThreadDetail } from "@/features/journal/stores/journalThreadDetail";
import { createJournalThreadPicker } from "@/features/journal/stores/journalThreadPicker";

export type {
  ThreadTagItem,
  ThreadPrefix,
  ThreadTagCmpstn,
  JournalThreadDto,
  JournalThreadRegistModel,
  JournalPeriodThreadSummaryItem,
  JournalPeriodThreadSummaryQuery,
} from "@/features/journal/stores/journalThread.types";

// ---- 스토어 ----

export const useJournalThreadStore = defineStore("journalThread", () => {
  const { t } = useLocaleStore();
  const personalPrefixOptionsStore = usePersonalPrefixOptionsStore();

  const list = createJournalThreadList({ t, personalPrefixOptionsStore });
  const detail = createJournalThreadDetail({
    t,
    fetchList: list.fetchList,
    fetchPrefixOptions: list.fetchPrefixOptions,
    prefixOptions: list.prefixOptions,
    threadList: list.threadList,
  });
  const picker = createJournalThreadPicker({ t });

  return {
    // 목록
    threadList: list.threadList,
    totalElements: list.totalElements,
    totalPages: list.totalPages,
    currentPage: list.currentPage,
    pageSize: list.pageSize,
    loading: list.loading,
    error: list.error,
    filterKeyword: list.filterKeyword,
    filterPrefixId: list.filterPrefixId,
    filterLifecycleKey: list.filterLifecycleKey,
    filterTagIds: list.filterTagIds,
    filterTagLabelMap: list.filterTagLabelMap,
    filterTagCtgrMap: list.filterTagCtgrMap,
    prefixOptions: list.prefixOptions,
    prefixError: list.prefixError,
    periodSummary: list.periodSummary,
    periodSummaryLoading: list.periodSummaryLoading,
    periodSummaryError: list.periodSummaryError,
    fetchList: list.fetchList,
    fetchPrefixOptions: list.fetchPrefixOptions,
    ensurePrefixOptions: list.ensurePrefixOptions,
    quickAddPrefix: detail.quickAddPrefix,
    fetchPeriodSummary: list.fetchPeriodSummary,
    refreshPeriodSummary: list.refreshPeriodSummary,
    clearPeriodSummaryQuery: list.clearPeriodSummaryQuery,
    cacheFilterTagLabel: list.cacheFilterTagLabel,
    addFilterTag: list.addFilterTag,
    removeFilterTag: list.removeFilterTag,
    resetFilters: list.resetFilters,
    // 등록/수정
    registOpen: detail.registOpen,
    registSurface: detail.registSurface,
    registLoading: detail.registLoading,
    registModel: detail.registModel,
    registDirty: detail.registDirty,
    submitting: detail.submitting,
    hasSuspendedDetailEdit: detail.hasSuspendedDetailEdit,
    openRegist: detail.openRegist,
    openModifyPage: detail.openModifyPage,
    openModifyFromDetail: detail.openModifyFromDetail,
    closeRegist: detail.closeRegist,
    submitRegist: detail.submitRegist,
    setLifecycle: detail.setLifecycle,
    deleteThread: detail.deleteThread,
    // 상세 (모달/독립 페이지 공용)
    detailOpen: detail.detailOpen,
    detailSurface: detail.detailSurface,
    detailEntries: detail.detailEntries,
    detailEntriesLoading: detail.detailEntriesLoading,
    detailEntriesError: detail.detailEntriesError,
    detailLoading: detail.detailLoading,
    detailModel: detail.detailModel,
    detailIncludedRelatedThreadIds: detail.detailIncludedRelatedThreadIds,
    detailRelatedThreads: detail.detailRelatedThreads,
    detailRelatedThreadsLoading: detail.detailRelatedThreadsLoading,
    detailRelatedError: detail.detailRelatedError,
    openDetail: detail.openDetail,
    openDetailPage: detail.openDetailPage,
    closeDetail: detail.closeDetail,
    refreshOpenDetail: detail.refreshOpenDetail,
    fetchRelatedThreads: detail.fetchRelatedThreads,
    addRelatedThread: detail.addRelatedThread,
    removeRelatedThread: detail.removeRelatedThread,
    toggleRelatedThreadInclude: detail.toggleRelatedThreadInclude,
    // 스레드 피커 모달
    pickerOpen: picker.pickerOpen,
    pickerLoading: picker.pickerLoading,
    pickerSearched: picker.pickerSearched,
    pickerSearchResults: picker.pickerSearchResults,
    pickerSearchError: picker.pickerSearchError,
    openPicker: picker.openPicker,
    closePicker: picker.closePicker,
    searchThreadsForPicker: picker.searchThreadsForPicker,
  };
});
