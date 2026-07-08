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
          :title="t('journal.aside.open.tooltip')"
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
    <JournalMetaProfileModal />
    <!--end::저널 모달 컨테이너-->
    <!--begin::태그·메타 컨텍스트 메뉴 (전역 단일 인스턴스)-->
    <JournalTagContextMenu />
    <JournalMetaContextMenu />
    <!--end::태그·메타 컨텍스트 메뉴-->
  </div>
  <!--end::저널 레이아웃-->
</template>

<script setup lang="ts">
import { useJournalAsideStore } from "@/features/journal/stores/journalAside";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalAside from "./components/JournalAside.vue";
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
import JournalMetaProfileModal from "../shared/modals/JournalMetaProfileModal.vue";
import JournalTagContextMenu from "../shared/components/JournalTagContextMenu.vue";
import JournalMetaContextMenu from "../shared/components/JournalMetaContextMenu.vue";

const asideStore = useJournalAsideStore();
const journalStore = useJournalStore();
const { t } = useLocaleStore();

/** 이력 복원/삭제 성공 시 일지 목록을 다시 조회한다. */
function onHistorySuccess(): void {
  void journalStore.fetchDays();
}
</script>
