/**
 * jrnl_day_page.ts
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
            dF.JrnlDay.init('LIST');
            dF.JrnlDiary.init('LIST');
            dF.JrnlDream.init('LIST');
            dF.JrnlTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });
            dF.State.init();

            dF.JrnlDayAside.init();
            // 목록 조회
            dF.JrnlDay.yyMnthListAjax();

            // 일기/꿈 키워드 검색에 엔터키 처리
            cF.util.enterKey("#diaryKeyword", dF.JrnlDiary.searchPopup);
            cF.util.enterKey("#dreamKeyword", dF.JrnlDream.searchPopup);
        },

        /**
         * VIEW 변경
         *
         * @param {string} url
         */
        changeView: function(url: string): void {
            cF.ui.blockUIReplace(url);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});