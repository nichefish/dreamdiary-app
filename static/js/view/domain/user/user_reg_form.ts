/**
 * user_reg_form.ts
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
            /* initialize modules. */
            dF.User.init();
            dF.UserEmplym.init();
            dF.UserProfl.init();
            /* initialize form. */
            dF.User.initForm();

            if (!dF.User.isMdf) {
                // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
                $("#userId").on("keyup", function(): void {
                    $("#userId_validate_span").empty();
                    $("#ipDupChckPassed").val("N");
                    $("#idDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
                }).on("keydown", function(): void {
                    $("#userId_validate_span").text("");
                    $("#ipDupChckPassed").val("N");
                    $("#idDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
                });
            }

            // 프로필 정보 / 인사정보 창 활성화
            const hasProfl = $("#userProflBtn").data("profl");
            if (hasProfl) dF.UserProfl.enableUserProfl();
            const hasEmplym = $("#userEmplymBtn").data("emplym");
            if (hasEmplym) dF.UserEmplym.enableUserEmplym();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});