/**
 * jrnl_dream_search.ts
 * 저널 꿈 검색 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         * @return Promise<void>
         */
         init: async function(): Promise<void> {
            /* initialize modules. */
            await dF.JrnlDream.init();
            dF.JrnlDreamSearch.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });
            dF.State.init();
            // 목록 조회
            dF.JrnlDreamSearch.initKeyword();
            dF.JrnlDreamSearch.initTag();
            dF.JrnlDreamSearch.search();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});