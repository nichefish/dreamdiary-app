/**
 * jrnl_diary_search.ts
 * 저널 일기 검색 페이지 스크립트
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
            await dF.JrnlDiary.init();
            dF.JrnlDiarySearch.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });
            dF.State.init();
            // 목록 조회
            dF.JrnlDiarySearch.initKeyword();
            dF.JrnlDiarySearch.initTag();
            dF.JrnlDiarySearch.search();

            const input: HTMLElement = document.getElementById("keywordInput");
            input.addEventListener("keydown", (e: KeyboardEvent): void => {
                if (e.key === "Enter" && !e.isComposing) {
                    e.preventDefault();
                    dF.JrnlDiarySearch.addKeyword();
                }
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});