<template>
  <!--begin::결산 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-annual-layout-vue">
    <!--begin::결산 본문 + aside 컨테이너-->
    <div class="d-flex align-items-start gap-6">
      <div class="flex-grow-1 min-w-0">
        <div v-if="!asideVisible" class="d-flex justify-content-end mb-3">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-light-primary"
            title="필터 패널"
            @click="asideVisible = true"
          >
            <i class="bi bi-layout-sidebar-inset-reverse"></i>
          </button>
        </div>
        <router-view />
      </div>
      <aside v-if="asideVisible" class="journal-annual-layout-vue__aside flex-shrink-0">
        <JournalAnnualAside @close="asideVisible = false" />
      </aside>
    </div>
    <!--end::결산 본문 + aside 컨테이너-->

    <!--begin::결산 모달 컨테이너-->
    <JournalAnnualRegistModal />
    <JournalAnnualReviewRegistModal />
    <!--end::결산 모달 컨테이너-->
  </div>
  <!--end::결산 레이아웃-->
</template>

<script setup lang="ts">
import { ref } from "vue";
import JournalAnnualAside from "./components/JournalAnnualAside.vue";
import JournalAnnualRegistModal from "./modals/JournalAnnualRegistModal.vue";
import JournalAnnualReviewRegistModal from "./modals/JournalAnnualReviewRegistModal.vue";

const asideVisible = ref(true);
</script>
