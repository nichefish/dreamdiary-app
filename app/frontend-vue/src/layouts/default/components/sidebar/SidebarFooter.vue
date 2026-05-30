<template>
  <!--begin::Sidebar Footer-->
  <div class="app-sidebar-footer flex-column-auto pt-2 pb-6 px-6" id="kt_app_sidebar_footer">
    <div class="d-flex align-items-center justify-content-between">
      <span class="text-muted fs-7 text-truncate" style="max-width: 140px;">
        {{ authStore.user?.nickname ?? authStore.user?.username ?? '' }}
      </span>
      <button
        type="button"
        class="btn btn-sm btn-light-danger"
        @click="handleLogout"
      >
        로그아웃
      </button>
    </div>
  </div>
  <!--end::Sidebar Footer-->
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const router = useRouter();

async function handleLogout() {
  await authStore.logout();
  await router.push({ name: "sign-in" });
}
</script>
