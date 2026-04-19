/**
 * journal_annual_review_module.ts
 * 저널 결산 리뷰 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalAnnualReview = (function(): dfModule {
    return {
        initialized: false,
        tagify: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalAnnualReview.initialized) return;

            dF.JournalAnnualReview.initialized = true;
            console.log("'dF.JournalAnnualReview' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_annual_review_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#journalAnnualReviewRegForm", dF.JournalAnnualReview.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#journalAnnualReviewRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_journalAnnualReviewCn');
            cF.tinymce.setContentWhenReady("tinymce_journalAnnualReviewCn", obj.content || "");
            /* tagify */
            dF.JournalAnnualReview.tagify = cF.tagify.init("#journalAnnualReviewRegForm #tagListStr");
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_journalAnnualReviewCn").save();
            $("#journalAnnualReviewRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} journalAnnualId - 저널 결산 번호
         */
        regModal: function({ journalAnnualId }: { journalAnnualId: string|number}): void {
            if (isNaN(Number(journalAnnualId))) return;

            const obj: Record<string, any> = { journalAnnualId };
            /* initialize form. */
            dF.JournalAnnualReview.initForm(obj);
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} id - 년도.
         */
        mdfModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JournalAnnualReview.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalAnnualReviewRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id }) : Url.JOURNAL_ANNUAL_REVIEWS;
                const ajaxData: FormData = new FormData(document.getElementById("journalAnnualReviewRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} id - 글 번호.
         */
        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_REVIEW, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
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
