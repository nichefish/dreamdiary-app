<template>
  <!--begin::저널 사이드 패널 (년월 이동 + 필터)-->
  <div class="journal-aside card p-5" style="min-width:200px; max-width:220px;">
    <div class="d-flex justify-content-end mb-3">
      <button
        type="button"
        class="btn btn-sm btn-icon btn-light"
        title="필터 패널 닫기"
        @click="asideStore.hide()"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!--begin::년월 네비게이션-->
    <div class="d-flex flex-column gap-3">
      <!--begin::연도 선택-->
      <select
        class="form-select form-select-sm"
        :value="store.yy"
        @change="onYyChange"
      >
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}</option>
      </select>
      <!--end::연도 선택-->

      <!--begin::월 이동 컨트롤-->
      <div class="d-flex align-items-center justify-content-between">
        <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateMonth(-1)">
          <i class="bi bi-chevron-left"></i>
        </button>
        <span class="fw-bold fs-6">{{ store.mnth }}월</span>
        <button type="button" class="btn btn-sm btn-icon btn-light" @click="store.navigateMonth(1)">
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
          :class="['btn btn-sm', m === store.mnth ? 'btn-primary' : 'btn-light']"
          @click="store.gotoYyMnth(store.yy, m)"
        >
          {{ m }}월
        </button>
      </div>
      <!--end::월 그리드-->

      <!--begin::TODAY 버튼-->
      <button type="button" class="btn btn-sm btn-light-primary w-100" @click="store.gotoToday()">
        TODAY
      </button>
      <!--end::TODAY 버튼-->

      <div class="separator"></div>

      <!--begin::보기 필터 토글-->
      <div class="d-flex flex-column gap-2">
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showDiaries"
            @change="toggleDiaries"
          />
          <span class="form-check-label text-muted fs-7">DIARIES</span>
        </label>
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showDreams"
            @change="toggleDreams"
          />
          <span class="form-check-label text-muted fs-7">DREAMS</span>
        </label>
        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
          <input
            class="form-check-input w-30px h-20px"
            type="checkbox"
            :checked="store.showTagCloud"
            @change="toggleTagCloud"
          />
          <span class="form-check-label text-muted fs-7">TAGCLOUD</span>
        </label>
      </div>
      <!--end::보기 필터 토글-->

      <!--begin::할일 등록 버튼-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary w-100"
        @click="openTodoReg"
      >
        <i class="bi bi-check2-square me-1"></i>
        할일 등록
      </button>
      <!--end::할일 등록 버튼-->

      <!--begin::태그 목록 버튼-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary w-100"
        @click="openTagList"
      >
        <i class="bi bi-tags me-1"></i>
        태그 목록
      </button>
      <!--end::태그 목록 버튼-->

      <!--begin::키워드 필터-->
      <div class="d-flex flex-column gap-2">
        <input
          v-model="store.diaryKeyword"
          type="text"
          class="form-control form-control-sm"
          placeholder="일기 키워드"
          @keyup.enter="store.fetchDays()"
        />
        <input
          v-model="store.dreamKeyword"
          type="text"
          class="form-control form-control-sm"
          placeholder="꿈 키워드"
          @keyup.enter="store.fetchDays()"
        />
      </div>
      <!--end::키워드 필터-->
    </div>
    <!--end::년월 네비게이션-->
  </div>
  <!--end::저널 사이드 패널-->
</template>

<script setup lang="ts">
import { useJournalStore } from "@/stores/journal";
import { useJournalAsideStore } from "@/stores/journalAside";
import { useJournalModalStore } from "@/stores/journalModal";
import { useAttachableModalStore } from "@/stores/attachableModal";

const store = useJournalStore();
const asideStore = useJournalAsideStore();
const modalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

function onYyChange(e: Event) {
  const val = Number((e.target as HTMLSelectElement).value);
  store.gotoYyMnth(val, store.mnth);
}

function toggleDiaries() {
  store.showDiaries = !store.showDiaries;
  store.fetchDays();
}

function toggleDreams() {
  store.showDreams = !store.showDreams;
  store.fetchDays();
}

/** 할일 등록 모달 열기 */
function openTodoReg() {
  modalStore.openTodoReg({ yy: store.yy, mnth: store.mnth });
}

function toggleTagCloud() {
  store.showTagCloud = !store.showTagCloud;
}

function openTagList() {
  void attachableStore.openTagList({
    yy: store.yy,
    mnth: store.mnth,
    weekStartDt: store.weekStartDt || undefined,
  });
}
</script>
