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
            dF.JrnlDay.init('DAILY');
            dF.JrnlDiary.init();
            dF.JrnlDream.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.getStdrdData
            });

            // datepicker
            const stdrdDt: string = window.JRNL?.stdrdDt;
            const pattern: string = cF.date.ptnDate.toUpperCase();
            // @ts-ignore
            cF.datepicker.singleDatePicker("#stdrdDt", pattern, stdrdDt, function(date: monent): void {
                const dateStr: string = date.format(pattern);
                history.pushState(null, '', cF.util.bindUrl(Url.JRNL_DAY_VIEW, { stdrdDt: dateStr }));
                dF.JrnlDay.getStdrdData(dateStr);
            });
            // 데이터 조회
            dF.JrnlDay.getStdrdData(stdrdDt);
        },

        changeStdrdDt: function(): void {

        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});