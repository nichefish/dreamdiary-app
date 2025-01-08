/**
 * log_actvty_list.ts
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
            cF.util.initSortTable();
        },

        /**
         * 사용자별 로그 페이지로 이동
         */
        logStatsUserList: function(): void {
            const url: string = Url.LOG_STATS_USER_LIST;
            cF.util.blockUISubmit("#listForm", url);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});