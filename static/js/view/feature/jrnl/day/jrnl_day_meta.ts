/**
 * jrnl_day_cal.ts
 * 저널 달력 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        calendar: null,
        calDt: null,

        init: function(): void {
            /* initialize modules. */
            dF.JrnlDay.init('CAL');
            dF.JrnlDiary.init('CAL');
            dF.JrnlDream.init('CAL');
            dF.JrnlTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });
            dF.State.init();

            // 태그 조회
            dF.JrnlDayMeta.listAjax();

            dF.JrnlDayAside.init();
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
