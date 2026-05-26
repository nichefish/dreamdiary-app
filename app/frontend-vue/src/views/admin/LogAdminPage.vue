<template>
  <div class="log-admin-page">
    <div class="log-admin-toolbar">
      <div>
        <h2 class="mb-1">{{ isStatsView ? "사용자별 로그 통계" : "로그 관측" }}</h2>
        <div class="text-muted fs-7">
          {{ isStatsView ? "사용자별 활동 로그 통계를 확인합니다." : "실패, 지연, trace 흐름을 중심으로 운영 로그를 확인합니다." }}
        </div>
      </div>
      <div class="log-admin-actions">
        <RouterLink class="btn btn-sm" :class="!isStatsView ? 'btn-primary' : 'btn-light-primary'" to="/admin/log">
          <i class="bi bi-list-ul"></i>
          로그 목록
        </RouterLink>
        <RouterLink class="btn btn-sm" :class="isStatsView ? 'btn-primary' : 'btn-light-primary'" to="/admin/log/stats-user">
          <i class="bi bi-bar-chart"></i>
          사용자별 통계
        </RouterLink>
      </div>
    </div>

    <template v-if="isStatsView">
      <div class="alert alert-secondary mb-0">
        사용자별 로그 통계는 현재 연결된 데이터가 있으면 그대로 표시합니다.
      </div>
      <div class="card post">
        <div class="card-body">
          <div class="table-responsive">
            <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
              <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                  <th class="text-center hidden-table">번호</th>
                  <th>사용자</th>
                  <th class="hidden-table">권한</th>
                  <th class="text-center">활동수</th>
                  <th class="hidden-table">URL</th>
                  <th class="text-center hidden-table">결과</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!store.statsRows.length">
                  <td colspan="6" class="text-center text-muted py-8">표시할 사용자별 로그 통계 데이터가 없습니다.</td>
                </tr>
                <tr v-for="(row, index) in store.statsRows" :key="`${row.username ?? 'anonymous'}-${index}`">
                  <td class="text-center hidden-table text-gray-600">{{ row.rnum ?? index + 1 }}</td>
                  <td>
                    <div class="log-admin-primary">
                      <strong>{{ row.userNm || row.username || "비회원" }}</strong>
                      <span>{{ row.username || "-" }}</span>
                    </div>
                  </td>
                  <td class="hidden-table">{{ row.roleName || row.roleKey || "-" }}</td>
                  <td class="text-center">{{ formatNumber(row.actvtyCnt) }}</td>
                  <td class="hidden-table">
                    <div class="log-admin-ellipsis">{{ row.url || "-" }}</div>
                  </td>
                  <td class="text-center hidden-table">
                    <span class="badge" :class="isSuccess(row.rslt) ? 'badge-light-success' : 'badge-light-danger'">
                      {{ isSuccess(row.rslt) ? "성공" : "실패" }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="log-admin-metrics">
        <div class="log-admin-metric">
          <span>전체</span>
          <strong>{{ formatNumber(store.totalElements) }}</strong>
        </div>
        <div class="log-admin-metric danger">
          <span>현재 페이지 실패</span>
          <strong>{{ formatNumber(store.pageFailureCount) }}</strong>
        </div>
        <div class="log-admin-metric warning">
          <span>현재 페이지 느림</span>
          <strong>{{ formatNumber(store.pageSlowCount) }}</strong>
        </div>
        <div class="log-admin-metric">
          <span>평균 응답</span>
          <strong>{{ formatNumber(store.pageAvgDurationMs) }} ms</strong>
        </div>
      </div>

      <div class="card post">
        <div class="card-body">
          <div class="log-admin-listbar">
            <div class="log-admin-search">
              <select v-model="store.searchType" class="form-select form-select-solid log-admin-search-type">
                <option value="requestUri">URI</option>
                <option value="traceId">Trace</option>
                <option value="username">사용자</option>
                <option value="message">메시지</option>
                <option value="signature">핸들러</option>
              </select>
              <input
                v-model.trim="store.keyword"
                type="search"
                class="form-control form-control-solid"
                maxlength="200"
                placeholder="검색어"
                @keyup.enter="store.fetchLogs(0)"
              />
              <select v-model="store.resultFilter" class="form-select form-select-solid log-admin-result">
                <option value="">전체 결과</option>
                <option value="true">성공</option>
                <option value="false">실패</option>
              </select>
              <label class="form-check form-check-sm form-check-custom form-check-solid log-admin-check">
                <input v-model="store.slowOnly" class="form-check-input" type="checkbox" @change="store.fetchLogs(0)" />
                <span class="form-check-label">느림</span>
              </label>
              <label class="form-check form-check-sm form-check-custom form-check-solid log-admin-check">
                <input v-model="store.exceptionOnly" class="form-check-input" type="checkbox" @change="store.fetchLogs(0)" />
                <span class="form-check-label">예외</span>
              </label>
              <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchLogs(0)">
                <i class="bi bi-search"></i>
              </button>
              <button type="button" class="btn btn-sm btn-light" :disabled="store.loading" @click="store.clearFilters">
                초기화
              </button>
            </div>
            <div class="log-admin-actions">
              <select :value="store.pageSize" class="form-select form-select-solid log-admin-page-size" @change="onPageSizeChange">
                <option :value="10">10개</option>
                <option :value="25">25개</option>
                <option :value="50">50개</option>
              </select>
              <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchLogs(store.currentPage)">
                <i class="bi bi-arrow-clockwise"></i>
              </button>
            </div>
          </div>

          <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
          <div v-if="store.loading" class="log-admin-loading">
            <span class="spinner-border spinner-border-sm me-2"></span>
            불러오는 중
          </div>

          <div v-else class="log-admin-observe">
            <div class="table-responsive log-admin-table-wrap">
              <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
                <thead>
                  <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th>시간</th>
                    <th>결과</th>
                    <th>요청</th>
                    <th>URI</th>
                    <th class="hidden-table">사용자</th>
                    <th>Trace</th>
                    <th class="text-end">상세</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!store.rows.length">
                    <td colspan="7" class="text-center text-muted py-8">조회된 로그가 없습니다.</td>
                  </tr>
                  <tr
                    v-for="row in store.rows"
                    :key="row.id"
                    :class="{
                      'log-admin-row-fail': !isSuccess(row.rslt, row.success),
                      'log-admin-row-slow': isSlow(row),
                      'log-admin-row-active': row.traceId && row.traceId === store.selectedTraceId,
                    }"
                  >
                    <td class="log-admin-time">{{ row.logDt || "-" }}</td>
                    <td>
                      <span class="badge" :class="isSuccess(row.rslt, row.success) ? 'badge-light-success' : 'badge-light-danger'">
                        {{ isSuccess(row.rslt, row.success) ? "성공" : "실패" }}
                      </span>
                      <span v-if="row.exceptionNm" class="badge badge-light-danger ms-1">예외</span>
                    </td>
                    <td>
                      <div class="log-admin-request">
                        <strong>{{ row.httpMethod || "-" }} {{ row.httpStatus ?? "-" }}</strong>
                        <span :class="{ 'text-warning': isSlow(row) }">{{ formatNumber(row.durationMs) }} ms</span>
                      </div>
                    </td>
                    <td>
                      <div class="log-admin-ellipsis">{{ row.requestUri || "-" }}</div>
                      <div class="text-muted fs-8">{{ row.signature || row.actionTyNm || row.actvtyCtgrNm || "-" }}</div>
                    </td>
                    <td class="hidden-table">
                      <div class="log-admin-primary">
                        <strong>{{ row.logUserNm || row.username || "-" }}</strong>
                        <span>{{ row.ipAddr || "-" }}</span>
                      </div>
                    </td>
                    <td>
                      <button
                        type="button"
                        class="log-admin-trace"
                        :disabled="!row.traceId"
                        @click="store.selectTrace(row.traceId)"
                      >
                        {{ shortTrace(row.traceId) }}
                      </button>
                    </td>
                    <td class="text-end">
                      <button type="button" class="btn btn-sm btn-light-primary" @click="openDetail(row.id)">
                        보기
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <aside class="log-admin-flow">
              <div class="log-admin-flow-head">
                <div>
                  <span>Trace 흐름</span>
                  <strong>{{ shortTrace(store.selectedTraceId) }}</strong>
                </div>
                <button
                  type="button"
                  class="btn btn-sm btn-light-primary"
                  :disabled="!store.selectedTraceId"
                  @click="store.filterByTrace(store.selectedTraceId)"
                >
                  이 trace만
                </button>
              </div>
              <div v-if="!store.selectedTraceId" class="text-muted fs-7">
                목록에서 trace 값을 선택하면 같은 요청 흐름이 여기에 묶여 보입니다.
              </div>
              <div v-else-if="!store.selectedTraceRows.length" class="text-muted fs-7">
                현재 페이지에 같은 trace 로그가 없습니다.
              </div>
              <button
                v-for="row in store.selectedTraceRows"
                :key="`flow-${row.id}`"
                type="button"
                class="log-admin-flow-item"
                :class="{ fail: !isSuccess(row.rslt, row.success), slow: isSlow(row) }"
                @click="openDetail(row.id)"
              >
                <span>{{ row.logDt || "-" }}</span>
                <strong>{{ row.httpMethod || "-" }} {{ row.requestUri || "-" }}</strong>
                <em>{{ row.httpStatus ?? "-" }} · {{ formatNumber(row.durationMs) }} ms · {{ row.signature || "-" }}</em>
              </button>
            </aside>
          </div>
        </div>
        <div class="card-footer log-admin-footer">
          <span class="text-muted fs-8">총 {{ formatNumber(store.totalElements) }}건</span>
          <div v-if="pageNumbers.length" class="pagination mb-0">
            <button type="button" class="page-link" :disabled="store.currentPage <= 0" @click="store.fetchLogs(0)">
              <i class="previous"></i>
            </button>
            <button
              v-for="page in pageNumbers"
              :key="page"
              type="button"
              class="page-link"
              :class="{ active: page === store.currentPage }"
              @click="store.fetchLogs(page)"
            >
              {{ page + 1 }}
            </button>
            <button
              type="button"
              class="page-link"
              :disabled="store.currentPage >= store.totalPages - 1"
              @click="store.fetchLogs(store.totalPages - 1)"
            >
              <i class="next"></i>
            </button>
          </div>
        </div>
      </div>
    </template>

    <template v-if="store.detailOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">로그 상세</h5>
              <button type="button" class="btn-close" @click="store.closeDetail"></button>
            </div>
            <div class="modal-body">
              <div v-if="store.detailLoading" class="log-admin-loading">
                <span class="spinner-border spinner-border-sm me-2"></span>
                불러오는 중
              </div>
              <template v-else-if="store.detail">
                <div class="log-admin-detail-grid">
                  <div>
                    <span>Trace</span>
                    <strong>{{ store.detail.traceId || "-" }}</strong>
                  </div>
                  <div>
                    <span>핸들러</span>
                    <strong>{{ store.detail.signature || "-" }}</strong>
                  </div>
                  <div>
                    <span>사용자</span>
                    <strong>{{ store.detail.logUserNm || store.detail.username || "-" }}</strong>
                  </div>
                  <div>
                    <span>일시</span>
                    <strong>{{ store.detail.logDt || "-" }}</strong>
                  </div>
                  <div>
                    <span>요청</span>
                    <strong>{{ store.detail.httpMethod || "-" }} {{ store.detail.httpStatus ?? "-" }}</strong>
                  </div>
                  <div>
                    <span>소요시간</span>
                    <strong>{{ store.detail.durationMs != null ? `${formatNumber(store.detail.durationMs)} ms` : "-" }}</strong>
                  </div>
                  <div>
                    <span>URL</span>
                    <strong>{{ store.detail.url || store.detail.requestUri || "-" }}</strong>
                  </div>
                  <div>
                    <span>IP</span>
                    <strong>{{ store.detail.ipAddr || "-" }}</strong>
                  </div>
                  <div>
                    <span>Referer</span>
                    <strong>{{ store.detail.referer || "-" }}</strong>
                  </div>
                  <div>
                    <span>결과</span>
                    <strong>{{ isSuccess(store.detail.rslt, store.detail.success) ? "성공" : "실패" }}</strong>
                  </div>
                </div>

                <div class="log-admin-detail-block">
                  <h4>파라미터</h4>
                  <pre>{{ store.detail.param || "-" }}</pre>
                </div>
                <div class="log-admin-detail-block">
                  <h4>내용</h4>
                  <pre>{{ store.detail.content || "-" }}</pre>
                </div>
                <div class="log-admin-detail-block">
                  <h4>결과 메시지</h4>
                  <pre>{{ store.detail.rsltMsg || "-" }}</pre>
                </div>
                <div v-if="store.detail.exceptionNm || store.detail.exceptionMsg" class="log-admin-detail-block">
                  <h4>예외</h4>
                  <pre>{{ [store.detail.exceptionNm, store.detail.exceptionMsg].filter(Boolean).join("\n") }}</pre>
                </div>
              </template>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-sm btn-light-primary"
                :disabled="!store.detail?.traceId"
                @click="store.filterByTrace(store.detail?.traceId)"
              >
                이 trace만 보기
              </button>
              <button type="button" class="btn btn-sm btn-light" @click="store.closeDetail">닫기</button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { swalAlert } from "@/utils/swal";
import { computed, onMounted, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { useLogAdminStore, type LogListRow } from "@/stores/logAdmin";

const route = useRoute();
const store = useLogAdminStore();

const isStatsView = computed(() => route.name === "log-stats-user");

const pageNumbers = computed(() => {
  if (store.totalPages <= 1) return [];
  const start = Math.max(0, store.currentPage - 2);
  const end = Math.min(store.totalPages - 1, store.currentPage + 2);
  const pages: number[] = [];
  for (let page = start; page <= end; page += 1) pages.push(page);
  return pages;
});

function formatNumber(value: number | undefined): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

function isSuccess(value: string | boolean | undefined, fallback?: boolean): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "string") return value.toLowerCase() === "true" || value === "Y";
  return Boolean(fallback);
}

function isSlow(row: LogListRow): boolean {
  return Number(row.durationMs ?? 0) >= 1000;
}

function shortTrace(traceId?: string): string {
  if (!traceId) return "-";
  return traceId.length > 12 ? `${traceId.slice(0, 8)}...${traceId.slice(-4)}` : traceId;
}

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

async function openDetail(id: number) {
  try {
    await store.openDetail(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "로그 상세를 불러오지 못했습니다.");
  }
}

onMounted(async () => {
  if (!isStatsView.value) await store.fetchLogs(0);
});

watch(isStatsView, async (next) => {
  if (!next && !store.rows.length) await store.fetchLogs(0);
});
</script>

<style scoped>
.log-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.log-admin-toolbar,
.log-admin-listbar,
.log-admin-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.log-admin-actions,
.log-admin-search {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.log-admin-search {
  min-width: min(920px, 100%);
}

.log-admin-search .form-control {
  min-width: 260px;
}

.log-admin-search-type {
  width: 120px;
}

.log-admin-result {
  width: 120px;
}

.log-admin-page-size {
  width: 110px;
}

.log-admin-check {
  min-height: 34px;
  padding: 0 0.35rem;
}

.log-admin-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.log-admin-metric {
  padding: 0.9rem 1rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
  background: #fff;
}

.log-admin-metric span {
  display: block;
  color: var(--bs-gray-600);
  font-size: 0.78rem;
}

.log-admin-metric strong {
  display: block;
  margin-top: 0.25rem;
  font-size: 1.25rem;
}

.log-admin-metric.danger strong {
  color: var(--bs-danger);
}

.log-admin-metric.warning strong {
  color: var(--bs-warning);
}

.log-admin-observe {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 1rem;
}

.log-admin-table-wrap {
  min-width: 0;
}

.log-admin-row-fail {
  background: rgba(var(--bs-danger-rgb), 0.035);
}

.log-admin-row-slow {
  background: rgba(var(--bs-warning-rgb), 0.04);
}

.log-admin-row-active {
  outline: 1px solid rgba(var(--bs-primary-rgb), 0.35);
  outline-offset: -1px;
}

.log-admin-time {
  min-width: 140px;
  color: var(--bs-gray-700);
  font-size: 0.82rem;
}

.log-admin-request,
.log-admin-primary {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.log-admin-request span,
.log-admin-primary span {
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.log-admin-ellipsis {
  max-width: 440px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-admin-trace {
  max-width: 120px;
  padding: 0.25rem 0.45rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 6px;
  background: var(--bs-light);
  color: var(--bs-primary);
  font-family: monospace;
  font-size: 0.78rem;
}

.log-admin-trace:disabled {
  color: var(--bs-gray-500);
}

.log-admin-flow {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 0;
  padding-left: 1rem;
  border-left: 1px solid var(--bs-gray-200);
}

.log-admin-flow-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.log-admin-flow-head span {
  display: block;
  color: var(--bs-gray-600);
  font-size: 0.78rem;
}

.log-admin-flow-head strong {
  font-family: monospace;
}

.log-admin-flow-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  width: 100%;
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--bs-gray-200);
  border-left: 3px solid var(--bs-primary);
  border-radius: 8px;
  background: #fff;
  text-align: left;
}

.log-admin-flow-item.fail {
  border-left-color: var(--bs-danger);
}

.log-admin-flow-item.slow {
  border-left-color: var(--bs-warning);
}

.log-admin-flow-item span,
.log-admin-flow-item em {
  color: var(--bs-gray-600);
  font-size: 0.76rem;
  font-style: normal;
}

.log-admin-flow-item strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-admin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.log-admin-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.log-admin-detail-grid > div {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
}

.log-admin-detail-grid span {
  color: var(--bs-gray-600);
  font-size: 0.78rem;
}

.log-admin-detail-grid strong {
  overflow-wrap: anywhere;
}

.log-admin-detail-block {
  margin-top: 1rem;
}

.log-admin-detail-block h4 {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 700;
}

.log-admin-detail-block pre {
  min-height: 48px;
  max-height: 240px;
  margin: 0;
  padding: 0.75rem;
  overflow: auto;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-700);
  white-space: pre-wrap;
  word-break: break-word;
}

.page-link.active {
  background: var(--bs-primary);
  border-color: var(--bs-primary);
  color: #fff;
}

@media (max-width: 1200px) {
  .log-admin-observe {
    grid-template-columns: 1fr;
  }

  .log-admin-flow {
    padding-left: 0;
    padding-top: 1rem;
    border-left: 0;
    border-top: 1px solid var(--bs-gray-200);
  }
}

@media (max-width: 768px) {
  .log-admin-toolbar,
  .log-admin-listbar,
  .log-admin-footer,
  .log-admin-search,
  .log-admin-actions {
    align-items: stretch;
    width: 100%;
  }

  .log-admin-search .form-control,
  .log-admin-search-type,
  .log-admin-result,
  .log-admin-page-size {
    width: 100%;
    min-width: 0;
  }

  .log-admin-metrics,
  .log-admin-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
