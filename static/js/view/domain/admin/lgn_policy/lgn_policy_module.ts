/**
 * lgn_policy_module.ts
 *
 * @author nichefish
 */
const LgnPolicy: Module = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'LgnPolicy' module initialized.");
        },

        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj = {}): void {
            /* jquery validation */
            cF.validate.validateForm("#lgnPolicyForm", LgnPolicy.regAjax, {
                rules: {
                    lgnLockDy: { maxlength: 3 },
                    lgnTryLmt: { maxlength: 3 },
                    pwChgDy: { maxlength: 3 },
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
            $("#lgnPolicyForm").submit();
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
                const url: string = Url.LGN_POLICY_REG_AJAX;
                const ajaxData: Record<string, any> = $("#lgnPolicyForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReplace(Url.LGN_POLICY_FORM);
                        });
                }, "block");
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    LgnPolicy.init();
});