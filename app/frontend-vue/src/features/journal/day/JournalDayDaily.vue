<template>
  <!--begin::저널 일간 페이지 (새 창 전용)-->
  <div class="journal-day-daily-page p-5">

    <!--begin::일 이동 네비게이션-->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <button type="button" class="btn btn-sm btn-light-primary" @click="movePrev">
        <i class="bi bi-chevron-left"></i> {{ t("journal.day.daily.previous") }}
      </button>
      <!--begin::날짜 선택 (클릭 시 달력 피커 오픈)-->
      <input
        type="date"
        :value="currentDt"
        class="fs-5 fw-bold text-center border-0 bg-transparent"
        style="cursor: pointer; width: 12rem;"
        :title="t('journal.day.daily.date-select.tooltip')"
        @change="onDateSelect"
      />
      <!--end::날짜 선택-->
      <button type="button" class="btn btn-sm btn-light-primary" @click="moveNext">
        {{ t("journal.day.daily.next") }} <i class="bi bi-chevron-right"></i>
      </button>
    </div>
    <!--end::일 이동 네비게이션-->

    <!--begin::로딩-->
    <div v-if="store.loading" class="d-flex justify-content-center py-10">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    <!--end::로딩-->

    <!--begin::에러-->
    <div v-else-if="store.error" class="text-danger py-10 text-center">
      {{ store.error }}
    </div>
    <!--end::에러-->

    <!--begin::일자 카드-->
    <template v-else>
      <template v-if="store.dayList.length > 0">
        <JournalDayCard
          v-for="day in store.dayList"
          :key="day.id || day.stdrdDt"
          :day="day"
          :show-diaries="store.showDiaries"
          :show-dreams="store.showDreams"
        />
      </template>
      <div v-else class="d-flex-center py-10 text-muted">
        {{ t("journal.day.list.empty") }}
      </div>
    </template>
    <!--end::일자 카드-->

  </div>
  <!--end::저널 일간 페이지-->
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { buildDailyFetchParams } from "@/features/journal/utils/journalDayRefresh";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalDayCard from "./components/JournalDayCard.vue";

const store = useJournalStore();
const modalStore = useJournalModalStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const currentDt = computed(() => (route.query.stdrdDt as string | undefined) ?? "");

function load(stdrdDt?: string): void {
  store.setViewType("DAILY");
  // stdrdDt 에서 yy·mnth 를 추출해 명시적으로 전달한다.
  // fetchDays 내부의 store.yy/mnth 기본값(현재 날짜)이 적용되면 백엔드 필터와 불일치하여 빈 결과가 반환된다.
  if (stdrdDt?.trim()) {
    void store.fetchDays(buildDailyFetchParams(stdrdDt));
  } else {
    void store.fetchDays({ viewType: "DAILY" });
  }
}

/** stdrdDt 에서 n일 이동한 날짜 문자열 반환 (로컬 날짜 파싱 — UTC 오프셋 문제 방지) */
function shiftDate(stdrdDt: string, days: number): string {
  const [y, m, d] = stdrdDt.split("-").map(Number);
  const date = new Date(y, m - 1, d + days);
  const yy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yy}-${mm}-${dd}`;
}

/** 달력 피커에서 날짜 선택 시 해당 날짜로 이동 */
function onDateSelect(event: Event): void {
  const target = event.target as HTMLInputElement | null;
  if (!target?.value) return;
  void router.replace({ query: { stdrdDt: target.value } });
}

function movePrev(): void {
  if (!currentDt.value) return;
  void router.replace({ query: { stdrdDt: shiftDate(currentDt.value, -1) } });
}

function moveNext(): void {
  if (!currentDt.value) return;
  void router.replace({ query: { stdrdDt: shiftDate(currentDt.value, 1) } });
}

onMounted(() => {
  load(currentDt.value || undefined);
  void modalStore.prefetchChapterPrefixes();
});

watch(
  () => route.query.stdrdDt,
  (dt) => {
    if (route.name === "journal-daily") {
      load(dt as string | undefined);
    }
  }
);
</script>
