<template>
  <div class="user-my-page">
    <div class="card post">
      <div class="card-body">
        <div v-if="store.loading" class="user-my-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>

        <div class="user-my-summary">
          <div class="user-my-avatar">
            <img v-if="user.profileImageUrl" :src="user.profileImageUrl" alt="" />
            <i v-else class="fas fa-user-circle"></i>
            <button type="button" class="btn btn-sm btn-icon btn-light-primary user-my-avatar__edit" :title="t('user.my.upload-profile-image.tooltip')" @click="openFilePicker">
              <i class="bi bi-pencil"></i>
            </button>
            <button type="button" class="btn btn-sm btn-icon btn-light-danger user-my-avatar__remove" :title="t('user.my.remove-profile-image.tooltip')" @click="removeProfileImage">
              <i class="bi bi-x"></i>
            </button>
            <input ref="fileInput" type="file" class="d-none" accept=".png,.jpg,.jpeg,image/png,image/jpeg" @change="uploadProfileImage" />
          </div>

          <div class="user-my-summary__main">
            <div class="d-flex align-items-center gap-3 flex-wrap">
              <h3 class="mb-0">{{ fallback(user.nickname || user.username) }}</h3>
              <span class="badge badge-light-primary">{{ user.username }}</span>
            </div>
            <div class="user-my-role-list">
              <span v-for="role in user.userRoles" :key="role.roleKey" class="badge badge-light">
                <i :class="roleIcon(role.roleKey)" class="me-1"></i>
                {{ role.roleName || role.roleKey }}
              </span>
            </div>
          </div>

          <button type="button" class="btn btn-sm btn-secondary" @click="openPasswordModal">
            <i class="bi bi-key"></i>
            {{ t('user.my.password-change') }}
          </button>
        </div>
      </div>

      <div class="separator"></div>

      <div class="card-body">
        <div class="user-my-grid">
          <section class="user-my-section">
            <h4>{{ t('user.my.section.account') }}</h4>
            <dl>
              <div>
                <dt>{{ t('user.admin.list.col.email') }}</dt>
                <dd>{{ fallback(user.email) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.admin.detail.col.contact') }}</dt>
                <dd>{{ fallback(user.phoneNumber) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.admin.detail.col.allowed-ip') }}</dt>
                <dd>
                  <span :class="store.hasAllowedIp ? 'badge-light-primary' : 'badge-light'" class="badge">
                    {{ store.hasAllowedIp ? t('status.use') : t('status.unuse') }}
                  </span>
                  <span v-if="store.hasAllowedIp" class="ms-2 text-muted">
                    {{ allowedIpText }}
                  </span>
                </dd>
              </div>
            </dl>
          </section>

          <section class="user-my-section">
            <h4>{{ t('user.admin.detail.section.profile') }}</h4>
            <dl>
              <div>
                <dt>{{ t('user.profile.birth-date') }}</dt>
                <dd>{{ fallback(user.profile?.brthdy) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.my.detail.lunar-yn') }}</dt>
                <dd>{{ user.profile?.lunarYn === "Y" ? t('user.profile.lunar') : t('user.profile.solar') }}</dd>
              </div>
              <div>
                <dt>{{ t('user.my.detail.intro') }}</dt>
                <dd class="user-my-preline">{{ fallback(user.profile?.proflCn) }}</dd>
              </div>
            </dl>
          </section>

          <section class="user-my-section user-my-section--wide">
            <h4>{{ t('user.my.section.employment') }}</h4>
            <dl>
              <div>
                <dt>{{ t('user.emplym.name-placeholder') }}</dt>
                <dd>{{ fallback(user.emplym?.userNm) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.emplym.affiliation') }}</dt>
                <dd>{{ affiliationText }}</dd>
              </div>
              <div>
                <dt>{{ t('user.emplym.rank') }}</dt>
                <dd>{{ rankText }}</dd>
              </div>
              <div>
                <dt>{{ t('user.my.detail.join-retire') }}</dt>
                <dd>{{ joinRetireText }}</dd>
              </div>
              <div>
                <dt>{{ t('user.signup.work-email') }}</dt>
                <dd>{{ fallback(user.emplym?.emplymEmail) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.admin.form.emplym.phone.label') }}</dt>
                <dd>{{ fallback(user.emplym?.emplymPhoneNumber) }}</dd>
              </div>
              <div>
                <dt>{{ t('user.my.detail.payroll-account') }}</dt>
                <dd>{{ payrollText }}</dd>
              </div>
            </dl>
          </section>
        </div>
      </div>
    </div>

    <div ref="passwordModalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-md">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('user.my.password-change') }}</h5>
            <button type="button" class="btn-close" @click="closePasswordModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="passwordError" class="alert alert-danger py-3">{{ passwordError }}</div>
            <form class="form" @submit.prevent="submitPasswordChange">
              <div class="mb-4">
                <label for="currPw" class="form-label required">{{ t('user.my.current-password') }}</label>
                <input id="currPw" v-model="passwordForm.currPw" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="current-password" />
              </div>
              <div class="mb-4">
                <label for="newPw" class="form-label required">{{ t('user.my.new-password') }}</label>
                <input id="newPw" v-model="passwordForm.newPw" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" />
                <div class="form-text">{{ t('user.my.pw-change.form.desc') }}</div>
              </div>
              <div>
                <label for="newPwCf" class="form-label required">{{ t('user.my.new-password-confirm') }}</label>
                <input id="newPwCf" v-model="passwordForm.newPwCf" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" />
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-primary" :disabled="submittingPassword" @click="submitPasswordChange">
              <span v-if="submittingPassword" class="spinner-border spinner-border-sm me-1"></span>
              {{ t('common.save') }}
            </button>
            <button type="button" class="btn btn-sm btn-light" @click="closePasswordModal">{{ t('common.close') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { computed, onMounted, reactive, ref } from "vue";
import { Modal } from "bootstrap";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { useUserMyStore } from "@/features/user/stores/userMy";

const store = useUserMyStore();
const { t } = useLocaleStore();
const authStore = useAuthStore();

const fileInput = ref<HTMLInputElement | null>(null);
const passwordModalEl = ref<HTMLElement | null>(null);
let passwordModal: Modal | null = null;

const passwordError = ref("");
const submittingPassword = ref(false);
const passwordForm = reactive({
  currPw: "",
  newPw: "",
  newPwCf: "",
});

const user = computed(() => store.user);

const allowedIpText = computed(() => {
  const items = user.value.allowedIpList ?? [];
  if (!items.length) return "-";
  return items.map((item) => item.allowedIp).filter(Boolean).join(", ");
});

const affiliationText = computed(() => {
  return compact([user.value.emplym?.cmpyNm, user.value.emplym?.teamNm, user.value.emplym?.emplymNm]).join(" / ") || "-";
});

const rankText = computed(() => {
  const rank = user.value.emplym?.rankNm || "-";
  return user.value.emplym?.apntcYn === "Y" ? `${rank} (${t("user.emplym.probation.active")})` : rank;
});

const joinRetireText = computed(() => {
  const joinDate = user.value.emplym?.ecnyDt || "-";
  if (user.value.emplym?.retireYn === "Y") return `${joinDate} / ${t("user.emplym.retired")} ${user.value.emplym?.retireDt || "-"}`;
  return joinDate;
});

const payrollText = computed(() => {
  return compact([user.value.emplym?.acntBank, user.value.emplym?.acntNo]).join(" / ") || "-";
});

function compact(values: Array<string | null | undefined>): string[] {
  return values.map((value) => (value ?? "").trim()).filter(Boolean);
}

function fallback(value: string | null | undefined): string {
  return value?.trim() || "-";
}

function roleIcon(roleKey?: string): string {
  if (roleKey === "ROLE_MNGR") return "bi bi-shield-check text-primary";
  if (roleKey === "ROLE_DEV") return "bi bi-code-slash text-success";
  return "bi bi-person text-muted";
}

async function reload() {
  await store.fetchMyInfo();
}

function openFilePicker() {
  fileInput.value?.click();
}

async function uploadProfileImage(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";
  if (!file) return;

  const extOk = /\.(png|jpe?g)$/i.test(file.name);
  if (!extOk || !file.type.startsWith("image/")) {
    void swalAlert(t("user.my.profile-image.format.invalid"));
    return;
  }

  try {
    await store.uploadProfileImage(file);
    await Promise.all([reload(), authStore.verifyAuth()]);
    void swalAlert(t("user.my.profile-image.change.success"));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("user.my.profile-image.change.failure"));
  }
}

async function removeProfileImage() {
  if (!await swalConfirm(t("user.my.profile-image.delete.confirm"))) return;
  try {
    await store.removeProfileImage();
    await Promise.all([reload(), authStore.verifyAuth()]);
    void swalAlert(t("user.my.profile-image.delete.success"));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("user.my.profile-image.delete.failure"));
  }
}

function resetPasswordForm() {
  passwordForm.currPw = "";
  passwordForm.newPw = "";
  passwordForm.newPwCf = "";
  passwordError.value = "";
}

async function openPasswordModal() {
  if (!await assertAuthenticatedBeforeModal()) return;
  resetPasswordForm();
  passwordModal?.show();
}

function closePasswordModal() {
  passwordModal?.hide();
}

function validatePasswordForm(): boolean {
  if (!passwordForm.currPw || !passwordForm.newPw || !passwordForm.newPwCf) {
    passwordError.value = t("user.my.validate.required");
    return false;
  }
  if (passwordForm.newPw !== passwordForm.newPwCf) {
    passwordError.value = t("user.my.validate.pw-confirm.mismatch");
    return false;
  }
  if (passwordForm.newPw.length < 8) {
    passwordError.value = t("user.my.validate.pw.min-length");
    return false;
  }
  passwordError.value = "";
  return true;
}

async function submitPasswordChange() {
  if (!validatePasswordForm()) return;
  if (!await swalConfirm(t("user.my.pw-change.confirm"))) return;

  submittingPassword.value = true;
  try {
    await store.changePassword({
      username: user.value.username,
      currPw: passwordForm.currPw,
      newPw: passwordForm.newPw,
    });
    closePasswordModal();
    void swalAlert(t("user.my.pw-change.success"));
  } catch (error) {
    passwordError.value = error instanceof Error ? error.message : t("user.my.pw-change.failure");
  } finally {
    submittingPassword.value = false;
  }
}

onMounted(async () => {
  if (passwordModalEl.value) passwordModal = new Modal(passwordModalEl.value);
  await reload();
});
</script>

<style scoped>
.user-my-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.user-my-loading {
  position: absolute;
  top: 1rem;
  right: 1.5rem;
  z-index: 2;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  background: var(--bs-body-bg);
  box-shadow: 0 0.25rem 1rem rgba(15, 23, 42, 0.12);
  color: var(--bs-gray-700);
}

.user-my-summary {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 1.5rem;
}

.user-my-avatar {
  position: relative;
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-500);
  overflow: visible;
}

.user-my-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.user-my-avatar > i {
  font-size: 3rem;
}

.user-my-avatar__edit,
.user-my-avatar__remove {
  position: absolute;
  width: 28px;
  height: 28px;
}

.user-my-avatar__edit {
  top: -8px;
  right: -8px;
}

.user-my-avatar__remove {
  right: -8px;
  bottom: -8px;
}

.user-my-summary__main {
  min-width: 0;
}

.user-my-role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.user-my-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.5rem;
}

.user-my-section {
  min-width: 0;
}

.user-my-section--wide {
  grid-column: 1 / -1;
}

.user-my-section h4 {
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 700;
}

.user-my-section dl {
  display: grid;
  gap: 0.75rem;
  margin: 0;
}

.user-my-section dl > div {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px dashed var(--bs-gray-300);
}

.user-my-section dt {
  color: var(--bs-gray-700);
  font-weight: 700;
}

.user-my-section dd {
  margin: 0;
  min-width: 0;
  word-break: break-word;
}

.user-my-preline {
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .user-my-summary,
  .user-my-grid,
  .user-my-section dl > div {
    grid-template-columns: 1fr;
  }

  .user-my-summary {
    justify-items: start;
  }
}
</style>
