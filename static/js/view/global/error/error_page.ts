/**
 * error_page.ts
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
            // do nothing
        },

        /**
         * 메인으로 돌아가기
         */
        main: function(): void {
            const url: string = Url.MAIN;
            cF.util.blockUIReplace(url);
        }
    }
})();