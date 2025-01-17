/**
 * error_page.ts
 * 에러 페이지 스크립트
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
            //
        },

        /**
         * 메인으로 돌아가기
         */
        main: function(): void {
            const url: string = Url.MAIN;
            cF.ui.blockUIReplace(url);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});