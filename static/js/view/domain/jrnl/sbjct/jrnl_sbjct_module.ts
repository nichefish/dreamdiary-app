/**
 * jrnl_sbjct_module.ts
 * 저널 주제 스크립트 모듈
 * 
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlSbjct = (function(): dfModule {
    return {
        initialized: false,
        isMdf: $("#jrnlSbjctRegForm").data("mode") === "modify",

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlSbjct.initialized) return;

            dF.JrnlSbjct.initialized = true;
            console.log("'dF.JrnlSbjct' module initialized.");
        },

        /**
         * form init
         */
        initForm: function() {
            /* jquery validation */
            cF.validate.validateForm("#jrnlSbjctRegForm", dF.JrnlSbjct.submitHandler);
            /* tinymce init */
            cF.tinymce.init("#tinymce_cn");
            /* tagify */
            cF.tagify.initWithCtgr("#tagListStr", undefined);
            // 잔디발송여부 클릭시 글씨 변경
            cF.ui.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function() {
                $("#trgetTopicSpan").show();
            }, function() {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if (dF.JrnlSbjct.submitMode === "preview") {
                const popupNm: string = "preview";
                const options: string = 'width=1280,height=1440,top=0,left=270';
                const popup: Window = cF.ui.openPopup("", popupNm, options);
                if (popup) popup.focus();
                const popupUrl: string = Url.JRNL_SBJCT_REG_PREVIEW_POP;
                $("#jrnlSbjctRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            } else if (dF.JrnlSbjct.submitMode === "submit") {
                $("#jrnlSbjctRegForm").removeAttr("action");
                Swal.fire({
                    text: Message.get(dF.JrnlSbjct.isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    dF.JrnlSbjct.regAjax();
                });
            }
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", `${Url.JRNL_SBJCT_LIST!}?actionTyCd=SEARCH`);
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const url: string = Url.JRNL_SBJCT_LIST;
            const param: string = "?searchType=nickNm&searchKeyword=${authInfo.nickNm!}&regstrId=${authInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR";
            cF.ui.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.JRNL_SBJCT_REG_FORM);
        },

        /**
         * form submit
         */
        submit: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            dF.JrnlSbjct.submitMode = "submit";
            $("#jrnlSbjctRegForm").submit();
        },

        /**
         * 미리보기 팝업 호출
         */
        preview: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            dF.JrnlSbjct.submitMode = "preview";
            $("#jrnlSbjctRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = dF.JrnlSbjct.isMdf ? Url.JRNL_SBJCT_MDF_AJAX : Url.JRNL_SBJCT_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("jrnlSbjctRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({text: res.message})
                    .then(function(): void {
                        if (!res.rslt) return;

                        if (res.rsltObj === undefined) dF.JrnlSbjct.list();
                        const postNo: number = res.rsltObj.postNo;
                        cF.ui.blockUIReplace(`${Url.JRNL_SBJCT_DTL!}?postNo=${postNo}`);
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            $("#procForm #postNo").val(postNo);
            cF.form.blockUISubmit("#procForm", Url.JRNL_SBJCT_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.JRNL_SBJCT_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({text: res.message});
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "jrnlSbjct_dtl");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.JRNL_SBJCT_MDF_FORM);
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
                
                const url: string = Url.JRNL_SBJCT_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({text: res.message})
                        .then(function(): void {
                            if (res.rslt) dF.JrnlSbjct.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = `${Url.JRNL_SBJCT_LIST!}<#if isMdf!false>?isBackToList=Y</#if>`;
            cF.ui.blockUIReplace(listUrl);
        }
    }
})();