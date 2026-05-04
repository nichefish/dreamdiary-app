/**
 * code_group_list.ts
 * code_group list page script
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * 목록 초기화는 {@code static/vue/admin/code/CodeAdminApp.ts} 모듈의 DOMContentLoaded에서 수행한다.
         * (Vue 미포함 빌드·다른 페이지에서 Page 참조 시 테이블 정렬 등을 위해 객체는 유지)
         */
        init: function(): void {
            cF.table.initSort();
        },
    }
})();
