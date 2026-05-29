import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

export interface AdminPageMeta {
  authMngrKey: string;
  authUserKey: string;
  authDevKey: string;
  currYy: number;
}

export interface RoleRow {
  id: number;
  roleKey: string;
  roleName: string;
  authLevel: number | null;
  parentRoleId: number | null;
  sortOrder: number | null;
  useYn: string;
}

export interface EmbeddingStats {
  total: number;
  pending: number;
  processing: number;
  embedded: number;
  failed: number;
  skipped: number;
  remaining: number;
  completed: number;
  completionRate: number;
  vectorizedRate: number;
  syncRunning: boolean;
  syncPhase: string;
  syncProcessed: number;
  syncTotal: number;
  syncStartedAt: string | null;
  syncFinishedAt: string | null;
  syncResult: EmbeddingSyncResult | null;
  syncErrorMessage: string;
}

export interface EmbeddingSyncResult {
  activeEntryCount: number;
  activeEmbeddingCountBefore: number;
  created: number;
  requeued: number;
  unchanged: number;
  skipped: number;
  removed: number;
  activeEmbeddingCountAfter: number;
}

export interface EmbeddingSyncJobStatus {
  running: boolean;
  phase: string;
  startedAt: string | null;
  finishedAt: string | null;
  processed: number;
  total: number;
  result: EmbeddingSyncResult | null;
  errorMessage: string;
}

export type CacheMap = Record<string, Record<string, unknown>>;
export type CacheDetail = Record<string, unknown> | unknown[] | string | number | boolean | null;

const DEFAULT_META: AdminPageMeta = {
  authMngrKey: "MNGR",
  authUserKey: "USER",
  authDevKey: "DEV",
  currYy: new Date().getFullYear(),
};

function emptyEmbeddingStats(): EmbeddingStats {
  return {
    total: 0,
    pending: 0,
    processing: 0,
    embedded: 0,
    failed: 0,
    skipped: 0,
    remaining: 0,
    completed: 0,
    completionRate: 0,
    vectorizedRate: 0,
    syncRunning: false,
    syncPhase: "IDLE",
    syncProcessed: 0,
    syncTotal: 0,
    syncStartedAt: null,
    syncFinishedAt: null,
    syncResult: null,
    syncErrorMessage: "",
  };
}

function normalizeEmbeddingStats(stats: Partial<EmbeddingStats> | null | undefined): EmbeddingStats {
  return {
    total: Number(stats?.total || 0),
    pending: Number(stats?.pending || 0),
    processing: Number(stats?.processing || 0),
    embedded: Number(stats?.embedded || 0),
    failed: Number(stats?.failed || 0),
    skipped: Number(stats?.skipped || 0),
    remaining: Number(stats?.remaining || 0),
    completed: Number(stats?.completed || 0),
    completionRate: Number(stats?.completionRate || 0),
    vectorizedRate: Number(stats?.vectorizedRate || 0),
    syncRunning: Boolean(stats?.syncRunning),
    syncPhase: String(stats?.syncPhase || "IDLE"),
    syncProcessed: Number(stats?.syncProcessed || 0),
    syncTotal: Number(stats?.syncTotal || 0),
    syncStartedAt: typeof stats?.syncStartedAt === "string" ? stats.syncStartedAt : null,
    syncFinishedAt: typeof stats?.syncFinishedAt === "string" ? stats.syncFinishedAt : null,
    syncResult: stats?.syncResult ? normalizeEmbeddingSyncResult(stats.syncResult) : null,
    syncErrorMessage: String(stats?.syncErrorMessage || ""),
  };
}

function normalizeEmbeddingSyncResult(result: Partial<EmbeddingSyncResult> | null | undefined): EmbeddingSyncResult {
  return {
    activeEntryCount: Number(result?.activeEntryCount || 0),
    activeEmbeddingCountBefore: Number(result?.activeEmbeddingCountBefore || 0),
    created: Number(result?.created || 0),
    requeued: Number(result?.requeued || 0),
    unchanged: Number(result?.unchanged || 0),
    skipped: Number(result?.skipped || 0),
    removed: Number(result?.removed || 0),
    activeEmbeddingCountAfter: Number(result?.activeEmbeddingCountAfter || 0),
  };
}

function normalizeEmbeddingSyncJobStatus(status: Partial<EmbeddingSyncJobStatus> | null | undefined): EmbeddingSyncJobStatus {
  return {
    running: Boolean(status?.running),
    phase: String(status?.phase || "IDLE"),
    startedAt: typeof status?.startedAt === "string" ? status.startedAt : null,
    finishedAt: typeof status?.finishedAt === "string" ? status.finishedAt : null,
    processed: Number(status?.processed || 0),
    total: Number(status?.total || 0),
    result: status?.result ? normalizeEmbeddingSyncResult(status.result) : null,
    errorMessage: String(status?.errorMessage || ""),
  };
}

export const useAdminPageStore = defineStore("adminPage", () => {
  const meta = ref<AdminPageMeta>({ ...DEFAULT_META });
  const roles = ref<RoleRow[]>([]);
  const bootstrapLoading = ref(false);

  const embeddingStats = ref<EmbeddingStats>(emptyEmbeddingStats());
  const embeddingStatsLoading = ref(false);
  const embeddingStatsError = ref("");
  const embeddingSyncRunning = ref(false);
  const embeddingSyncResult = ref<EmbeddingSyncResult | null>(null);

  const cacheMap = ref<CacheMap>({});
  const cacheDetail = ref<CacheDetail>(null);
  const cacheLoading = ref(false);

  const yearOptions = computed(() => {
    const yy = Number(meta.value.currYy) || new Date().getFullYear();
    return [yy - 1, yy, yy + 1];
  });

  async function fetchBootstrap() {
    bootstrapLoading.value = true;
    try {
      const res = await axios.get("/api/admin/page/bootstrap");
      const payload = res.data?.rsltObj ?? {};
      meta.value = { ...DEFAULT_META, ...(payload.meta ?? {}) };
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
    embeddingSyncResult,
    cacheMap,
    cacheDetail,
    cacheLoading,
    yearOptions,
    fetchBootstrap,
    fetchEmbeddingStats,
    syncEmbeddingQueue,
    syncHolyday,
    fetchNotion,
    fetchCacheMap,
    fetchCacheDetail,
    clearCacheByName,
    evictCacheEntry,
    clearAllCaches,
  };
});
