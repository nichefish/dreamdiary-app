/**
 * auth_policy_module.ts
 * 인증 정책 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.AuthPolicy = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.AuthPolicy.initialized) return;

            dF.AuthPolicy.initialized = true;
            console.log("'dF.AuthPolicy' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#authPolicyForm", dF.AuthPolicy.regAjax, {
                rules: {
                    inactiveLockDays: { maxlength: 3 },
                    loginAttemptLimit: { maxlength: 3 },
                    loginAttemptWindowMinutes: { maxlength: 3 },
                    accountLockDurationMinutes: { maxlength: 4 },
                    passwordChangeCycleDays: { maxlength: 3 },
                    passwordResetTokenExpiryMinutes: { maxlength: 5 },
                    pwForReset: { minlength: 8, maxlength: 20, regex: cF.regex.pw },
                },
                messages: {
                    pwForReset: { regex: "비밀번호가 형식에 맞지 않습니다." },
                },
            });
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#authPolicyForm").submit();
        },

        /**
         * 등록/수정 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.AUTH_POLICY_REG_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#authPolicyForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReplace(Url.AUTH_POLICY_FORM);
                        });
                }, "block");
            });
        }
    }
})();
