<template>
  <div class="admin-page">
    <!--begin::뷰 탭 — 저널 일자 JournalDayViewToolbar 와 동일 골격(nav-tabs-line + ps-5 mt-5)-->
    <div class="admin-view-toolbar d-flex flex-column-fluid w-100">
      <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5 mb-0 flex-grow-1" role="tablist" :aria-label="t('admin.page.aria-label')">
        <li class="nav-item" role="presentation">
          <button
            type="button"
            class="nav-link px-6"
            :class="{ active: activeTab === 'general' }"
            role="tab"
            :aria-selected="activeTab === 'general'"
            @click="selectTab('general')"
          >
            {{ t('admin.page.tab.general') }}
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            type="button"
            class="nav-link px-6"
            :class="{ active: activeTab === 'ai' }"
            role="tab"
            :aria-selected="activeTab === 'ai'"
            @click="selectTab('ai')"
          >
            {{ t('admin.page.tab.ai') }}
          </button>
        </li>
      </ul>
    </div>
    <!--end::뷰 탭-->

    <div v-if="store.backfillWorkActive" class="admin-backfill-banner" role="status">
      <i class="bi bi-cloud-check fs-4 text-primary"></i>
      <div class="flex-grow-1">
        <strong>{{ t('admin.page.background-processing') }}</strong>
        <div class="text-muted fs-8">
          {{ t('admin.page.background-note') }}
          {{ t('admin.page.background-refresh') }}
        </div>
      </div>
    </div>

    <div class="admin-layout" :class="{ 'admin-layout-ai': activeTab === 'ai' }">
      <AdminGeneralSettingsCard
        v-if="activeTab === 'general'"
        v-model:holydayYy="holydayYy"
        @sync-holyday="syncHolyday"
        @open-cache-list="cacheModals?.openCacheList()"
        @clear-all-caches="cacheModals?.clearAllCaches()"
      />

      <AdminAiSettingsCard v-if="activeTab === 'ai'" />
      <AdminEntityQueueCard v-if="activeTab === 'ai'" />

      <AdminDevToolsCard v-if="activeTab === 'general'" />

      <AdminRoleCard v-if="activeTab === 'general'" />
    </div>

    <AdminCacheModals ref="cacheModals" />
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import AdminRoleCard from "@/features/admin/components/AdminRoleCard.vue";
import AdminDevToolsCard from "@/features/admin/components/AdminDevToolsCard.vue";
import AdminGeneralSettingsCard from "@/features/admin/components/AdminGeneralSettingsCard.vue";
import AdminEntityQueueCard from "@/features/admin/components/AdminEntityQueueCard.vue";
import AdminAiSettingsCard from "@/features/admin/components/AdminAiSettingsCard.vue";
import AdminCacheModals from "@/features/admin/components/AdminCacheModals.vue";

const store = useAdminPageStore();
const { t } = useLocaleStore();
type AdminTab = "general" | "ai";
const route = useRoute();
const router = useRouter();
const holydayYy = ref(String(new Date().getFullYear()));
const cacheModals = ref<InstanceType<typeof AdminCacheModals> | null>(null);

let statsTimer: number | undefined;

const activeTab = computed<AdminTab>(() => (route.query.tab === "ai" ? "ai" : "general"));

async function reload() {
  if (activeTab.value === "ai") {
    await Promise.all([
      store.fetchEmbeddingStats(),
      store.fetchEntityQueueStats(),
      store.fetchOllamaHealth(),
      store.fetchChatRagSettings(),
      store.fetchJournalSetting(),
    ]);
    return;
  }
  await store.fetchBootstrap();
  holydayYy.value = String(store.meta.currYy);
}

async function selectTab(tab: AdminTab) {
  await router.replace({ query: { ...route.query, tab } });
}

async function syncHolyday() {
  try {
    void swalAlert(await store.syncHolyday(holydayYy.value));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.holyday.sync.failure"));
  }
}

onMounted(async () => {
  await reload();
  statsTimer = window.setInterval(() => {
    void Promise.all([store.fetchEmbeddingStats(), store.fetchEntityQueueStats()]);
  }, 30000);
});

watch(activeTab, () => {
  void reload();
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

.admin-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
  gap: 1rem;
}

.admin-layout-ai {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-backfill-banner {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  border: 1px solid #cfe2ff;
  border-radius: 8px;
  background: #f1faff;
}

@media (max-width: 1200px) {
  .admin-layout {
    display: flex;
    flex-direction: column;
  }
}

</style>
