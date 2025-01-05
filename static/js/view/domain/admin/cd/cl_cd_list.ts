/**
 * cl_cd_list.ts
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
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();
            /* init : Draggable */
            ClCd.initDraggable();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});