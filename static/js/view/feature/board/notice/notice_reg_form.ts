/**
 * notice_reg_form.ts
 * 공지사항 등록/수정 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        isReg: $("#noticeRegForm").data("mode") === "regist",

        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize modules. */
            dF.Notice.init();
            /* initialize form. */
            dF.Notice.initForm();

            if (Page.isReg) {
                $("#jandiYn").click();
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
