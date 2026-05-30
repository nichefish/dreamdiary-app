<template>
  <!--begin::게시판 레이아웃 (모달 컨테이너 포함)-->
  <div class="board-post-layout-vue">
    <router-view />
    <!--begin::게시판 모달 컨테이너-->
    <BoardPostRegistModal />
    <BoardPostDetailModal />
    <CommentListModal />
    <!--end::게시판 모달 컨테이너-->
  </div>
  <!--end::게시판 레이아웃-->
</template>

<script setup lang="ts">
import BoardPostRegistModal from "./modals/BoardPostRegistModal.vue";
import BoardPostDetailModal from "./modals/BoardPostDetailModal.vue";
import CommentListModal from "@/views/attachable/CommentListModal.vue";
</script>
