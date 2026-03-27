/**
 * user_reqst_form.ts
 * 사용자 등록 페이지 스크립트
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
            dF.UserReqst.initForm();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});