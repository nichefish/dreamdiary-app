<template>
  <!--begin::저널 주간 페이지-->
  <div class="journal-day-weekly-page">
    <div class="d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4">
      <!--begin::보기 타입 탭-->
      <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
        <li class="nav-item">
          <router-link
            :to="{ name: 'journal-weekly' }"
            class="nav-link px-6 cursor-pointer"
            active-class="active"
          >
            <span class="nav-icon"><i class="bi bi-calendar-week"></i></span>
            <span class="nav-text">위클리 VIEW</span>
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            :to="{ name: 'journal-monthly' }"
            class="nav-link px-6 cursor-pointer"
            active-class="active"
          >
            <span class="nav-icon"><i class="bi bi-view-stacked"></i></span>
            <span class="nav-text">목록 VIEW</span>
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            :to="{ name: 'journal-calendar' }"
            class="nav-link px-6 cursor-pointer"
            active-class="active"
          >
            <span class="nav-icon"><i class="bi bi-calendar3"></i></span>
            <span class="nav-text">달력 VIEW</span>
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            :to="{ name: 'journal-meta' }"
            class="nav-link px-6 cursor-pointer"
            active-class="active"
          >
            <span class="nav-icon"><i class="bi bi-bar-chart-line"></i></span>
            <span class="nav-text">메타 VIEW</span>
          </router-link>
        </li>
      </ul>
      <!--end::보기 타입 탭-->
    </div>

    <!--begin::카드-->
    <div class="card post" style="margin-top: 0 !important;">
      <div class="card-body">
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
  <!--end::저널 주간 페이지-->
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useJournalStore } from "@/stores/journal";
import JournalDayCard from "./components/JournalDayCard.vue";

const store = useJournalStore();

onMounted(() => {
  store.setViewType("WEEKLY");
  void store.fetchDays({ viewType: "WEEKLY" });
});
</script>
