<template>
  <!--begin::저널 스레드 독립 상세 페이지-->
  <div class="card mt-3 mb-5">
    <div class="card-header border-0">
      <div class="card-title">
        <h3 class="fw-bold text-gray-900">{{ t("journal.thread.detail.modal.title") }}</h3>
      </div>
      <div class="card-toolbar">
        <button
          type="button"
          class="btn btn-sm btn-light-primary"
          :title="t('route.title.journal-thread')"
          @click="goToList"
        >
          <i class="bi bi-arrow-left me-1"></i>
          {{ t("route.title.journal-thread") }}
        </button>
      </div>
    </div>

    <div class="card-body pt-3">
      <div v-if="store.detailLoading" class="d-flex justify-content-center py-10">
        <span class="spinner-border text-primary" role="status"></span>
      </div>
      <JournalThreadDetailContent v-else-if="store.detailModel" />
    </div>
  </div>
  <!--end::저널 스레드 독립 상세 페이지-->
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import JournalThreadDetailContent from "@/features/journal/thread/components/JournalThreadDetailContent.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const router = useRouter();
const store = useJournalThreadStore();
const { t } = useLocaleStore();

/** 독립 상세를 닫고 스레드 목록의 정식 route로 이동한다. */
function goToList(): void {
  void router.push({ name: "thread-list" });
}
</script>
