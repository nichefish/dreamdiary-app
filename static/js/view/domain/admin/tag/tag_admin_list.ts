/**
 * tag_admin_list.ts
 * 태그 관리 페이지 스크립트
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
            dF.TagAdmin.init();
            // 태그 조회
            const tagCtgrDiv: HTMLInputElement = document.querySelector("#jrnl_tag_ctgr_div");
            const refContentType: string = tagCtgrDiv.value;

            if (cF.util.isNotEmpty(refContentType)) dF.TagAdmin.tagListAjax(refContentType);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});