<template>
  <!--begin::Navbar-->
  <div class="app-navbar flex-shrink-0">
    <!--begin::Activities-->
    <div class="app-navbar-item ms-1 ms-md-4">
      <!--begin::Drawer toggle-->
      <div
        class="btn btn-icon btn-custom btn-icon-muted btn-active-light btn-active-color-primary w-35px h-35px"
        id="kt_activities_toggle"
      >
        <KTIcon icon-name="messages" icon-class="fs-2" />
      </div>
      <!--end::Drawer toggle-->
    </div>
    <!--end::Activities-->
    <!--begin::Notifications-->
    <div class="app-navbar-item ms-1 ms-md-4">
      <!--begin::Menu- wrapper-->
      <div
        class="btn btn-icon btn-custom btn-icon-muted btn-active-light btn-active-color-primary w-35px h-35px"
        data-kt-menu-trigger="{default: 'click', lg: 'hover'}"
        data-kt-menu-attach="parent"
        data-kt-menu-placement="bottom-end"
        id="kt_menu_item_wow"
      >
        <KTIcon icon-name="notification-status" icon-class="fs-2" />
      </div>
      <KTNotificationMenu />
      <!--end::Menu wrapper-->
    </div>
    <!--end::Notifications-->
    <!--begin::Chat-->
    <div class="app-navbar-item ms-1 ms-md-4">
      <!--begin::Menu wrapper-->
      <div
        class="btn btn-icon btn-custom btn-icon-muted btn-active-light btn-active-color-primary w-35px h-35px position-relative"
        id="kt_drawer_chat_toggle"
      >
        <KTIcon icon-name="message-text-2" icon-class="fs-2" />
        <span
          class="bullet bullet-dot bg-success h-6px w-6px position-absolute translate-middle top-0 start-50 animation-blink"
        ></span>
      </div>
      <!--end::Menu wrapper-->
    </div>
    <!--end::Chat-->
    <!--begin::Theme mode-->
    <div class="app-navbar-item ms-1 ms-md-3">
      <!--begin::Menu toggle-->
      <button
        type="button"
        class="btn btn-icon btn-custom btn-icon-muted btn-active-light btn-active-color-primary w-30px h-30px w-md-40px h-md-40px"
        :title="themeMode === 'light' ? 'Dark mode' : 'Light mode'"
        @click="toggleThemeMode"
      >
        <KTIcon
          v-if="themeMode === 'light'"
          icon-name="night-day"
          icon-class="fs-2"
        />
        <KTIcon v-else icon-name="moon" icon-class="fs-2" />
      </button>
    </div>
    <!--end::Theme mode-->
    <!--begin::User mode-->
    <div v-if="canSwitchMenuMode" class="app-navbar-item ms-1 ms-md-3">
      <select
        class="form-select form-select-solid form-select-sm fw-bold w-115px"
        :value="menuMode"
        title="사용자/관리자 권한별로 화면을 전환합니다."
        @change="onMenuModeChange"
      >
        <option value="USER">사용자 모드</option>
        <option value="MNGR">관리자 모드</option>
      </select>
    </div>
    <!--end::User mode-->
    <!--begin::User menu-->
    <div class="app-navbar-item ms-1 ms-md-4" id="kt_header_user_menu_toggle">
      <!--begin::Menu wrapper-->
      <div
        class="cursor-pointer symbol symbol-35px"
        data-kt-menu-trigger="{default: 'click', lg: 'hover'}"
        data-kt-menu-attach="parent"
        data-kt-menu-placement="bottom-end"
      >
        <img
          :src="profileImageUrl"
          class="rounded-3"
          alt="user"
          @error="handleProfileImageError"
        />
      </div>
      <KTUserMenu />
      <!--end::Menu wrapper-->
    </div>
    <!--end::User menu-->
    <!--begin::Header menu toggle-->
    <div
      class="app-navbar-item d-lg-none ms-2 me-n2"
      v-tooltip
      title="Show header menu"
    >
      <div
        class="btn btn-flex btn-icon btn-active-color-primary w-30px h-30px"
        id="kt_app_header_menu_toggle"
      >
        <KTIcon icon-name="element-4" icon-class="fs-2" />
      </div>
    </div>
    <!--end::Header menu toggle-->
  </div>
  <!--end::Navbar-->
</template>

<script lang="ts">
import { getAssetPath } from "@metronic/core/helpers/assets";
import {
  handleProfileImageError,
  resolveProfileImageUrl,
} from "@/utils/profileImage";
import { computed, defineComponent } from "vue";
import { useRouter } from "vue-router";
import KTNotificationMenu from "@/layouts/default/components/menus/NotificationsMenu.vue";
import KTUserMenu from "@/layouts/default/components/menus/UserAccountMenu.vue";
import { useAuthStore } from "@/stores/auth";
import { useMenuStore, type MenuMode } from "@/stores/menu";
import { useThemeStore } from "@/stores/theme";

export default defineComponent({
  name: "header-navbar",
  components: {
    KTNotificationMenu,
    KTUserMenu,
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const menuStore = useMenuStore();
    const store = useThemeStore();

    const themeMode = computed(() => store.mode);
    const menuMode = computed(() => menuStore.mode);
    const canSwitchMenuMode = computed(() => !!authStore.user?.isMngr);
    const profileImageUrl = computed(() =>
      resolveProfileImageUrl(authStore.user?.profileImageUrl)
    );
    const toggleThemeMode = () => {
      store.setThemeMode(themeMode.value === "light" ? "dark" : "light");
    };
    const onMenuModeChange = async (event: Event) => {
      const nextMode = (event.target as HTMLSelectElement).value as MenuMode;
      await menuStore.setMenuMode(nextMode);
      await router.push(nextMode === "MNGR" ? "/admin" : "/dashboard");
    };
    return {
      canSwitchMenuMode,
      themeMode,
      menuMode,
      toggleThemeMode,
      onMenuModeChange,
      handleProfileImageError,
      profileImageUrl,
      getAssetPath,
    };
  },
});
</script>
