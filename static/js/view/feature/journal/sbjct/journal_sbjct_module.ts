/**
 * journal_sbjct_module.ts
 * 저널 주제 스크립트 모듈
 * 
 * @author nichefish
 *
 * 변경(P10): classic <code>var dF</code> 제거. <code>globalThis.dF</code> + <code>dfNs</code> + IIFE
 * (<code>journal_entry_tag_module.ts</code> P9 와 동일). FTL classic 이므로 <code>export {}</code> 없음.
 */
(function (g: any): void {
    if (g.dF == null) {
        g.dF = {};
    }
    const dfNs: any = g.dF;

    dfNs.JournalSbjct = (function(): dfModule {
    return {
        initialized: false,
        isReg: $("#journalSbjctRegForm").data("mode") === "regist",
        isMdf: $("#journalSbjctRegForm").data("mode") === "modify",

        /**
         * 모듈을 초기화한다.
         */
        init: function(): void {
            if (dfNs.JournalSbjct.initialized) return;

            dfNs.JournalSbjct.initialized = true;
            console.log("'dF.JournalSbjct' module initialized.");
        },

        /**
         * 폼 초기화
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#journalSbjctRegForm", dfNs.JournalSbjct.submitHandler);
            /* tinymce init */
            cF.tinymce.init("#tinymce_content");
            /* tagify */
            cF.tagify.initWithCtgr("#tagListStr", undefined);
            // 잔디발송여부 클릭시 글씨 변경
            cF.ui.chckboxLabel("#journalSbjctRegForm #jandiYn", "발송//미발송", "blue//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if (dfNs.JournalSbjct.submitMode === "preview") {
                const popupNm: string = "preview";
                const options: string = 'width=1280,height=1440,top=0,left=270';
                const popup: Window = cF.ui.openPopup("", popupNm, options);
                if (popup) popup.focus();
                const popupUrl: string = Url.JOURNAL_SBJCT_REG_PREVIEW_POP;
                $("#journalSbjctRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            } else if (dfNs.JournalSbjct.submitMode === "submit") {
                $("#journalSbjctRegForm").removeAttr("action");
                Swal.fire({
                    text: Message.get(dfNs.JournalSbjct.isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    dfNs.JournalSbjct.regAjax();
                });
            }
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", `${Url.JOURNAL_SBJCT_LIST!}?actionTyCd=SEARCH`);
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const url: string = Url.JOURNAL_SBJCT_LIST;
            const param: string = "?searchType=nickname&searchKeyword=${authInfo.nickname!}&createdBy=${authInfo.username!}&pageSize=50&actionTyCd=MY_PAPR";
            cF.ui.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.JOURNAL_SBJCT_REG_FORM);
        },

        /**
         * 폼 제출
         */
        submit: function(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            dfNs.JournalSbjct.submitMode = "submit";
            $("#journalSbjctRegForm").submit();
        },

        /**
         * 미리보기 팝업 호출
         */
        preview: function(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            dfNs.JournalSbjct.submitMode = "preview";
            $("#journalSbjctRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = dfNs.JournalSbjct.isMdf ? Url.JOURNAL_SBJCT_MDF_AJAX : Url.JOURNAL_SBJCT_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("journalSbjctRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({text: res.message})
                    .then(function(): void {
                        if (!res.rslt) return;

                        if (res.rsltObj == null) dfNs.JournalSbjct.list();
                        const id: number = res.rsltObj.id;
                        cF.ui.blockUIReplace(`${Url.JOURNAL_SBJCT_DTL!}?id=${id}`);
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} id - 조회할 글 번호.
         */
        dtl: function(id: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(id))) return;

            $("#procForm #id").val(id);
            cF.form.blockUISubmit("#procForm", Url.JOURNAL_SBJCT_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} id - 조회할 글 번호.
         */
        dtlModal: function(id: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(id))) return;

            const url: string = Url.JOURNAL_SBJCT_DTL_AJAX;
            const ajaxData: Record<string, any> = { id };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({text: res.message});
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "journalSbjct_dtl");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.JOURNAL_SBJCT_MDF_FORM);
        },

        /**
         * 삭제 (Ajax)
         */
        delAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                
                const url: string = Url.JOURNAL_SBJCT_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({text: res.message})
                        .then(function(): void {
                            if (res.rslt) dfNs.JournalSbjct.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = `${Url.JOURNAL_SBJCT_LIST!}<#if isMdf!false>?isBackToList=Y</#if>`;
            cF.ui.blockUIReplace(listUrl);
        }
    }
    })();
})(typeof globalThis !== "undefined" ? globalThis : (window as any));
