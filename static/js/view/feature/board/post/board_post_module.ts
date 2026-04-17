/**
 * board_post_module.ts
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.BoardPost = (function(): dfModule {
    return {
        isReg: $("#boardPostRegForm").data("mode") === "regist",
        isMdf: $("#boardPostRegForm").data("mode") === "modify",
        initialized: false,

        init: function(): void {
            if (dF.BoardPost.initialized) return;

            dF.BoardPost.initialized = true;
            console.log("'dF.BoardPost' module initialized.");
        },

        initForm: function(): void {
            cF.validate.validateForm("#postRegForm", dF.BoardPost.submitHandler);
            cF.tinymce.init("#tinymce_content");
            cF.tagify.initWithCtgr("#postRegForm #tagListStr");
            cF.ui.chckboxLabel(
                "#postRegForm #jandiYn",
                "\ubc1c\uc1a1//\ubbf8\ubc1c\uc1a1",
                "blue//gray",
                function(): void {
                    $("#trgetTopicSpan").show();
                },
                function(): void {
                    $("#trgetTopicSpan").hide();
                }
            );
        },

        submitHandler: function(): boolean {
            if (Page.submitMode === "preview") {
                const popupNm: string = "preview";
                const options: string = "width=1280,height=1440,top=0,left=270";
                const popup = cF.ui.openPopup("", popupNm, options);
                if (popup) popup.focus();

                const popupUrl: string = Url.BOARD_POST_REG_PREVIEW_POP;
                $("#postRegForm").attr("action", popupUrl).attr("target", popupNm);
                return true;
            }

            if (Page.submitMode === "submit") {
                $("#postRegForm").removeAttr("action");
                Swal.fire({
                    text: dF.BoardPost.isMdf ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;
                    dF.BoardPost.regAjax();
                });
            }

            return false;
        },

        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", Url.BOARD_POST_LIST + "?actionTyCd=SEARCH");
        },

        myPaprList: function(): void {
            const contentTypeElement: HTMLInputElement|null = document.querySelector("#contentType");
            if (!contentTypeElement) return;

            const contentType: string = contentTypeElement.value;
            const url: string = Url.BOARD_POST_LIST;
            const param: string = `?contentType=${contentType}&searchType=nickname&searchKeyword=${AuthInfo.nickname!}&createdBy=${AuthInfo.username!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.ui.blockUIReplace(url + param);
        },

        regForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_REG_FORM);
        },

        submit: function(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            Page.submitMode = "submit";
            $("#postRegForm").submit();
        },

        preview: function(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            Page.submitMode = "preview";
            $("#postRegForm").submit();
        },

        regAjax: function(): void {
            const url: string = dF.BoardPost.isMdf ? Url.BOARD_POST_MDF_AJAX : Url.BOARD_POST_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("postRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message }).then(function(): void {
                    if (res.rslt) dF.BoardPost.list();
                });
            }, "block");
        },

        dtl: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            $("#procForm #id").val(id);
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_DTL);
        },

        dtlModal: function(id: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(id))) return;

            const url: string = Url.BOARD_POST_DTL_AJAX;
            const ajaxData: Record<string, any> = { id: id, contentType: $("#contentType").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "board_post_dtl");
            });
        },

        mdfForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_MDF_FORM);
        },

        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.BOARD_POST_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (res.rslt) dF.BoardPost.list();
                    });
                }, "block");
            });
        },

        list: function(): void {
            const contentType: string = $("#contentType").val() as string;
            const listUrl: string = `${Url.BOARD_POST_LIST}?contentType=${contentType}` + (dF.BoardPost.isMdf ? "&isBackToList=Y" : "");
            cF.ui.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.BoardPost.init();
});
