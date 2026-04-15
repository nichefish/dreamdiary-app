/**
 * journal_sumry_dtl.ts
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
            dF.JournalSumry.init();
            dF.JournalSumryReview.init();

            const yy: string = cF.util.getPathVariableFromUrl(/\/sumry\/(\d{4})(?:\.do)?$/);
            if (yy) dF.JournalSumry.dtlAjax(yy);
            const section: string = cF.util.getUrlParam("section");
            switch (section) {
                case "DIARY":
                    dF.JournalSumry.getSumryDiaryListAjax(yy);
                    dF.JournalSumry.getTagListAjax(yy, "DAY");
                    dF.JournalSumry.getTagListAjax(yy, "DIARY");
                    break;
                case "DREAM":
                    dF.JournalSumry.getMySumryDreamListAjax(yy);
                    dF.JournalSumry.getTagListAjax(yy, "DREAM");
                    break;
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
