<template>
  <!--begin::Footer-->
  <div v-if="footerDisplay" id="kt_app_footer" class="app-footer">
    <!--begin::Footer container-->
    <div class="app-container container-fluid d-flex flex-column flex-md-row flex-center flex-md-stack py-3">
      <!--begin::Copyright-->
      <div class="order-1 order-md-1 text-gray-900">
        <span
          class="text-muted fw-semibold me-1"
          data-bs-toggle="tooltip"
          data-bs-placement="top"
          data-bs-dismiss="click"
          title="프로젝트 기간: &#10;2024.03.20 ~ (진행중)"
        >2024©</span>
        <a
          href="/"
          class="text-gray-600 text-hover-primary fw-bold blink cursor-help"
          data-bs-toggle="tooltip"
          data-bs-placement="top"
          data-bs-dismiss="click"
          title="작업인원: &#10;nysnyari"
        >dreamdiary.nicheblog.io</a>
      </div>
      <!--end::Copyright-->
      <!--begin::Menu-->
      <ul class="order-2 menu menu-gray-600 menu-hover-primary fw-semibold">
        <li class="menu-item">
          <a href="https://keenthemes.com" target="_blank" class="menu-link px-2">About</a>
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

export default defineComponent({
  name: "theme-footer",
  components: {},
  setup() {
    return {
      footerDisplay,
    };
  },
});
</script>
