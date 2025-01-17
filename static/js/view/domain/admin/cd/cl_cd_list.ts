/**
 * cl_cd_list.ts
 * 분류 코드 목록 페이지 스크립트
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

            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();
            /* init : Draggable */
            dF.ClCd.initDraggable();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});