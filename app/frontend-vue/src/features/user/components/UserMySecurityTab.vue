<template>
  <div class="user-my-security">
    <section>
      <h4>{{ t("user.my.security.allowed-ip") }}</h4>
      <span :class="store.hasAllowedIp ? 'badge-light-primary' : 'badge-light'" class="badge">
        {{ store.hasAllowedIp ? t("status.use") : t("status.unuse") }}
      </span>
      <span v-if="store.hasAllowedIp" class="ms-2 text-muted">{{ allowedIpText }}</span>
    </section>

    <div class="separator my-6"></div>

    <section>
      <h4>{{ t("user.my.password-change") }}</h4>
      <p class="text-muted">{{ t("user.my.security.password.description") }}</p>
      <button type="button" class="btn btn-sm btn-primary" @click="openPasswordModal">
        <i class="bi bi-key me-1"></i>{{ t("user.my.password-change") }}
      </button>
    </section>

    <div ref="modalEl" class="modal fade" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-md"><div class="modal-content">
        <div class="modal-header"><h5 class="modal-title">{{ t("user.my.password-change") }}</h5><button type="button" class="btn-close" @click="closeModal"></button></div>
        <div class="modal-body">
          <div v-if="errorMessage" class="alert alert-danger py-3">{{ errorMessage }}</div>
          <form @submit.prevent="submit">
            <div class="mb-4"><label for="currPw" class="form-label required">{{ t("user.my.current-password") }}</label><input id="currPw" v-model="form.currPw" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="current-password" /></div>
            <div class="mb-4"><label for="newPw" class="form-label required">{{ t("user.my.new-password") }}</label><input id="newPw" v-model="form.newPw" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" /><div class="form-text">{{ t("user.my.pw-change.form.desc") }}</div></div>
            <div><label for="newPwCf" class="form-label required">{{ t("user.my.new-password-confirm") }}</label><input id="newPwCf" v-model="form.newPwCf" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" /></div>
          </form>
        </div>
        <div class="modal-footer"><button type="button" class="btn btn-sm btn-primary" :disabled="submitting" @click="submit"><span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>{{ t("common.save") }}</button><button type="button" class="btn btn-sm btn-light" @click="closeModal">{{ t("common.close") }}</button></div>
      </div></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { Modal } from "bootstrap";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useUserMyStore } from "@/features/user/stores/userMy";
import { assertAuthenticatedBeforeModal } from "@/shared/auth/sessionPing";
import { swalAlert, swalConfirm } from "@/shared/utils/swal";

const store = useUserMyStore();
const { t } = useLocaleStore();
const modalEl = ref<HTMLElement | null>(null);
const errorMessage = ref("");
const submitting = ref(false);
const form = reactive({ currPw: "", newPw: "", newPwCf: "" });
let modal: Modal | null = null;
const allowedIpText = computed(() => (store.user.allowedIpList ?? []).map((item) => item.allowedIp).filter(Boolean).join(", "));

async function openPasswordModal() {
  if (!await assertAuthenticatedBeforeModal()) return;
  form.currPw = ""; form.newPw = ""; form.newPwCf = ""; errorMessage.value = "";
  modal?.show();
}
function closeModal() { modal?.hide(); }
function validate(): boolean {
  if (!form.currPw || !form.newPw || !form.newPwCf) errorMessage.value = t("user.my.validate.required");
  else if (form.newPw !== form.newPwCf) errorMessage.value = t("user.my.validate.pw-confirm.mismatch");
  else if (form.newPw.length < 8) errorMessage.value = t("user.my.validate.pw.min-length");
  else { errorMessage.value = ""; return true; }
  return false;
}
async function submit() {
  if (!validate() || !await swalConfirm(t("user.my.pw-change.confirm"))) return;
  submitting.value = true;
  try {
    await store.changePassword({ username: store.user.username, currPw: form.currPw, newPw: form.newPw });
    closeModal(); void swalAlert(t("user.my.pw-change.success"));
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t("user.my.pw-change.failure");
  } finally { submitting.value = false; }
}
onMounted(() => { if (modalEl.value) modal = new Modal(modalEl.value); });
</script>

<style scoped>
.user-my-security section h4 { margin-bottom:1rem; font-size:1rem; font-weight:700; }
</style>
