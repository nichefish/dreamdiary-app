<template>
  <!--begin::Footer-->
  <div v-if="footerDisplay" id="kt_app_footer" class="app-footer">
    <!--begin::Footer container-->
    <div class="app-container container-fluid d-flex flex-column flex-md-row flex-center flex-md-stack py-3">
      <!--begin::Copyright-->
      <div class="order-1 order-md-1 text-gray-900">
        <span
          class="text-muted fw-semibold me-1"
          v-tooltip
          data-bs-placement="top"
          data-bs-dismiss="click"
          :title="`${t('layout.footer.project-period')}:\n2024.03.20 ~ (${t('layout.footer.in-progress')})`"
        >2024©</span>
        <a
          href="/"
          class="text-gray-600 text-hover-primary fw-bold blink cursor-help"
          v-tooltip
          data-bs-placement="top"
          data-bs-dismiss="click"
          :title="`${t('layout.footer.contributors')}:\nnysnyari`"
        >dreamdiary.nicheblog.io</a>
      </div>
      <!--end::Copyright-->
      <!--begin::Menu-->
      <ul class="order-2 menu menu-gray-600 menu-hover-primary fw-semibold">
        <li class="menu-item">
          <a href="https://keenthemes.com" target="_blank" class="menu-link px-2">{{ t("layout.footer.about") }}</a>
        </li>
        <li class="menu-item">
          <a href="https://preview.keenthemes.com/html/metronic/docs/index" target="_blank" class="menu-link px-2">Metronic</a>
        </li>
      </ul>
      <!--end::Menu-->
    </div>
    <!--end::Footer container-->
  </div>
  <!--end::Footer-->
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { footerDisplay } from "@/app/layouts/default/config/helper";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export default defineComponent({
  name: "theme-footer",
  components: {},
  setup() {
    const localeStore = useLocaleStore();
    const t = (key: string) => localeStore.t(key);

    return {
      footerDisplay,
      t,
    };
  },
});
</script>
