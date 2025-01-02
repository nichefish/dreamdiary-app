/**
 * log_sys_list.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         */
        init: function() {
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            const url: string = `${Url.LOG_SYS_LIST!}?actionTyCd=SEARCH`;
            cF.util.blockUISubmit("#listForm", url);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});