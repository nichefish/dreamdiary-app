<template>
  <!--begin::저널 일간 페이지 (새 창 전용)-->
  <div class="journal-day-daily-page p-5">

    <!--begin::일 이동 네비게이션-->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <button type="button" class="btn btn-sm btn-light-primary" @click="movePrev">
        <i class="bi bi-chevron-left"></i> 이전
      </button>
      <span class="fs-5 fw-bold">{{ currentDt }}</span>
      <button type="button" class="btn btn-sm btn-light-primary" @click="moveNext">
        다음 <i class="bi bi-chevron-right"></i>
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
        조회된 저널이 없습니다.
      </div>
    </template>
    <!--end::일자 카드-->

  </div>
  <!--end::저널 일간 페이지-->
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useJournalStore } from "@/stores/journal";
import { useJournalModalStore } from "@/stores/journalModal";
import JournalDayCard from "./components/JournalDayCard.vue";

const store = useJournalStore();
const modalStore = useJournalModalStore();
const route = useRoute();
const router = useRouter();

const currentDt = computed(() => (route.query.stdrdDt as string | undefined) ?? "");

function load(stdrdDt?: string): void {
  store.setViewType("DAILY");
  void store.fetchDays({ viewType: "DAILY", stdrdDt });
}

/** stdrdDt 에서 n일 이동한 날짜 문자열 반환 */
function shiftDate(stdrdDt: string, days: number): string {
  const d = new Date(stdrdDt);
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
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
  void modalStore.prefetchChapterCategories();
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
