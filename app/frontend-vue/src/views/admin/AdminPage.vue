<template>
  <div class="admin-page">
    <div class="admin-toolbar">
      <div>
        <h2 class="mb-1">사이트 관리</h2>
        <div class="text-muted fs-7">캐시, 외부 동기화, 권한, 임베딩 큐를 관리합니다.</div>
      </div>
      <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.bootstrapLoading" @click="reload">
        <i class="bi bi-arrow-clockwise"></i>
      </button>
    </div>

    <div class="admin-layout">
      <section class="card post">
        <div class="card-body">
          <h3 class="admin-section-title">운영 도구</h3>

          <div class="admin-tool-row">
            <div>
              <div class="fw-bold">캐시</div>
              <div class="text-muted fs-8">현재 활성 캐시를 확인하거나 초기화합니다.</div>
            </div>
            <div class="admin-tool-actions">
              <button type="button" class="btn btn-sm btn-primary" @click="openCacheList">
                <i class="bi bi-list-ul"></i>
                활성 목록
              </button>
              <button type="button" class="btn btn-sm btn-light-danger" @click="clearAllCaches">
                <i class="bi bi-trash"></i>
                전체 삭제
              </button>
            </div>
          </div>

          <div class="separator my-5"></div>

          <div class="admin-tool-row">
            <div>
              <label for="holydayYy" class="fw-bold">공휴일 동기화</label>
              <div class="text-muted fs-8">선택 연도의 휴일 정보를 다시 가져옵니다.</div>
            </div>
            <div class="admin-inline-form">
              <select id="holydayYy" v-model="holydayYy" class="form-select form-select-solid">
                <option v-for="yy in store.yearOptions" :key="yy" :value="String(yy)">{{ yy }}</option>
              </select>
              <button type="button" class="btn btn-sm btn-primary" @click="syncHolyday">실행</button>
            </div>
          </div>

          <div class="separator my-5"></div>

          <div class="admin-tool-row">
            <div>
              <label for="notionDataType" class="fw-bold">Notion 요청</label>
              <div class="text-muted fs-8">Notion API 연결을 확인합니다.</div>
            </div>
            <div class="admin-notion-form">
              <select id="notionDataType" v-model="notionDataType" class="form-select form-select-solid">
                <option value="PAGE">PAGE</option>
                <option value="BLOCK">BLOCK</option>
                <option value="BLOCKS">BLOCKS</option>
                <option value="DATABASE">DATABASE</option>
              </select>
              <input v-model.trim="notionDataId" type="text" class="form-control form-control-solid" maxlength="64" />
              <button type="button" class="btn btn-sm btn-primary" @click="runNotion">실행</button>
            </div>
          </div>
        </div>
      </section>

      <section class="card post">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-4">
            <div>
              <h3 class="admin-section-title mb-1">AI Embedding Backfill</h3>
              <div class="text-muted fs-8">30초마다 자동 갱신됩니다.</div>
            </div>
            <div class="admin-tool-actions">
              <button type="button" class="btn btn-sm btn-light-primary" :disabled="embeddingBusy" @click="store.fetchEmbeddingStats">
                Refresh
              </button>
              <button type="button" class="btn btn-sm btn-primary" :disabled="embeddingBusy" @click="store.syncEmbeddingQueue">
                <span v-if="store.embeddingSyncRunning" class="spinner-border spinner-border-sm me-1"></span>
                Sync Entries
              </button>
            </div>
          </div>

          <div v-if="store.embeddingStatsError" class="alert alert-warning py-2">
            {{ store.embeddingStatsError }}
          </div>
          <div v-if="store.embeddingSyncResult" class="alert alert-success py-2">
            {{ embeddingSyncMessage }}
          </div>

          <div class="admin-stat-grid">
            <div v-for="stat in embeddingStatsCards" :key="stat.label" class="admin-stat">
              <span>{{ stat.label }}</span>
              <strong :class="stat.className">{{ formatNumber(stat.value) }}</strong>
            </div>
          </div>

          <div class="d-flex flex-wrap gap-2 my-4">
            <span class="badge badge-light-success">Completed {{ formatNumber(store.embeddingStats.completed) }}</span>
            <span class="badge badge-light-warning">Remaining {{ formatNumber(store.embeddingStats.remaining) }}</span>
            <span class="badge badge-light-danger">Failed {{ formatNumber(store.embeddingStats.failed) }}</span>
            <span class="badge badge-light">Skipped {{ formatNumber(store.embeddingStats.skipped) }}</span>
          </div>

          <div class="d-flex justify-content-between fs-8 text-muted mb-1">
            <span>Completion {{ formatPercent(store.embeddingStats.completionRate) }}</span>
            <span>Vectorized {{ formatPercent(store.embeddingStats.vectorizedRate) }}</span>
          </div>
          <div class="progress h-8px">
            <div class="progress-bar bg-success" role="progressbar" :style="embeddingProgressStyle"></div>
          </div>
        </div>
      </section>

      <section class="card post admin-role-card">
        <div class="card-body">
          <h3 class="admin-section-title">권한 정보</h3>
          <div class="table-responsive">
            <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
              <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                  <th>권한 코드</th>
                  <th>권한명</th>
                  <th class="text-center">정렬</th>
                  <th class="text-center">사용</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!store.roles.length">
                  <td colspan="4" class="text-center text-muted py-8">권한 정보가 없습니다.</td>
                </tr>
                <tr v-for="role in store.roles" :key="role.id">
                  <td class="fw-bold text-muted">{{ role.roleKey }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <i :class="roleIcon(role)" class="fs-2 me-2"></i>
                      <span :class="roleNameClass(role)" class="fw-bold">
                        <template v-if="role.roleKey === store.meta.authDevKey && role.parentRoleId != null">({{ role.parentRoleId }}) </template>
                        {{ role.roleName }}
                      </span>
                      <span class="badge ms-3" :class="roleBadgeClass(role)">{{ role.authLevel ?? "-" }}</span>
                    </div>
                  </td>
                  <td class="text-center">{{ role.sortOrder ?? "-" }}</td>
                  <td class="text-center">{{ role.useYn }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>

    <div ref="cacheListModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">사이트 캐시 목록</h5>
            <button type="button" class="btn-close" @click="closeCacheList"></button>
          </div>
          <div class="modal-body">
            <div v-if="store.cacheLoading" class="text-center text-muted py-8">
              <span class="spinner-border spinner-border-sm me-2"></span>
              불러오는 중
            </div>
            <div v-else-if="!cacheNames.length" class="text-center text-muted py-8">활성 캐시가 없습니다.</div>
            <template v-else>
              <div v-for="(cacheName, index) in cacheNames" :key="cacheName">
                <div class="admin-cache-block">
                  <div class="admin-cache-name">
                    <strong>"{{ cacheName }}"</strong>
                    <button type="button" class="btn btn-sm btn-light-danger" @click="clearCache(cacheName)">
                      <i class="bi bi-trash"></i>
                      전체 삭제
                    </button>
                  </div>
                  <div class="admin-cache-entry-list">
                    <div v-for="entry in cacheEntries(cacheName)" :key="entry[0]" class="admin-cache-entry">
                      <button type="button" class="btn btn-sm btn-light-primary" @click="openCacheDetail(cacheName, entry[0])">
                        {{ displayCacheKey(entry[0]) }}
                        <i class="bi bi-stickies ms-1"></i>
                      </button>
                      <button type="button" class="btn btn-sm btn-light-danger" @click="evictCacheEntry(cacheName, entry[0])">
                        <i class="bi bi-trash"></i>
                      </button>
                      <span>{{ stringify(entry[1]) }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="index < cacheNames.length - 1" class="separator my-5"></div>
              </div>
            </template>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light" @click="closeCacheList">닫기</button>
          </div>
        </div>
      </div>
    </div>

    <div ref="cacheDetailModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">사이트 캐시 상세</h5>
            <button type="button" class="btn-close" @click="closeCacheDetail"></button>
          </div>
          <div class="modal-body">
            <pre class="admin-cache-detail">{{ cacheDetailText }}</pre>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light-primary" @click="backToCacheList">목록</button>
            <button type="button" class="btn btn-sm btn-light" @click="closeCacheDetail">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import { Modal } from "bootstrap";
import { useAdminPageStore, type RoleRow } from "@/stores/adminPage";

const store = useAdminPageStore();
const holydayYy = ref(String(new Date().getFullYear()));
const notionDataType = ref("PAGE");
const notionDataId = ref("");
const cacheListModalEl = ref<HTMLElement | null>(null);
const cacheDetailModalEl = ref<HTMLElement | null>(null);
let cacheListModal: Modal | null = null;
let cacheDetailModal: Modal | null = null;
let statsTimer: number | undefined;

const embeddingBusy = computed(() => store.embeddingStatsLoading || store.embeddingSyncRunning);
const embeddingProgressStyle = computed(() => {
  const value = Math.max(0, Math.min(100, Number(store.embeddingStats.completionRate) || 0));
  return { width: `${value}%` };
});

const embeddingStatsCards = computed(() => [
  { label: "Total", value: store.embeddingStats.total, className: "" },
  { label: "Pending", value: store.embeddingStats.pending, className: "text-warning" },
  { label: "Processing", value: store.embeddingStats.processing, className: "text-primary" },
  { label: "Embedded", value: store.embeddingStats.embedded, className: "text-success" },
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
  ].join(" / ");
});

const cacheNames = computed(() => Object.keys(store.cacheMap || {}));
const cacheDetailText = computed(() => stringifyPretty(store.cacheDetail));

function formatNumber(value: number): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

function formatPercent(value: number): string {
  return `${(Number(value) || 0).toFixed(2)}%`;
}

function roleIcon(role: RoleRow): string {
  if (role.roleKey === store.meta.authMngrKey) return "bi bi-person-lines-fill text-info";
  if (role.roleKey === store.meta.authDevKey) return "bi bi-person-fill-gear text-info";
  return "bi bi-people-fill text-muted";
}

function roleNameClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "text-muted" : "text-info";
}

function roleBadgeClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "badge-dark opacity-50" : "badge-info";
}

function stringify(value: unknown): string {
  if (value === undefined) return "undefined";
  if (value === null) return "null";
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function stringifyPretty(value: unknown): string {
  if (value === null || value === undefined) return "캐시 상세가 없습니다.";
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function cacheEntries(cacheName: string): Array<[string, unknown]> {
  return Object.entries(store.cacheMap[cacheName] ?? {});
}

function displayCacheKey(cacheKey: string): string {
  return cacheKey === "SimpleKey()" ? "-" : cacheKey;
}

async function reload() {
  await Promise.all([store.fetchBootstrap(), store.fetchEmbeddingStats()]);
  holydayYy.value = String(store.meta.currYy);
}

async function syncHolyday() {
  try {
    window.alert(await store.syncHolyday(holydayYy.value));
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "휴일 정보를 동기화하지 못했습니다.");
  }
}

async function runNotion() {
  try {
    const res = await store.fetchNotion(notionDataType.value, notionDataId.value);
    window.alert(JSON.stringify(res.rsltObj ?? res.rsltList ?? res, null, 2));
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "Notion 요청에 실패했습니다.");
  }
}

async function openCacheList() {
  await store.fetchCacheMap();
  cacheListModal?.show();
}

function closeCacheList() {
  cacheListModal?.hide();
}

async function openCacheDetail(cacheName: string, cacheKey: string) {
  await store.fetchCacheDetail(cacheName, cacheKey);
  cacheListModal?.hide();
  cacheDetailModal?.show();
}

function closeCacheDetail() {
  cacheDetailModal?.hide();
}

function backToCacheList() {
  cacheDetailModal?.hide();
  cacheListModal?.show();
}

async function clearCache(cacheName: string) {
  if (!window.confirm(`${cacheName} 캐시를 전체 삭제할까요?`)) return;
  try {
    await store.clearCacheByName(cacheName);
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "캐시를 삭제하지 못했습니다.");
  }
}

async function evictCacheEntry(cacheName: string, cacheKey: string) {
  if (!window.confirm("캐시 항목을 삭제할까요?")) return;
  try {
    await store.evictCacheEntry(cacheName, cacheKey);
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "캐시 항목을 삭제하지 못했습니다.");
  }
}

async function clearAllCaches() {
  if (!window.confirm("전체 캐시를 삭제할까요?")) return;
  try {
    window.alert(await store.clearAllCaches());
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "전체 캐시를 삭제하지 못했습니다.");
  }
}

onMounted(async () => {
  if (cacheListModalEl.value) cacheListModal = new Modal(cacheListModalEl.value);
  if (cacheDetailModalEl.value) cacheDetailModal = new Modal(cacheDetailModalEl.value);
  await reload();
  statsTimer = window.setInterval(() => {
    void store.fetchEmbeddingStats();
  }, 30000);
});

onUnmounted(() => {
  if (statsTimer) window.clearInterval(statsTimer);
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
  gap: 1rem;
}

.admin-role-card {
  grid-column: 2;
  grid-row: 1 / span 2;
}

.admin-section-title {
  font-size: 1rem;
  font-weight: 700;
}

.admin-tool-row {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto;
  gap: 1rem;
  align-items: center;
}

.admin-tool-actions,
.admin-inline-form,
.admin-notion-form {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.admin-inline-form .form-select {
  width: 110px;
}

.admin-notion-form {
  justify-content: flex-end;
}

.admin-notion-form .form-select {
  width: 130px;
}

.admin-notion-form .form-control {
  width: 240px;
}

.admin-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.admin-stat {
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--bs-light);
}

.admin-stat span {
  display: block;
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.admin-stat strong {
  display: block;
  margin-top: 0.25rem;
  font-size: 1.25rem;
}

.admin-cache-block {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 1rem;
}

.admin-cache-name {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.75rem;
  min-width: 0;
  word-break: break-word;
}

.admin-cache-entry-list {
  display: grid;
  gap: 0.5rem;
  min-width: 0;
}

.admin-cache-entry {
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr);
  gap: 0.5rem;
  align-items: center;
}

.admin-cache-entry span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-cache-detail {
  min-height: 360px;
  max-height: 65vh;
  padding: 1rem;
  margin: 0;
  overflow: auto;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-800);
}

@media (max-width: 1200px) {
  .admin-layout,
  .admin-role-card {
    display: flex;
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .admin-tool-row,
  .admin-cache-block,
  .admin-cache-entry {
    grid-template-columns: 1fr;
  }

  .admin-notion-form,
  .admin-notion-form .form-select,
  .admin-notion-form .form-control,
  .admin-inline-form,
  .admin-inline-form .form-select {
    width: 100%;
  }

  .admin-stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
