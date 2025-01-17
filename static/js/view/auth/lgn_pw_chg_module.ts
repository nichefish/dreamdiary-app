/**
 * lgn_pw_chg_module.ts
 * 로그인 비밀번호 변경 스크립트 모듈
 *
 * @namespace: dF.LgnPwChg (노출식 모듈 패턴)
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.LgnPwChg = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.LgnPwChg.initialized) return;

            dF.LgnPwChg.initialized = true;
            console.log("'dF.LgnPwChg' module initialized.");
        },

        /**
         * 비밀번호 변경 팝업 호출
         */
        pwChgModal: function(): void {
            const data: Record<string, any> = { "userId": Model.userId, "errorMsg": Model.errorMsg };
            /* initialize form. */
            dF.LgnPwChg.initForm(data);
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "lgn_pw_chg");

            // 엔터키 처리
            cF.util.enterKey("#currPw, #newPw, #newPwCf", dF.LgnPwChg.submit);

            /* jquery validation */
            cF.validate.validateForm("#lgnPwChgForm", dF.LgnPwChg.lgnPwChgAjax, {
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
         * form submit
         */
        submit: function(): void {
            $("#lgnPwChgForm").submit();
        },

        /**
         * 비밀번호 수정 (Ajax)
         */
        lgnPwChgAjax: function(): void {
            const url: string = Url.AUTH_LGN_PW_CHG_AJAX;

            // 순수 JavaScript로 DOM 요소 접근
            const userIdElement: HTMLInputElement = document.getElementById("lgnUserId") as HTMLInputElement;
            const currPwElement: HTMLInputElement = document.getElementById("currPw") as HTMLInputElement;
            const newPwElement: HTMLInputElement = document.getElementById("newPw") as HTMLInputElement;
            const userId: string = userIdElement?.value || '';
            const currPw: string = currPwElement?.value || '';
            const newPw: string = newPwElement?.value || '';
            const ajaxData: Record<string, any> = { "userId" : userId, "currPw" : currPw, "newPw" : newPw };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({
                    text: res.message
                }).then(function(): void {
                    ($("#lgnPwChgForm")[0] as HTMLFormElement).reset();
                    if (res.rslt) cF.util.blockUIReplace(Url.AUTH_LGN_FORM);
                });
            });
        },
    };
})();