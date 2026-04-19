/**
 * log_list.ts
 * 로그 목록 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        init: function(): void {
            dF.Log.init();
            cF.table.initSort();
        },

        logStatsUserList: function(): void {
            const url: string = Url.LOG_STATS_USER_LIST;
            cF.form.blockUISubmit("#listForm", url);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
