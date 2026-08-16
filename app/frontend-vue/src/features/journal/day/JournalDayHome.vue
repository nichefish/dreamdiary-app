<template>
  <div class="d-flex justify-content-center align-items-center py-10 text-muted" role="status">
    <span class="spinner-border spinner-border-sm me-2"></span>
    {{ t("journal.day.home.resolving") }}
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { reportRuntimeError } from "@/shared/utils/appRuntimeStatus";
import {
  resolveJournalDefaultEntryRouteName,
  useUserJournalSettingStore,
} from "@/features/user/stores/userJournalSetting";

const router = useRouter();
const settingStore = useUserJournalSettingStore();
const { t } = useLocaleStore();

/** 사용자 설정을 조회해 저널 일자 영역의 확정된 기본 보기로 이동한다. */
async function resolveEntryView(): Promise<void> {
  try {
    const defaultEntryView = await settingStore.fetchSetting();
    const routeName = resolveJournalDefaultEntryRouteName(defaultEntryView);
    console.info("[journal-day-home] default entry route resolved", { defaultEntryView, routeName });
    await router.replace({ name: routeName });
  } catch (error) {
    reportRuntimeError(error, "journal-day-home", "runtime.error.navigation");
  }
}

onMounted(resolveEntryView);
</script>
