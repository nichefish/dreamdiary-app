import { AuthPolicyFormFields } from "../types.js";

/**
 * API 본문은 정수 필드(서버 `AuthPolicyDto` / Bean validation)에 맞춘다.
 * 변경 전/후: 기존 jquery `getJsonFormData` + `PUT` 과 동일한 키·의미.
 */
function parsePositiveIntField(raw: string, fieldName: string): number | null {
    const t = String(raw).trim();
    if (t === "") {
        console.warn("[AuthPolicyApp] required field empty: " + fieldName);
        return null;
    }
    const v = parseInt(t, 10);
    if (Number.isNaN(v)) {
        console.warn("[AuthPolicyApp] invalid number: " + fieldName);
        return null;
    }
    return v;
}

export function buildPutPayload(form: AuthPolicyFormFields): Record<string, unknown> | null {
    const idRaw = String(form.id).trim();
    /** 싱글톤 행: 클라이언트에 id 가 비면 서버 PK 1 로 간주(초기화 시드와 동일). */
    let id: number | null = idRaw === "" ? null : parseInt(idRaw, 10);
    if (id !== null && Number.isNaN(id)) id = null;
    const resolvedId = id !== null ? id : 1;
    const inactiveLockDays = parsePositiveIntField(form.inactiveLockDays, "inactiveLockDays");
    const loginAttemptLimit = parsePositiveIntField(form.loginAttemptLimit, "loginAttemptLimit");
    const loginAttemptWindowMinutes = parsePositiveIntField(form.loginAttemptWindowMinutes, "loginAttemptWindowMinutes");
    const accountLockDurationMinutes = parsePositiveIntField(form.accountLockDurationMinutes, "accountLockDurationMinutes");
    const passwordChangeCycleDays = parsePositiveIntField(form.passwordChangeCycleDays, "passwordChangeCycleDays");
    const passwordResetTokenExpiryMinutes = parsePositiveIntField(form.passwordResetTokenExpiryMinutes, "passwordResetTokenExpiryMinutes");

    if (
        inactiveLockDays === null ||
        loginAttemptLimit === null ||
        loginAttemptWindowMinutes === null ||
        accountLockDurationMinutes === null ||
        passwordChangeCycleDays === null ||
        passwordResetTokenExpiryMinutes === null
    ) {
        return null;
    }

    return {
        id: resolvedId,
        inactiveLockDays,
        loginAttemptLimit,
        loginAttemptWindowMinutes,
        accountLockDurationMinutes,
        passwordChangeCycleDays,
        passwordResetTokenExpiryMinutes,
    };
}

export type AuthPolicyActions = {
    save: () => void;
};

export default function createAuthPolicyActions(deps: {
    getForm: () => AuthPolicyFormFields;
    t: (key: string) => string;
}): AuthPolicyActions {
    return {
        save(): void {
            Swal.fire({
                text: deps.t("view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const payload = buildPutPayload(deps.getForm());
                if (payload === null) {
                    Swal.fire({ text: deps.t("txt.admin.auth.policy.error.invalid-input") });
                    return;
                }

                const url: string = Url.AUTH_POLICY;
                cF.$ajax.put(url, payload, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (res.rslt) cF.ui.blockUIReplace(Url.AUTH_POLICY_PAGE);
                    });
                }, "block");
            });
        },
    };
}
