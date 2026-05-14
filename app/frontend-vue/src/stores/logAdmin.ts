import { computed, ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

export interface LogListRow {
  id: number;
  rnum?: number;
  logDt?: string;
  logUserNm?: string;
  username?: string;
  ipAddr?: string;
  actionTyNm?: string;
  actvtyCtgrNm?: string;
  requestUri?: string;
  rsltMsg?: string;
  rslt?: string | boolean;
  success?: boolean;
}

export interface LogDetail extends LogListRow {
  title?: string;
  httpMethod?: string;
  url?: string;
  referer?: string;
  param?: string;
  content?: string;
  exceptionNm?: string;
  exceptionMsg?: string;
  durationMs?: number;
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
  const resultFilter = ref("");
  const loading = ref(false);
  const error = ref("");

  const detailOpen = ref(false);
  const detailLoading = ref(false);
  const detail = ref<LogDetail | null>(null);

  const statsUserRows = ref<LogStatsUserRow[]>([]);
  const statsAnonymousRows = ref<LogStatsUserRow[]>([]);
  const statsRows = computed(() => [...statsUserRows.value, ...statsAnonymousRows.value]);

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
        params.searchType = "requestUri";
        params.searchKeyword = keyword.value.trim();
      }
      if (resultFilter.value) params.rslt = resultFilter.value;

      const res = await axios.get("/api/logs", { params });
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "로그 목록을 불러오지 못했습니다.");

      const pageResult = res.data?.rsltObj ?? {};
      rows.value = Array.isArray(pageResult.content) ? pageResult.content : [];
      totalElements.value = Number(pageResult.totalElements ?? 0);
      totalPages.value = Number(pageResult.totalPages ?? 0);
      currentPage.value = Number(pageResult.number ?? targetPage);
      pageSize.value = Number(pageResult.size ?? pageSize.value);
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

  async function openDetail(id: number) {
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
    resultFilter,
    loading,
    error,
    detailOpen,
    detailLoading,
    detail,
    statsUserRows,
    statsAnonymousRows,
    statsRows,
    fetchLogs,
    changePageSize,
    openDetail,
    closeDetail,
  };
});
