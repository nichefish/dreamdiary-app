/**
 * jrnl_sumry_dtl.ts
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
            /* 글 단락 init */
            dF.Sectn.init();
            dF.Sectn.initDraggable();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});