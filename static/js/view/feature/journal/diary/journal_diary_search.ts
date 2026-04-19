/**
 * journal_diary_search.ts
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
            await dF.JournalDiary.init("SEARCH");
            await dF.JournalNote.init("SEARCH");
            dF.JournalDiarySearch.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.yyMnthListAjax
            });
            dF.State.init();
            // 목록 조회
            dF.JournalDiarySearch.initKeyword();
            dF.JournalDiarySearch.initTag();
            dF.JournalDiarySearch.search();

            const input: HTMLElement = document.getElementById("keywordInput");
            input.addEventListener("keydown", (e: KeyboardEvent): void => {
                if (e.key === "Enter" && !e.isComposing) {
                    e.preventDefault();
                    dF.JournalDiarySearch.addKeyword();
                }
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
