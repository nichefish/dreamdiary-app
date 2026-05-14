<template>
  <div class="log-admin-page">
    <div class="log-admin-toolbar">
      <div>
        <h2 class="mb-1">{{ isStatsView ? "사용자별 로그 통계" : "로그 목록" }}</h2>
        <div class="text-muted fs-7">
          {{ isStatsView ? "현재 연결된 데이터가 있으면 그대로 표시하고, 기능은 placeholder로 유지합니다." : "관리자 작업 로그를 조회합니다." }}
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
        사용자별 로그 통계는 준비 중입니다. 현재 연결된 데이터가 있으면 이 표에 그대로 표시됩니다.
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
      <div class="card post">
        <div class="card-body">
          <div class="log-admin-listbar">
            <div class="log-admin-search">
              <input
                v-model.trim="store.keyword"
                type="search"
                class="form-control form-control-solid"
                placeholder="URL 검색"
                @keyup.enter="store.fetchLogs(0)"
              />
              <select v-model="store.resultFilter" class="form-select form-select-solid log-admin-result">
                <option value="">전체 결과</option>
                <option value="true">성공</option>
                <option value="false">실패</option>
              </select>
              <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchLogs(0)">
                <i class="bi bi-search"></i>
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

          <div v-else class="table-responsive">
            <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
              <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                  <th class="text-center hidden-table">번호</th>
                  <th>일시</th>
                  <th class="hidden-table">작업자</th>
                  <th class="hidden-table">IP</th>
                  <th>작업유형</th>
                  <th>URL</th>
                  <th class="hidden-table">결과메시지</th>
                  <th class="text-center">결과</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!store.rows.length">
                  <td colspan="8" class="text-center text-muted py-8">조회된 로그가 없습니다.</td>
                </tr>
                <tr v-for="row in store.rows" :key="row.id" class="cursor-pointer" @click="openDetail(row.id)">
                  <td class="text-center hidden-table text-gray-600">{{ row.rnum }}</td>
                  <td>{{ row.logDt || "-" }}</td>
                  <td class="hidden-table">
                    <div class="log-admin-primary">
                      <strong>{{ row.logUserNm || row.username || "-" }}</strong>
                      <span>{{ row.username || "-" }}</span>
                    </div>
                  </td>
                  <td class="hidden-table">{{ row.ipAddr || "-" }}</td>
                  <td>{{ row.actionTyNm || row.actvtyCtgrNm || "-" }}</td>
                  <td>
                    <div class="log-admin-ellipsis">{{ row.requestUri || "-" }}</div>
                  </td>
                  <td class="hidden-table">
                    <div class="log-admin-ellipsis">{{ row.rsltMsg || "-" }}</div>
                  </td>
                  <td class="text-center">
                    <span class="badge" :class="isSuccess(row.rslt, row.success) ? 'badge-light-success' : 'badge-light-danger'">
                      {{ isSuccess(row.rslt, row.success) ? "성공" : "실패" }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
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
                    <span>제목</span>
                    <strong>{{ store.detail.title || store.detail.actvtyCtgrNm || "-" }}</strong>
                  </div>
                  <div>
                    <span>작업자</span>
                    <strong>{{ store.detail.logUserNm || store.detail.username || "-" }}</strong>
                  </div>
                  <div>
                    <span>일시</span>
                    <strong>{{ store.detail.logDt || "-" }}</strong>
                  </div>
                  <div>
                    <span>IP</span>
                    <strong>{{ store.detail.ipAddr || "-" }}</strong>
                  </div>
                  <div>
                    <span>URL</span>
                    <strong>{{ store.detail.url || store.detail.requestUri || "-" }}</strong>
                  </div>
                  <div>
                    <span>Referer</span>
                    <strong>{{ store.detail.referer || "-" }}</strong>
                  </div>
                  <div>
                    <span>결과</span>
                    <strong>{{ isSuccess(store.detail.rslt, store.detail.success) ? "성공" : "실패" }}</strong>
                  </div>
                  <div>
                    <span>소요시간</span>
                    <strong>{{ store.detail.durationMs != null ? `${formatNumber(store.detail.durationMs)} ms` : "-" }}</strong>
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
import { computed, onMounted, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { useLogAdminStore } from "@/stores/logAdmin";

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

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

async function openDetail(id: number) {
  try {
    await store.openDetail(id);
  } catch (e) {
    window.alert(e instanceof Error ? e.message : "로그 상세를 불러오지 못했습니다.");
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
  min-width: min(620px, 100%);
}

.log-admin-search .form-control {
  min-width: 240px;
}

.log-admin-result {
  width: 120px;
}

.log-admin-page-size {
  width: 110px;
}

.log-admin-primary {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.log-admin-primary span {
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.log-admin-ellipsis {
  max-width: 360px;
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
  .log-admin-result,
  .log-admin-page-size {
    width: 100%;
    min-width: 0;
  }

  .log-admin-detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
