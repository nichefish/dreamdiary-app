import { ref } from "vue";
import { defineStore } from "pinia";
import axios from "axios";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

export interface AuthPolicy {
  id: number | null;
  duplicateLoginAllowedYn: "Y" | "N" | null;
  inactiveLockDays: number | null;
  loginAttemptLimit: number | null;
  loginAttemptWindowMinutes: number | null;
  accountLockDurationMinutes: number | null;
  sessionTimeoutMinutes: number | null;
  passwordChangeCycleDays: number | null;
  passwordHistoryCount: number | null;
  passwordResetTokenExpiryMinutes: number | null;
}

const EMPTY_POLICY: AuthPolicy = {
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
};

export const useAuthPolicyStore = defineStore("authPolicy", () => {
  const { t } = useLocaleStore();
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
        duplicateLoginAllowedYn: nextPolicy.duplicateLoginAllowedYn === "Y" ? "Y" : "N",
      };
      const res = await axios.put("/api/auth/policy", payload);
      if (!res.data?.rslt) throw new Error(res.data?.message ?? t("auth.policy.save.failure"));
      policy.value = {
        ...EMPTY_POLICY,
        ...(res.data?.rsltObj ?? payload),
      };
      return res.data?.message ?? t("common.result.saved");
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
