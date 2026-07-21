<template>
  <!--begin::사용자 휴가 표시-->
  <span
    v-if="badge"
    class="journal-day-vacation-indicator d-inline-flex align-items-center flex-wrap gap-2"
  >
    <span class="badge fs-8" :class="badge.className">{{ t(badge.labelKey) }}</span>
    <span
      v-if="reasonText"
      class="journal-day-vacation-indicator__reason fs-7 fw-normal text-muted"
      :class="{ 'text-truncate': compact }"
      :title="`${t('journal.day.vacation.reason')}: ${reasonText}`"
    >{{ reasonText }}</span>
  </span>
  <!--end::사용자 휴가 표시-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { VacationDayStatus } from "@/features/journal/stores/journal";
import { getVacationBadge, getVacationReasonText } from "@/features/journal/utils/journalVacation";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const props = defineProps<{
  status?: VacationDayStatus;
  reasonList?: string[];
  compact?: boolean;
}>();

const { t } = useLocaleStore();
const badge = computed(() => getVacationBadge(props.status));
const reasonText = computed(() => getVacationReasonText(props.reasonList));
</script>

<style scoped>
.journal-day-vacation-indicator__reason.text-truncate {
  max-width: 14rem;
}
</style>
