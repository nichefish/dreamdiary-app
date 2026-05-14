<template>
  <!--begin::결산 사이드 패널 (연도 필터)-->
  <div class="journal-annual-aside card p-5" style="min-width:200px; max-width:220px;">
    <div class="d-flex justify-content-end mb-3">
      <button
        type="button"
        class="btn btn-sm btn-icon btn-light"
        title="필터 패널 닫기"
        @click="emit('close')"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!--begin::연도 필터-->
    <div class="d-flex flex-column gap-3">
      <label class="text-gray-700 fw-bold fs-7">연도</label>
      <select
        class="form-select form-select-sm"
        :value="store.filterYy ?? ''"
        @change="onYyChange"
      >
        <option value="">전체</option>
        <option v-for="y in yyOptions" :key="y" :value="y">{{ y }}년</option>
      </select>

      <!--begin::결산 등록 버튼-->
      <div class="separator my-2"></div>
      <button
        type="button"
        class="btn btn-sm btn-light-primary w-100"
        @click="store.openRegist()"
      >
        <i class="bi bi-plus-circle me-1"></i>
        결산 등록
      </button>
      <!--end::결산 등록 버튼-->
    </div>
    <!--end::연도 필터-->
  </div>
  <!--end::결산 사이드 패널-->
</template>

<script setup lang="ts">
import { useJournalAnnualStore } from "@/stores/journalAnnual";

const emit = defineEmits<{ close: [] }>();
const store = useJournalAnnualStore();

const currentYear = new Date().getFullYear();
const yyOptions = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

function onYyChange(e: Event) {
  const val = (e.target as HTMLSelectElement).value;
  store.setFilterYy(val === "" ? null : Number(val));
}
</script>
