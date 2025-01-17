/**
 * jrnl_sbjct_dtl.ts
 * 저널 주제 상세 페이지 스크립트
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
            dF.JrnlSbjct.init();
        },
    };
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});