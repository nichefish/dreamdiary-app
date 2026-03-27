/**
 * notice_reg_form.ts
 * 공지사항 등록/수정 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        isReg: $("#noticeRegForm").data("mode") === "regist",

        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize modules. */
            dF.Notice.init();
            /* initialize form. */
            dF.Notice.initForm();

            if (Page.isReg) {
                $("#jandiYn").click();
            } else {
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
            }
        },

        /**
         * 새로고침 함수 :: 페이지별 특성에 따라 별도로 세팅
         */
        refreshFunc: function(): void {
            const refPostNo = $("#noticeRegForm [name='postNo']").val();
            const refContentType = $("#noticeRegForm [name='contentType']").val();
            dF.Sectn.listAjax({ refPostNo, refContentType });
            $("#sectn_reg_modal").modal("hide");
            dF.Sectn.initDraggable({
                refreshFunc: Page.refreshFunc
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});