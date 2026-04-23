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
            dF.JournalDay.init('DAILY');
            void dF.JournalEntry.initAll("DAILY");
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.getStdrdData
            });
            dF.State.init();

            // datepicker
            const stdrdDt: string = window.JOURNAL?.stdrdDt;
            const pattern: string = cF.date.ptnDate.toUpperCase();
            // @ts-ignore
            cF.datepicker.singleDatePicker("#stdrdDt", pattern, stdrdDt, function(date: monent): void {
                const dateStr: string = date.format(pattern);
                history.pushState(null, '', cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt: dateStr }));
                dF.JournalDay.getStdrdData(dateStr);
            });

            // 데이터 조회
            dF.JournalDay.getStdrdData(stdrdDt);
        },

        changeStdrdDt: function(): void {

        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
