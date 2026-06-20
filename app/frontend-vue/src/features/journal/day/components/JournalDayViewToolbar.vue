<template>
  <!--begin::저널 일자 뷰 툴바 (탭 + 일자 등록)-->
  <div class="d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4 w-100">
    <!--begin::보기 타입 탭-->
    <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5 mb-0 flex-grow-1">
      <li class="nav-item">
        <router-link
          :to="weeklyRoute"
          class="nav-link px-6 cursor-pointer"
          exact-active-class="active"
        >
          <span class="nav-icon"><i class="bi bi-calendar-week"></i></span>
          <span class="nav-text">주간 VIEW</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link
          :to="monthlyRoute"
          class="nav-link px-6 cursor-pointer"
          exact-active-class="active"
        >
          <span class="nav-icon"><i class="bi bi-view-stacked"></i></span>
          <span class="nav-text">월간 VIEW</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link
          :to="calendarRoute"
          class="nav-link px-6 cursor-pointer"
          exact-active-class="active"
        >
          <span class="nav-icon"><i class="bi bi-calendar3"></i></span>
          <span class="nav-text">달력 VIEW</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link
          :to="metaRoute"
          class="nav-link px-6 cursor-pointer"
          exact-active-class="active"
        >
          <span class="nav-icon"><i class="bi bi-bar-chart-line"></i></span>
          <span class="nav-text">메타 VIEW</span>
        </router-link>
      </li>
    </ul>
    <!--end::보기 타입 탭-->

    <!--begin::키워드 검색 + 등록 버튼-->
    <div class="d-none d-md-flex align-items-center flex-shrink-0 pe-5 mt-3 gap-2">
      <!--begin::일기 키워드 전체검색 (새 탭) — store와 무관한 로컬 ref 사용-->
      <div class="input-group input-group-sm">
        <input
          v-model="localDiaryKw"
          type="text"
          class="form-control form-control-sm form-control-solid"
          placeholder="일기 키워드 검색"
          maxlength="200"
          style="min-width: 130px;"
          @keyup.enter="openSearchTab('DIARY', localDiaryKw)"
        />
        <button
          type="button"
          class="btn btn-sm btn-icon btn-light"
          title="일기 전체 검색 (새 탭)"
          @click="openSearchTab('DIARY', localDiaryKw)"
        >
          <i class="bi bi-search fs-7"></i>
        </button>
      </div>
      <!--end::일기 키워드 전체검색-->
      <!--begin::꿈 키워드 전체검색 (새 탭) — store와 무관한 로컬 ref 사용-->
      <div class="input-group input-group-sm">
        <input
          v-model="localDreamKw"
          type="text"
          class="form-control form-control-sm form-control-solid"
          placeholder="꿈 키워드 검색"
          maxlength="200"
          style="min-width: 130px;"
          @keyup.enter="openSearchTab('DREAM', localDreamKw)"
        />
        <button
          type="button"
          class="btn btn-sm btn-icon btn-light"
          title="꿈 전체 검색 (새 탭)"
          @click="openSearchTab('DREAM', localDreamKw)"
        >
          <i class="bi bi-search fs-7"></i>
        </button>
      </div>
      <!--end::꿈 키워드 전체검색-->
      <!--begin::구분자-->
      <div class="vr mx-1 opacity-25"></div>
      <!--end::구분자-->
      <!--begin::저널 일자 등록 버튼 (레거시 _journal_day_page_header 등록 모달)-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer text-nowrap"
        title="저널 일자 등록"
        @click="openDayRegist"
      >
        <i class="bi bi-calendar-plus fs-4 pe-1"></i>
        저널 일자 등록
      </button>
      <!--end::저널 일자 등록 버튼-->
    </div>
    <!--end::키워드 검색 + 등록 버튼-->
  </div>
  <!--end::저널 일자 뷰 툴바-->
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { resolveWeekStartDt } from "@/features/journal/utils/journalDate";
import { assertAuthenticatedBeforePopup } from "@/shared/auth/popupAuth";

/** 툴바 전체검색 전용 로컬 키워드 — store.diaryKeyword/dreamKeyword(필터)와 분리 */
const localDiaryKw = ref("");
const localDreamKw = ref("");

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const route = useRoute();
const router = useRouter();

const monthlyQuery = computed(() => ({
  yy: String(journalStore.yy),
  mnth: String(journalStore.mnth),
}));
const weeklyQuery = computed(() => ({
  weekStartDt: journalStore.weekStartDt || resolveWeekStartDt({ yy: journalStore.yy, mnth: journalStore.mnth }),
}));
const weeklyRoute = computed(() => ({ name: "journal-weekly", query: weeklyQuery.value }));
const monthlyRoute = computed(() => ({ name: "journal-monthly", query: monthlyQuery.value }));
const calendarRoute = computed(() => ({ name: "journal-calendar", query: monthlyQuery.value }));
const metaRoute = computed(() => ({ name: "journal-meta", query: monthlyQuery.value }));

/** 전체검색: 새 창으로 /vue-app/journal/entry/search 열기 (base = import.meta.env.BASE_URL) */
async function openSearchTab(type: "DIARY" | "DREAM", keyword: string): Promise<void> {
  if (!await assertAuthenticatedBeforePopup(router, route)) return;
  const params = new URLSearchParams({ type });
  if (keyword.trim()) params.set("searchKeywords", keyword.trim());
  const base = import.meta.env.BASE_URL.replace(/\/$/, "");
  const popup = window.open(`${base}/journal/entry/search?${params.toString()}`, `journal-entry-search-${type}`, "width=1960,height=1440,top=0,left=270");
  if (popup) popup.focus();
}

/** 신규 저널 일자 등록 모달 */
function openDayRegist(): void {
  void modalStore.openDayRegist();
}
</script>
