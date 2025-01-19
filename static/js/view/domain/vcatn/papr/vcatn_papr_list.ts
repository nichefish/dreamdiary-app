/**
 * vcatn_papr_list.ts
 * 휴가계획서 목록 페이지 스크립트
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
            dF.VcatnPapr.init();
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.table.initSort();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});