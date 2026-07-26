<template>
  <!--begin::결산 뷰 툴바 — 저널 일자 액션 행과 동일(mt-3). 탭이 없으므로 탭용 mt-5 빈 여백은 두지 않는다.-->
  <div class="journal-annual-view-toolbar d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
    <div class="d-none d-md-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
      <template v-if="isListRoute">
        <button
          type="button"
          class="btn btn-sm btn-primary text-nowrap"
          :disabled="store.syncing"
          @click="makeTotalAnnual"
        >
          <span v-if="store.syncing" class="spinner-border spinner-border-sm me-1" role="status"></span>
          <i v-else class="bi bi-arrow-repeat me-1"></i>
          {{ t("journal.annual.refresh-all") }}
        </button>
        <button
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer text-nowrap"
          @click="store.openRegist()"
        >
          <i class="bi bi-plus-circle fs-4 pe-1"></i>
          {{ t("journal.annual.register") }}
        </button>
      </template>
      <template v-if="!asideStore.visible">
        <div v-if="isListRoute" class="vr mx-1 opacity-25"></div>
        <button
          type="button"
          class="btn btn-sm btn-icon btn-light"
          :title="t('journal.annual.filter.panel.tooltip')"
          @click="asideStore.show()"
        >
          <i class="bi bi-layout-sidebar-inset-reverse"></i>
        </button>
      </template>
    </div>
  </div>
  <!--end::결산 뷰 툴바-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import { useJournalAnnualAsideStore } from "@/features/journal/stores/journalAnnualAside";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const route = useRoute();
const store = useJournalAnnualStore();
const asideStore = useJournalAnnualAsideStore();
const { t } = useLocaleStore();

const isListRoute = computed(() => route.name === "annual-list");

function makeTotalAnnual(): void {
  void store.makeTotalAnnual();
}
</script>
