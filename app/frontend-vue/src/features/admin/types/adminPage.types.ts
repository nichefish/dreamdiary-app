/**
 * Admin page API/view contracts and response normalizers.
 *
 * <p>Separated from {@link ./adminPage.ts} so the Pinia store stays actions/state only.</p>
 */

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
  queueRows: number;
  unqueuedEntries: number;
  pending: number;
  processing: number;
  embedded: number;
  failed: number;
  skipped: number;
  remaining: number;
  completed: number;
  completionRate: number;
  vectorizedRate: number;
  queueCompletionRate: number;
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

/** Admin Ollama runtime health */
export interface OllamaHealth {
  status: string;
  reachable: boolean;
  baseUrl: string;
  chatModelRequired: string;
  embeddingModelRequired: string;
  chatModelReady: boolean;
  embeddingModelReady: boolean;
  installedModels: string[];
  errorMessage: string | null;
  latencyMs: number;
}

export function normalizeOllamaHealth(
  health: Partial<OllamaHealth> | null | undefined
): OllamaHealth | null {
  if (health == null) return null;
  return {
    status: String(health.status ?? "DOWN"),
    reachable: Boolean(health.reachable),
    baseUrl: String(health.baseUrl ?? ""),
    chatModelRequired: String(health.chatModelRequired ?? ""),
    embeddingModelRequired: String(health.embeddingModelRequired ?? ""),
    chatModelReady: Boolean(health.chatModelReady),
    embeddingModelReady: Boolean(health.embeddingModelReady),
    installedModels: Array.isArray(health.installedModels)
      ? health.installedModels.map((name) => String(name ?? ""))
      : [],
    errorMessage: health.errorMessage == null ? null : String(health.errorMessage),
    latencyMs: Number(health.latencyMs ?? 0),
  };
}

/** Admin embedding quality eval — single case row */
export interface EmbeddingQualityEvalCase {
  caseId: string;
  description: string;
  expectation: string;
  passed: boolean;
  metric: number | null;
  comparisonMetric: number | null;
  detail: string | null;
}

/** Admin embedding quality eval — suite summary */
export interface EmbeddingQualityEvalSuite {
  code: string;
  description: string;
  passCriteria: string;
  passedCount: number;
  failedCount: number;
  suitePassed: boolean;
  cases: EmbeddingQualityEvalCase[];
}

/** Admin embedding quality eval — SKIPPED sample */
export interface EmbeddingSkippedSample {
  journalEntryId: number | null;
  errorMessage: string | null;
}

/** Admin embedding quality eval report */
export interface EmbeddingQualityEvalReport {
  embeddingModel: string;
  embeddedCount: number;
  cachedVectorCount: number;
  vectorDimension: number | null;
  skippedCount: number;
  skippedSamples: EmbeddingSkippedSample[];
  ollamaHealth: OllamaHealth | null;
  suites: EmbeddingQualityEvalSuite[];
  overallPassed: boolean;
  recommendation: string;
  summary: string;
  elapsedMs: number;
}

export function normalizeEmbeddingQualityEvalReport(
  report: Partial<EmbeddingQualityEvalReport> | null | undefined
): EmbeddingQualityEvalReport {
  return {
    embeddingModel: String(report?.embeddingModel ?? ""),
    embeddedCount: Number(report?.embeddedCount ?? 0),
    cachedVectorCount: Number(report?.cachedVectorCount ?? 0),
    vectorDimension: report?.vectorDimension == null ? null : Number(report.vectorDimension),
    skippedCount: Number(report?.skippedCount ?? 0),
    skippedSamples: Array.isArray(report?.skippedSamples)
      ? report!.skippedSamples.map((sample) => ({
          journalEntryId: sample?.journalEntryId == null ? null : Number(sample.journalEntryId),
          errorMessage: sample?.errorMessage == null ? null : String(sample.errorMessage),
        }))
      : [],
    ollamaHealth: normalizeOllamaHealth(report?.ollamaHealth as Partial<OllamaHealth> | null | undefined),
    suites: Array.isArray(report?.suites)
      ? report!.suites.map((suite) => ({
          code: String(suite?.code ?? ""),
          description: String(suite?.description ?? ""),
          passCriteria: String(suite?.passCriteria ?? ""),
          passedCount: Number(suite?.passedCount ?? 0),
          failedCount: Number(suite?.failedCount ?? 0),
          suitePassed: Boolean(suite?.suitePassed),
          cases: Array.isArray(suite?.cases)
            ? suite!.cases.map((item) => ({
                caseId: String(item?.caseId ?? ""),
                description: String(item?.description ?? ""),
                expectation: String(item?.expectation ?? ""),
                passed: Boolean(item?.passed),
                metric: item?.metric == null ? null : Number(item.metric),
                comparisonMetric: item?.comparisonMetric == null ? null : Number(item.comparisonMetric),
                detail: item?.detail == null ? null : String(item.detail),
              }))
            : [],
        }))
      : [],
    overallPassed: Boolean(report?.overallPassed),
    recommendation: String(report?.recommendation ?? ""),
    summary: String(report?.summary ?? ""),
    elapsedMs: Number(report?.elapsedMs ?? 0),
  };
}

export interface EntityQueueStats {
  total: number;
  queueRows: number;
  unqueuedEntries: number;
  pending: number;
  processing: number;
  synced: number;
  failed: number;
  skipped: number;
  remaining: number;
  completed: number;
  completionRate: number;
  queueCompletionRate: number;
}

export interface EntityQueueSyncResult {
  activeEntryCount: number;
  queueCountBefore: number;
  created: number;
  requeued: number;
  unchanged: number;
  removed: number;
  queueCountAfter: number;
}

export type CacheMap = Record<string, Record<string, unknown>>;
export type CacheDetail = Record<string, unknown> | unknown[] | string | number | boolean | null;

export const DEFAULT_ADMIN_PAGE_META: AdminPageMeta = {
  authMngrKey: "MNGR",
  authUserKey: "USER",
  authDevKey: "DEV",
  currYy: new Date().getFullYear(),
};

export function emptyEmbeddingStats(): EmbeddingStats {
  return {
    total: 0,
    queueRows: 0,
    unqueuedEntries: 0,
    pending: 0,
    processing: 0,
    embedded: 0,
    failed: 0,
    skipped: 0,
    remaining: 0,
    completed: 0,
    completionRate: 0,
    vectorizedRate: 0,
    queueCompletionRate: 0,
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

export function normalizeEmbeddingSyncResult(
  result: Partial<EmbeddingSyncResult> | null | undefined
): EmbeddingSyncResult {
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

export function normalizeEmbeddingStats(stats: Partial<EmbeddingStats> | null | undefined): EmbeddingStats {
  return {
    total: Number(stats?.total || 0),
    queueRows: Number(stats?.queueRows || 0),
    unqueuedEntries: Number(stats?.unqueuedEntries || 0),
    pending: Number(stats?.pending || 0),
    processing: Number(stats?.processing || 0),
    embedded: Number(stats?.embedded || 0),
    failed: Number(stats?.failed || 0),
    skipped: Number(stats?.skipped || 0),
    remaining: Number(stats?.remaining || 0),
    completed: Number(stats?.completed || 0),
    completionRate: Number(stats?.completionRate || 0),
    vectorizedRate: Number(stats?.vectorizedRate || 0),
    queueCompletionRate: Number(stats?.queueCompletionRate || 0),
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

export function normalizeEmbeddingSyncJobStatus(
  status: Partial<EmbeddingSyncJobStatus> | null | undefined
): EmbeddingSyncJobStatus {
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

export function emptyEntityQueueStats(): EntityQueueStats {
  return {
    total: 0,
    queueRows: 0,
    unqueuedEntries: 0,
    pending: 0,
    processing: 0,
    synced: 0,
    failed: 0,
    skipped: 0,
    remaining: 0,
    completed: 0,
    completionRate: 0,
    queueCompletionRate: 0,
  };
}

export function normalizeEntityQueueStats(stats: Partial<EntityQueueStats> | null | undefined): EntityQueueStats {
  return {
    total: Number(stats?.total || 0),
    queueRows: Number(stats?.queueRows || 0),
    unqueuedEntries: Number(stats?.unqueuedEntries || 0),
    pending: Number(stats?.pending || 0),
    processing: Number(stats?.processing || 0),
    synced: Number(stats?.synced || 0),
    failed: Number(stats?.failed || 0),
    skipped: Number(stats?.skipped || 0),
    remaining: Number(stats?.remaining || 0),
    completed: Number(stats?.completed || 0),
    completionRate: Number(stats?.completionRate || 0),
    queueCompletionRate: Number(stats?.queueCompletionRate || 0),
  };
}

export function normalizeEntityQueueSyncResult(
  result: Partial<EntityQueueSyncResult> | null | undefined
): EntityQueueSyncResult {
  return {
    activeEntryCount: Number(result?.activeEntryCount || 0),
    queueCountBefore: Number(result?.queueCountBefore || 0),
    created: Number(result?.created || 0),
    requeued: Number(result?.requeued || 0),
    unchanged: Number(result?.unchanged || 0),
    removed: Number(result?.removed || 0),
    queueCountAfter: Number(result?.queueCountAfter || 0),
  };
}
