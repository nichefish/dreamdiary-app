import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";

export interface LogListRow {
  id: number;
  rnum?: number;
  logDt?: string;
  logUserNm?: string;
  username?: string;
  traceId?: string;
  httpMethod?: string;
  httpStatus?: number;
  durationMs?: number;
  logType?: string;
  signature?: string;
  ipAddr?: string;
  actionTyNm?: string;
  actvtyCtgrNm?: string;
  requestUri?: string;
  rsltMsg?: string;
  rslt?: string | boolean;
  success?: boolean;
  exceptionNm?: string;
  exceptionMsg?: string;
}

export interface LogDetail extends LogListRow {
  title?: string;
  url?: string;
  referer?: string;
  param?: string;
  content?: string;
}

export interface LogStatsUserRow {
  rnum?: number;
  userNm?: string;
  username?: string;
  userProflYn?: string;
  retireYn?: string;
  roleKey?: string;
  roleName?: string;
  actvtyCnt?: number;
  url?: string;
  param?: string;
  content?: string;
  rslt?: string | boolean;
}

export const useLogAdminStore = defineStore("logAdmin", () => {
  const rows = ref<LogListRow[]>([]);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const keyword = ref("");
  const searchType = ref("requestUri");
  const resultFilter = ref("");
  const slowOnly = ref(false);
  const exceptionOnly = ref(false);
  const selectedTraceId = ref("");
  const loading = ref(false);
  const error = ref("");

  const detailOpen = ref(false);
  const detailLoading = ref(false);
  const detail = ref<LogDetail | null>(null);

  const statsUserRows = ref<LogStatsUserRow[]>([]);
  const statsAnonymousRows = ref<LogStatsUserRow[]>([]);
  const statsRows = computed(() => [...statsUserRows.value, ...statsAnonymousRows.value]);
  const pageFailureCount = computed(() => rows.value.filter((row) => !isSuccess(row.rslt, row.success)).length);
  const pageSlowCount = computed(() => rows.value.filter((row) => Number(row.durationMs ?? 0) >= 1000).length);
  const pageAvgDurationMs = computed(() => {
    if (!rows.value.length) return 0;
    const total = rows.value.reduce((sum, row) => sum + Number(row.durationMs ?? 0), 0);
    return Math.round(total / rows.value.length);
  });
  const selectedTraceRows = computed(() => {
    if (!selectedTraceId.value) return [];
    return rows.value.filter((row) => row.traceId === selectedTraceId.value);
  });

  function isSuccess(value: string | boolean | undefined, fallback?: boolean): boolean {
    if (typeof value === "boolean") return value;
    if (typeof value === "string") return value.toLowerCase() === "true" || value === "Y";
    return Boolean(fallback);
  }

  async function fetchLogs(page?: number) {
    loading.value = true;
    error.value = "";
    const targetPage = page ?? currentPage.value;
    try {
      const params: Record<string, unknown> = {
        page: targetPage,
        size: pageSize.value,
      };
      if (keyword.value.trim()) {
        params.searchType = searchType.value;
        params.searchKeyword = keyword.value.trim();
      }
      if (resultFilter.value) params.rslt = resultFilter.value;
      if (slowOnly.value) params.minDurationMs = 1000;
      if (exceptionOnly.value) params.hasException = true;

      const res = await axios.get("/api/logs", { params });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "로그 목록을 불러오지 못했습니다.");

      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
      if (!rows.value.some((row) => row.traceId === selectedTraceId.value)) {
        selectedTraceId.value = rows.value[0]?.traceId ?? "";
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : "로그 목록을 불러오지 못했습니다.";
      rows.value = [];
      totalElements.value = 0;
      totalPages.value = 0;
    } finally {
      loading.value = false;
    }
  }

  async function changePageSize(size: number) {
    pageSize.value = size;
    await fetchLogs(0);
  }

  function selectTrace(traceId?: string) {
    selectedTraceId.value = traceId ?? "";
  }

  async function filterByTrace(traceId?: string) {
    if (!traceId) return;
    searchType.value = "traceId";
    keyword.value = traceId;
    selectedTraceId.value = traceId;
    await fetchLogs(0);
  }

  async function clearFilters() {
    keyword.value = "";
    searchType.value = "requestUri";
    resultFilter.value = "";
    slowOnly.value = false;
    exceptionOnly.value = false;
    await fetchLogs(0);
  }

  async function openDetail(id: number) {
    if (!await assertAuthenticatedBeforeModal()) return;
    detailOpen.value = true;
    detailLoading.value = true;
    try {
      const res = await axios.get(`/api/logs/${id}`);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "로그 상세를 불러오지 못했습니다.");
      detail.value = res.data?.rsltObj ?? null;
    } finally {
      detailLoading.value = false;
    }
  }

  function closeDetail() {
    detailOpen.value = false;
    detail.value = null;
  }

  return {
    rows,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    keyword,
    searchType,
    resultFilter,
    slowOnly,
    exceptionOnly,
    selectedTraceId,
    loading,
    error,
    detailOpen,
    detailLoading,
    detail,
    statsUserRows,
    statsAnonymousRows,
    statsRows,
    pageFailureCount,
    pageSlowCount,
    pageAvgDurationMs,
    selectedTraceRows,
    fetchLogs,
    changePageSize,
    selectTrace,
    filterByTrace,
    clearFilters,
    openDetail,
    closeDetail,
  };
});
