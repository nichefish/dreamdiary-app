<template>
  <div ref="cacheListModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ t('admin.page.cache.modal.title') }}</h5>
          <button type="button" class="btn-close" @click="closeCacheList"></button>
        </div>
        <div class="modal-body">
          <div v-if="store.cacheLoading" class="text-center text-muted py-8">
            <span class="spinner-border spinner-border-sm me-2"></span>
            {{ t('common.loading') }}
          </div>
          <div v-else-if="!cacheNames.length" class="text-center text-muted py-8">{{ t('admin.page.cache.empty') }}</div>
          <template v-else>
            <div v-for="(cacheName, index) in cacheNames" :key="cacheName">
              <div class="admin-cache-block">
                <div class="admin-cache-name">
                  <strong>"{{ cacheName }}"</strong>
                  <button type="button" class="btn btn-sm btn-light-danger" @click="clearCache(cacheName)">
                    <i class="bi bi-trash"></i>
                    {{ t('admin.page.cache.delete-all') }}
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
          <button type="button" class="btn btn-sm btn-light" @click="closeCacheList">{{ t('common.close') }}</button>
        </div>
      </div>
    </div>
  </div>

  <div ref="cacheDetailModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ t('admin.page.cache.detail.modal.title') }}</h5>
          <button type="button" class="btn-close" @click="closeCacheDetail"></button>
        </div>
        <div class="modal-body">
          <pre class="admin-cache-detail">{{ cacheDetailText }}</pre>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light-primary" @click="backToCacheList">{{ t('admin.page.cache.detail.list') }}</button>
          <button type="button" class="btn btn-sm btn-light" @click="closeCacheDetail">{{ t('common.close') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Modal } from "bootstrap";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";

/**
 * 캐시 관리 모달(목록·상세): 서버 캐시 맵 조회·엔트리 상세·개별/전체 삭제를 담당한다.
 * bootstrap Modal 인스턴스를 소유하며, 목록 열기·전체 삭제는 상위(AdminPage) 도구 카드에서
 * 호출할 수 있도록 defineExpose 로 노출한다.
 */
const store = useAdminPageStore();
const { t } = useLocaleStore();

const cacheListModalEl = ref<HTMLElement | null>(null);
const cacheDetailModalEl = ref<HTMLElement | null>(null);
let cacheListModal: Modal | null = null;
let cacheDetailModal: Modal | null = null;

const cacheNames = computed(() => Object.keys(store.cacheMap || {}));
const cacheDetailText = computed(() => stringifyPretty(store.cacheDetail));

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
  if (value === null || value === undefined) return t("admin.page.cache.detail.empty");
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

async function openCacheList() {
  if (!await assertAuthenticatedBeforeModal()) return;
  await store.fetchCacheMap();
  cacheListModal?.show();
}

function closeCacheList() {
  cacheListModal?.hide();
}

async function openCacheDetail(cacheName: string, cacheKey: string) {
  if (!await assertAuthenticatedBeforeModal()) return;
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
  if (!await swalConfirm(t("admin.page.cache.delete.confirm").replace("{cacheName}", cacheName))) return;
  try {
    await store.clearCacheByName(cacheName);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.delete.failure"));
  }
}

async function evictCacheEntry(cacheName: string, cacheKey: string) {
  if (!await swalConfirm(t("admin.page.cache.item.delete.confirm"))) return;
  try {
    await store.evictCacheEntry(cacheName, cacheKey);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.item.delete.failure"));
  }
}

async function clearAllCaches() {
  if (!await swalConfirm(t("admin.page.cache.all.delete.confirm"))) return;
  try {
    void swalAlert(await store.clearAllCaches());
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.cache.all.delete.failure"));
  }
}

onMounted(() => {
  if (cacheListModalEl.value) cacheListModal = new Modal(cacheListModalEl.value);
  if (cacheDetailModalEl.value) cacheDetailModal = new Modal(cacheDetailModalEl.value);
});

defineExpose({ openCacheList, clearAllCaches });
</script>

<style scoped>
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

@media (max-width: 768px) {
  .admin-cache-block,
  .admin-cache-entry {
    grid-template-columns: 1fr;
  }
}
</style>
