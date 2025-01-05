/**
 * jrnl_sbjct_reg_form.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Module = (function(): Module {
    return {
        isMdf: $("#boardPostRegForm").data("mode") === "modify",

        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize form. */
            dF.JrnlSbjct.initForm();

            if (Page.isMdf) {
                /* 글 단락 init */
                dF.Sectn.init({
                    refreshFunc: function(): void {
                        setTimeout(function(): void {
                            Page.refreshFunc();
                        });
                    }
                });
                /* 글 단락 정렬순서 변경 init */
                dF.Sectn.initDraggable({
                    refreshFunc: Page.refreshFunc
                });
            } else {
                $("#jandiYn").click();
            }
        },

        refreshFunc: function({ refPostNo, refContentType }): void {
            dF.Sectn.listAjax({ "refPostNo": refPostNo, "refContentType": refContentType });
            $("#sectn_reg_modal").modal("hide");
            dF.Sectn.initDraggable({
                refreshFunc: Page.refreshFunc
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});