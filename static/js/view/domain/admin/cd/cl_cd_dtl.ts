/**
 * cl_cd_dtl.ts
 * 분류 코드 상세 페이지 스크립트
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
            dF.ClCd.init();
            dF.DtlCd.init();

            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.table.initSort();
            /* init : Draggable */
            dF.DtlCd.initDraggable();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});