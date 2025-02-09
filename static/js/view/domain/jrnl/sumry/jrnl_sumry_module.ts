/**
 * jrnl_sumry_module.ts
 * 저널 결산 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlSumry = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlSumry.initialized) return;

            dF.JrnlSumry.initialized = true;
            console.log("'dF.JrnlSumry' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_sumry_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlSumryRegForm", dF.JrnlSumry.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlSumryRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_jrnlSumryCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlSumryCn", obj.cn || "");
        },

        /**
         * 상세 화면으로 이동 (key로 조회)
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#procForm #postNo").val(postNo);
            cF.form.blockUISubmit("#procForm", Url.JRNL_SUMRY_DTL);
        },

        /**
         * 상세 화면으로 이동 (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlByYy: function(yy: string|number): void {
            if (isNaN(Number(yy))) return;

            const yYElmt: HTMLSelectElement = document.querySelector("#yyForm #yy");
            yYElmt.value = String(yy);
            cF.form.blockUISubmit("#procForm", Url.JRNL_SUMRY_DTL);
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_SUMRY_LIST);
        },

        /**
         * 특정 년도 결산 생성 (Ajax)
         * @param {string|number} yy - 결산을 생성할 년도.
         */
        makeYySumryAjax: function(yy: string|number): void {
            const yYElmt: HTMLSelectElement = document.querySelector("#listForm #yy");
            if (yy === undefined) yy = yYElmt.value;
            if (cF.util.isEmpty(yy)) {
                cF.ui.swalOrAlert("yy는 필수 항목입니다.");
                return;
            }
            const url: string = Url.JRNL_SUMRY_MAKE_AJAX;
            const ajaxData: Record<string, any> = { "yy": yy };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 전체 년도 결산 갱신 (Ajax)
         */
        makeTotalSumryAjax: function(): void {
            const url: string = Url.JRNL_SUMRY_MAKE_TOTAL_AJAX;
            cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 꿈 기록 완료 처리 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        comptAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_SUMRY_DREAM_COMPT_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) cF.ui.blockUIReload();
                    });
            }, "block");
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlSumryCn").save();
            $("#jrnlSumryRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        regModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_SUMRY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlSumry.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.JRNL_SUMRY_REG_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlSumryRegForm") as HTMLFormElement);
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