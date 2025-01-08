/**
 * jrnl_sbjct_dtl.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Module = (function(): Module {
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