<template>
  <!--begin::저널 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-layout-vue">
    <!--begin::저널 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0">
        <div v-if="!asideStore.visible" class="d-flex justify-content-end mb-3">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-light-primary"
            title="필터 패널"
            @click="asideStore.show()"
          >
            <i class="bi bi-layout-sidebar-inset-reverse"></i>
          </button>
        </div>
        <router-view />
      </div>
      <aside v-if="asideStore.visible" class="journal-layout-vue__aside flex-shrink-0">
        <JournalAside />
      </aside>
    </div>
    <!--end::저널 본문 + aside 컨테이너-->

    <!--begin::저널 모달 컨테이너-->
    <JournalDayRegModal />
    <JournalDayDtlModal />
    <JournalChapterRegModal />
    <JournalInterpretationRegModal />
    <JournalEntryRegModal />
    <JournalDayTagDtlModal />
    <JournalTodoRegModal />
    <JournalDayMetaModal />
    <CommentRegModal />
    <CommentListModal />
    <HistoryModal @success="onHistorySuccess" />
    <RelatedContentAddModal />
    <JournalTagListModal />
    <JournalTagProfileModal />
    <!--end::저널 모달 컨테이너-->
  </div>
  <!--end::저널 레이아웃-->
</template>

<script setup lang="ts">
import { useJournalAsideStore } from "@/stores/journalAside";
import { useJournalStore } from "@/stores/journal";
import JournalAside from "./components/JournalAside.vue";
import JournalDayRegModal from "./modals/JournalDayRegModal.vue";
import JournalDayDtlModal from "./modals/JournalDayDtlModal.vue";
import JournalChapterRegModal from "./modals/JournalChapterRegModal.vue";
import JournalInterpretationRegModal from "./modals/JournalInterpretationRegModal.vue";
import JournalEntryRegModal from "./modals/JournalEntryRegModal.vue";
import JournalDayTagDtlModal from "./modals/JournalDayTagDtlModal.vue";
import JournalTodoRegModal from "./modals/JournalTodoRegModal.vue";
import JournalDayMetaModal from "./modals/JournalDayMetaModal.vue";
import CommentRegModal from "./modals/CommentRegModal.vue";
import CommentListModal from "@/views/attachable/CommentListModal.vue";
import HistoryModal from "@/views/attachable/HistoryModal.vue";
import RelatedContentAddModal from "./modals/RelatedContentAddModal.vue";
import JournalTagListModal from "./modals/JournalTagListModal.vue";
import JournalTagProfileModal from "./modals/JournalTagProfileModal.vue";

const asideStore = useJournalAsideStore();
const journalStore = useJournalStore();

/** 이력 복원/삭제 성공 시 일지 목록을 다시 조회한다. */
function onHistorySuccess(): void {
  void journalStore.fetchDays();
}
</script>
