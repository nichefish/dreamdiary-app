/**
 * notice_reg_form.ts
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
            /* initialize form. */
            Notice.initForm();

            const isReg = $("#noticeRegForm").data("mode") === "regist";
            if (isReg) {
                $("#jandiYn").click();
            } else {
                /* 글 단락 init */
                Sectn.init({
                    refreshFunc: function(): void {
                        setTimeout(function(): void {
                            Page.refreshFunc();
                        });
                    }
                });
                /* 글 단락 정렬순서 변경 init */
                Sectn.initDraggable({
                    refreshFunc: Page.refreshFunc
                });
            }
        },

        /**
         * 새로고침 함수 :: 페이지별 특성에 따라 별도로 세팅
         */
        refreshFunc: function(): void {
            const refPostNo = $("#noticeRegForm [name='postNo']").val();
            const refContentType = $("#noticeRegForm [name='contentType']").val();
            Sectn.listAjax({ refPostNo, refContentType });
            $("#sectn_reg_modal").modal("hide");
            Sectn.initDraggable({
                refreshFunc: Page.refreshFunc
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});