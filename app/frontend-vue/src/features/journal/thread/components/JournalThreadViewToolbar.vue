<template>
  <!--begin::스레드 뷰 툴바 — 결산·일자 액션 행과 동일(mt-3 mb-1). ASIDE 없음. 탭용 mt-5 빈 여백은 두지 않는다.-->
  <div class="journal-thread-view-toolbar d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
    <div class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
      <button
        type="button"
        class="btn btn-sm btn-primary text-nowrap"
        @click="openCreate"
      >
        <i class="bi bi-plus fs-5 pe-1"></i>
        {{ t("common.reg") }}
      </button>
    </div>
  </div>
  <!--end::스레드 뷰 툴바-->
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const router = useRouter();
const { t } = useLocaleStore();

function openCreate(): void {
  void router.push({ name: "thread-create" });
}
</script>