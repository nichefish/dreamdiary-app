import { computed } from "vue";
import { defineStore } from "pinia";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { usePersonalPrefixOptionsStore } from "@/features/attachable/stores/personalPrefixOptions";
import { useJournalCategoryMapStore } from "@/features/journal/stores/journalCategoryMaps";

export type {
  JournalDayRegistModel,
  JournalChapterRegistModel,
  JournalReflectionRegistModel,
  DayFilterSeedType,
  JournalDayFilterPayload,
  JournalTodoRegistModel,
  JournalChapterOption,
  JournalEntryRegistModel,
} from "@/features/journal/stores/journalModal.types";

export {
  preloadCategoryMaps,
  syncCategoryMaps,
} from "@/features/journal/stores/journalCategoryMaps";

import { createJournalModalOneShotSignals } from "@/features/journal/stores/journalModalOneShot";
import { createJournalModalDay } from "@/features/journal/stores/journalModalDay";
import { createJournalModalChapter } from "@/features/journal/stores/journalModalChapter";
import { createJournalModalReflection } from "@/features/journal/stores/journalModalReflection";
import { createJournalModalTodo } from "@/features/journal/stores/journalModalTodo";
import { createJournalModalEntry } from "@/features/journal/stores/journalModalEntry";

// ---- 스토어 ----

export const useJournalModalStore = defineStore("journalModal", () => {
  const { t } = useLocaleStore();
  const personalPrefixOptionsStore = usePersonalPrefixOptionsStore();
  const categoryMaps = useJournalCategoryMapStore();
  const oneShot = createJournalModalOneShotSignals();
  const day = createJournalModalDay({
    t,
    ensureCategoryMap: (url) => categoryMaps.ensure(url),
  });
  const chapter = createJournalModalChapter({ personalPrefixOptionsStore });
  const reflection = createJournalModalReflection();
  const todo = createJournalModalTodo();
  const entry = createJournalModalEntry({
    t,
    ensureCategoryMap: (url) => categoryMaps.ensure(url),
    mapForEntryContentType: (contentType) => categoryMaps.mapForEntryContentType(contentType),
    personalPrefixOptionsStore,
    openReflectionRegist: reflection.openReflectionRegist,
  });

  async function preloadAllCategoryMaps(): Promise<void> {
    await categoryMaps.preloadAll();
  }

  /**
   * 앱 세션 categoryMap 을 서버 기준으로 다시 적재한다.
   * 레거시 상단 「태그 카테고리 동기화」와 동일 목적.
   */
  async function syncAllCategoryMaps(): Promise<void> {
    await categoryMaps.syncAll();
  }

  /**
   * 저장 성공 응답 rsltMap 으로 앱 세션 categoryMap 을 교체한다.
   * 서버가 evict 후 조회한 전역 map — 추가 GET 없이 삭제 포함 동기화.
   */
  function applyCategoryMapsFromSaveResponse(
    rsltMap: unknown,
    entryContentType?: string,
  ): void {
    categoryMaps.applyFromSaveResponse(rsltMap, entryContentType);
  }

  /** 로그아웃·세션 만료 시 앱 categoryMap과 개인 Prefix 캐시를 비운다 (다음 사용자 preload 용). */
  function resetCategoryMaps(): void {
    categoryMaps.reset();
    personalPrefixOptionsStore.resetAll();
  }

  return {
    // 일자 등록/수정·상세·필터·메타 프로필
    dayRegistOpen: day.dayRegistOpen,
    dayRegistModel: day.dayRegistModel,
    openDayRegist: day.openDayRegist,
    closeDayRegist: day.closeDayRegist,
    dayDetailOpen: day.dayDetailOpen,
    dayDetailLoading: day.dayDetailLoading,
    dayDetailData: day.dayDetailData,
    openDayDetail: day.openDayDetail,
    closeDayDetail: day.closeDayDetail,
    filterModalOpen: day.filterModalOpen,
    filterModalLoading: day.filterModalLoading,
    filterModalPayload: day.filterModalPayload,
    openDayFilterModal: day.openDayFilterModal,
    closeDayFilterModal: day.closeDayFilterModal,
    metaProfileOpen: day.metaProfileOpen,
    metaProfileLoading: day.metaProfileLoading,
    metaProfileModel: day.metaProfileModel,
    openMetaProfile: day.openMetaProfile,
    closeMetaProfile: day.closeMetaProfile,
    // 챕터 등록/수정
    chapterRegistOpen: chapter.chapterRegistOpen,
    chapterRegistModel: chapter.chapterRegistModel,
    chapterPrefixOptionsFor: chapter.chapterPrefixOptionsFor,
    chapterPrefixLoadFailedFor: chapter.chapterPrefixLoadFailedFor,
    prefetchChapterPrefixes: chapter.prefetchChapterPrefixes,
    openChapterRegist: chapter.openChapterRegist,
    closeChapterRegist: chapter.closeChapterRegist,
    // Reflection 등록/수정
    reflectionRegistOpen: reflection.reflectionRegistOpen,
    reflectionRegistModel: reflection.reflectionRegistModel,
    openReflectionRegist: reflection.openReflectionRegist,
    closeReflectionRegist: reflection.closeReflectionRegist,
    // 할일 등록/수정
    todoRegistOpen: todo.todoRegistOpen,
    todoRegistModel: todo.todoRegistModel,
    openTodoRegist: todo.openTodoRegist,
    closeTodoRegist: todo.closeTodoRegist,
    // 엔트리 등록/수정·읽기 전용
    entryRegistOpen: entry.entryRegistOpen,
    entryRegistLoading: entry.entryRegistLoading,
    entryRegistModel: entry.entryRegistModel,
    entryPrefixOptionsFor: entry.entryPrefixOptionsFor,
    entryPrefixLoadFailedFor: entry.entryPrefixLoadFailedFor,
    prefetchEntryPrefixes: entry.prefetchEntryPrefixes,
    entryCreatedExpandChapterId: oneShot.entryCreatedExpandChapterId,
    reflectionCreatedCollapseId: oneShot.reflectionCreatedCollapseId,
    dayTagCategoryMap: computed(() => categoryMaps.dayTagCategoryMap),
    dayMetaCategoryMap: computed(() => categoryMaps.dayMetaCategoryMap),
    entryCategoryMap: entry.entryCategoryMap,
    entryDiaryCategoryMap: computed(() => categoryMaps.entryDiaryCategoryMap),
    openEntryRegist: entry.openEntryRegist,
    openDreamEntryRegist: entry.openDreamEntryRegist,
    openEntryModify: entry.openEntryModify,
    closeEntryRegist: entry.closeEntryRegist,
    requestEntryCreatedChapterExpand: oneShot.requestEntryCreatedChapterExpand,
    clearEntryCreatedChapterExpand: oneShot.clearEntryCreatedChapterExpand,
    requestReflectionCreatedCollapse: oneShot.requestReflectionCreatedCollapse,
    clearReflectionCreatedCollapse: oneShot.clearReflectionCreatedCollapse,
    entryViewOpen: entry.entryViewOpen,
    entryViewLoading: entry.entryViewLoading,
    entryViewModel: entry.entryViewModel,
    openEntryView: entry.openEntryView,
    closeEntryView: entry.closeEntryView,
    openEntryModifyFromView: entry.openEntryModifyFromView,
    preloadAllCategoryMaps,
    syncAllCategoryMaps,
    applyCategoryMapsFromSaveResponse,
    resetCategoryMaps,
  };
});
