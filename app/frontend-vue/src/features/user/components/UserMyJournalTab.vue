<template>
  <form class="user-my-journal" @submit.prevent="save">
    <section>
      <h4>{{ t("user.my.journal.section.title") }}</h4>
      <p class="text-muted mb-5">{{ t("user.my.journal.section.description") }}</p>

      <div v-if="store.errorMessage" class="alert alert-danger py-3" role="alert">
        {{ store.errorMessage }}
      </div>

      <div v-if="store.loading" class="d-flex align-items-center text-muted py-4">
        <span class="spinner-border spinner-border-sm me-2"></span>{{ t("common.loading") }}
      </div>

      <template v-else>
        <label for="user-my-journal-default-entry-view" class="form-label fw-bold">
          {{ t("user.my.journal.default-entry-view") }}
        </label>
        <div class="d-flex align-items-center flex-wrap gap-3">
          <select
            id="user-my-journal-default-entry-view"
            v-model="selectedView"
            class="form-select form-select-sm user-my-journal__select"
            :disabled="store.saving"
          >
            <option v-for="option in options" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <button
            type="submit"
            class="btn btn-sm btn-primary"
            :disabled="store.saving || !store.loaded || selectedView === store.defaultEntryView"
          >
            <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
            {{ t("common.save") }}
          </button>
        </div>
        <div class="form-text mt-2">{{ t("user.my.journal.default-entry-view.description") }}</div>
      </template>
    </section>
  </form>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import {
  JOURNAL_DEFAULT_ENTRY_VIEWS,
  useUserJournalSettingStore,
  type JournalDefaultEntryView,
} from "@/features/user/stores/userJournalSetting";

const store = useUserJournalSettingStore();
const { t } = useLocaleStore();
const selectedView = ref<JournalDefaultEntryView>("DAILY");

const options = computed(() => JOURNAL_DEFAULT_ENTRY_VIEWS.map((value) => ({
  value,
  label: t(`user.my.journal.default-entry-view.${value.toLowerCase()}`),
})));

async function load(): Promise<void> {
  try {
    selectedView.value = await store.fetchSetting();
  } catch {
    // store가 오류 문구와 진단 로그를 보유한다.
  }
}

async function save(): Promise<void> {
  try {
    selectedView.value = await store.saveSetting(selectedView.value);
    void swalAlert(t("user.my.journal.save.success"));
  } catch {
    // store가 오류 문구와 진단 로그를 보유한다.
  }
}

onMounted(load);
</script>

<style scoped>
.user-my-journal section h4 { margin-bottom:1rem; font-size:1rem; font-weight:700; }
.user-my-journal__select { width:min(320px,100%); }
</style>
