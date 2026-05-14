<template>
  <!--begin::App-->
  <div class="d-flex flex-column flex-root app-root" id="kt_app_root">
    <!--begin::Page-->
    <div class="app-page flex-column flex-column-fluid" id="kt_app_page">
      <KTHeader />
      <!--begin::Wrapper-->
      <div class="app-wrapper flex-column flex-row-fluid" id="kt_app_wrapper">
        <KTSidebar />
        <!--begin::Main-->
        <div class="app-main flex-column flex-row-fluid" id="kt_app_main">
          <div class="d-flex flex-column flex-column-fluid">
            <div id="kt_app_content" class="app-content flex-column-fluid">
              <KTContent />
            </div>
          </div>
          <KTFooter />
        </div>
        <!--end::Main-->
      </div>
      <!--end::Wrapper-->
    </div>
    <!--end::Page-->
  </div>
  <!--end::App-->
</template>

<script lang="ts">
import { defineComponent } from "vue";
import KTHeader from "@/layouts/default/components/header/Header.vue";
import KTSidebar from "@/layouts/default/components/sidebar/Sidebar.vue";
import KTContent from "@/layouts/default/components/content/Content.vue";
import KTFooter from "@/layouts/default/components/footer/Footer.vue";

export default defineComponent({
  name: "default",
  components: {
    KTHeader,
    KTSidebar,
    KTContent,
    KTFooter,
  },
});
</script>
