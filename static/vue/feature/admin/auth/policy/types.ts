/**
 * 인증 정책 Vue 화면 — 폼 상태 (입력은 문자열로 다룬다)
 */
export type AuthPolicyFormFields = {
    id: string;
    inactiveLockDays: string;
    loginAttemptLimit: string;
    loginAttemptWindowMinutes: string;
    accountLockDurationMinutes: string;
    passwordChangeCycleDays: string;
    passwordResetTokenExpiryMinutes: string;
};

export type AuthPolicyPageState = {
    form: AuthPolicyFormFields;
};
