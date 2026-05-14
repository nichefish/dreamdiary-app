<template>
  <RouterView />
  <AppChat />
</template>

<script lang="ts">
import { defineComponent, onMounted } from "vue";
import { RouterView } from "vue-router";
import AppChat from "@/views/chat/AppChat.vue";

export default defineComponent({
  name: "app",
  components: {
    AppChat,
    RouterView,
  },
  setup() {
    onMounted(() => {
      document.body.classList.remove("page-loading");
    });
  },
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
