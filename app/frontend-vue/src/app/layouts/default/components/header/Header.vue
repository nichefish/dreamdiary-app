<template>
  <!--begin::Header-->
  <div v-if="headerDisplay" id="kt_app_header" class="app-header">
    <!--begin::Header container-->
    <div
      class="app-container d-flex align-items-stretch justify-content-between"
      :class="{
        'container-fluid': headerWidthFluid,
        'container-xxl': !headerWidthFluid,
      }"
    >
      <template>
        <!--begin::sidebar mobile toggle-->
        <div
          class="d-flex align-items-center d-lg-none ms-n3 me-1 me-md-2"
          v-tooltip
          title="Show sidebar menu"
        >
          <div
            class="btn btn-icon btn-active-color-primary w-35px h-35px"
            id="kt_app_sidebar_mobile_toggle"
          >
            <KTIcon icon-name="abstract-14" icon-class="fs-2 fs-md-1" />
          </div>
        </div>
        <!--end::sidebar mobile toggle-->
      </template>
      <!--begin::Header wrapper-->
      <div
        class="d-flex align-items-stretch justify-content-between flex-lg-grow-1"
        id="kt_app_header_wrapper"
      >
        <!--begin::breadcrumb — 헤더 좌측, 서버 메뉴 트리 기반 현재 경로와 설명 표시-->
        <PageBreadcrumb />
        <!--end::breadcrumb-->
        <KTHeaderNavbar class="ms-auto" />
      </div>
      <!--end::Header wrapper-->
    </div>
    <!--end::Header container-->
  </div>
  <!--end::Header-->
</template>

<script lang="ts">
import { defineComponent } from "vue";
import KTHeaderNavbar from "@/app/layouts/default/components/header/Navbar.vue";
import PageBreadcrumb from "@/app/layouts/default/components/content/PageBreadcrumb.vue";
import {
  headerDisplay,
  headerWidthFluid,
  headerDesktopFixed,
  headerMobileFixed,
} from "@/app/layouts/default/config/helper";

export default defineComponent({
  name: "layout-header",
  components: {
    KTHeaderNavbar,
    PageBreadcrumb,
  },
  setup() {
    return {
      headerWidthFluid,
      headerDisplay,
      headerDesktopFixed,
      headerMobileFixed,
    };
  },
});
</script>
