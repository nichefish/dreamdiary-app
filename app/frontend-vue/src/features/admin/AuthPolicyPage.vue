<template>
  <div class="auth-policy-page">
    <div class="auth-policy-toolbar">
      <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="reload">
        <i class="bi bi-arrow-clockwise"></i>
      </button>
    </div>

    <div class="card post">
      <div class="card-body">
        <div v-if="store.loading" class="auth-policy-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t('common.loading') }}
        </div>

        <form class="auth-policy-form" @submit.prevent="save">
          <div class="auth-policy-row">
            <div>
              <label for="duplicateLoginAllowedYn" class="form-label fw-bold">{{ t('auth.policy.dup-login.label') }}</label>
              <div class="text-muted fs-8">
              {{ t('auth.policy.dup-login.notice') }}
              </div>
            </div>
            <div class="auth-policy-toggle">
              <label class="form-check form-switch form-check-custom form-check-solid justify-content-end">
                <input
                  id="duplicateLoginAllowedYn"
                  v-model="duplicateLoginAllowed"
                  class="form-check-input"
                  type="checkbox"
                />
                <span class="form-check-label">{{ duplicateLoginAllowed ? t("common.allow") : t("common.block") }}</span>
              </label>
            </div>
          </div>

          <div v-for="field in fields" :key="field.key" class="auth-policy-row">
            <div>
              <label :for="field.key" class="form-label fw-bold">{{ field.label }}</label>
              <div class="text-muted fs-8">{{ field.notice }}</div>
            </div>
            <div class="auth-policy-input">
              <input
                :id="field.key"
                v-model.number="form[field.key]"
                type="number"
                class="form-control form-control-solid text-end"
                :min="field.min ?? 1"
                :max="field.max"
                required
              />
              <span>{{ field.unit }}</span>
            </div>
          </div>
        </form>
      </div>

      <div class="card-footer">
        <div class="d-flex justify-content-end">
          <button type="button" class="btn btn-sm btn-primary" :disabled="store.saving" @click="save">
            <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
            <i v-else class="bi bi-pencil-square"></i>
            {{ t('common.save') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, watch } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert, swalConfirm } from "@/shared/utils/swal";
import { useAuthPolicyStore, type AuthPolicy } from "@/features/admin/stores/authPolicy";

type NumberPolicyKey = Exclude<keyof AuthPolicy, "id" | "duplicateLoginAllowedYn">;

interface FieldDef {
  key: NumberPolicyKey;
  label: string;
  notice: string;
  unit: string;
  min?: number;
  max: number;
}

const store = useAuthPolicyStore();
const { t } = useLocaleStore();

const form = reactive<AuthPolicy>({
  id: null,
  duplicateLoginAllowedYn: "N",
  inactiveLockDays: null,
  loginAttemptLimit: null,
  loginAttemptWindowMinutes: null,
  accountLockDurationMinutes: null,
  sessionTimeoutMinutes: null,
  passwordChangeCycleDays: null,
  passwordHistoryCount: null,
  passwordResetTokenExpiryMinutes: null,
});

const fields = computed<FieldDef[]>(() => [
  {
    key: "inactiveLockDays",
    label: t("auth.policy.inactive-lock.label"),
    notice: t("auth.policy.inactive-lock.notice"),
    unit: t("common.unit.day"),
    max: 365,
  },
  {
    key: "loginAttemptLimit",
    label: t("auth.policy.login-attempt-limit.label"),
    notice: t("auth.policy.login-attempt-limit.notice"),
    unit: t("common.unit.count"),
    max: 999,
  },
  {
    key: "loginAttemptWindowMinutes",
    label: t("auth.policy.login-attempt-window.label"),
    notice: t("auth.policy.login-attempt-window.notice"),
    unit: t("common.unit.minute"),
    max: 999,
  },
  {
    key: "passwordChangeCycleDays",
    label: t("auth.policy.password-change-cycle.label"),
    notice: t("auth.policy.password-change-cycle.notice"),
    unit: t("common.unit.day"),
    max: 365,
  },
  {
    key: "passwordHistoryCount",
    label: t("auth.policy.password-history.label"),
    notice: t("auth.policy.password-history.notice"),
    unit: t("common.unit.item"),
    min: 0,
    max: 24,
  },
  {
    key: "accountLockDurationMinutes",
    label: t("auth.policy.account-lock-duration.label"),
    notice: t("auth.policy.account-lock-duration.notice"),
    unit: t("common.unit.minute"),
    max: 9999,
  },
  {
    key: "sessionTimeoutMinutes",
    label: t("auth.policy.session-timeout.label"),
    notice: t("auth.policy.session-timeout.notice"),
    unit: t("common.unit.minute"),
    max: 10080,
  },
  {
    key: "passwordResetTokenExpiryMinutes",
    label: t("auth.policy.password-reset-expiry.label"),
    notice: t("auth.policy.password-reset-expiry.notice"),
    unit: t("common.unit.minute"),
    max: 10080,
  },
]);

const duplicateLoginAllowed = computed({
  get: () => form.duplicateLoginAllowedYn === "Y",
  set: (value: boolean) => {
    form.duplicateLoginAllowedYn = value ? "Y" : "N";
  },
});

watch(
  () => store.policy,
  (policy) => {
    Object.assign(form, policy);
    form.duplicateLoginAllowedYn = policy.duplicateLoginAllowedYn === "Y" ? "Y" : "N";
  },
  { deep: true }
);

function validate(): boolean {
  for (const field of fields.value) {
    const value = form[field.key];
    const min = field.min ?? 1;
    if (value == null || Number.isNaN(Number(value)) || Number(value) < min || Number(value) > field.max) {
      void swalAlert(t("auth.policy.validate.range").replace("{label}", field.label).replace("{min}", String(min)).replace("{max}", String(field.max)));
      return false;
    }
  }
  return true;
}

async function reload() {
  await store.fetchPolicy();
  Object.assign(form, store.policy);
  form.duplicateLoginAllowedYn = store.policy.duplicateLoginAllowedYn === "Y" ? "Y" : "N";
}

async function save() {
  if (!validate()) return;
  if (!await swalConfirm(t("auth.policy.save.confirm"))) return;

  try {
    const message = await store.savePolicy({ ...form });
    void swalAlert(message);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("auth.policy.save.failure"));
  }
}

onMounted(async () => {
  await reload();
});
</script>

<style scoped>
.auth-policy-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.auth-policy-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 1rem;
}

.auth-policy-loading {
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

.auth-policy-form {
  display: grid;
  gap: 1rem;
}

.auth-policy-row {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 180px;
  gap: 1rem;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px dashed var(--bs-gray-300);
}

.auth-policy-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 32px;
  gap: 0.5rem;
  align-items: center;
}

.auth-policy-input span {
  color: var(--bs-gray-600);
}

.auth-policy-toggle {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .auth-policy-row {
    grid-template-columns: 1fr;
  }

  .auth-policy-toggle {
    justify-content: flex-start;
  }
}
</style>
