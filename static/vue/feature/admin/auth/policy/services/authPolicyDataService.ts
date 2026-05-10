import { AuthPolicyFormFields } from "../types.js";

function numToStr(v: unknown): string {
    if (v === null || v === undefined) return "";
    return String(v);
}

export default {
    /**
     * FTL `#auth_policy_page_data` JSON → 폼 초깃값
     */
    parseInitialForm(): AuthPolicyFormFields {
        const dataEl: HTMLElement | null = document.getElementById("auth_policy_page_data");
        const empty: AuthPolicyFormFields = {
            id: "",
            inactiveLockDays: "",
            loginAttemptLimit: "",
            loginAttemptWindowMinutes: "",
            accountLockDurationMinutes: "",
            passwordChangeCycleDays: "",
            passwordResetTokenExpiryMinutes: "",
        };
        if (!dataEl) return empty;
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "{}");
            const o = parsed as Record<string, unknown>;
            return {
                id: numToStr(o.id),
                inactiveLockDays: numToStr(o.inactiveLockDays),
                loginAttemptLimit: numToStr(o.loginAttemptLimit),
                loginAttemptWindowMinutes: numToStr(o.loginAttemptWindowMinutes),
                accountLockDurationMinutes: numToStr(o.accountLockDurationMinutes),
                passwordChangeCycleDays: numToStr(o.passwordChangeCycleDays),
                passwordResetTokenExpiryMinutes: numToStr(o.passwordResetTokenExpiryMinutes),
            };
        } catch (e) {
            console.error("[AuthPolicyApp] auth_policy_page_data parse failed", e);
            return empty;
        }
    },
};
