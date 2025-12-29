/**
 * jrnl_sumry_dtl.ts
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
            dF.JrnlSumry.init();

            const yy: string = cF.util.getPathVariableFromUrl(/\/sumry\/(\d{4})(?:\.do)?$/);
            if (yy) dF.JrnlSumry.dtlAjax(yy);
            const section: string = cF.util.getUrlParam("section");
            switch (section) {
                case "DIARY":
                    dF.JrnlSumry.getImprtcDiaryListAjax(yy);
                    dF.JrnlSumry.getTagListAjax(yy, "DAY");
                    dF.JrnlSumry.getTagListAjax(yy, "DIARY");
                    break;
                case "DREAM":
                    dF.JrnlSumry.getImprtcDreamListAjax(yy);
                    dF.JrnlSumry.getTagListAjax(yy, "DREAM");
                    break;
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});