<template>
  <!--begin::스레드 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-thread-layout-vue">
    <router-view />
    <!--begin::스레드 모달 컨테이너-->
    <JournalThreadRegistModal />
    <JournalThreadDetailModal />
    <CommentListModal />
    <!--end::스레드 모달 컨테이너-->
  </div>
  <!--end::스레드 레이아웃-->
</template>

<script setup lang="ts">
import JournalThreadRegistModal from "./modals/JournalThreadRegistModal.vue";
import JournalThreadDetailModal from "./modals/JournalThreadDetailModal.vue";
import CommentListModal from "@/views/attachable/CommentListModal.vue";
</script>
