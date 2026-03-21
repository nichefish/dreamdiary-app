/**
 * board_post_reg_form.ts
 * 일반게시판 게시물 등록/수정 페이지 스크립트
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
            dF.BoardPost.init();
            /* initialize form. */
            dF.BoardPost.initForm();
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});