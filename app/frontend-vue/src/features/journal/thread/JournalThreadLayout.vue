<template>
  <!--begin::스레드 레이아웃 (모달 컨테이너 포함)-->
  <div class="journal-thread-layout-vue">
    <router-view />
    <!--begin::스레드 모달 컨테이너-->
    <JournalThreadRegistModal />
    <JournalThreadDetailModal />
    <CommentListModal />
    <CommentRegistModal />
    <!--end::스레드 모달 컨테이너-->
  </div>
  <!--end::스레드 레이아웃-->
</template>

<script setup lang="ts">
import { watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import JournalThreadRegistModal from "./modals/JournalThreadRegistModal.vue";
import JournalThreadDetailModal from "./modals/JournalThreadDetailModal.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
import CommentRegistModal from "@/features/journal/shared/modals/CommentRegistModal.vue";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";

const route = useRoute();
const router = useRouter();
const store = useJournalThreadStore();

let syncToken = 0;

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
    await store.openDetail(routeId);
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
