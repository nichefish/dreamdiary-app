<template>
  <div
    class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg menu-state-color fw-semibold py-4 fs-6 w-275px"
    data-kt-menu="true"
  >
    <div class="menu-item px-3">
      <div class="menu-content d-flex align-items-center px-3">
        <div class="symbol symbol-50px me-5">
          <img :alt="t('navbar.profile-image.alt')" :src="profileImageUrl" @error="handleProfileImageError" />
        </div>
        <div class="d-flex flex-column min-w-0">
          <div class="fw-bold d-flex align-items-center fs-5 text-truncate">
            {{ user?.nickname || user?.username || t("account.role.user") }}
            <i
              v-if="user?.isMngr"
              class="bi bi-person-lines-fill fs-5 text-info ms-2 opacity-75"
              :title="t('account.role.manager')"
            ></i>
            <i v-else class="bi bi-people-fill fs-5 ms-2 opacity-75" :title="t('account.role.user')"></i>
          </div>
          <span class="fw-semibold text-muted fs-7 text-truncate">
            {{ user?.email || user?.username }}
          </span>
        </div>
      </div>
    </div>

    <div class="separator my-2"></div>

    <div class="menu-item px-5">
      <router-link to="/my" class="menu-link px-5">{{ t("account.menu.my-info") }}</router-link>
    </div>

    <div class="separator my-2"></div>

    <div class="menu-item px-5">
      <button type="button" class="menu-link px-5 border-0 bg-transparent w-100 text-start" @click="signOut">
        {{ t("account.logout") }}
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { swalConfirm } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import {
  handleProfileImageError,
  resolveProfileImageUrl,
} from "@/shared/utils/profileImage";

export default defineComponent({
  name: "kt-user-menu",
  setup() {
    const router = useRouter();
    const store = useAuthStore();
    const localeStore = useLocaleStore();
    const t = (key: string) => localeStore.t(key);

    const user = computed(() => store.user);
    const profileImageUrl = computed(() =>
      resolveProfileImageUrl(user.value?.profileImageUrl)
    );

    const signOut = async () => {
      if (!await swalConfirm(t("account.logout.confirm"))) return;
      await store.logout();
      await router.push({ name: "sign-in" });
    };

    return {
      handleProfileImageError,
      profileImageUrl,
      signOut,
      t,
      user,
    };
  },
});
</script>
