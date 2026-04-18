/**
 * journal_sbjct_reg_form.ts
 * 저널 주제 등록/수정 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        isReg: $("#journalSbjctRegForm").data("mode") === "regist",
        isMdf: $("#journalSbjctRegForm").data("mode") === "modify",

        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize modules. */
            dF.JournalSbjct.init();
            /* initialize form. */
            dF.JournalSbjct.initForm();

            if (!Page.isMdf) {
                $("#jandiYn").click();
            }
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
