<template>
  <!--begin::결산 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-annual-layout-vue">
    <!--begin::결산 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0 position-relative">
        <!--begin::뷰 툴바 — 저널 일자와 동일: 데스크톱 aside 열기는 툴바 우측-->
        <JournalAnnualViewToolbar />
        <!--end::뷰 툴바-->
        <!-- aside 접힘 시 모바일 전용 열기 버튼 (데스크톱은 툴바). 저널 일자 JournalDayLayout 과 동일 패턴. -->
        <button
          v-if="!asideStore.visible"
          type="button"
          class="btn btn-sm btn-icon btn-light-primary position-absolute d-md-none"
          style="top: 4rem; right: 0; z-index: 1;"
          :title="t('journal.annual.filter.panel.tooltip')"
          @click="asideStore.show()"
        >
          <i class="bi bi-layout-sidebar-inset-reverse"></i>
        </button>
        <router-view />
      </div>
      <aside v-if="asideStore.visible" class="journal-annual-layout-vue__aside flex-shrink-0">
        <JournalAnnualAside @close="asideStore.hide()" />
      </aside>
    </div>
    <!--end::결산 본문 + aside 컨테이너-->

    <!--begin::결산 모달 컨테이너-->
    <JournalAnnualRegistModal />
    <JournalAnnualReviewRegistModal />
    <!-- 태그 컨텍스트 메뉴의 '검색'(JOURNAL_DAY)·'프로필'은 store 상태만 켠다.
         그 상태를 구독해 실제로 그리는 모달이 같은 화면에 마운트돼 있어야 열린다.
         (저널 일자 레이아웃과 동일 구성: DayMeta + TagProfile + TagContextMenu) -->
    <JournalDayMetaModal />
    <JournalTagProfileModal />
    <JournalTagContextMenu />
    <!--end::결산 모달 컨테이너-->
  </div>
  <!--end::결산 레이아웃-->
</template>

<script setup lang="ts">
import JournalAnnualAside from "./components/JournalAnnualAside.vue";
import JournalAnnualViewToolbar from "./components/JournalAnnualViewToolbar.vue";
import JournalAnnualRegistModal from "./modals/JournalAnnualRegistModal.vue";
import JournalAnnualReviewRegistModal from "./modals/JournalAnnualReviewRegistModal.vue";
import JournalDayMetaModal from "@/features/journal/day/modals/JournalDayMetaModal.vue";
import JournalTagProfileModal from "@/features/journal/shared/modals/JournalTagProfileModal.vue";
import JournalTagContextMenu from "@/features/journal/shared/components/JournalTagContextMenu.vue";
import { useJournalAnnualAsideStore } from "@/features/journal/stores/journalAnnualAside";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const { t } = useLocaleStore();
const asideStore = useJournalAnnualAsideStore();
</script>
