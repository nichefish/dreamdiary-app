/**
 * journal_annual_dtl.ts
 * 저널 결산 상세 페이지 스크립트
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
            dF.JournalAnnual.init();
            dF.JournalAnnualReview.init();

            const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
            if (yy) dF.JournalAnnual.dtlAjax(yy);
            const section: string = cF.util.getUrlParam("section");
            switch (section) {
                case "DIARY":
                    dF.JournalAnnual.getAnnualDiaryListAjax(yy);
                    dF.JournalAnnual.getTagListAjax(yy, "DAY");
                    dF.JournalAnnual.getTagListAjax(yy, "DIARY");
                    break;
                case "DREAM":
                    dF.JournalAnnual.getMyAnnualDreamListAjax(yy);
                    dF.JournalAnnual.getTagListAjax(yy, "DREAM");
                    break;
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
