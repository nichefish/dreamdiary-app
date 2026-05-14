import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";

export interface AuthPolicy {
  id: number | null;
  inactiveLockDays: number | null;
  loginAttemptLimit: number | null;
  loginAttemptWindowMinutes: number | null;
  accountLockDurationMinutes: number | null;
  passwordChangeCycleDays: number | null;
  passwordResetTokenExpiryMinutes: number | null;
}

const EMPTY_POLICY: AuthPolicy = {
  id: null,
  inactiveLockDays: null,
  loginAttemptLimit: null,
  loginAttemptWindowMinutes: null,
  accountLockDurationMinutes: null,
  passwordChangeCycleDays: null,
  passwordResetTokenExpiryMinutes: null,
};

export const useAuthPolicyStore = defineStore("authPolicy", () => {
  const policy = ref<AuthPolicy>({ ...EMPTY_POLICY });
  const loading = ref(false);
  const saving = ref(false);

  async function fetchPolicy() {
    loading.value = true;
    try {
      const res = await axios.get("/api/auth/policy");
      policy.value = {
        ...EMPTY_POLICY,
        ...(res.data?.rsltObj ?? {}),
      };
    } finally {
      loading.value = false;
    }
  }

  async function savePolicy(nextPolicy: AuthPolicy) {
    saving.value = true;
    try {
      const payload = {
        ...nextPolicy,
        id: nextPolicy.id ?? 1,
      };
      const res = await axios.put("/api/auth/policy", payload);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? "인증 정책을 저장하지 못했습니다.");
      policy.value = {
        ...EMPTY_POLICY,
        ...(res.data?.rsltObj ?? payload),
      };
      return res.data?.message ?? "저장되었습니다.";
    } finally {
      saving.value = false;
    }
  }

  return {
    policy,
    loading,
    saving,
    fetchPolicy,
    savePolicy,
  };
});
