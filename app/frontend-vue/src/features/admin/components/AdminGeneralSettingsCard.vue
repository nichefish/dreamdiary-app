<template>
  <section class="card post">
    <div class="card-body">
      <h3 class="admin-section-title">{{ t('admin.page.section.tools') }}</h3>

      <div class="admin-tool-row">
        <div>
          <div class="fw-bold">{{ t('admin.page.cache.title') }}</div>
          <div class="text-muted fs-8">{{ t('admin.page.cache.desc') }}</div>
        </div>
        <div class="admin-tool-actions">
          <button type="button" class="btn btn-sm btn-primary" @click="emit('open-cache-list')">
            <i class="bi bi-list-ul"></i>
            {{ t('admin.page.cache.list') }}
          </button>
          <button type="button" class="btn btn-sm btn-light-danger" @click="emit('clear-all-caches')">
            <i class="bi bi-trash"></i>
            {{ t('admin.page.cache.delete-all') }}
          </button>
        </div>
      </div>

      <div class="separator my-5"></div>

      <div class="admin-tool-row">
        <div>
          <label for="holydayYy" class="fw-bold">{{ t('admin.page.holyday.title') }}</label>
          <div class="text-muted fs-8">{{ t('admin.page.holyday.desc') }}</div>
        </div>
        <div class="admin-inline-form">
          <select id="holydayYy" v-model="holydayYy" class="form-select form-select-solid">
            <option v-for="yy in store.yearOptions" :key="yy" :value="String(yy)">{{ yy }}</option>
          </select>
          <button type="button" class="btn btn-sm btn-primary" @click="emit('sync-holyday')">{{ t('admin.page.run') }}</button>
        </div>
      </div>

      <div class="separator my-5"></div>

      <div class="admin-tool-row">
        <div>
          <label for="notionDataType" class="fw-bold">{{ t('admin.page.notion.title') }}</label>
          <div class="text-muted fs-8">{{ t('admin.page.notion.desc') }}</div>
        </div>
        <div class="admin-notion-form">
          <select id="notionDataType" v-model="notionDataType" class="form-select form-select-solid">
            <option value="PAGE">PAGE</option>
            <option value="BLOCK">BLOCK</option>
            <option value="BLOCKS">BLOCKS</option>
            <option value="DATABASE">DATABASE</option>
          </select>
          <input v-model.trim="notionDataId" type="text" class="form-control form-control-solid" maxlength="64" />
          <button type="button" class="btn btn-sm btn-primary" @click="runNotion">{{ t('admin.page.run') }}</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";

/**
 * 일반 설정(도구) 카드: 캐시 관리·공휴일 동기화·Notion 조회 도구(general 탭).
 * 공휴일 연도는 상위(AdminPage)가 소유하는 값을 v-model 로 받아 표시하고(부트스트랩 후 store.meta.currYy 초기화),
 * 실제 공휴일 동기화·캐시 모달 열기·전체 캐시 삭제는 상위가 소유하므로 sync-holyday·open-cache-list·
 * clear-all-caches 이벤트로 위임한다. Notion 조회는 카드 로컬 상태로 자기완결 처리한다.
 * 렌더 게이트(activeTab === "general")는 상위 AdminPage 가 소유한다.
 */
const props = defineProps<{ holydayYy: string }>();
const emit = defineEmits<{
  (e: "update:holydayYy", value: string): void;
  (e: "sync-holyday"): void;
  (e: "open-cache-list"): void;
  (e: "clear-all-caches"): void;
}>();

const store = useAdminPageStore();
const { t } = useLocaleStore();

const holydayYy = computed({
  get: () => props.holydayYy,
  set: (value: string) => emit("update:holydayYy", value),
});

const notionDataType = ref("PAGE");
const notionDataId = ref("");

async function runNotion() {
  try {
    const res = await store.fetchNotion(notionDataType.value, notionDataId.value);
    void swalAlert(JSON.stringify(res.rsltObj ?? res.rsltList ?? res, null, 2));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("admin.page.notion.failure"));
  }
}
</script>

<style scoped>
@import "./adminCards.scss";
</style>
