/**
 * comment_modal_module.ts
 *
 * @author nichefish
 */
// @ts-ignore
if (typeof Comment === 'undefined') { var Comment = {} as any; }
Comment.modal = (function(): Module {
    return {
        /**
         * Comment.modal 객체 초기화
         * @param {Object} options - 초기화 옵션 객체.
         * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
         */
        init: function({ refreshFunc }: { refreshFunc?: Function } = {}): void {
            console.log("'Comment.modal' module initialized.");

            if (refreshFunc !== undefined) Comment.modal.refreshFunc = refreshFunc;
        },

        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj = {}): void {
            /* show modal */
            cF.handlebars.template(obj, "comment_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#commentRegForm", Comment.modal.regAjax);
        },

        /**
         * 등록 모달 호출
         * @param {string|number} refPostNo - 참조할 게시물 번호.
         * @param {string} refContentType - 참조할 콘텐츠 타입.
         */
        regModal: function(refPostNo: number|string, refContentType: string): void {
            if (isNaN(Number(refPostNo)) || !refContentType) return;

            const obj = { "refPostNo": refPostNo, "refContentType": refContentType };
            /* initialize form. */
            Comment.modal.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#commentRegForm").submit();
        },

        /**
         * 댓글 입력(등록/수정) 처리 (Ajax)
         */
        regAjax: function(): void {
            const isReg: boolean = $("#commentRegForm #postNo").val() === "";
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.COMMENT_REG_AJAX : Url.COMMENT_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("commentRegForm") as HTMLFormElement);
                cF.ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (Comment.modal.refreshFunc !== undefined) {
                                Comment.modal.refreshFunc();
                            } else {
                                cF.util.blockUIReload();
                            }
                        });
                });
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} postNo - 댓글 번호.
         */
        mdfModal: function(postNo: string | number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.COMMENT_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                Comment.modal.initForm(rsltObj);
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
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (Comment.modal.refreshFunc !== undefined) {
                                Comment.modal.refreshFunc();
                            } else {
                                cF.util.blockUIReload();
                            }
                        });
                });
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Comment.modal.init();
});