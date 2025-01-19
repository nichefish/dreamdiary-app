/**
 * comment_page_module.ts
 * 댓글 페이지 스크립트 모듈
 *
 * @author nichefish
 */
// @ts-ignore
if (typeof dF === 'undefined') { var dF = {} as any; }
if (typeof dF.Comment === 'undefined') { dF.Comment = {} as any; }
dF.Comment.page = (function(): dfModule {
    return {
        initialized: false,

        /**
         * Comments.page 객체 초기화
         */
        init: function(): void {
            if (dF.Comment.page.initialized) return;

            /* initialize form. */
            dF.Comment.page.initForm();

            dF.Comment.page.initialized = true;
            console.log("'dF.Comment.modal' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show reg area */
            cF.handlebars.template(obj, "comment_page_reg");

            /* jquery validation */
            cF.validate.validateForm("#commentPageRegForm", dF.Comment.page.regAjax);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#commentPageRegForm").submit();
        },

        /**
         * 댓글 입력 처리 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.COMMENT_REG_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("commentPageRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                });
            });
        },

        /**
         * 댓글 수정 폼 생성
         * @param {string|number} postNo - 댓글 번호.
         */
        mdfForm: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#commentDtlSpan" + postNo).hide();
            const str: string = $("#commentMdfTemplate").html().replace(/__INDEX__/g, String(postNo));
            $("#commentSpan" + postNo).append(str);
            $("#commentMdfCn" + postNo).html($("#commentCnSpanDiv" + postNo).html());
            $("#showMdfBtnDiv" + postNo).hide();
            $("#mdfSaveBtnDiv" + postNo).show();
            cF.validate.validateForm("#commentPageMdfForm", dF.Comment.page.mdfAjax);
        },

        /**
         * 댓글 수정 폼 닫기
         * @param {string|number} postNo - 댓글 번호.
         */
        closeMdfForm: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#commentDtlSpan" + postNo).show();
            $("#commentSpan" + postNo).empty();
            $("#commentMdfCn" + postNo).empty();
            $("#showMdfBtnDiv" + postNo).show();
            $("#mdfSaveBtnDiv" + postNo).hide();
        },

        /**
         * 댓글 수정 (Ajax)
         * @param {string|number} postNo - 댓글 번호.
         */
        mdfAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            if (cF.util.isEmpty($("#commentMdfCn" + postNo), "value")) {
                Swal.fire("댓글 내용을 입력해주세요.");
                return;
            }
            Swal.fire({
                text: Message.get("view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.COMMENT_MDF_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#commentPageMdfForm" + postNo);
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                });
            });
        },

        /**
         * 댓글 삭제 (Ajax)
         * @param {string|number} postNo - 댓글 번호.
         */
        delAjax: function(postNo: string | number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const url: string = Url.COMMENT_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo, "actvtyCtgrCd": "${actvtyCtgrCd!}" };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                });
            });
        }
    } as any;
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.Comment.page.init();
});