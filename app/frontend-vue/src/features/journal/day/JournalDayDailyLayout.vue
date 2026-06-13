<template>
  <!--begin::저널 일간 레이아웃 (새 창 전용 — aside/태그클라우드 없이 날짜 카드만)-->
  <div class="journal-daily-layout">
    <router-view />

    <!--begin::저널 모달 컨테이너-->
    <JournalDayRegistModal />
    <JournalDayDetailModal />
    <JournalChapterRegistModal />
    <JournalInterpretationRegistModal />
    <JournalEntryRegistModal />
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
import { useRoute } from "vue-router";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import JournalDayRegistModal from "./modals/JournalDayRegistModal.vue";
import JournalDayDetailModal from "./modals/JournalDayDetailModal.vue";
import JournalChapterRegistModal from "../chapter/modals/JournalChapterRegistModal.vue";
import JournalInterpretationRegistModal from "../interpretation/modals/JournalInterpretationRegistModal.vue";
import JournalEntryRegistModal from "../entry/modals/JournalEntryRegistModal.vue";
import JournalTodoRegistModal from "../todo/modals/JournalTodoRegistModal.vue";
import JournalDayMetaModal from "./modals/JournalDayMetaModal.vue";
import CommentRegistModal from "../shared/modals/CommentRegistModal.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
import HistoryModal from "@/features/attachable/HistoryModal.vue";
import RelatedContentAddModal from "../shared/modals/RelatedContentAddModal.vue";
import JournalTagProfileModal from "../shared/modals/JournalTagProfileModal.vue";
import JournalTagContextMenu from "../shared/components/JournalTagContextMenu.vue";

const journalStore = useJournalStore();
const route = useRoute();

/** 이력 복원/삭제 성공 시 일지 목록을 다시 조회한다. */
function onHistorySuccess(): void {
  void refreshJournalDaysForRoute(journalStore, route);
}
</script>
