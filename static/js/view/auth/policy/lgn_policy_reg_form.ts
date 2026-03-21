/**
 * lgn_policy_reg_form.ts
 * 로그인 정책 등록/수정 페이지 스크립트
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
            dF.LgnPolicy.init();
            /* initialize form. */
            dF.LgnPolicy.initForm();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});