<template>
  <section class="card post">
    <div class="card-body">
      <!--begin::저널 임베딩 ON/OFF 토글-->
      <div class="admin-tool-row mb-5">
        <div>
          <div class="fw-bold">{{ t('admin.page.journal-ai.title') }}</div>
          <div class="text-muted fs-8">{{ t('admin.page.journal-ai.desc') }}</div>
        </div>
        <div class="admin-tool-actions">
          <label class="form-check form-switch">
            <input
              v-model="store.journalSettingAiEnabled"
              class="form-check-input"
              type="checkbox"
              :disabled="store.journalSettingLoading || store.journalSettingSaving"
              @change="saveJournalEmbeddingSetting"
            />
            <span class="form-check-label">{{ store.journalSettingAiEnabled ? 'ON' : 'OFF' }}</span>
          </label>
        </div>
      </div>
      <div v-if="store.journalSettingError" class="alert alert-warning py-2 mb-4">
        {{ store.journalSettingError }}
      </div>
      <!--end::저널 임베딩 ON/OFF 토글-->

      <div class="separator my-5"></div>

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

      <div class="admin-rag-settings mb-4">
        <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-2">
          <div>
            <h4 class="fs-6 fw-bold mb-1">{{ t('admin.page.rag.title') }}</h4>
            <div class="text-muted fs-8">{{ t('admin.page.rag.desc') }}</div>
          </div>
          <button
            type="button"
            class="btn btn-sm btn-primary"
            :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving"
            @click="saveRagSettings"
          >
            <span v-if="store.chatRagSettingsSaving" class="spinner-border spinner-border-sm me-1"></span>
            {{ t('admin.page.rag.save') }}
          </button>
        </div>
        <div v-if="store.chatRagSettingsError" class="alert alert-warning py-2">
          {{ store.chatRagSettingsError }}
        </div>
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-check form-switch">
              <input
                v-model="store.chatRagSettings.ragEnabled"
                class="form-check-input"
                type="checkbox"
                :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving"
              />
              <span class="form-check-label">{{ t('admin.page.rag.enabled') }}</span>
            </label>
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.top-k') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragTopK"
              class="form-control form-control-sm"
              type="number"
              min="1"
              max="50"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.min-score') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragMinScore"
              class="form-control form-control-sm"
              type="number"
              min="0.05"
              max="0.95"
              step="0.01"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.summary-top-k') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragSummaryTopK"
              class="form-control form-control-sm"
              type="number"
              min="1"
              max="100"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.synthesis-top-k') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragSynthesisTopK"
              class="form-control form-control-sm"
              type="number"
              min="1"
              max="100"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.stance-top-k') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragStanceTopK"
              class="form-control form-control-sm"
              type="number"
              min="1"
              max="100"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label fs-8 mb-1">{{ t('admin.page.rag.synthesis-min-score') }}</label>
            <input
              v-model.number="store.chatRagSettings.ragSynthesisMinScore"
              class="form-control form-control-sm"
              type="number"
              min="0.05"
              max="0.95"
              step="0.01"
              :disabled="store.chatRagSettingsLoading || store.chatRagSettingsSaving || !store.chatRagSettings.ragEnabled"
            />
          </div>
        </div>
        <div class="text-muted fs-8 mt-2">{{ t('admin.page.rag.note') }}</div>
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

</template>

<script setup lang="ts">
import { computed } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import { formatNumber, formatPercent } from "@/features/admin/adminFormat";

/**
 * AI 설정 카드: 저널 임베딩 게이트·백필/재적재/동기화·Ollama 상태·Chat RAG 설정·임베딩 품질평가(ai 탭).
 * 설정 폼은 store 상태에 직접 v-model 바인딩하고, 저장·조회 액션은 store 를 호출한다.
 * 렌더 게이트(activeTab === "ai")는 상위 AdminPage 가 소유한다.
 */
const store = useAdminPageStore();
const { t } = useLocaleStore();

const BACKGROUND_SYNC_NOTE = t("admin.page.background.queue-note");

const syncButtonDisabled = computed(
  () => !store.journalSettingAiEnabled || store.embeddingSyncRunning || store.embeddingStats.syncRunning
);
const ollamaHealthBadgeClass = computed(() => {
  const status = store.ollamaHealth?.status ?? "DOWN";
  if (status === "UP") return "badge-light-success";
  if (status === "DEGRADED") return "badge-light-warning";
  return "badge-light-danger";
});
const embeddingFailedRequeueDisabled = computed(
  () =>
    !store.journalSettingAiEnabled || store.embeddingRequeueRunning || store.embeddingStats.failed <= 0
);
const embeddingWorkerActive = computed(() => !store.embeddingStats.syncRunning && (store.embeddingStats.pending > 0 || store.embeddingStats.processing > 0));
const embeddingProgressStyle = computed(() => {
  const value = Math.max(0, Math.min(100, Number(store.embeddingStats.vectorizedRate) || 0));
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

async function saveRagSettings() {
  try {
    const msg = await store.saveChatRagSettings();
    await swalAlert(msg);
  } catch (error) {
    await swalAlert(error instanceof Error ? error.message : t("admin.page.rag.save.failure"));
  }
}

async function saveJournalEmbeddingSetting() {
  try {
    await store.saveJournalSetting();
  } catch {
    // 실패 시 토글을 원래 값으로 복원
    store.journalSettingAiEnabled = !store.journalSettingAiEnabled;
  }
}
</script>

<style scoped>
@import "./adminCards.scss";
</style>
