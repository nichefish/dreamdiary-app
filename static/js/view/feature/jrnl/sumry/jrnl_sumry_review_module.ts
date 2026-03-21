/**
 * jrnl_sumry_review_module.ts
 * 저널 결산 리뷰 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlSumryReview = (function(): dfModule {
    return {
        initialized: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlSumryReview.initialized) return;

            dF.JrnlSumryReview.initialized = true;
            console.log("'dF.JrnlSumryReview' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_sumry_review_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlSumryReviewRegForm", dF.JrnlSumryReview.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlSumryReviewRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_jrnlSumryReviewCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlSumryReviewCn", obj.cn || "");
            /* tagify */
            dF.JrnlSumryReview.tagify = cF.tagify.init("#jrnlSumryReviewRegForm #tagListStr");
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlSumryReviewCn").save();
            $("#jrnlSumryReviewRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} jrnlSumryNo - 저널 결산 번호
         */
        regModal: function({ jrnlSumryNo }: { jrnlSumryNo: string|number}): void {
            if (isNaN(Number(jrnlSumryNo))) return;

            const obj: Record<string, any> = { jrnlSumryNo };
            /* initialize form. */
            dF.JrnlSumryReview.initForm(obj);
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} postNo - 년도.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = cF.util.bindUrl(Url.JRNL_SUMRY_REVIEW, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlSumryReview.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const postNo: string = cF.util.getInputValue("#jrnlSumryReviewRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_SUMRY_REVIEW, { postNo }) : Url.JRNL_SUMRY_REVIEWS;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlSumryReviewRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            cF.ui.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();
