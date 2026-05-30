<template>
  <!--begin::저널 일간 레이아웃 (새 창 전용 — aside/태그클라우드 없이 날짜 카드만)-->
  <div class="journal-daily-layout">
    <router-view />

    <!--begin::저널 모달 컨테이너-->
    <JournalDayRegistModal />
    <JournalDayDtlModal />
    <JournalChapterRegistModal />
    <JournalInterpretationRegistModal />
    <JournalEntryRegistModal />
    <JournalDayTagDtlModal />
    <JournalTodoRegistModal />
    <JournalDayMetaModal />
    <CommentRegistModal />
    <CommentListModal />
    <HistoryModal @success="onHistorySuccess" />
    <RelatedContentAddModal />
    <JournalTagProfileModal />
    <!--end::저널 모달 컨테이너-->
    <!--begin::태그 컨텍스트 메뉴 (전역 단일 인스턴스)-->
    <JournalTagContextMenu />
    <!--end::태그 컨텍스트 메뉴-->
  </div>
  <!--end::저널 일간 레이아웃-->
</template>

<script setup lang="ts">
import { useJournalStore } from "@/stores/journal";
import JournalDayRegistModal from "./modals/JournalDayRegistModal.vue";
import JournalDayDtlModal from "./modals/JournalDayDtlModal.vue";
import JournalChapterRegistModal from "./modals/JournalChapterRegistModal.vue";
import JournalInterpretationRegistModal from "./modals/JournalInterpretationRegistModal.vue";
import JournalEntryRegistModal from "./modals/JournalEntryRegistModal.vue";
import JournalDayTagDtlModal from "./modals/JournalDayTagDtlModal.vue";
import JournalTodoRegistModal from "./modals/JournalTodoRegistModal.vue";
import JournalDayMetaModal from "./modals/JournalDayMetaModal.vue";
import CommentRegistModal from "./modals/CommentRegistModal.vue";
import CommentListModal from "@/views/attachable/CommentListModal.vue";
import HistoryModal from "@/views/attachable/HistoryModal.vue";
import RelatedContentAddModal from "./modals/RelatedContentAddModal.vue";
import JournalTagProfileModal from "./modals/JournalTagProfileModal.vue";
import JournalTagContextMenu from "./components/JournalTagContextMenu.vue";

const journalStore = useJournalStore();

/** 이력 복원/삭제 성공 시 일지 목록을 다시 조회한다. */
function onHistorySuccess(): void {
  void journalStore.fetchDays();
}
</script>
