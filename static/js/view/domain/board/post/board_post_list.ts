/**
 * board_post_list.ts
 *
 * @author nichefish
 */
const Page = (function() {
    return {
        /**
         * Page 객체 초기화
         */
        init: function() {
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function() {
    Page.init();
});