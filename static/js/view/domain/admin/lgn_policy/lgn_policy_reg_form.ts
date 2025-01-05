/**
 * lgn_policy_reg_form.ts
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
            dF.LgnPolicy.initForm();

            console.log("Page scripts initialized.");
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});