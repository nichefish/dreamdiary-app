<template>
  <!--begin::스레드 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-thread-layout-vue">
    <!--begin::뷰 툴바 — 등록은 결산·일자와 동일하게 툴바 우측. ASIDE 없음.-->
    <JournalThreadViewToolbar v-if="route.name !== 'thread-detail'" />
    <!--end::뷰 툴바-->
    <router-view />
    <!--begin::스레드 모달 컨테이너-->
    <JournalThreadRegistModal />
    <CommentListModal />
    <CommentRegistModal />
    <JournalInterpretationRegistModal />
    <HistoryModal @success="refreshOpenThreadDetail" />
    <RelatedContentAddModal />
    <JournalTagProfileModal />
    <JournalTagContextMenu />
    <!--end::스레드 모달 컨테이너-->
  </div>
  <!--end::스레드 레이아웃-->
</template>

<script setup lang="ts">
import { watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import JournalThreadViewToolbar from "./components/JournalThreadViewToolbar.vue";
import JournalThreadRegistModal from "./modals/JournalThreadRegistModal.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
import CommentRegistModal from "@/features/journal/shared/modals/CommentRegistModal.vue";
import JournalInterpretationRegistModal from "@/features/journal/interpretation/modals/JournalInterpretationRegistModal.vue";
import HistoryModal from "@/features/attachable/HistoryModal.vue";
import RelatedContentAddModal from "@/features/journal/shared/modals/RelatedContentAddModal.vue";
import JournalTagProfileModal from "@/features/journal/shared/modals/JournalTagProfileModal.vue";
import JournalTagContextMenu from "@/features/journal/shared/components/JournalTagContextMenu.vue";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";

const route = useRoute();
const router = useRouter();
const store = useJournalThreadStore();

let syncToken = 0;

/** 이력 복원·삭제처럼 자식 모달이 원본 엔트리를 바꾸면 열린 스레드 상세를 다시 조회한다. */
function refreshOpenThreadDetail(): void {
  void store.refreshOpenDetail();
}

async function syncThreadRoute(): Promise<void> {
  const token = ++syncToken;
  const routeName = String(route.name ?? "");
  const routeId = Number(route.params.id);

  if (routeName === "thread-create") {
    if (store.detailOpen) store.closeDetail();
    store.openRegist();
    return;
  }

  if (routeName === "thread-edit" && Number.isFinite(routeId) && routeId > 0) {
    if (store.detailOpen) store.closeDetail();
    await store.openModify(routeId);
    if (token !== syncToken) return;
    return;
  }

  if (routeName === "thread-detail" && Number.isFinite(routeId) && routeId > 0) {
    if (store.registOpen) store.closeRegist();
    await store.openDetailPage(routeId);
    if (token !== syncToken) return;
    return;
  }

  if (store.registOpen) store.closeRegist();
  if (store.detailOpen) store.closeDetail();
}

watch(
  () => route.fullPath,
  () => {
    void syncThreadRoute();
  },
  { immediate: true }
);

watch(
  () => [store.registOpen, store.detailOpen] as const,
  ([registOpen, detailOpen]) => {
    if ((registOpen || detailOpen) || route.name === "thread-list") return;
    void router.replace({ name: "thread-list" });
  }
);
</script>
