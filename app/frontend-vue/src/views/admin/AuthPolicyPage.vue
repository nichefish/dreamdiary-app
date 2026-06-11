<template>
  <div class="auth-policy-page">
    <div class="auth-policy-toolbar">
      <div class="text-muted fs-7">로그인 실패, 계정 잠금, 비밀번호 변경 주기, 세션 정책을 관리합니다.</div>
      <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="reload">
        <i class="bi bi-arrow-clockwise"></i>
      </button>
    </div>

    <div class="card post">
      <div class="card-body">
        <div v-if="store.loading" class="auth-policy-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>

        <form class="auth-policy-form" @submit.prevent="save">
          <div class="auth-policy-row">
            <div>
              <label for="duplicateLoginAllowedYn" class="form-label fw-bold">중복 로그인 허용</label>
              <div class="text-muted fs-8">
                켜면 같은 계정으로 여러 브라우저나 기기에서 동시에 로그인할 수 있습니다. 변경 후 서버 재시작 시 보안 정책에 반영됩니다.
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
                <span class="form-check-label">{{ duplicateLoginAllowed ? "허용" : "차단" }}</span>
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
                :min="1"
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
            저장
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, watch } from "vue";
import { swalAlert, swalConfirm } from "@/utils/swal";
import { useAuthPolicyStore, type AuthPolicy } from "@/stores/authPolicy";

type NumberPolicyKey = Exclude<keyof AuthPolicy, "id" | "duplicateLoginAllowedYn">;

interface FieldDef {
  key: NumberPolicyKey;
  label: string;
  notice: string;
  unit: string;
  max: number;
}

const store = useAuthPolicyStore();

const form = reactive<AuthPolicy>({
  id: null,
  duplicateLoginAllowedYn: "N",
  inactiveLockDays: null,
  loginAttemptLimit: null,
  loginAttemptWindowMinutes: null,
  accountLockDurationMinutes: null,
  passwordChangeCycleDays: null,
  passwordResetTokenExpiryMinutes: null,
});

const fields: FieldDef[] = [
  {
    key: "inactiveLockDays",
    label: "미로그인 계정 잠금",
    notice: "해당 일수 동안 로그인하지 않은 계정을 자동 잠금 처리합니다.",
    unit: "일",
    max: 365,
  },
  {
    key: "loginAttemptLimit",
    label: "로그인 실패 제한",
    notice: "실패 집계 시간 안에서 허용할 로그인 실패 횟수입니다.",
    unit: "회",
    max: 999,
  },
  {
    key: "loginAttemptWindowMinutes",
    label: "로그인 실패 집계 시간",
    notice: "로그인 실패 횟수를 누적해서 판단하는 시간 범위입니다.",
    unit: "분",
    max: 999,
  },
  {
    key: "passwordChangeCycleDays",
    label: "비밀번호 변경 주기",
    notice: "비밀번호 변경 경고 또는 변경 유도 기준이 되는 일수입니다.",
    unit: "일",
    max: 365,
  },
  {
    key: "accountLockDurationMinutes",
    label: "계정 잠금 지속 시간",
    notice: "로그인 실패 제한 초과 후 잠금이 유지되는 시간입니다.",
    unit: "분",
    max: 9999,
  },
  {
    key: "passwordResetTokenExpiryMinutes",
    label: "비밀번호 재설정 토큰 만료",
    notice: "관리자 초기화 또는 재설정 링크가 유효한 시간입니다.",
    unit: "분",
    max: 10080,
  },
];

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
  for (const field of fields) {
    const value = form[field.key];
    if (value == null || Number.isNaN(Number(value)) || Number(value) < 1 || Number(value) > field.max) {
      void swalAlert(`${field.label} 값은 1부터 ${field.max} 사이여야 합니다.`);
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
  if (!await swalConfirm("인증 정책을 저장할까요?")) return;

  try {
    const message = await store.savePolicy({ ...form });
    void swalAlert(message);
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : "인증 정책을 저장하지 못했습니다.");
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
  justify-content: space-between;
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
