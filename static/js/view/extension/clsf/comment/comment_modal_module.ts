/**
 * comment_modal_module.ts
 * 댓글 모달 스크립트 모듈
 *
 * @author nichefish
 */
// @ts-ignore
if (typeof dF === 'undefined') { var dF = {} as any; }
if (typeof dF.Comment === 'undefined') { dF.Comment = {} as any; }
dF.Comment.modal = (function(): dfModule {
    return {
        initialized: false,

        /**
         * Comments.modal 객체 초기화
         * @param {Object} options - 초기화 옵션 객체.
         * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
         */
        init: function({ refreshFunc }: { refreshFunc?: Function } = {}): void {
            if (dF.Comment.modal.initialized) return;

            if (refreshFunc != null) dF.Comment.modal.refreshFunc = refreshFunc;

            dF.Comment.modal.initialized = true;
            console.log("'dF.Comment.modal' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "comment_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#commentRegForm", dF.Comment.modal.regAjax);
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_commentCn');
            cF.tinymce.setContentWhenReady("tinymce_commentCn", obj.cn || "");
        },

        /**
         * 등록 모달 호출
         * @param {string|number} refPostNo - 참조할 게시물 번호.
         * @param {string} refContentType - 참조할 콘텐츠 타입.
         */
        regModal: function(refPostNo: number|string, refContentType: string): void {
            if (isNaN(Number(refPostNo)) || !refContentType) return;

            const obj: Record<string, any> = { "refPostNo": refPostNo, "refContentType": refContentType };
            /* initialize form. */
            dF.Comment.modal.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce?.get("tinymce_commentCn")?.save();
            $("#commentRegForm").submit();
        },

        /**
         * 댓글 입력(등록/수정) 처리 (Ajax)
         */
        regAjax: function(): void {
            const postNo = cF.util.getInputValue("#commentRegForm #postNo");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.COMMENT, { postNo }) : Url.COMMENTS;
                const ajaxData: FormData = new FormData(document.getElementById("commentRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (dF.Comment.modal.refreshFunc != null) {
                                dF.Comment.modal.refreshFunc();
                            } else {
                                cF.ui.blockUIReload();
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

            const url: string = cF.util.bindUrl(Url.COMMENT, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* initialize form. */
                dF.Comment.modal.initForm(rsltObj);
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

                const url: string = cF.util.bindUrl(Url.COMMENT, { postNo });
                const ajaxData: Record<string, any> = { }; // TODO: actvtyCtgrCd 다시 추가하기.
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (dF.Comment.modal.refreshFunc != null) {
                                dF.Comment.modal.refreshFunc();
                            } else {
                                cF.ui.blockUIReload();
                            }
                        });
                });
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.Comment.modal.init();
});