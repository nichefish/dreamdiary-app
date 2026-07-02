<template>
  <!--begin::저널 월간 페이지-->
  <div class="journal-day-monthly-page">
    <JournalDayViewToolbar />

    <!--begin::카드-->
    <div class="card post" style="margin-top: 0 !important;">
      <!--begin::태그 클라우드 헤더-->
      <div v-if="store.showTagCloud" class="card-header">
        <JournalTagCloudHeader />
      </div>
      <!--end::태그 클라우드 헤더-->
      <div class="card-body">
        <!--begin::로딩-->
        <div v-if="store.loading && store.dayList.length === 0" class="d-flex justify-content-center py-10">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <!--end::로딩-->

        <!--begin::에러-->
        <div v-else-if="store.error" class="text-danger py-10 text-center">
          {{ store.error }}
        </div>
        <!--end::에러-->

        <!--begin::일자 목록-->
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
        <!--end::일자 목록-->
      </div>
    </div>
    <!--end::카드-->

  </div>
  <!--end::저널 월간 페이지-->
</template>

<script setup lang="ts">
import { onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import JournalDayCard from "./components/JournalDayCard.vue";
import JournalDayViewToolbar from "./components/JournalDayViewToolbar.vue";
import JournalTagCloudHeader from "./components/JournalTagCloudHeader.vue";

const store = useJournalStore();
const modalStore = useJournalModalStore();
const route = useRoute();

function loadMonthlyView(): void {
  store.setViewType("LIST");
  const yy = parsePositiveInt(route.query.yy);
  const mnth = parsePositiveInt(route.query.mnth);
  if (yy) store.yy = yy;
  if (mnth && mnth >= 1 && mnth <= 12) store.mnth = mnth;
  void store.fetchDays({ viewType: "LIST", yy: store.yy, mnth: store.mnth });
  if (store.showTagCloud) {
    void store.fetchTagCloud();
  }
}

/** URL query 의 월간 기간 상태를 숫자로 복원한다. */
function parsePositiveInt(value: unknown): number | null {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string" || !/^\d+$/.test(raw)) return null;
  const parsed = Number(raw);
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;
}

onMounted(() => {
  /* 챕터 카테고리를 화면 로드 시점에 미리 캐시해 모달 오픈 시 로딩 없이 사용한다. */
  void modalStore.prefetchChapterCategories();
});

watch(
  () => route.fullPath,
  () => {
    if (route.name === "journal-monthly") {
      loadMonthlyView();
    }
  },
  { immediate: true },
);
</script>
