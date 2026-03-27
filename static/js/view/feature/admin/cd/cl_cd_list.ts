/**
 * cl_cd_list.ts
 * CL_CD list page script
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page initialize
         */
        init: function(): void {
            dF.ClCd.init();
            dF.ClCd.renderListFromPageData();

            cF.table.initSort();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
