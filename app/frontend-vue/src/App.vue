<template>
  <RouterView />
  <AppChat v-if="authStore.isAuthenticated && !isPopup" />
  <!--
    Chat RAG source deep-link opens read-only entry view (and optional edit) on non-popup routes.
    Popup routes keep their own JournalEntryRegistModal mount (AppChat is hidden there).
  -->
  <JournalEntryRegistModal v-if="authStore.isAuthenticated && !isPopup" />
  <JournalEntryViewModal v-if="authStore.isAuthenticated && !isPopup" />
  <!--
    스레드 상세·편집 모달은 주간·월간·일간·검색의 현재 화면 문맥을 보존한 채 전환한다.
    thread-detail/thread-edit route는 독립 페이지를 사용하고, 전역 인스턴스는 문맥형 모달만 담당한다.
  -->
  <JournalThreadDetailModal v-if="authStore.isAuthenticated" />
  <JournalThreadRegistModal v-if="authStore.isAuthenticated" />
  <AppRuntimeStatus />
</template>

<script setup lang="ts">
import { computed, onErrorCaptured, onMounted, watchEffect } from "vue";
import { RouterView, useRoute } from "vue-router";
import AppRuntimeStatus from "@/shared/components/system/AppRuntimeStatus.vue";
import AppChat from "@/features/chat/AppChat.vue";
import JournalEntryRegistModal from "@/features/journal/entry/modals/JournalEntryRegistModal.vue";
import JournalEntryViewModal from "@/features/journal/entry/modals/JournalEntryViewModal.vue";
import JournalThreadDetailModal from "@/features/journal/thread/modals/JournalThreadDetailModal.vue";
import JournalThreadRegistModal from "@/features/journal/thread/modals/JournalThreadRegistModal.vue";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { preloadCategoryMaps } from "@/features/journal/stores/journalModal";
import { reportRuntimeError } from "@/shared/utils/appRuntimeStatus";
import { installModalStacking } from "@/shared/utils/modalStack";

const authStore = useAuthStore();
const localeStore = useLocaleStore();
const route = useRoute();
const appName = import.meta.env.VITE_APP_NAME;

/** 팝업 전용 라우트(검색 팝업 등)에서는 AI 챗 숨김 */
const isPopup = computed(() => ["journal-entry-search", "journal-daily"].includes(String(route.name)));

/** 현재 route와 locale에 맞춰 브라우저 탭 제목을 갱신한다. */
watchEffect(() => {
  const pageTitleKey = typeof route.meta.pageTitleKey === "string" ? route.meta.pageTitleKey : "";
  if (!pageTitleKey) {
    if (route.name != null) {
      console.warn("[app] route is missing page title key", { name: route.name, path: route.path });
    }
    document.title = appName;
    return;
  }
  document.title = `${localeStore.t(pageTitleKey)} - ${appName}`;
});

onMounted(() => {
  installModalStacking();
  document.body.classList.remove("page-loading");
  if (authStore.isAuthenticated) void preloadCategoryMaps();
});

onErrorCaptured((error) => {
  reportRuntimeError(error, "vue-render");
  return false;
});
</script>

<style lang="scss">
@import "bootstrap-icons/font/bootstrap-icons.css";
@import "quill/dist/quill.snow.css";
@import "animate.css";
@import "sweetalert2/dist/sweetalert2.css";
@import "nouislider/dist/nouislider.css";
@import "@fortawesome/fontawesome-free/css/all.min.css";
@import "socicon/css/socicon.css";
@import "line-awesome/dist/line-awesome/css/line-awesome.css";
@import "dropzone/dist/dropzone.css";
@import "@vueform/multiselect/themes/default.css";
@import "prism-themes/themes/prism-shades-of-purple.css";

// Main demo style scss
@import "@metronic/assets/keenicons/duotone/style.css";
@import "@metronic/assets/keenicons/outline/style.css";
@import "@metronic/assets/keenicons/solid/style.css";
@import "@metronic/assets/sass/plugins";
@import "@metronic/assets/sass/style";

// Project-specific styles (migrated from static/css/)
@import "styles/commons";
@import "styles/journal";

#app {
  display: contents;
}

/*
 * AppChat drawer uses z-index 6002 (above Metronic chrome).
 * Bootstrap modals and SweetAlert confirmations default to ~1055/1060, so
 * entry/detail modals opened from chat RAG sources and modal-scoped confirms
 * would render under the drawer or the active modal without this raise.
 * Nested modals get incremental z-index from modalStack.ts (installModalStacking).
 * TinyMCE aux (code/link 등) defaults ~1300 and would sit under raised modals —
 * SSOT: overlayZIndex.ts TINYMCE_AUX_Z (6190). SweetAlert stays above (SWAL_Z 6200).
 * Keep these CSS numbers in sync with overlayZIndex.ts.
 * !important beats library .swal2-container { 1060 } and nested-modal inline races.
 */
body.modal-open .modal {
  z-index: 6100;
}
body.modal-open .modal-backdrop {
  z-index: 6090;
}
body.modal-open .tox-tinymce-aux {
  z-index: 6190 !important;
}
body.modal-open .swal2-container,
.swal2-container {
  z-index: 6200 !important;
}
</style>
