/**
 * jrnl_diary_module.ts
 * 저널 일기 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDiary = (function(): dfModule {
    return {
        initialized: false,
        inKeywordSearchMode: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDiary.initialized) return;

            /* initialize submodules. */
            dF.JrnlDiaryTag.init();

            dF.JrnlDiary.initialized = true;
            console.log("'dF.JrnlDiary' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}) {
            /* show modal */
            cF.handlebars.modal(obj, "jrnl_diary_reg", ["header"]);

            /* jquery validation */
            cF.validate.validateForm("#jrnlDiaryRegForm", dF.JrnlDiary.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlDiaryRegForm #tagListStr");
            // checkbox init
            cF.util.chckboxLabel("imprtcYn", "중요//해당없음", "red//gray");
            /* tinymce editor reset */
            cF.tinymce.init('#tinymce_jrnlDiaryCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlDiaryCn", obj.cn || "");
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlDiaryRegForm #tagListStr", dF.JrnlDiaryTag.ctgrMap);
        },

        /**
         * 목록 조회 (Ajax)
         */
        keywordListAjax: function(): void {
            const keyword: string = (document.querySelector("#jrnl_aside #diaryKeyword") as HTMLInputElement)?.value;
            if (cF.util.isEmpty(keyword)) return;

            const url: string =Url.JRNL_DIARY_LIST_AJAX;
            const ajaxData: Record<string, any> = { "diaryKeyword": keyword };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                $("#jrnl_aside #yy").val("");
                $("#jrnl_aside #mnth").val("");
                // 목록 영역 클리어
                $("#jrnl_aside #dreamKeyword").val("");
                $("#jrnl_day_list_div").empty();
                $("#jrnl_dream_list_div").empty();
                // 태그 헤더 클리어
                $("#jrnl_day_tag_list_div").empty();
                $("#jrnl_diary_tag_list_div").empty();
                $("#jrnl_dream_tag_list_div").empty();
                cF.util.closeModal();
                cF.handlebars.template(res.rsltList, "jrnl_diary_list");
                dF.JrnlDiary.inKeywordSearchMode = true;
            }, "block");
        },

        /**
         * 등록 모달 호출
         * @param {string|number} jrnlDayNo - 저널 일자 번호.
         * @param {string} stdrdDt - 기준 날짜.
         */
        regModal: function(jrnlDayNo: string|number, stdrdDt): void {
            if (isNaN(Number(jrnlDayNo))) return;

            const obj: Record<string, any> = { "jrnlDayNo": jrnlDayNo, "stdrdDt": stdrdDt };
            /* initialize form. */
            dF.JrnlDiary.initForm(obj);
        },

        /**
         * form submit
         */
        submit: function(): void {
            tinymce.get("tinymce_jrnlDiaryCn").save();
            $("#jrnlDiaryRegForm").submit();
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function() {
            const isReg = $("#jrnlDiaryRegForm #postNo").val() === "";
            Swal.fire({
                text: Message.get(isReg ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = isReg ? Url.JRNL_DIARY_REG_AJAX : Url.JRNL_DIARY_MDF_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("jrnlDiaryRegForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            if (dF.JrnlDiary.inKeywordSearchMode) {
                                dF.JrnlDiary.keywordListAjax();
                            } else {
                                dF.JrnlDay.yyMnthListAjax();
                                dF.JrnlDiaryTag.listAjax();
                            }
                            // TODO: 결산 페이지에서 처리시도 처리해 줘야 한다.
                            cF.util.unblockUI();
                        });
                }, "block");
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_DIARY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo" : postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.JrnlDiary.initForm(rsltObj);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.JRNL_DIARY_DEL_AJAX;
                const ajaxData: Record<string, any> = { "postNo": postNo };
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.yyMnthListAjax();
                            dF.JrnlDiaryTag.listAjax();
                        });
                }, "block");
            });
        },
    }
})();