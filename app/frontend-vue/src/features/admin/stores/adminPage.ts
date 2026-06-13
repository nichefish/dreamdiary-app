import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import {
  DEFAULT_ADMIN_PAGE_META,
  emptyEmbeddingStats,
  emptyEntityQueueStats,
  normalizeEmbeddingStats,
  normalizeEmbeddingSyncJobStatus,
  normalizeEntityQueueStats,
  normalizeEntityQueueSyncResult,
  type AdminPageMeta,
  type CacheDetail,
  type CacheMap,
  type EmbeddingStats,
  type EmbeddingSyncResult,
  type EntityQueueStats,
  type EntityQueueSyncResult,
  type RoleRow,
} from "@/features/admin/types/adminPage.types";

export type {
  AdminPageMeta,
  RoleRow,
  EmbeddingStats,
  EmbeddingSyncResult,
  EmbeddingSyncJobStatus,
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
  const meta = ref<AdminPageMeta>({ ...DEFAULT_ADMIN_PAGE_META });
  const roles = ref<RoleRow[]>([]);
  const bootstrapLoading = ref(false);

  const embeddingStats = ref<EmbeddingStats>(emptyEmbeddingStats());
  const embeddingStatsLoading = ref(false);
  const embeddingStatsError = ref("");
  const embeddingSyncRunning = ref(false);
  const embeddingRequeueRunning = ref(false);
  const embeddingSyncResult = ref<EmbeddingSyncResult | null>(null);
  const entityQueueStats = ref<EntityQueueStats>(emptyEntityQueueStats());
  const entityQueueStatsLoading = ref(false);
  const entityQueueError = ref("");
  const entityQueueSyncRunning = ref(false);
  const entityQueueSyncResult = ref<EntityQueueSyncResult | null>(null);
  const entityQueueRequeueRunning = ref(false);

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

  async function fetchEmbeddingStats() {
    embeddingStatsLoading.value = true;
    embeddingStatsError.value = "";
    try {
      const res = await axios.get("/api/admin/journal-entry-embeddings/stats");
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "Embedding stats request failed");
      embeddingStats.value = normalizeEmbeddingStats(res.data.rsltObj);
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
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "휴일 정보를 동기화하지 못했습니다.");
    return res.data?.message ?? "처리되었습니다.";
  }

  async function fetchNotion(dataType: string, dataId: string) {
    const res = await axios.get("/api/notion/notion.do", { params: { dataType, dataId } });
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "Notion 요청에 실패했습니다.");
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
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "캐시를 삭제하지 못했습니다.");
    const next = { ...cacheMap.value };
    delete next[cacheName];
    cacheMap.value = next;
  }

  async function evictCacheEntry(cacheName: string, cacheKey: string) {
    const fd = new FormData();
    fd.append("cacheName", cacheName);
    fd.append("cacheKey", cacheKey);
    const res = await axios.post("/api/cache/cache-evict", fd);
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "캐시 항목을 삭제하지 못했습니다.");
    const cache = { ...(cacheMap.value[cacheName] ?? {}) };
    delete cache[cacheKey];
    cacheMap.value = { ...cacheMap.value, [cacheName]: cache };
  }

  async function clearAllCaches() {
    const res = await axios.post("/api/cache-clear");
    if (!res.data?.rslt) throw new Error(res.data?.message ?? "전체 캐시를 삭제하지 못했습니다.");
    cacheMap.value = {};
    return res.data?.message ?? "처리되었습니다.";
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
    entityQueueStats,
    entityQueueStatsLoading,
    entityQueueError,
    entityQueueSyncRunning,
    entityQueueSyncResult,
    entityQueueRequeueRunning,
    backfillWorkActive,
    cacheMap,
    cacheDetail,
    cacheLoading,
    yearOptions,
    fetchBootstrap,
    fetchEmbeddingStats,
    syncEmbeddingQueue,
    requeueFailedEmbeddingQueue,
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
