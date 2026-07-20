<template>
  <!--begin::게시판 레이아웃 (모달 컨테이너 포함)-->
  <div class="board-post-layout-vue">
    <!--begin::뷰 툴바 — 등록은 저널 스레드·결산과 동일하게 툴바 우측. ASIDE 없음.-->
    <BoardPostViewToolbar />
    <!--end::뷰 툴바-->
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
import BoardPostViewToolbar from "./components/BoardPostViewToolbar.vue";
import BoardPostRegistModal from "./modals/BoardPostRegistModal.vue";
import BoardPostDetailModal from "./modals/BoardPostDetailModal.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
</script>