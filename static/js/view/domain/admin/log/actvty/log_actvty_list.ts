/**
 * log_actvty_list.ts
 * 활동 로그 목록 페이지 스크립트
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
         * 사용자별 로그 페이지로 이동
         */
        logStatsUserList: function(): void {
            const url: string = Url.LOG_STATS_USER_LIST;
            cF.form.blockUISubmit("#listForm", url);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});