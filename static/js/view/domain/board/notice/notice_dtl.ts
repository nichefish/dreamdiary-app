/**
 * notice_dtl.ts
 * 공지사항 상세 페이지 스크립트
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
            dF.Notice.init();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});