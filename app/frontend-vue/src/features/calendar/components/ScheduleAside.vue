<template>
  <!--begin::일정 사이드 패널 (년월 내비게이션) — 저널 aside(JournalAside)와 동일 폭·카드 구성-->
  <div class="schedule-aside card card-reset card-p-0 p-5" style="width:280px; min-width:280px; max-width:280px;">
    <div class="d-flex justify-content-end mb-2">
      <button
        type="button"
        class="btn btn-sm btn-icon btn-light"
        :title="t('schedule.aside.close')"
        @click="asideStore.hide()"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!--begin::년월 내비게이션-->
    <div class="card-body p-0 d-flex flex-column gap-3">
      <!--begin::연도 선택-->
      <select
        class="form-select form-select-sm"
        :value="yy"
        @change="onYyChange"
      >
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}</option>
      </select>
      <!--end::연도 선택-->

      <!--begin::월 이동 컨트롤-->
      <div class="d-flex align-items-center justify-content-between">
        <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(-1)">
          <i class="bi bi-chevron-left"></i>
        </button>
        <span class="fw-bold fs-6">{{ monthLabel(mnth) }}</span>
        <button type="button" class="btn btn-sm btn-icon btn-light" @click="navigateMonth(1)">
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
      <!--end::월 이동 컨트롤-->

      <!--begin::월 그리드-->
      <div class="d-grid gap-1" style="grid-template-columns: repeat(3, 1fr);">
        <button
          v-for="m in 12"
          :key="m"
          type="button"
          :class="['btn btn-sm', m === mnth ? 'btn-primary' : 'btn-light']"
          @click="emit('goto', yy, m)"
        >
          {{ monthLabel(m) }}
        </button>
      </div>
      <!--end::월 그리드-->

      <!--begin::TODAY 버튼-->
      <button type="button" class="btn btn-sm btn-light-primary w-100" @click="emit('today')">
        TODAY
      </button>
      <!--end::TODAY 버튼-->
    </div>
    <!--end::년월 내비게이션-->
  </div>
  <!--end::일정 사이드 패널-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useScheduleAsideStore } from "@/features/calendar/stores/scheduleAside";

/** 현재 표시 중인 연/월 (1-based) — 부모(ScheduleCalendar)가 이동일 기준으로 내려준다. */
const props = defineProps<{
  yy: number;
  mnth: number;
}>();

const emit = defineEmits<{
  /** 연/월 선택 → 부모가 달력 gotoDate 또는 목록 재조회로 반영 */
  (e: "goto", yy: number, mnth: number): void;
  /** 오늘로 이동 */
  (e: "today"): void;
}>();

const asideStore = useScheduleAsideStore();
const { t } = useLocaleStore();

const currentYear = new Date().getFullYear();
/** 연도 옵션 — 저널 aside 와 동일하게 2010년부터 현재 연도까지. 표시 중 연도가 범위 밖이면 포함시킨다. */
const yyOptions = computed(() => {
  const options = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);
  if (!options.includes(props.yy)) options.push(props.yy);
  return options.sort((a, b) => b - a);
});

/** 월 버튼 레이블 — locale 카탈로그의 {0} 포맷 적용 (ko: "N월") */
function monthLabel(m: number): string {
  return t("schedule.aside.month-format").replace("{0}", String(m));
}

function onYyChange(e: Event) {
  const val = Number((e.target as HTMLSelectElement).value);
  emit("goto", val, props.mnth);
}

function navigateMonth(delta: number) {
  let nextMnth = props.mnth + delta;
  let nextYy = props.yy;
  if (nextMnth < 1) { nextMnth = 12; nextYy -= 1; }
  if (nextMnth > 12) { nextMnth = 1; nextYy += 1; }
  emit("goto", nextYy, nextMnth);
}
</script>
