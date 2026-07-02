<template>
  <RouterView />
  <AppChat v-if="authStore.isAuthenticated && !isPopup" />
  <AppRuntimeStatus />
</template>

<script setup lang="ts">
import { computed, onErrorCaptured, onMounted } from "vue";
import { RouterView, useRoute } from "vue-router";
import AppRuntimeStatus from "@/shared/components/system/AppRuntimeStatus.vue";
import AppChat from "@/features/chat/AppChat.vue";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { preloadCategoryMaps } from "@/features/journal/stores/journalModal";
import { reportRuntimeError } from "@/shared/utils/appRuntimeStatus";

const authStore = useAuthStore();
const route = useRoute();

/** 팝업 전용 라우트(검색 팝업 등)에서는 AI 챗 숨김 */
const isPopup = computed(() => ["journal-entry-search", "journal-daily"].includes(String(route.name)));

onMounted(() => {
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
@import "apexcharts/dist/apexcharts.css";
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
@import "element-plus/dist/index.css";

// Main demo style scss
@import "@metronic/assets/keenicons/duotone/style.css";
@import "@metronic/assets/keenicons/outline/style.css";
@import "@metronic/assets/keenicons/solid/style.css";
@import "@metronic/assets/sass/element-ui.dark";
@import "@metronic/assets/sass/plugins";
@import "@metronic/assets/sass/style";

// Project-specific styles (migrated from static/css/)
@import "styles/commons";
@import "styles/journal";

#app {
  display: contents;
}
</style>
