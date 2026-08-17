<template>
  <div class="user-my-page">
    <div class="card post">
      <div class="card-body">
        <div v-if="store.loading" class="user-my-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>{{ t("common.loading") }}
        </div>

        <div class="user-my-summary">
          <div class="user-my-avatar">
            <img v-if="user.profileImageUrl" :src="user.profileImageUrl" alt="" />
            <i v-else class="fas fa-user-circle"></i>
            <button type="button" class="btn btn-sm btn-icon btn-light-primary user-my-avatar__edit" :title="t('user.my.upload-profile-image.tooltip')" @click="openFilePicker"><i class="bi bi-pencil"></i></button>
            <button type="button" class="btn btn-sm btn-icon btn-light-danger user-my-avatar__remove" :title="t('user.my.remove-profile-image.tooltip')" @click="removeProfileImage"><i class="bi bi-x"></i></button>
            <input ref="fileInput" type="file" class="d-none" accept=".png,.jpg,.jpeg,image/png,image/jpeg" @change="uploadProfileImage" />
          </div>

          <div class="user-my-summary__main">
            <div class="d-flex align-items-center gap-3 flex-wrap">
              <h3 class="mb-0">{{ fallback(user.nickname || user.username) }}</h3>
              <span class="badge badge-light-primary">{{ user.username }}</span>
            </div>
            <div class="user-my-role-list">
              <span v-for="role in user.userRoles" :key="role.roleKey" class="badge badge-light">
                <i :class="roleIcon(role.roleKey)" class="me-1"></i>{{ role.roleName || role.roleKey }}
              </span>
            </div>
          </div>

        </div>
      </div>

      <div class="separator"></div>

      <div class="card-header min-h-auto pt-4 px-6">
        <ul class="nav nav-tabs nav-line-tabs border-0 fs-6">
          <li class="nav-item"><router-link :to="{ name: 'user-my-profile' }" class="nav-link">{{ t("user.my.tab.profile") }}</router-link></li>
          <li class="nav-item"><router-link :to="{ name: 'user-my-security' }" class="nav-link">{{ t("user.my.tab.security") }}</router-link></li>
          <li class="nav-item"><router-link :to="{ name: 'user-my-journal' }" class="nav-link">{{ t("user.my.tab.journal") }}</router-link></li>
          <li class="nav-item"><router-link :to="{ name: 'user-my-prefixes' }" class="nav-link">{{ t("user.my.tab.prefixes") }}</router-link></li>
        </ul>
      </div>

      <div class="card-body"><router-view /></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useUserMyStore } from "@/features/user/stores/userMy";
import { swalAlert, swalConfirm } from "@/shared/utils/swal";

const store = useUserMyStore();
const authStore = useAuthStore();
const { t } = useLocaleStore();
const fileInput = ref<HTMLInputElement | null>(null);
const user = computed(() => store.user);
const fallback = (value: string | null | undefined) => value?.trim() || "-";

function roleIcon(roleKey?: string): string {
  if (roleKey === "ROLE_MNGR") return "bi bi-shield-check text-primary";
  if (roleKey === "ROLE_DEV") return "bi bi-code-slash text-success";
  return "bi bi-person text-muted";
}
async function reload() { await store.fetchMyInfo(); }
function openFilePicker() { fileInput.value?.click(); }
async function uploadProfileImage(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";
  if (!file) return;
  if (!/\.(png|jpe?g)$/i.test(file.name) || !file.type.startsWith("image/")) {
    void swalAlert(t("user.my.profile-image.format.invalid")); return;
  }
  try {
    await store.uploadProfileImage(file);
    await Promise.all([reload(), authStore.verifyAuth({ force: true })]);
    void swalAlert(t("user.my.profile-image.change.success"));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("user.my.profile-image.change.failure"));
  }
}
async function removeProfileImage() {
  if (!await swalConfirm(t("user.my.profile-image.delete.confirm"))) return;
  try {
    await store.removeProfileImage();
    await Promise.all([reload(), authStore.verifyAuth({ force: true })]);
    void swalAlert(t("user.my.profile-image.delete.success"));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("user.my.profile-image.delete.failure"));
  }
}
onMounted(reload);
</script>

<style scoped>
.user-my-page { display:flex; flex-direction:column; gap:1rem; }
.user-my-loading { position:absolute; top:1rem; right:1.5rem; z-index:2; padding:.5rem .75rem; border-radius:6px; background:var(--bs-body-bg); box-shadow:0 .25rem 1rem rgba(15,23,42,.12); color:var(--bs-gray-700); }
.user-my-summary { display:grid; grid-template-columns:auto minmax(0,1fr); align-items:center; gap:1.5rem; }
.user-my-avatar { position:relative; display:grid; place-items:center; width:84px; height:84px; border-radius:8px; background:var(--bs-light); color:var(--bs-gray-500); overflow:visible; }
.user-my-avatar img { width:100%; height:100%; object-fit:cover; border-radius:8px; }
.user-my-avatar>i { font-size:3rem; }
.user-my-avatar__edit,.user-my-avatar__remove { position:absolute; width:28px; height:28px; }
.user-my-avatar__edit { top:-8px; right:-8px; }
.user-my-avatar__remove { right:-8px; bottom:-8px; }
.user-my-summary__main { min-width:0; }
.user-my-role-list { display:flex; flex-wrap:wrap; gap:.5rem; margin-top:.75rem; }
.nav-link.router-link-active { color:var(--bs-primary); border-bottom-color:var(--bs-primary); }
@media (max-width:768px) { .user-my-summary { grid-template-columns:1fr; justify-items:start; } }
</style>
