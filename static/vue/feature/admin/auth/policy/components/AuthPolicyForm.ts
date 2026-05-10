import { AuthPolicyFormFields } from "../types.js";

/**
 * 인증 정책 입력 행 묶음 (card-body).
 * 부모의 반응형 `form` 객체 참조를 그대로 받아 필드를 바인딩한다.
 */
export default {
    name: "AuthPolicyForm",
    props: {
        form: { type: Object, required: true },
    },
    computed: {
        f(): AuthPolicyFormFields {
            return this.form as AuthPolicyFormFields;
        },
    },
    template: `
    <div class="card-body">
        <div class="row mb-4">
            <div class="col-xl-2 col-form-label fs-6 fw-bold">
                <label for="inactiveLockDays">{{ $t('txt.admin.auth.policy.form.inactive-lock-days') }}</label>
            </div>
            <div class="col-xl-1">
                <input type="text" id="inactiveLockDays" class="form-control form-control-solid text-end number"
                       v-model="f.inactiveLockDays" maxlength="3" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.days') }}</div>
            <div class="col-xl-8 text-noti">
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.inactive-lock-days.notice.ln1') }}</p>
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.inactive-lock-days.notice.ln2') }}</p>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 col-form-label fs-6 fw-bold">
                <label for="loginAttemptLimit">{{ $t('txt.admin.auth.policy.form.login-attempt-limit') }}</label>
            </div>
            <div class="col-xl-1">
                <input type="text" id="loginAttemptLimit" class="form-control form-control-solid text-end number"
                       v-model="f.loginAttemptLimit" maxlength="3" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.times') }}</div>
            <div class="col-xl-2">
                <input type="text" id="loginAttemptWindowMinutes" class="form-control form-control-solid text-end number"
                       v-model="f.loginAttemptWindowMinutes" maxlength="3" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.minutes') }}</div>
            <div class="col-xl-5 text-noti">
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.login-fail-row.notice.ln1') }}</p>
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.login-fail-row.notice.ln2') }}</p>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 col-form-label fs-6 fw-bold">
                <label for="passwordChangeCycleDays">{{ $t('txt.admin.auth.policy.form.password-change-cycle-days') }}</label>
            </div>
            <div class="col-xl-1">
                <input type="text" id="passwordChangeCycleDays" class="form-control form-control-solid text-end number"
                       v-model="f.passwordChangeCycleDays" maxlength="3" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.days') }}</div>
            <div class="col-xl-8 text-noti">
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.password-change-cycle-days.notice.ln1') }}</p>
                <p class="mb-0">{{ $t('txt.admin.auth.policy.form.password-change-cycle-days.notice.ln2') }}</p>
            </div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 col-form-label fs-6 fw-bold">
                <label for="accountLockDurationMinutes">{{ $t('txt.admin.auth.policy.form.account-lock-duration-minutes') }}</label>
            </div>
            <div class="col-xl-1">
                <input type="text" id="accountLockDurationMinutes" class="form-control form-control-solid text-end number"
                       v-model="f.accountLockDurationMinutes" maxlength="4" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.minutes') }}</div>
            <div class="col-xl-8 text-noti">{{ $t('txt.admin.auth.policy.form.account-lock-duration-minutes.notice') }}</div>
        </div>
        <div class="row mb-4">
            <div class="col-xl-2 col-form-label fs-6 fw-bold">
                <label for="passwordResetTokenExpiryMinutes">{{ $t('txt.admin.auth.policy.form.password-reset-token-expiry-minutes') }}</label>
            </div>
            <div class="col-xl-1">
                <input type="text" id="passwordResetTokenExpiryMinutes" class="form-control form-control-solid text-end number"
                       v-model="f.passwordResetTokenExpiryMinutes" maxlength="5" autocomplete="off" />
            </div>
            <div class="col-xl-1 col-form-label w-5">{{ $t('txt.admin.auth.policy.unit.minutes') }}</div>
            <div class="col-xl-8 text-noti">{{ $t('txt.admin.auth.policy.form.password-reset-token-expiry-minutes.notice') }}</div>
        </div>
    </div>
    `,
};
