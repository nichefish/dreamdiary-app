/**
 * notice_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Notice = (function(): Module {
    return {
        isMdf: $("#noticeRegForm").data("mode") === "modify",

        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'Notice' module initialized.");
        },

        /**
         * form init
         */
        initForm: function() {
            /* jquery validation */
            cF.validate.validateForm("#noticeRegForm", dF.Notice.submitHandler);
            /* tinymce init */
            cF.tinymce.init("#tinymce_cn");
            /* tagify */
            cF.tagify.initWithCtgr("#noticeRegForm #tagListStr");
            // 잔디발송여부 클릭시 글씨 변경
            cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function(): void {
                $("#trgetTopicSpan").show();
            }, function(): void {
                $("#trgetTopicSpan").hide();
            });
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if (dF.Notice.submitMode === "preview") {
                const popupNm: string = "preview";
                const options: string = 'width=1280,height=1440,top=0,left=270';
                const popup: Window = cF.util.openPopup("", popupNm, options);
                if (popup) popup.focus();
                const popupUrl: string = Url.NOTICE_REG_PREVIEW_POP;
                $("#noticeRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            } else if (dF.Notice.submitMode === "submit") {
                $("#noticeRegForm").removeAttr("action");
                Swal.fire({
                    text: Message.get(dF.Notice.isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult) {
                    if (!result.value) return;

                    dF.Notice.regAjax();
                });
            }
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            const url: string = Url.NOTICE_LIST;
            cF.util.blockUISubmit("#listForm", url + "?actionTyCd=SEARCH");
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const url: string = Url.NOTICE_LIST;
            const param: string = `?searchType=nickNm&searchKeyword=${AuthInfo.nickNm!}&regstrId=${AuthInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.util.blockUIReplace(url + param);
        },

        /**
         * 엑셀 다운로드
         */
        xlsxDownload: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.NOTICE_LIST_XLSX_DOWNLOAD).submit();
            });
        },

        /**
         * 등록 화면 이동
         */
        regForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.NOTICE_REG_FORM);
        },

        /**
         * form submit
         */
        submit: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            dF.Notice.submitMode = "submit";
            $("#noticeRegForm").submit();
        },

        /**
         * 미리보기 팝업 호출
         */
        preview: function(): void {
            if (tinymce !== undefined) tinymce.activeEditor.save();
            dF.Notice.submitMode = "preview";
            $("#noticeRegForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = dF.Notice.isMdf ? Url.NOTICE_MDF_AJAX : Url.NOTICE_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("noticeRegForm") as HTMLFormElement);
            cF.ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({text: res.message})
                    .then(function(): void {
                        if (res.rslt) dF.Notice.list();
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#procForm #postNo").val(postNo);
            cF.util.blockUISubmit("#procForm", Url.NOTICE_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.NOTICE_DTL_AJAX;
            const ajaxData: Record<string, any> = {"postNo": postNo};
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({text: res.message});
                    return;
                }
                cF.handlebars.template(res.rsltObj, "notice_dtl", "show");
            });
        },

        /**
         * 수정 화면 이동
         */
        mdfForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.NOTICE_MDF_FORM);
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

                const url: string = Url.NOTICE_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({text: res.message})
                        .then(function(): void {
                            if (res.rslt) dF.Notice.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = Url.NOTICE_LIST + dF.Notice.isMdf ? "?isBackToList=Y" : "";
            cF.util.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.Notice.init();
});