/**
 * tag_admin_list.ts
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
            // 태그 조회
            const tagCtgrDiv: HTMLInputElement = document.querySelector("#jrnl_tag_ctgr_div");
            const refContentType: string = tagCtgrDiv.value;

            if (cF.util.isNotEmpty(refContentType)) dF.TagAdmin.tagListAjax(refContentType);

            console.log("Page scripts initialized.");
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});