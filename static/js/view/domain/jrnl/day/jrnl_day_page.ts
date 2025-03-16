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
            dF.JrnlDay.init();
            dF.JrnlDiary.init();
            dF.JrnlDream.init();
            dF.JrnlTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });

            dF.JrnlDayAside.init();
            // 목록 조회
            dF.JrnlDay.yyMnthListAjax();
            // 태그 조회
            dF.JrnlDayTag.listAjax();
            dF.JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            // 일기/꿈 키워드 검색에 엔터키 처리
            cF.util.enterKey("#diaryKeyword", dF.JrnlDiary.keywordListAjax);
            cF.util.enterKey("#dreamKeyword", dF.JrnlDream.keywordListAjax);
        },

        /**
         * 목록 화면으로 이동
         */
        listPage: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_DAY_PAGE);
        },

        /**
         * 달력 화면으로 이동
         */
        calPage: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_DAY_CAL);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});