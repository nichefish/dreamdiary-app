/**
 * code_group_list.ts
 * code_group list page script
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        init: function(): void {
            dF.CodeGroup.init();
            dF.CodeGroup.renderListFromPageData();
            cF.table.initSort();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
