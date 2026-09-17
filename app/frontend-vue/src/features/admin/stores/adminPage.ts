import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { apiGet, apiPost, apiPut, apiPatch, assertOk } from "@/shared/api/client";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import {
  DEFAULT_ADMIN_PAGE_META,
  emptyEmbeddingStats,
  emptyEntityQueueStats,
  normalizeEmbeddingStats,
  normalizeEmbeddingSyncJobStatus,
  normalizeEmbeddingQualityEvalReport,
  normalizeEntityQueueStats,
  normalizeEntityQueueSyncResult,
  normalizeOllamaHealth,
  type AdminPageMeta,
  type CacheDetail,
  type CacheMap,
  type EmbeddingStats,
  type EmbeddingSyncResult,
  type EmbeddingSyncJobStatus,
  type EmbeddingQualityEvalReport,
  type EntityQueueStats,
  type EntityQueueSyncResult,
  type OllamaHealth,
  type RoleRow,
} from "@/features/admin/types/adminPage.types";

export type {
  AdminPageMeta,
  RoleRow,
  EmbeddingStats,
  EmbeddingSyncResult,
  EmbeddingSyncJobStatus,
  EmbeddingQualityEvalReport,
  EntityQueueStats,
  EntityQueueSyncResult,
  CacheMap,
  CacheDetail,
} from "@/features/admin/types/adminPage.types";

/** Embedding/Entity backfill is server-side; poll stats while queue or sync work is active. */
let backfillPollTimer: number | undefined;

function isBackfillWorkActive(
  embedding: EmbeddingStats,
  entity: EntityQueueStats,
  embeddingSyncRequestRunning: boolean,
  entitySyncRequestRunning: boolean
): boolean {
  return (
    embeddingSyncRequestRunning
    || entitySyncRequestRunning
    || embedding.syncRunning
    || embedding.pending > 0
    || embedding.processing > 0
    || entity.pending > 0
    || entity.processing > 0
  );
}

export const useAdminPageStore = defineStore("adminPage", () => {
  const { t } = useLocaleStore();
  const meta = ref<AdminPageMeta>({ ...DEFAULT_ADMIN_PAGE_META });
  const roles = ref<RoleRow[]>([]);
  const bootstrapLoading = ref(false);

  const embeddingStats = ref<EmbeddingStats>(emptyEmbeddingStats());
  const embeddingStatsLoading = ref(false);
  const embeddingStatsError = ref("");
  const embeddingSyncRunning = ref(false);
  const embeddingRequeueRunning = ref(false);
  const embeddingSyncResult = ref<EmbeddingSyncResult | null>(null);
  const embeddingQualityEvalRunning = ref(false);
  const embeddingQualityEvalError = ref("");
  const embeddingQualityEvalReport = ref<EmbeddingQualityEvalReport | null>(null);
  const ollamaHealth = ref<OllamaHealth | null>(null);
  const ollamaHealthError = ref("");
  const chatRagSettings = ref({
    ragEnabled: true,
    ragTopK: 5,
    ragMinScore: 0.35,
    ragSummaryTopK: 12,
    ragSynthesisTopK: 25,
    ragStanceTopK: 50,
    ragSynthesisMinScore: 0.25,
  });
  const chatRagSettingsLoading = ref(false);
  const chatRagSettingsSaving = ref(false);
  const chatRagSettingsError = ref("");
  const entityQueueStats = ref<EntityQueueStats>(emptyEntityQueueStats());
  const entityQueueStatsLoading = ref(false);
  const entityQueueError = ref("");
  const entityQueueSyncRunning = ref(false);
  const entityQueueSyncResult = ref<EntityQueueSyncResult | null>(null);
  const entityQueueRequeueRunning = ref(false);

  /** 저널 설정 (임베딩 ON/OFF) */
  const journalSettingAiEnabled = ref(true);
  const journalSettingLoading = ref(false);
  const journalSettingSaving = ref(false);
  const journalSettingError = ref("");

  const cacheMap = ref<CacheMap>({});
  const cacheDetail = ref<CacheDetail>(null);
  const cacheLoading = ref(false);

  const yearOptions = computed(() => {
    const yy = Number(meta.value.currYy) || new Date().getFullYear();
    return [yy - 1, yy, yy + 1];
  });

  const backfillWorkActive = computed(() =>
    isBackfillWorkActive(
      embeddingStats.value,
      entityQueueStats.value,
      embeddingSyncRunning.value,
      entityQueueSyncRunning.value
    )
  );

  function stopBackfillPolling() {
    if (backfillPollTimer !== undefined) {
      window.clearInterval(backfillPollTimer);
      backfillPollTimer = undefined;
    }
  }

  /** Start 5s polling while backfill work is active; survives AdminPage unmount. */
  function evaluateBackfillPolling() {
    if (backfillWorkActive.value) {
      if (backfillPollTimer === undefined) {
        backfillPollTimer = window.setInterval(() => {
          void Promise.all([fetchEmbeddingStats(), fetchEntityQueueStats()]).finally(() => {
            if (!backfillWorkActive.value) {
              stopBackfillPolling();
            }
          });
        }, 5000);
      }
      return;
    }
    stopBackfillPolling();
  }

  async function fetchBootstrap() {
    bootstrapLoading.value = true;
    try {
      const res = await apiGet<{ meta?: Partial<AdminPageMeta>; roleList?: RoleRow[] }>("/api/admin/page/bootstrap");
      const payload = res.rsltObj ?? {};
      meta.value = { ...DEFAULT_ADMIN_PAGE_META, ...(payload.meta ?? {}) };
      roles.value = Array.isArray(payload.roleList) ? payload.roleList : [];
    } finally {
      bootstrapLoading.value = false;
    }
  }

  async function fetchOllamaHealth() {
    ollamaHealthError.value = "";
    try {
      const res = await apiGet<Partial<OllamaHealth>>("/api/admin/ollama/health");
      assertOk(res, "Ollama health request failed");
      ollamaHealth.value = normalizeOllamaHealth(res.rsltObj);
    } catch (error) {
      ollamaHealthError.value = error instanceof Error ? error.message : "Ollama health request failed";
    }
  }

  async function fetchEmbeddingStats() {
    embeddingStatsLoading.value = true;
    embeddingStatsError.value = "";
    try {
      const [statsRes] = await Promise.all([
        apiGet<Partial<EmbeddingStats>>("/api/admin/journal-entry-embeddings/stats"),
        fetchOllamaHealth(),
      ]);
      assertOk(statsRes, "Embedding stats request failed");
      embeddingStats.value = normalizeEmbeddingStats(statsRes.rsltObj);
      embeddingSyncResult.value = embeddingStats.value.syncResult;
    } catch (error) {
      embeddingStatsError.value = error instanceof Error ? error.message : "Embedding stats request failed";
    } finally {
      embeddingStatsLoading.value = false;
      evaluateBackfillPolling();
    }
  }

  async function syncEmbeddingQueue() {
    if (!journalSettingAiEnabled.value) {
      return;
    }
    embeddingSyncRunning.value = true;
    embeddingStatsError.value = "";
    try {
      const res = await apiPost<Partial<EmbeddingSyncJobStatus>>("/api/admin/journal-entry-embeddings/sync");
      assertOk(res, "Embedding sync request failed");
      const status = normalizeEmbeddingSyncJobStatus(res.rsltObj);
      embeddingSyncResult.value = status.result;
      embeddingStats.value = {
        ...embeddingStats.value,
        syncRunning: status.running,
        syncPhase: status.phase,
        syncProcessed: status.processed,
        syncTotal: status.total,
        syncStartedAt: status.startedAt,
        syncFinishedAt: status.finishedAt,
        syncResult: status.result,
        syncErrorMessage: status.errorMessage,
      };
      await fetchEmbeddingStats();
    } catch (error) {
      embeddingStatsError.value = error instanceof Error ? error.message : "Embedding sync request failed";
    } finally {
      embeddingSyncRunning.value = false;
      evaluateBackfillPolling();
    }
  }

  async function requeueFailedEmbeddingQueue() {
    if (!journalSettingAiEnabled.value) {
      return;
    }
    embeddingRequeueRunning.value = true;
    embeddingStatsError.value = "";
    try {
      const res = await apiPost("/api/admin/journal-entry-embeddings/requeue-failed");
      assertOk(res, "Embedding requeue request failed");
      await fetchEmbeddingStats();
    } catch (error) {
      embeddingStatsError.value = error instanceof Error ? error.message : "Embedding requeue request failed";
    } finally {
      embeddingRequeueRunning.value = false;
      evaluateBackfillPolling();
    }
  }

  async function runEmbeddingQualityEval() {
    embeddingQualityEvalRunning.value = true;
    embeddingQualityEvalError.value = "";
    try {
      const res = await apiGet<Partial<EmbeddingQualityEvalReport>>("/api/admin/journal-entry-embeddings/quality-eval");
      assertOk(res, "Embedding quality eval failed");
      embeddingQualityEvalReport.value = normalizeEmbeddingQualityEvalReport(res.rsltObj);
    } catch (error) {
      embeddingQualityEvalError.value = error instanceof Error ? error.message : "Embedding quality eval request failed";
    } finally {
      embeddingQualityEvalRunning.value = false;
    }
  }

  async function fetchEntityQueueStats() {
    entityQueueStatsLoading.value = true;
    entityQueueError.value = "";
    try {
      const res = await apiGet<Partial<EntityQueueStats>>("/api/admin/journal-entry-entities/stats");
      assertOk(res, "Entity queue stats request failed");
      entityQueueStats.value = normalizeEntityQueueStats(res.rsltObj);
    } catch (error) {
      entityQueueError.value = error instanceof Error ? error.message : "Entity queue stats request failed";
    } finally {
      entityQueueStatsLoading.value = false;
      evaluateBackfillPolling();
    }
  }

  async function syncEntityQueue() {
    entityQueueSyncRunning.value = true;
    entityQueueError.value = "";
    try {
      const res = await apiPost<Partial<EntityQueueSyncResult>>("/api/admin/journal-entry-entities/sync");
      assertOk(res, "Entity queue sync request failed");
      entityQueueSyncResult.value = normalizeEntityQueueSyncResult(res.rsltObj);
      await fetchEntityQueueStats();
    } catch (error) {
      entityQueueError.value = error instanceof Error ? error.message : "Entity queue sync request failed";
    } finally {
      entityQueueSyncRunning.value = false;
      evaluateBackfillPolling();
    }
  }

  async function requeueFailedEntityQueue() {
    entityQueueRequeueRunning.value = true;
    entityQueueError.value = "";
    try {
      const res = await apiPost("/api/admin/journal-entry-entities/requeue-failed");
      assertOk(res, "Entity queue failed-row requeue request failed");
      await fetchEntityQueueStats();
    } catch (error) {
      entityQueueError.value = error instanceof Error ? error.message : "Entity queue failed-row requeue request failed";
    } finally {
      entityQueueRequeueRunning.value = false;
      evaluateBackfillPolling();
    }
  }

  async function syncHolyday(yy: string) {
    const fd = new FormData();
    fd.append("yy", yy);
    const res = await apiPost("/api/holyday/get-holyday-account.do", fd);
    assertOk(res, t("admin.page.holyday.sync.failure"));
    return res.message ?? t("common.result.processed");
  }

  async function fetchNotion(dataType: string, dataId: string) {
    const res = await apiGet("/api/notion/notion.do", { params: { dataType, dataId } });
    assertOk(res, t("admin.page.notion.failure"));
    return res;
  }

  async function fetchCacheMap() {
    cacheLoading.value = true;
    try {
      const res = await apiGet("/api/cache/cache-active-map");
      cacheMap.value = ((res as { rsltMap?: CacheMap }).rsltMap ?? {}) as CacheMap;
    } finally {
      cacheLoading.value = false;
    }
  }

  async function fetchCacheDetail(cacheName: string, cacheKey: string) {
    cacheLoading.value = true;
    try {
      const res = await apiGet("/api/cache/cache-active-dtl", { params: { cacheName, cacheKey } });
      cacheDetail.value = (res.rsltObj ?? null) as CacheDetail;
    } finally {
      cacheLoading.value = false;
    }
  }

  async function clearCacheByName(cacheName: string) {
    const fd = new FormData();
    fd.append("cacheName", cacheName);
    const res = await apiPost("/api/cache/cache-clear-by-nm", fd);
    assertOk(res, t("admin.page.cache.delete.failure"));
    const next = { ...cacheMap.value };
    delete next[cacheName];
    cacheMap.value = next;
  }

  async function evictCacheEntry(cacheName: string, cacheKey: string) {
    const fd = new FormData();
    fd.append("cacheName", cacheName);
    fd.append("cacheKey", cacheKey);
    const res = await apiPost("/api/cache/cache-evict", fd);
    assertOk(res, t("admin.page.cache.item.delete.failure"));
    const cache = { ...(cacheMap.value[cacheName] ?? {}) };
    delete cache[cacheKey];
    cacheMap.value = { ...cacheMap.value, [cacheName]: cache };
  }

  async function clearAllCaches() {
    const res = await apiPost("/api/cache-clear");
    assertOk(res, t("admin.page.cache.all.delete.failure"));
    cacheMap.value = {};
    return res.message ?? t("common.result.processed");
  }


  async function fetchChatRagSettings() {
    chatRagSettingsLoading.value = true;
    chatRagSettingsError.value = "";
    try {
      const res = await apiGet("/admin/chat/settings");
      assertOk(res, "Failed to load chat settings");
      const obj = (res.rsltObj ?? {}) as Record<string, unknown>;
      chatRagSettings.value = {
        ragEnabled: obj.ragEnabled !== false,
        ragTopK: Number(obj.ragTopK ?? 5),
        ragMinScore: Number(obj.ragMinScore ?? 0.35),
        ragSummaryTopK: Number(obj.ragSummaryTopK ?? 12),
        ragSynthesisTopK: Number(obj.ragSynthesisTopK ?? 25),
        ragStanceTopK: Number(obj.ragStanceTopK ?? 50),
        ragSynthesisMinScore: Number(obj.ragSynthesisMinScore ?? 0.25),
      };
    } catch (error) {
      chatRagSettingsError.value =
        error instanceof Error ? error.message : "Failed to load chat settings";
    } finally {
      chatRagSettingsLoading.value = false;
    }
  }

  async function saveChatRagSettings() {
    chatRagSettingsSaving.value = true;
    chatRagSettingsError.value = "";
    try {
      const res = await apiPatch("/admin/chat/settings", {
        ragEnabled: chatRagSettings.value.ragEnabled,
        ragTopK: chatRagSettings.value.ragTopK,
        ragMinScore: chatRagSettings.value.ragMinScore,
        ragSummaryTopK: chatRagSettings.value.ragSummaryTopK,
        ragSynthesisTopK: chatRagSettings.value.ragSynthesisTopK,
        ragStanceTopK: chatRagSettings.value.ragStanceTopK,
        ragSynthesisMinScore: chatRagSettings.value.ragSynthesisMinScore,
      });
      assertOk(res, "Failed to save chat settings");
      const obj = (res.rsltObj ?? {}) as Record<string, unknown>;
      chatRagSettings.value = {
        ragEnabled: obj.ragEnabled !== false,
        ragTopK: Number(obj.ragTopK ?? chatRagSettings.value.ragTopK),
        ragMinScore: Number(obj.ragMinScore ?? chatRagSettings.value.ragMinScore),
        ragSummaryTopK: Number(obj.ragSummaryTopK ?? chatRagSettings.value.ragSummaryTopK),
        ragSynthesisTopK: Number(obj.ragSynthesisTopK ?? chatRagSettings.value.ragSynthesisTopK),
        ragStanceTopK: Number(obj.ragStanceTopK ?? chatRagSettings.value.ragStanceTopK),
        ragSynthesisMinScore: Number(obj.ragSynthesisMinScore ?? chatRagSettings.value.ragSynthesisMinScore),
      };
      return res.message ?? "Saved";
    } catch (error) {
      chatRagSettingsError.value =
        error instanceof Error ? error.message : "Failed to save chat settings";
      throw error;
    } finally {
      chatRagSettingsSaving.value = false;
    }
  }

  async function fetchJournalSetting() {
    journalSettingLoading.value = true;
    journalSettingError.value = "";
    try {
      const res = await apiGet<{ aiEnabled?: boolean }>("/api/journal/settings");
      assertOk(res, "Failed to load journal settings");
      journalSettingAiEnabled.value = res.rsltObj?.aiEnabled !== false;
    } catch (error) {
      journalSettingError.value = error instanceof Error ? error.message : "Failed to load journal settings";
    } finally {
      journalSettingLoading.value = false;
    }
  }

  async function saveJournalSetting() {
    journalSettingSaving.value = true;
    journalSettingError.value = "";
    try {
      const res = await apiPut<{ aiEnabled?: boolean }>("/api/journal/settings", {
        aiEnabled: journalSettingAiEnabled.value,
      });
      assertOk(res, "Failed to save journal settings");
      journalSettingAiEnabled.value = res.rsltObj?.aiEnabled !== false;
    } catch (error) {
      journalSettingError.value = error instanceof Error ? error.message : "Failed to save journal settings";
      throw error;
    } finally {
      journalSettingSaving.value = false;
    }
  }

  return {
    meta,
    roles,
    bootstrapLoading,
    embeddingStats,
    embeddingStatsLoading,
    embeddingStatsError,
    embeddingSyncRunning,
    embeddingRequeueRunning,
    embeddingSyncResult,
    embeddingQualityEvalRunning,
    embeddingQualityEvalError,
    embeddingQualityEvalReport,
    ollamaHealth,
    ollamaHealthError,
    chatRagSettings,
    chatRagSettingsLoading,
    chatRagSettingsSaving,
    chatRagSettingsError,
    entityQueueStats,
    entityQueueStatsLoading,
    entityQueueError,
    entityQueueSyncRunning,
    entityQueueSyncResult,
    entityQueueRequeueRunning,
    backfillWorkActive,
    journalSettingAiEnabled,
    journalSettingLoading,
    journalSettingSaving,
    journalSettingError,
    cacheMap,
    cacheDetail,
    cacheLoading,
    yearOptions,
    fetchBootstrap,
    fetchJournalSetting,
    saveJournalSetting,
    fetchEmbeddingStats,
    fetchOllamaHealth,
    fetchChatRagSettings,
    saveChatRagSettings,
    syncEmbeddingQueue,
    requeueFailedEmbeddingQueue,
    runEmbeddingQualityEval,
    fetchEntityQueueStats,
    syncEntityQueue,
    requeueFailedEntityQueue,
    syncHolyday,
    fetchNotion,
    fetchCacheMap,
    fetchCacheDetail,
    clearCacheByName,
    evictCacheEntry,
    clearAllCaches,
  };
});
