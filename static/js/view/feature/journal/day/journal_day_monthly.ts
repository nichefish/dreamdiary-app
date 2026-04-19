/**
 * journal_day_page.ts
 * 저널 일자 페이지 스크립트
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
            dF.JournalDay.init('LIST');
            dF.JournalDiary.init('LIST');
            void dF.JournalNote.init("LIST");
            dF.JournalDream.init('LIST');
            dF.JournalTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.yyMnthListAjax
            });
            dF.State.init();

            dF.JournalDayAside.init();

            // 목록 조회
            dF.JournalDay.yyMnthListAjax();

            // 일기/꿈 키워드 검색창에 엔터키 처리
            cF.util.enterKey("#diarySearchKeyword", dF.JournalDiary.searchPopup);
            cF.util.enterKey("#dreamSearchKeyword", dF.JournalDream.searchPopup);
        },

        /**
         * VIEW 변경
         *
         * @param {string} url
         */
        changeView: function(url: string): void {
            cF.ui.blockUIReplace(dF.JournalDay.buildViewUrl(url));
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
