<template>
  <!--begin::저널 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-layout-vue">
    <!--begin::저널 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0 position-relative">
        <button
          v-if="!asideStore.visible"
          type="button"
          class="btn btn-sm btn-icon btn-light-primary position-absolute"
          style="top: 0; right: 0; z-index: 1;"
          title="필터 패널"
          @click="asideStore.show()"
        >
          <i class="bi bi-layout-sidebar-inset-reverse"></i>
        </button>
        <router-view />
      </div>
      <aside v-if="asideStore.visible" class="journal-layout-vue__aside flex-shrink-0">
        <JournalAside />
      </aside>
    </div>
    <!--end::저널 본문 + aside 컨테이너-->

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
  <!--end::저널 레이아웃-->
</template>

<script setup lang="ts">
import { useJournalAsideStore } from "@/stores/journalAside";
import { useJournalStore } from "@/stores/journal";
import JournalAside from "./components/JournalAside.vue";
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

const asideStore = useJournalAsideStore();
const journalStore = useJournalStore();

/** 이력 복원/삭제 성공 시 일지 목록을 다시 조회한다. */
function onHistorySuccess(): void {
  void journalStore.fetchDays();
}
</script>
