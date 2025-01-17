/**
 * jrnl_sumry_dtl.ts
 * 저널 결산 상세 페이지 스크립트
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