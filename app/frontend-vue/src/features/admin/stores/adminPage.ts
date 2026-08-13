import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
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
  const journalSettingEmbeddingEnabled = ref(true);
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
      const res = await axios.get("/api/admin/page/bootstrap");
      const payload = res.data?.rsltObj ?? {};
      meta.value = { ...DEFAULT_ADMIN_PAGE_META, ...(payload.meta ?? {}) };
      roles.value = Array.isArray(payload.roleList) ? payload.roleList : [];
    } finally {
      bootstrapLoading.value = false;
    }
  }

  async function fetchOllamaHealth() {
    ollamaHealthError.value = "";
    try {
      const res = await axios.get("/api/admin/ollama/health");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Ollama health request failed");
      ollamaHealth.value = normalizeOllamaHealth(res.data.rsltObj);
    } catch (error) {
      ollamaHealthError.value = error instanceof Error ? error.message : "Ollama health request failed";
    }
  }

  async function fetchEmbeddingStats() {
    embeddingStatsLoading.value = true;
    embeddingStatsError.value = "";
    try {
      const [statsRes] = await Promise.all([
        axios.get("/api/admin/journal-entry-embeddings/stats"),
        fetchOllamaHealth(),
      ]);
      if (!statsRes.data?.rslt) throw new Error(statsRes.data?.message ?? "Embedding stats request failed");
      embeddingStats.value = normalizeEmbeddingStats(statsRes.data.rsltObj);
      embeddingSyncResult.value = embeddingStats.value.syncResult;
    } catch (error) {
      embeddingStatsError.value = error instanceof Error ? error.message : "Embedding stats request failed";
    } finally {
      embeddingStatsLoading.value = false;
      evaluateBackfillPolling();
    }
  }

  async function syncEmbeddingQueue() {
    embeddingSyncRunning.value = true;
    embeddingStatsError.value = "";
    try {
      const res = await axios.post("/api/admin/journal-entry-embeddings/sync");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Embedding sync request failed");
      const status = normalizeEmbeddingSyncJobStatus(res.data.rsltObj);
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
    embeddingRequeueRunning.value = true;
    embeddingStatsError.value = "";
    try {
      const res = await axios.post("/api/admin/journal-entry-embeddings/requeue-failed");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Embedding requeue request failed");
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
      const res = await axios.get("/api/admin/journal-entry-embeddings/quality-eval");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Embedding quality eval failed");
      embeddingQualityEvalReport.value = normalizeEmbeddingQualityEvalReport(res.data.rsltObj);
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
      const res = await axios.get("/api/admin/journal-entry-entities/stats");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Entity queue stats request failed");
      entityQueueStats.value = normalizeEntityQueueStats(res.data.rsltObj);
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
      const res = await axios.post("/api/admin/journal-entry-entities/sync");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Entity queue sync request failed");
      entityQueueSyncResult.value = normalizeEntityQueueSyncResult(res.data.rsltObj);
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
      const res = await axios.post("/api/admin/journal-entry-entities/requeue-failed");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Entity queue failed-row requeue request failed");
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
    const res = await axios.post("/api/holyday/get-holyday-account.do", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.page.holyday.sync.failure"));
    return res.data?.message ?? t("common.result.processed");
  }

  async function fetchNotion(dataType: string, dataId: string) {
    const res = await axios.get("/api/notion/notion.do", { params: { dataType, dataId } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.page.notion.failure"));
    return res.data;
  }

  async function fetchCacheMap() {
    cacheLoading.value = true;
    try {
      const res = await axios.get("/api/cache/cache-active-map");
      cacheMap.value = (res.data?.rsltMap ?? {}) as CacheMap;
    } finally {
      cacheLoading.value = false;
    }
  }

  async function fetchCacheDetail(cacheName: string, cacheKey: string) {
    cacheLoading.value = true;
    try {
      const res = await axios.get("/api/cache/cache-active-dtl", { params: { cacheName, cacheKey } });
      cacheDetail.value = (res.data?.rsltObj ?? null) as CacheDetail;
    } finally {
      cacheLoading.value = false;
    }
  }

  async function clearCacheByName(cacheName: string) {
    const fd = new FormData();
    fd.append("cacheName", cacheName);
    const res = await axios.post("/api/cache/cache-clear-by-nm", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.page.cache.delete.failure"));
    const next = { ...cacheMap.value };
    delete next[cacheName];
    cacheMap.value = next;
  }

  async function evictCacheEntry(cacheName: string, cacheKey: string) {
    const fd = new FormData();
    fd.append("cacheName", cacheName);
    fd.append("cacheKey", cacheKey);
    const res = await axios.post("/api/cache/cache-evict", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.page.cache.item.delete.failure"));
    const cache = { ...(cacheMap.value[cacheName] ?? {}) };
    delete cache[cacheKey];
    cacheMap.value = { ...cacheMap.value, [cacheName]: cache };
  }

  async function clearAllCaches() {
    const res = await axios.post("/api/cache-clear");
    if (!res.data?.rslt) throw new Error(res.data?.message ?? t("admin.page.cache.all.delete.failure"));
    cacheMap.value = {};
    return res.data?.message ?? t("common.result.processed");
  }


  async function fetchChatRagSettings() {
    chatRagSettingsLoading.value = true;
    chatRagSettingsError.value = "";
    try {
      const res = await axios.get("/admin/chat/settings");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Failed to load chat settings");
      const obj = res.data.rsltObj ?? {};
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
      const res = await axios.patch("/admin/chat/settings", {
        ragEnabled: chatRagSettings.value.ragEnabled,
        ragTopK: chatRagSettings.value.ragTopK,
        ragMinScore: chatRagSettings.value.ragMinScore,
        ragSummaryTopK: chatRagSettings.value.ragSummaryTopK,
        ragSynthesisTopK: chatRagSettings.value.ragSynthesisTopK,
        ragStanceTopK: chatRagSettings.value.ragStanceTopK,
        ragSynthesisMinScore: chatRagSettings.value.ragSynthesisMinScore,
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Failed to save chat settings");
      const obj = res.data.rsltObj ?? {};
      chatRagSettings.value = {
        ragEnabled: obj.ragEnabled !== false,
        ragTopK: Number(obj.ragTopK ?? chatRagSettings.value.ragTopK),
        ragMinScore: Number(obj.ragMinScore ?? chatRagSettings.value.ragMinScore),
        ragSummaryTopK: Number(obj.ragSummaryTopK ?? chatRagSettings.value.ragSummaryTopK),
        ragSynthesisTopK: Number(obj.ragSynthesisTopK ?? chatRagSettings.value.ragSynthesisTopK),
        ragStanceTopK: Number(obj.ragStanceTopK ?? chatRagSettings.value.ragStanceTopK),
        ragSynthesisMinScore: Number(obj.ragSynthesisMinScore ?? chatRagSettings.value.ragSynthesisMinScore),
      };
      return res.data?.message ?? "Saved";
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
      const res = await axios.get("/api/journal/settings");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Failed to load journal settings");
      journalSettingEmbeddingEnabled.value = res.data.rsltObj?.embeddingEnabled !== false;
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
      const res = await axios.put("/api/journal/settings", {
        embeddingEnabled: journalSettingEmbeddingEnabled.value,
      });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Failed to save journal settings");
      journalSettingEmbeddingEnabled.value = res.data.rsltObj?.embeddingEnabled !== false;
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
    journalSettingEmbeddingEnabled,
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
