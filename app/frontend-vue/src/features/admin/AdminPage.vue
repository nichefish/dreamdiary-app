<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <button type="button" class="btn btn-sm btn-light-primary" :disabled="reloadDisabled" @click="reload">
        <i class="bi bi-arrow-clockwise"></i>
      </button>
    </div>

    <div class="admin-tabs nav nav-tabs nav-line-tabs" role="tablist" :aria-label="t('admin.page.aria-label')">
      <button
        type="button"
        class="nav-link"
        :class="{ active: activeTab === 'general' }"
        role="tab"
        :aria-selected="activeTab === 'general'"
        @click="selectTab('general')"
      >
        {{ t('admin.page.tab.general') }}
      </button>
      <button
        type="button"
        class="nav-link"
        :class="{ active: activeTab === 'ai' }"
        role="tab"
        :aria-selected="activeTab === 'ai'"
        @click="selectTab('ai')"
      >
        {{ t('admin.page.tab.ai') }}
      </button>
    </div>

    <div v-if="store.backfillWorkActive" class="admin-backfill-banner" role="status">
      <i class="bi bi-cloud-check fs-4 text-primary"></i>
      <div class="flex-grow-1">
        <strong>{{ t('admin.page.background-processing') }}</strong>
        <div class="text-muted fs-8">
          {{ t('admin.page.background-note') }}
          {{ t('admin.page.background-refresh') }}
        </div>
      </div>
    </div>

    <div class="admin-layout" :class="{ 'admin-layout-ai': activeTab === 'ai' }">
      <section v-if="activeTab === 'general'" class="card post">
        <div class="card-body">
          <h3 class="admin-section-title">{{ t('admin.page.section.tools') }}</h3>

          <div class="admin-tool-row">
            <div>
              <div class="fw-bold">{{ t('admin.page.cache.title') }}</div>
              <div class="text-muted fs-8">{{ t('admin.page.cache.desc') }}</div>
            </div>
            <div class="admin-tool-actions">
              <button type="button" class="btn btn-sm btn-primary" @click="openCacheList">
                <i class="bi bi-list-ul"></i>
                {{ t('admin.page.cache.list') }}
              </button>
              <button type="button" class="btn btn-sm btn-light-danger" @click="clearAllCaches">
                <i class="bi bi-trash"></i>
                {{ t('admin.page.cache.delete-all') }}
              </button>
            </div>
          </div>

          <div class="separator my-5"></div>

          <div class="admin-tool-row">
            <div>
              <label for="holydayYy" class="fw-bold">{{ t('admin.page.holyday.title') }}</label>
              <div class="text-muted fs-8">{{ t('admin.page.holyday.desc') }}</div>
            </div>
            <div class="admin-inline-form">
              <select id="holydayYy" v-model="holydayYy" class="form-select form-select-solid">
                <option v-for="yy in store.yearOptions" :key="yy" :value="String(yy)">{{ yy }}</option>
              </select>
              <button type="button" class="btn btn-sm btn-primary" @click="syncHolyday">{{ t('admin.page.run') }}</button>
            </div>
          </div>

          <div class="separator my-5"></div>

          <div class="admin-tool-row">
            <div>
              <label for="notionDataType" class="fw-bold">{{ t('admin.page.notion.title') }}</label>
              <div class="text-muted fs-8">{{ t('admin.page.notion.desc') }}</div>
            </div>
            <div class="admin-notion-form">
              <select id="notionDataType" v-model="notionDataType" class="form-select form-select-solid">
                <option value="PAGE">PAGE</option>
                <option value="BLOCK">BLOCK</option>
                <option value="BLOCKS">BLOCKS</option>
                <option value="DATABASE">DATABASE</option>
              </select>
              <input v-model.trim="notionDataId" type="text" class="form-control form-control-solid" maxlength="64" />
              <button type="button" class="btn btn-sm btn-primary" @click="runNotion">{{ t('admin.page.run') }}</button>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'ai'" class="card post">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-4">
            <div>
              <h3 class="admin-section-title mb-1">AI Embedding Backfill</h3>
              <div class="text-muted fs-8">{{ t('admin.page.embedding.total-desc') }}</div>
              <div class="text-muted fs-8 mt-1">{{ t('admin.page.embedding.sync-desc') }}</div>
            </div>
            <div class="admin-tool-actions">
              <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.embeddingStatsLoading" @click="store.fetchEmbeddingStats">
                Refresh
              </button>
              <button type="button" class="btn btn-sm btn-light-warning" :disabled="embeddingFailedRequeueDisabled" @click="store.requeueFailedEmbeddingQueue">
                <span v-if="store.embeddingRequeueRunning" class="spinner-border spinner-border-sm me-1"></span>
                Requeue Failed
              </button>
              <button type="button" class="btn btn-sm btn-primary" :disabled="syncButtonDisabled" @click="store.syncEmbeddingQueue">
                <span v-if="store.embeddingSyncRunning || store.embeddingStats.syncRunning" class="spinner-border spinner-border-sm me-1"></span>
                {{ store.embeddingStats.syncRunning ? "Sync Running" : "Sync Entries" }}
              </button>
              <button type="button" class="btn btn-sm btn-light-info" :disabled="store.embeddingQualityEvalRunning" @click="store.runEmbeddingQualityEval">
                <span v-if="store.embeddingQualityEvalRunning" class="spinner-border spinner-border-sm me-1"></span>
                Quality Eval
              </button>
            </div>
          </div>

          <div v-if="store.ollamaHealthError" class="alert alert-warning py-2">
            {{ store.ollamaHealthError }}
          </div>
          <div v-else-if="store.ollamaHealth" class="admin-ollama-health mb-4">
            <div class="d-flex flex-wrap gap-2 align-items-center mb-1">
              <span class="badge" :class="ollamaHealthBadgeClass">Ollama {{ store.ollamaHealth.status }}</span>
              <span class="text-muted fs-8">{{ store.ollamaHealth.baseUrl }} · {{ store.ollamaHealth.latencyMs }}ms</span>
            </div>
            <div class="fs-8 text-muted">
              Chat {{ store.ollamaHealth.chatModelRequired }}
              <span :class="store.ollamaHealth.chatModelReady ? 'text-success' : 'text-warning'">
                ({{ store.ollamaHealth.chatModelReady ? "ready" : "missing" }})
              </span>
              · Embed {{ store.ollamaHealth.embeddingModelRequired }}
              <span :class="store.ollamaHealth.embeddingModelReady ? 'text-success' : 'text-warning'">
                ({{ store.ollamaHealth.embeddingModelReady ? "ready" : "missing" }})
              </span>
            </div>
            <div v-if="store.ollamaHealth.errorMessage" class="fs-8 text-warning mt-1">
              {{ store.ollamaHealth.errorMessage }}
            </div>
          </div>

          <div v-if="store.embeddingQualityEvalError" class="alert alert-warning py-2">
            {{ store.embeddingQualityEvalError }}
          </div>
          <div v-if="store.embeddingQualityEvalReport" class="admin-quality-eval mb-4">
            <div class="d-flex flex-wrap gap-2 align-items-center mb-2">
              <span class="badge" :class="store.embeddingQualityEvalReport.overallPassed ? 'badge-light-success' : 'badge-light-warning'">
                {{ store.embeddingQualityEvalReport.recommendation }}
              </span>
              <span class="text-muted fs-8">{{ store.embeddingQualityEvalReport.embeddingModel }} · dim {{ store.embeddingQualityEvalReport.vectorDimension ?? "?" }} · {{ store.embeddingQualityEvalReport.elapsedMs }}ms</span>
            </div>
            <div class="fs-8 mb-3">{{ store.embeddingQualityEvalReport.summary }}</div>
            <div v-for="suite in store.embeddingQualityEvalReport.suites" :key="suite.code" class="mb-3">
              <div class="fw-semibold fs-8">
                {{ suite.code }}
                <span :class="suite.suitePassed ? 'text-success' : 'text-warning'">({{ suite.passedCount }}/{{ suite.passedCount + suite.failedCount }})</span>
              </div>
              <div class="text-muted fs-8">{{ suite.description }}</div>
              <ul class="mb-0 ps-4 fs-8">
                <li v-for="item in suite.cases.filter((c) => !c.passed)" :key="item.caseId">
                  {{ item.caseId }}: {{ item.description }} — {{ item.detail || item.expectation }}
                </li>
              </ul>
            </div>
            <div v-if="store.embeddingQualityEvalReport.skippedSamples.length" class="fs-8 text-muted">
              SKIPPED samples:
              <span v-for="(sample, index) in store.embeddingQualityEvalReport.skippedSamples" :key="sample.journalEntryId ?? index">
                <template v-if="index > 0">, </template>#{{ sample.journalEntryId }}<template v-if="sample.errorMessage"> ({{ sample.errorMessage }})</template>
              </span>
            </div>
          </div>

          <div v-if="store.embeddingStatsError" class="alert alert-warning py-2">
            {{ store.embeddingStatsError }}
          </div>
          <div v-if="store.embeddingSyncResult" class="alert alert-success py-2">
            {{ embeddingSyncMessage }}
          </div>
          <div v-if="store.embeddingStats.syncRunning || store.embeddingStats.syncErrorMessage || embeddingWorkerActive" class="admin-sync-status mb-4">
            <div class="d-flex justify-content-between gap-3 flex-wrap">
              <div>
                <strong>{{ syncStatusTitle }}</strong>
                <div class="text-muted fs-8">{{ syncStatusMessage }}</div>
              </div>
              <span class="badge" :class="syncStatusBadgeClass">{{ store.embeddingStats.syncPhase || "IDLE" }}</span>
            </div>
            <div v-if="store.embeddingStats.syncRunning" class="progress h-6px mt-3">
              <div class="progress-bar bg-primary" role="progressbar" :style="syncProgressStyle"></div>
            </div>
          </div>

          <div class="admin-stat-grid">
            <div v-for="stat in embeddingStatsCards" :key="stat.label" class="admin-stat">
              <span>{{ stat.label }}</span>
              <strong :class="stat.className">{{ formatNumber(stat.value) }}</strong>
            </div>
          </div>

          <div class="d-flex flex-wrap gap-2 my-4">
            <span class="badge badge-light-success">Embedded {{ formatNumber(store.embeddingStats.embedded) }}</span>
            <span class="badge badge-light-warning">Remaining {{ formatNumber(store.embeddingStats.remaining) }}</span>
            <span class="badge badge-light-danger">Failed {{ formatNumber(store.embeddingStats.failed) }}</span>
            <span class="badge badge-light">Skipped {{ formatNumber(store.embeddingStats.skipped) }}</span>
            <span class="badge badge-light-secondary">Unqueued {{ formatNumber(store.embeddingStats.unqueuedEntries) }}</span>
            <span class="badge badge-light">Queue Rows {{ formatNumber(store.embeddingStats.queueRows) }}</span>
          </div>

          <div class="d-flex justify-content-between fs-8 text-muted mb-1">
            <span>Entry Coverage {{ formatPercent(store.embeddingStats.vectorizedRate) }}</span>
            <span>Queue Completion {{ formatPercent(store.embeddingStats.queueCompletionRate) }}</span>
          </div>
          <div class="progress h-8px">
            <div class="progress-bar bg-success" role="progressbar" :style="embeddingProgressStyle"></div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'ai'" class="card post">
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

      <section v-if="activeTab === 'general'" class="card post admin-role-card">
        <div class="card-body">
          <h3 class="admin-section-title">{{ t('admin.page.section.roles') }}</h3>
          <div class="table-responsive">
            <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
              <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                  <th>{{ t('admin.page.roles.col.code') }}</th>
                  <th>{{ t('admin.page.roles.col.name') }}</th>
                  <th class="text-center">{{ t('admin.page.roles.col.sort') }}</th>
                  <th class="text-center">{{ t('admin.page.roles.col.use') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!store.roles.length">
                  <td colspan="4" class="text-center text-muted py-8">{{ t('admin.page.roles.empty') }}</td>
                </tr>
                <tr v-for="role in store.roles" :key="role.id">
                  <td class="fw-bold text-muted">{{ role.roleKey }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <i :class="roleIcon(role)" class="fs-2 me-2"></i>
                      <span :class="roleNameClass(role)" class="fw-bold">
                        <template v-if="role.roleKey === store.meta.authDevKey && role.parentRoleId != null">({{ role.parentRoleId }}) </template>
                        {{ role.roleName }}
                      </span>
                      <span class="badge ms-3" :class="roleBadgeClass(role)">{{ role.authLevel ?? "-" }}</span>
                    </div>
                  </td>
                  <td class="text-center">{{ role.sortOrder ?? "-" }}</td>
                  <td class="text-center">{{ role.useYn }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>

    <div ref="cacheListModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('admin.page.cache.modal.title') }}</h5>
            <button type="button" class="btn-close" @click="closeCacheList"></button>
          </div>
          <div class="modal-body">
            <div v-if="store.cacheLoading" class="text-center text-muted py-8">
              <span class="spinner-border spinner-border-sm me-2"></span>
              {{ t('common.loading') }}
            </div>
            <div v-else-if="!cacheNames.length" class="text-center text-muted py-8">{{ t('admin.page.cache.empty') }}</div>
            <template v-else>
              <div v-for="(cacheName, index) in cacheNames" :key="cacheName">
                <div class="admin-cache-block">
                  <div class="admin-cache-name">
                    <strong>"{{ cacheName }}"</strong>
                    <button type="button" class="btn btn-sm btn-light-danger" @click="clearCache(cacheName)">
                      <i class="bi bi-trash"></i>
                      {{ t('admin.page.cache.delete-all') }}
                    </button>
                  </div>
                  <div class="admin-cache-entry-list">
                    <div v-for="entry in cacheEntries(cacheName)" :key="entry[0]" class="admin-cache-entry">
                      <button type="button" class="btn btn-sm btn-light-primary" @click="openCacheDetail(cacheName, entry[0])">
                        {{ displayCacheKey(entry[0]) }}
                        <i class="bi bi-stickies ms-1"></i>
                      </button>
                      <button type="button" class="btn btn-sm btn-light-danger" @click="evictCacheEntry(cacheName, entry[0])">
                        <i class="bi bi-trash"></i>
                      </button>
                      <span>{{ stringify(entry[1]) }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="index < cacheNames.length - 1" class="separator my-5"></div>
              </div>
            </template>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light" @click="closeCacheList">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </div>

    <div ref="cacheDetailModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('admin.page.cache.detail.modal.title') }}</h5>
            <button type="button" class="btn-close" @click="closeCacheDetail"></button>
          </div>
          <div class="modal-body">
            <pre class="admin-cache-detail">{{ cacheDetailText }}</pre>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light-primary" @click="backToCacheList">{{ t('admin.page.cache.detail.list') }}</button>
            <button type="button" class="btn btn-sm btn-light" @click="closeCacheDetail">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Modal } from "bootstrap";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import type { RoleRow } from "@/features/admin/types/adminPage.types";

const store = useAdminPageStore();
const { t } = useLocaleStore();
type AdminTab = "general" | "ai";
const route = useRoute();
const router = useRouter();
const holydayYy = ref(String(new Date().getFullYear()));
const notionDataType = ref("PAGE");
const notionDataId = ref("");
const cacheListModalEl = ref<HTMLElement | null>(null);
const cacheDetailModalEl = ref<HTMLElement | null>(null);
let cacheListModal: Modal | null = null;
let cacheDetailModal: Modal | null = null;
let statsTimer: number | undefined;
const BACKGROUND_SYNC_NOTE = t("admin.page.background.queue-note");

const activeTab = computed<AdminTab>(() => (route.query.tab === "ai" ? "ai" : "general"));
const reloadDisabled = computed(() =>
  activeTab.value === "ai"
    ? store.embeddingStatsLoading || store.entityQueueStatsLoading
    : store.bootstrapLoading
);
const syncButtonDisabled = computed(() => store.embeddingSyncRunning || store.embeddingStats.syncRunning);
const ollamaHealthBadgeClass = computed(() => {
  const status = store.ollamaHealth?.status ?? "DOWN";
  if (status === "UP") return "badge-light-success";
  if (status === "DEGRADED") return "badge-light-warning";
  return "badge-light-danger";
});
const embeddingFailedRequeueDisabled = computed(
  () => store.embeddingRequeueRunning || store.embeddingStats.failed <= 0
);
const entitySyncButtonDisabled = computed(() => store.entityQueueSyncRunning);
const entityFailedRequeueDisabled = computed(() => store.entityQueueRequeueRunning || store.entityQueueStats.failed <= 0);
const embeddingWorkerActive = computed(() => !store.embeddingStats.syncRunning && (store.embeddingStats.pending > 0 || store.embeddingStats.processing > 0));
const entityWorkerActive = computed(() => store.entityQueueStats.pending > 0 || store.entityQueueStats.processing > 0);
const entityWorkerStatusMessage = computed(() =>
  `Worker still has ${formatNumber(store.entityQueueStats.pending)} pending and ${formatNumber(store.entityQueueStats.processing)} processing rows.`
);
const embeddingProgressStyle = computed(() => {
  const value = Math.max(0, Math.min(100, Number(store.embeddingStats.vectorizedRate) || 0));
  return { width: `${value}%` };
});
const entityQueueProgressStyle = computed(() => {
  const value = Math.max(0, Math.min(100, Number(store.entityQueueStats.completionRate) || 0));
  return { width: `${value}%` };
});
const syncProgressPercent = computed(() => {
  const total = Number(store.embeddingStats.syncTotal) || 0;
  if (total <= 0) return 0;
  return Math.max(0, Math.min(100, (Number(store.embeddingStats.syncProcessed) / total) * 100));
});
const syncProgressStyle = computed(() => ({ width: `${syncProgressPercent.value}%` }));
const syncStatusTitle = computed(() => {
  if (store.embeddingStats.syncErrorMessage) return "Queue sync failed";
  if (store.embeddingStats.syncRunning) return "Queue sync running";
  if (embeddingWorkerActive.value) return "Vector generation running";
  return "Embedding status";
});
const syncStatusMessage = computed(() => {
  if (store.embeddingStats.syncErrorMessage) return store.embeddingStats.syncErrorMessage;
  if (store.embeddingStats.syncRunning) {
    return `Syncing entries ${formatNumber(store.embeddingStats.syncProcessed)} / ${formatNumber(store.embeddingStats.syncTotal)}`;
  }
  if (embeddingWorkerActive.value) {
    return `Worker still has ${formatNumber(store.embeddingStats.pending)} pending and ${formatNumber(store.embeddingStats.processing)} processing rows.`;
  }
  return "";
});
const syncStatusBadgeClass = computed(() => {
  if (store.embeddingStats.syncErrorMessage) return "badge-light-danger";
  if (store.embeddingStats.syncRunning) return "badge-light-primary";
  if (embeddingWorkerActive.value) return "badge-light-warning";
  return "badge-light";
});

const embeddingStatsCards = computed(() => [
  { label: "Entries", value: store.embeddingStats.total, className: "" },
  { label: "Embedded", value: store.embeddingStats.embedded, className: "text-success" },
  { label: "Unqueued", value: store.embeddingStats.unqueuedEntries, className: "text-muted" },
  { label: "Pending", value: store.embeddingStats.pending, className: "text-warning" },
]);

const entityQueueStatsCards = computed(() => [
  { label: "Entries", value: store.entityQueueStats.total, className: "" },
  { label: "Synced", value: store.entityQueueStats.synced, className: "text-success" },
  { label: "Unqueued", value: store.entityQueueStats.unqueuedEntries, className: "text-muted" },
  { label: "Pending", value: store.entityQueueStats.pending, className: "text-warning" },
]);

const embeddingSyncMessage = computed(() => {
  const result = store.embeddingSyncResult;
  if (!result) return "";
  return [
    `entries ${formatNumber(result.activeEntryCount)}`,
    `created ${formatNumber(result.created)}`,
    `requeued ${formatNumber(result.requeued)}`,
    `unchanged ${formatNumber(result.unchanged)}`,
    `skipped ${formatNumber(result.skipped)}`,
    `removed ${formatNumber(result.removed)}`,
    BACKGROUND_SYNC_NOTE,
  ].join(" / ");
});

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

const cacheNames = computed(() => Object.keys(store.cacheMap || {}));
const cacheDetailText = computed(() => stringifyPretty(store.cacheDetail));

function formatNumber(value: number): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

function formatPercent(value: number): string {
  return `${(Number(value) || 0).toFixed(2)}%`;
}

function roleIcon(role: RoleRow): string {
  if (role.roleKey === store.meta.authMngrKey) return "bi bi-person-lines-fill text-info";
  if (role.roleKey === store.meta.authDevKey) return "bi bi-person-fill-gear text-info";
  return "bi bi-people-fill text-muted";
}

function roleNameClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "text-muted" : "text-info";
}

function roleBadgeClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "badge-dark opacity-50" : "badge-info";
}

function stringify(value: unknown): string {
  if (value === undefined) return "undefined";
  if (value === null) return "null";
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function stringifyPretty(value: unknown): string {
  if (value === null || value === undefined) return t("admin.page.cache.detail.empty");
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function cacheEntries(cacheName: string): Array<[string, unknown]> {
  return Object.entries(store.cacheMap[cacheName] ?? {});
}

function displayCacheKey(cacheKey: string): string {
  return cacheKey === "SimpleKey()" ? "-" : cacheKey;
}

async function reload() {
  if (activeTab.value === "ai") {
    await Promise.all([store.fetchEmbeddingStats(), store.fetchEntityQueueStats()]);
    return;
  }
  await store.fetchBootstrap();
  holydayYy.value = String(store.meta.currYy);
}

async function selectTab(tab: AdminTab) {
  await router.replace({ query: { ...route.query, tab } });
}

async function syncHolyday() {
  try {
    void swalAlert(await store.syncHolyday(holydayYy.value));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.holyday.sync.failure"));
  }
}

async function runNotion() {
  try {
    const res = await store.fetchNotion(notionDataType.value, notionDataId.value);
    void swalAlert(JSON.stringify(res.rsltObj ?? res.rsltList ?? res, null, 2));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.notion.failure"));
  }
}

async function openCacheList() {
  if (!await assertAuthenticatedBeforeModal()) return;
  await store.fetchCacheMap();
  cacheListModal?.show();
}

function closeCacheList() {
  cacheListModal?.hide();
}

async function openCacheDetail(cacheName: string, cacheKey: string) {
  if (!await assertAuthenticatedBeforeModal()) return;
  await store.fetchCacheDetail(cacheName, cacheKey);
  cacheListModal?.hide();
  cacheDetailModal?.show();
}

function closeCacheDetail() {
  cacheDetailModal?.hide();
}

function backToCacheList() {
  cacheDetailModal?.hide();
  cacheListModal?.show();
}

async function clearCache(cacheName: string) {
  if (!await swalConfirm(t("admin.page.cache.delete.confirm").replace("{cacheName}", cacheName))) return;
  try {
    await store.clearCacheByName(cacheName);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.delete.failure"));
  }
}

async function evictCacheEntry(cacheName: string, cacheKey: string) {
  if (!await swalConfirm(t("admin.page.cache.item.delete.confirm"))) return;
  try {
    await store.evictCacheEntry(cacheName, cacheKey);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.item.delete.failure"));
  }
}

async function clearAllCaches() {
  if (!await swalConfirm(t("admin.page.cache.all.delete.confirm"))) return;
  try {
    void swalAlert(await store.clearAllCaches());
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.all.delete.failure"));
  }
}

onMounted(async () => {
  if (cacheListModalEl.value) cacheListModal = new Modal(cacheListModalEl.value);
  if (cacheDetailModalEl.value) cacheDetailModal = new Modal(cacheDetailModalEl.value);
  await reload();
  statsTimer = window.setInterval(() => {
    void Promise.all([store.fetchEmbeddingStats(), store.fetchEntityQueueStats()]);
  }, 30000);
});

watch(activeTab, () => {
  void reload();
});

onUnmounted(() => {
  if (statsTimer) window.clearInterval(statsTimer);
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 1rem;
}

.admin-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
  gap: 1rem;
}

.admin-layout-ai {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-role-card {
  grid-column: 2;
  grid-row: 1 / span 2;
}

.admin-section-title {
  font-size: 1rem;
  font-weight: 700;
}

.admin-tool-row {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto;
  gap: 1rem;
  align-items: center;
}

.admin-tool-actions,
.admin-inline-form,
.admin-notion-form {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.admin-inline-form .form-select {
  width: 110px;
}

.admin-notion-form {
  justify-content: flex-end;
}

.admin-notion-form .form-select {
  width: 130px;
}

.admin-notion-form .form-control {
  width: 240px;
}

.admin-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.admin-stat {
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--bs-light);
}

.admin-stat span {
  display: block;
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.admin-stat strong {
  display: block;
  margin-top: 0.25rem;
  font-size: 1.25rem;
}

.admin-backfill-banner {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  border: 1px solid #cfe2ff;
  border-radius: 8px;
  background: #f1faff;
}

.admin-sync-status {
  padding: 0.85rem 1rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
  background: var(--bs-gray-100);
}

.admin-cache-block {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 1rem;
}

.admin-cache-name {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.75rem;
  min-width: 0;
  word-break: break-word;
}

.admin-cache-entry-list {
  display: grid;
  gap: 0.5rem;
  min-width: 0;
}

.admin-cache-entry {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr);
  gap: 0.5rem;
  align-items: center;
}

.admin-cache-entry span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-cache-detail {
  min-height: 360px;
  max-height: 65vh;
  padding: 1rem;
  margin: 0;
  overflow: auto;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-800);
}

@media (max-width: 1200px) {
  .admin-layout,
  .admin-role-card {
    display: flex;
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .admin-tool-row,
  .admin-cache-block,
  .admin-cache-entry {
    grid-template-columns: 1fr;
  }

  .admin-notion-form,
  .admin-notion-form .form-select,
  .admin-notion-form .form-control,
  .admin-inline-form,
  .admin-inline-form .form-select {
    width: 100%;
  }

  .admin-stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
