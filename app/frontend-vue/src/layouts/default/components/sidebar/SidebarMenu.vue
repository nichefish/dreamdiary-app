<template>
  <div class="app-sidebar-menu overflow-hidden flex-column-fluid">
    <div
      id="kt_app_sidebar_menu_wrapper"
      class="app-sidebar-wrapper hover-scroll-overlay-y my-5"
      data-kt-scroll="true"
      data-kt-scroll-activate="true"
      data-kt-scroll-height="auto"
      data-kt-scroll-dependencies="#kt_app_sidebar_logo, #kt_app_sidebar_footer"
      data-kt-scroll-wrappers="#kt_app_sidebar_menu"
      data-kt-scroll-offset="5px"
      data-kt-scroll-save-state="true"
    >
      <div
        id="kt_app_sidebar_menu"
        class="menu menu-column menu-rounded menu-sub-indention fw-semibold fs-6 px-3"
        data-kt-menu="true"
        data-kt-menu-expand="true"
      >
        <SidebarMenuItem
          v-for="menu in menuStore.menuList"
          :key="menu.id ?? menu.menuName"
          :menu="menu"
          :depth="0"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, nextTick, onMounted, watch } from "vue";
import { reinitializeComponents } from "@metronic/core/plugins/keenthemes";
import SidebarMenuItem from "@/layouts/default/components/sidebar/SidebarMenuItem.vue";
import { useMenuStore } from "@/stores/menu";

export default defineComponent({
  name: "sidebar-menu",
  components: {
    SidebarMenuItem,
  },
  setup() {
    const menuStore = useMenuStore();

    const refreshKtMenu = async () => {
      await nextTick();
      reinitializeComponents();
    };

    onMounted(async () => {
      await menuStore.fetchUserMenu();
      await refreshKtMenu();
    });

    watch(
      () => menuStore.menuList,
      () => {
        void refreshKtMenu();
      },
      { deep: true }
    );

    return {
      menuStore,
    };
  },
});
</script>
