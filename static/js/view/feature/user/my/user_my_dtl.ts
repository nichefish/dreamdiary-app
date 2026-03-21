/**
 * user_my_dtl.ts
 * 사용자 마이페이지 스크립트
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
            dF.UserMy.init();
            dF.UserMyPwChg.init();
            /* initialize form. */
            dF.UserMyPwChg.initForm();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});