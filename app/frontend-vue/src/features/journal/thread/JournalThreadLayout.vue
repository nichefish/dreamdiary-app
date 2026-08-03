<template>
  <!--begin::스레드 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-thread-layout-vue">
    <!--begin::뷰 툴바 — 등록은 결산·일자와 동일하게 툴바 우측. ASIDE 없음.-->
    <JournalThreadViewToolbar v-if="!['thread-detail', 'thread-edit'].includes(String(route.name))" />
    <!--end::뷰 툴바-->
    <router-view />
    <!--begin::스레드 모달 컨테이너-->
    <CommentListModal />
    <CommentRegistModal />
    <JournalReflectionRegistModal />
    <HistoryModal @success="refreshOpenThreadDetail" />
    <RelatedContentAddModal />
    <JournalTagProfileModal />
    <JournalTagContextMenu />
    <!--end::스레드 모달 컨테이너-->
  </div>
  <!--end::스레드 레이아웃-->
</template>

<script setup lang="ts">
import { onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import JournalThreadViewToolbar from "./components/JournalThreadViewToolbar.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
import CommentRegistModal from "@/features/journal/shared/modals/CommentRegistModal.vue";
import JournalReflectionRegistModal from "@/features/journal/reflection/modals/JournalReflectionRegistModal.vue";
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

  /*
   * 카테고리는 목록 검색·등록·수정이 함께 쓰는 스레드 기능 데이터다.
   * 레이아웃 진입과 하위 route 동기화에서 보장하고, 정상 결과는 store가 계속 공유한다.
   */
  void store.ensurePrefixOptions();

  if (routeName === "thread-create") {
    if (store.detailOpen) store.closeDetail();
    store.openRegist();
    return;
  }

  if (routeName === "thread-edit" && Number.isFinite(routeId) && routeId > 0) {
    if (store.detailOpen) store.closeDetail();
    const loaded = await store.openModifyPage(routeId);
    if (token !== syncToken) return;
    if (!loaded) {
      console.warn("[journal-thread] edit route returned to list: modify model load failed", {
        routeId,
      });
      await router.replace({ name: "thread-list" });
    }
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
    /*
     * 독립 편집 페이지는 저장·취소가 목적 route를 직접 결정한다.
     * submitRegist가 편집 상태를 먼저 닫아도 목록으로 선행 이동하지 않는다.
     */
    if ((registOpen || detailOpen) || route.name === "thread-list" || route.name === "thread-edit") return;
    void router.replace({ name: "thread-list" });
  }
);

/** 스레드 영역 자체를 벗어나면 전역 편집 모달이 다음 route 위에 남지 않도록 정리한다. */
onBeforeUnmount(() => {
  if (store.registOpen) store.closeRegist();
  if (store.detailOpen && store.detailSurface === "page") store.closeDetail();
});
</script>
