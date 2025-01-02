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
        init: function() {
            // 태그 조회
            const refContentType: string = "${refContentType!}";
            if (refContentType !== "") TagAdmin.tagListAjax(refContentType);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function() {
    Page.init();
});