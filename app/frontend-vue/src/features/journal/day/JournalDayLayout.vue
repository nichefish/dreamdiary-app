<template>
  <!--begin::저널 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-layout-vue">
    <!--begin::저널 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0 position-relative">
        <!-- aside 접힘 시 나타나는 열기 버튼.
             변경 전: top:0 이라 같은 위치의 뷰 툴바 액션 행(mt-3 + btn-sm ≈ 16~47px)을 가렸다.
             변경 후: 액션 행 아래 여백으로 내려 버튼을 가리지 않게 한다.
             데스크톱 열기 버튼은 JournalDayViewToolbar 우측 끝에서 제공하며, 이 버튼은 툴바 액션이 숨겨지는 모바일 전용이다. -->
        <button
          v-if="!asideStore.visible"
          type="button"
          class="btn btn-sm btn-icon btn-light-primary position-absolute d-md-none"
          style="top: 4rem; right: 0; z-index: 1;"
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
    <!-- JournalEntryRegistModal: App.vue global mount (chat RAG source deep-link) -->
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
import { useRoute } from "vue-router";
import { useJournalAsideStore } from "@/features/journal/stores/journalAside";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalAside from "./components/JournalAside.vue";
import JournalDayRegistModal from "./modals/JournalDayRegistModal.vue";
import JournalDayDetailModal from "./modals/JournalDayDetailModal.vue";
import JournalChapterRegistModal from "../chapter/modals/JournalChapterRegistModal.vue";
import JournalInterpretationRegistModal from "../interpretation/modals/JournalInterpretationRegistModal.vue";
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
const threadStore = useJournalThreadStore();
const route = useRoute();
const { t } = useLocaleStore();

/** 이력 복원/삭제 성공 시 열린 스레드 상세와 배경 일지 목록을 현재 호스트 계약으로 다시 조회한다. */
function onHistorySuccess(): void {
  void refreshJournalEntryHostForRoute(journalStore, threadStore, route);
}
</script>
