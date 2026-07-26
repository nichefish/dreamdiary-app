<template>
  <!--begin::게시판 뷰 툴바 — 저널 스레드·결산 액션 행과 동일(mt-3 mb-1). ASIDE 없음. 탭용 mt-5 빈 여백은 두지 않는다.-->
  <div class="board-post-view-toolbar d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
    <div class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
      <button
        type="button"
        class="btn btn-sm btn-primary text-nowrap"
        @click="store.openRegist()"
      >
        <i class="bi bi-plus fs-5 pe-1"></i>
        {{ t("common.register") }}
      </button>
    </div>
  </div>
  <!--end::게시판 뷰 툴바-->
</template>

<script setup lang="ts">
import { useBoardPostStore } from "@/features/board/stores/boardPost";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useBoardPostStore();
const { t } = useLocaleStore();
</script>