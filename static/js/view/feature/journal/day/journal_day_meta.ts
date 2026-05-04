/**
 * journal_day_meta.ts
 * 저널 메타 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        init: function(): void {
            /* initialize modules. */
            dF.JournalDay.init('CAL');
            void dF.JournalEntry.initAll("CAL");
            dF.JournalTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.yyMnthListAjax
            });
            dF.State.init();

            // 메타 조회
            dF.JournalDayMeta.listAjax();

            dF.JournalDayAside.init();
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
