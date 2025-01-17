/**
 * jrnl_sumry_list.ts
 * 저널 결산 목록 페이지 스크립트
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
            dF.JrnlSumry.init();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});