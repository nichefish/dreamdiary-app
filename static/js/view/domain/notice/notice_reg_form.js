/**
 * notice_reg_form.ts
 *
 * @author nichefish
 */
const Page = (function () {
    return {
        /**
         * Page 객체 초기화
         */
        init: function () {
            /* initialize form. */
            Notice.initForm();
            const isReg = $("#noticeRegForm").data("mode") === "regist";
            if (isReg) {
                $("#jandiYn").click();
            }
            else {
                /* 글 단락 init */
                Sectn.init({
                    refreshFunc: function () {
                        setTimeout(function () {
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
        refreshFunc: function () {
            const refPostNo = $("#noticeRegForm [name='postNo']").val();
            const refContentType = $("#noticeRegForm [name='contentType']").val();
            Sectn.listAjax({ refPostNo, refContentType });
            $("#sectn_reg_modal").modal("hide");
            Sectn.initDraggable({
                refreshFunc: Page.refreshFunc
            });
        }
    };
})();
document.addEventListener("DOMContentLoaded", function () {
    Page.init();
});
