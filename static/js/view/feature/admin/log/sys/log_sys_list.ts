/**
 * log_sys_list.ts
 * 시스템 로그 목록 페이지 스크립트
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
            dF.LogActvty.init();
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.table.initSort();
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            const url: string = `${Url.LOG_SYS_LIST!}?actionTyCd=SEARCH`;
            cF.form.blockUISubmit("#listForm", url);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});