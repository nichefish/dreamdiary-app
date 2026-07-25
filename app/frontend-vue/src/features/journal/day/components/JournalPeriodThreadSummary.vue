<template>
  <template v-if="shouldRender">
    <div class="separator"></div>
    <div id="journal_period_thread_summary" class="row align-items-center mt-4 ms-4 min-h-42px">
      <div class="journal-period-thread-summary__label col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
        <b>{{ periodLabel }} :</b>
      </div>
      <div class="col flex-grow-1">
        <span v-if="threadStore.periodSummaryLoading" class="text-muted fs-7">{{ t("common.loading") }}...</span>
        <span v-else-if="threadStore.periodSummaryError" class="text-danger fs-7">
          {{ threadStore.periodSummaryError }}
        </span>
        <div v-else class="d-flex flex-wrap align-items-center gap-2">
          <button
            v-for="thread in visibleThreads"
            :key="thread.threadId"
            type="button"
            class="btn btn-sm btn-light-primary py-1 px-3 d-inline-flex align-items-center gap-1"
            :title="t('journal.entry.thread.open.tooltip')"
            @click.stop="openThreadDetail(thread.threadId)"
          >
            <i class="bi bi-diagram-3 p-0 fs-8"></i>
            <span>{{ thread.title }}</span>
            <span class="text-muted fs-9">
              {{ formatEntryCount(thread.entryCount) }}
            </span>
          </button>
          <button
            v-if="hasMonthlyOverflow"
            type="button"
            class="btn btn-sm btn-link py-1 px-2 text-decoration-none"
            @click="expanded = !expanded"
          >
            {{ expanded ? t("common.collapse") : t("common.expand") }}
            <i :class="['bi p-0 ms-1', expanded ? 'bi-chevron-up' : 'bi-chevron-down']"></i>
          </button>
        </div>
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from "vue";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import type { JournalPeriodThreadSummaryQuery } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const MONTHLY_VISIBLE_LIMIT = 10;

const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();
const expanded = ref(false);

const periodQuery = computed<JournalPeriodThreadSummaryQuery | null>(() => {
  if (journalStore.viewType === "WEEKLY" && journalStore.weekStartDt) {
    return {
      viewType: "WEEKLY",
      weekStartDt: journalStore.weekStartDt,
    };
  }
  if (journalStore.viewType === "LIST" && journalStore.yy && journalStore.mnth) {
    return {
      viewType: "LIST",
      yy: journalStore.yy,
      mnth: journalStore.mnth,
    };
  }
  return null;
});

const periodRequestKey = computed(() => {
  const query = periodQuery.value;
  if (!query) return "";
  return query.viewType === "WEEKLY"
    ? `WEEKLY:${query.weekStartDt}`
    : `LIST:${query.yy}:${query.mnth}`;
});

const isMonthly = computed(() => periodQuery.value?.viewType === "LIST");
const periodLabel = computed(() => (
  isMonthly.value
    ? t("journal.thread.period-summary.monthly")
    : t("journal.thread.period-summary.weekly")
));
const hasMonthlyOverflow = computed(() => (
  isMonthly.value && threadStore.periodSummary.length > MONTHLY_VISIBLE_LIMIT
));
const visibleThreads = computed(() => (
  hasMonthlyOverflow.value && !expanded.value
    ? threadStore.periodSummary.slice(0, MONTHLY_VISIBLE_LIMIT)
    : threadStore.periodSummary
));
const shouldRender = computed(() => (
  periodQuery.value !== null
  && (
    threadStore.periodSummaryLoading
    || Boolean(threadStore.periodSummaryError)
    || threadStore.periodSummary.length > 0
  )
));

watch(periodRequestKey, () => {
  expanded.value = false;
  const query = periodQuery.value;
  if (query) {
    void threadStore.fetchPeriodSummary(query);
  }
}, { immediate: true });

/** 화면 이탈 시 기간 요약의 마지막 조회 조건을 비워 비활성 상태의 재조회를 막는다. */
onUnmounted(() => {
  threadStore.clearPeriodSummaryQuery();
});

function formatEntryCount(count: number): string {
  return t("journal.thread.period-summary.entry-count").replace("{0}", String(count));
}

function openThreadDetail(threadId: number): void {
  void threadStore.openDetail(threadId);
}
</script>

<style scoped>
.journal-period-thread-summary__label {
  width: 6.25rem;
  justify-content: center;
}
</style>
