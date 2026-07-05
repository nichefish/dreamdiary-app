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
        {{ t("account.logout") }}
      </button>
    </div>
  </div>
  <!--end::Sidebar Footer-->
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { swalConfirm } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const authStore = useAuthStore();
const router = useRouter();
const localeStore = useLocaleStore();
const t = (key: string) => localeStore.t(key);

async function handleLogout() {
  if (!await swalConfirm(t("account.logout.confirm"))) return;
  await authStore.logout();
  await router.push({ name: "sign-in" });
}
</script>
