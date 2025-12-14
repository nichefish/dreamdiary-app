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

            // 목록 조회
            const stdrdDt: string = window.JRNL?.stdrdDt;
            dF.JrnlDay.getStdrdData(stdrdDt);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});