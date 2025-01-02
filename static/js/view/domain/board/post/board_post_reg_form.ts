/**
 * board_post_reg_form.ts
 *
 * @author nichefish
 */
const Page = (function() {
    return {
        /**
         * Page 객체 초기화
         */
        init: function() {
            /* initialize form. */
            BoardPost.initForm();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function() {
    Page.init();
});