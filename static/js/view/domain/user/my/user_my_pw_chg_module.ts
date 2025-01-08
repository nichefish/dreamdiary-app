/**
 * user_my_pw_chg_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.UserMyPwChg = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'UserMyPwChg' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#myPwChgForm", dF.UserMyPwChg.submitHandler, {
                rules: {
                    newPw: { regex: cF.regex.pw },
                    newPwCf: { equalTo: "#newPw" }
                },
                messages: {
                    newPw: { regex: "변경할 비밀번호가 형식에 맞지 않습니다." },
                    newPwCf: { equalTo: "변경할 비밀번호에 입력한 값과 동일하게 입력해야 합니다." }
                },
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.chgPw"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                dF.UserMyPwChg.myPwChgAjax();
            });
        },

        /**
         * 내 비밀번호 수정 팝업 호출
         */
        myPwChgModal: function(): void {
            const data: Record<string, any> = { "errorMsg": Model.errorMsg };
            cF.handlebars.template(data, "user_my_pw_chg", "show");
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#myPwChgForm").submit();
        },

        /**
         * 내 비밀번호 수정 (Ajax)
         */
        myPwChgAjax: function(): void {
            const url: string = Url.USER_MY_PW_CHG_AJAX;
            const ajaxData: Record<string, any> = { "userId": AuthInfo.userId, "currPw" : $("#currPw").val(), "newPw" : $("#newPw").val() };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            });
        },
    }
})();