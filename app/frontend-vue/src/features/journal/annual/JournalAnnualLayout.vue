<template>
  <!--begin::결산 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-annual-layout-vue">
    <!--begin::결산 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0">
        <div v-if="!asideVisible" class="d-flex justify-content-end mb-3">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-light-primary"
            :title="t('journal.annual.filter.panel.tooltip')"
            @click="asideVisible = true"
          >
            <i class="bi bi-layout-sidebar-inset-reverse"></i>
          </button>
        </div>
        <router-view />
      </div>
      <aside v-if="asideVisible" class="journal-annual-layout-vue__aside flex-shrink-0">
        <JournalAnnualAside @close="asideVisible = false" />
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
import { ref } from "vue";
import JournalAnnualAside from "./components/JournalAnnualAside.vue";
import JournalAnnualRegistModal from "./modals/JournalAnnualRegistModal.vue";
import JournalAnnualReviewRegistModal from "./modals/JournalAnnualReviewRegistModal.vue";
import JournalDayMetaModal from "@/features/journal/day/modals/JournalDayMetaModal.vue";
import JournalTagProfileModal from "@/features/journal/shared/modals/JournalTagProfileModal.vue";
import JournalTagContextMenu from "@/features/journal/shared/components/JournalTagContextMenu.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const { t } = useLocaleStore();
const asideVisible = ref(true);
</script>
