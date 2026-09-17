<template>
  <section class="card post">
    <div class="card-body">
      <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-4">
        <div>
          <h3 class="admin-section-title mb-1">Entity Queue Backfill</h3>
          <div class="text-muted fs-8">{{ t('admin.page.entity.total-desc') }}</div>
          <div class="text-muted fs-8 mt-1">{{ t('admin.page.entity.sync-desc') }}</div>
        </div>
        <div class="admin-tool-actions">
          <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.entityQueueStatsLoading" @click="store.fetchEntityQueueStats">
            Refresh
          </button>
          <button type="button" class="btn btn-sm btn-light-warning" :disabled="entityFailedRequeueDisabled" @click="store.requeueFailedEntityQueue">
            <span v-if="store.entityQueueRequeueRunning" class="spinner-border spinner-border-sm me-1"></span>
            Requeue Failed
          </button>
          <button type="button" class="btn btn-sm btn-primary" :disabled="entitySyncButtonDisabled" @click="store.syncEntityQueue">
            <span v-if="store.entityQueueSyncRunning" class="spinner-border spinner-border-sm me-1"></span>
            Sync Entries
          </button>
        </div>
      </div>

      <div v-if="store.entityQueueError" class="alert alert-warning py-2">
        {{ store.entityQueueError }}
      </div>
      <div v-if="store.entityQueueSyncResult" class="alert alert-success py-2">
        {{ entityQueueSyncMessage }}
      </div>
      <div v-if="entityWorkerActive" class="admin-sync-status mb-4">
        <div class="d-flex justify-content-between gap-3 flex-wrap">
          <div>
            <strong>Entity extraction running</strong>
            <div class="text-muted fs-8">{{ entityWorkerStatusMessage }}</div>
          </div>
          <span class="badge badge-light-warning">WORKER</span>
        </div>
      </div>

      <div class="admin-stat-grid">
        <div v-for="stat in entityQueueStatsCards" :key="stat.label" class="admin-stat">
          <span>{{ stat.label }}</span>
          <strong :class="stat.className">{{ formatNumber(stat.value) }}</strong>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-2 my-4">
        <span class="badge badge-light-success">Synced {{ formatNumber(store.entityQueueStats.synced) }}</span>
        <span class="badge badge-light-warning">Remaining {{ formatNumber(store.entityQueueStats.remaining) }}</span>
        <span class="badge badge-light-danger">Failed {{ formatNumber(store.entityQueueStats.failed) }}</span>
        <span class="badge badge-light">Skipped {{ formatNumber(store.entityQueueStats.skipped) }}</span>
        <span class="badge badge-light-secondary">Unqueued {{ formatNumber(store.entityQueueStats.unqueuedEntries) }}</span>
        <span class="badge badge-light">Queue Rows {{ formatNumber(store.entityQueueStats.queueRows) }}</span>
      </div>

      <div class="d-flex justify-content-between fs-8 text-muted mb-1">
        <span>Entry Coverage {{ formatPercent(store.entityQueueStats.completionRate) }}</span>
        <span>Queue Completion {{ formatPercent(store.entityQueueStats.queueCompletionRate) }}</span>
      </div>
      <div class="progress h-8px">
        <div class="progress-bar bg-info" role="progressbar" :style="entityQueueProgressStyle"></div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import { formatNumber, formatPercent } from "@/features/admin/adminFormat";

/**
 * 엔티티 큐 카드: 엔티티 추출 큐 상태·진행률·재적재/동기화(ai 탭). 조회/액션은 store 를 직접 호출한다.
 * 렌더 게이트(activeTab === "ai")는 상위 AdminPage 가 소유한다.
 */
const store = useAdminPageStore();
const { t } = useLocaleStore();

const BACKGROUND_SYNC_NOTE = t("admin.page.background.queue-note");

const entitySyncButtonDisabled = computed(() => store.entityQueueSyncRunning);
const entityFailedRequeueDisabled = computed(() => store.entityQueueRequeueRunning || store.entityQueueStats.failed <= 0);
const entityWorkerActive = computed(() => store.entityQueueStats.pending > 0 || store.entityQueueStats.processing > 0);
const entityWorkerStatusMessage = computed(() =>
  `Worker still has ${formatNumber(store.entityQueueStats.pending)} pending and ${formatNumber(store.entityQueueStats.processing)} processing rows.`
);
const entityQueueProgressStyle = computed(() => {
  const value = Math.max(0, Math.min(100, Number(store.entityQueueStats.completionRate) || 0));
  return { width: `${value}%` };
});
const entityQueueStatsCards = computed(() => [
  { label: "Entries", value: store.entityQueueStats.total, className: "" },
  { label: "Synced", value: store.entityQueueStats.synced, className: "text-success" },
  { label: "Unqueued", value: store.entityQueueStats.unqueuedEntries, className: "text-muted" },
  { label: "Pending", value: store.entityQueueStats.pending, className: "text-warning" },
]);
const entityQueueSyncMessage = computed(() => {
  const result = store.entityQueueSyncResult;
  if (!result) return "";
  return [
    `entries ${formatNumber(result.activeEntryCount)}`,
    `created ${formatNumber(result.created)}`,
    `requeued ${formatNumber(result.requeued)}`,
    `unchanged ${formatNumber(result.unchanged)}`,
    `removed ${formatNumber(result.removed)}`,
    BACKGROUND_SYNC_NOTE,
  ].join(" / ");
});
</script>

<style scoped>
@import "./adminCards.scss";
</style>
