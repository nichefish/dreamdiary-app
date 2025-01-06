/**
 * lgn_form.ts
 * 로그인 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize form. */
            Page.initForm();

            /* 에러 메세지 존재시 표시 */
            if (Model.errorMsg) Page.showErrorMsg();
            /* 중복 로그인시 기존 로그인 끊기 팝업 호출 */
            if (Model.isDupIdLgn) Page.dupLgnAlert();
            /* 조건 일치시 비밀번호 변경 팝업 호출 */
            if (Model.isCredentialExpired || Model.needsPwReset) dF.LgnPwChg.pwChgModal();

            console.log("Page scripts initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#lgnForm", function(): void {
                cF.util.blockUISubmit("#lgnForm", Url.AUTH_LGN_PROC);
            });
            // 엔터키 처리
            cF.util.enterKey("#userId, #password", Page.lgn);
        },

        /**
         * request에 추가된 에러 메세지 존재시 표시
         */
        showErrorMsg: function(): void {
            const errorMsg: string = Model.errorMsg;
            if (cF.util.isEmpty(errorMsg)) return;

            const $errorMsgSpan: JQuery<HTMLElement> = $("#errorMsgSpan");
            $errorMsgSpan.html("");
            $("#userId_validate_span").text("");
            $("#password_validate_span").text("");
            $errorMsgSpan.html(errorMsg.replace(/&lt;br&gt;/g, '<br/>'));   // 줄바꿈 처리
        },

        /**
         * 기존 로그인 끊기 팝업 호출
         */
        dupLgnAlert: function(): void {
            Swal.fire({
                title: Message.get("view.rslt.dupLgn"),
                text: Message.get("view.cnfm.dupLgn"),
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "로그인",
                cancelButtonText: "취소",
        }).then(function(result: SwalResult): void {
                if (result.value) {
                    // 중복ID 로그인
                    $("#userId").val(Model.userId);
                    $("#password").attr("disabled", "disabled");
                    Page.lgn();
                } else {
                    // 로그인하지 않음. 중복ID 세션 attribute 만료시킴
                    const url: string = Url.AUTH_EXPIRE_SESSION_AJAX;
                    cF.$ajax.post(url, null, function(): void {
                        cF.util.blockUIReplace(Url.AUTH_LGN_FORM);
                    });
                }
            });
        },

        /**
         * 로그인 처리
         */
        lgn: function(): void {
            $("#lgnForm").submit();
        },

        /**
         * 신규계정 신청 페이지로 이동
         */
        reqstUser: function(): void {
            cF.util.blockUIReplace(Url.USER_REQST_REG_FORM);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
