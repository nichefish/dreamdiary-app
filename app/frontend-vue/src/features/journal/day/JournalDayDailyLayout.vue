<template>
  <!--begin::저널 일간 레이아웃 (새 창 전용 — aside 없이 선택 일자 태그클라우드와 날짜 카드 표시)-->
  <div class="journal-daily-layout">
    <router-view />

    <!--begin::저널 모달 컨테이너-->
    <JournalDayRegistModal />
    <JournalDayDetailModal />
    <JournalChapterRegistModal />
    <JournalReflectionRegistModal />
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
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import JournalDayRegistModal from "./modals/JournalDayRegistModal.vue";
import JournalDayDetailModal from "./modals/JournalDayDetailModal.vue";
import JournalChapterRegistModal from "../chapter/modals/JournalChapterRegistModal.vue";
import JournalReflectionRegistModal from "../reflection/modals/JournalReflectionRegistModal.vue";
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
const threadStore = useJournalThreadStore();
const route = useRoute();

/** 이력 복원/삭제 성공 시 열린 스레드 상세와 배경 일지 목록을 현재 호스트 계약으로 다시 조회한다. */
function onHistorySuccess(): void {
  void refreshJournalEntryHostForRoute(journalStore, threadStore, route);
}
</script>
