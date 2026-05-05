/**
 * BoardPostRegFormApp.ts
 * 일반게시판 등록/수정 화면 Vue 엔트리 (액션 브리지)
 */
export {};

type SubmitMode = "preview" | "submit" | "";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function createActions(): {
    preview: () => void;
    submit: () => void;
    list: () => void;
} {
    let submitMode: SubmitMode = "";
    const mode = String($("#postRegForm").data("mode") || "");
    const isMdf = mode === "modify";

    function submitHandler(): boolean {
        if (submitMode === "preview") {
            const popupNm = "preview";
            const options = "width=1280,height=1440,top=0,left=270";
            const popup = cF.ui.openPopup("", popupNm, options);
            if (popup) popup.focus();
            $("#postRegForm").attr("action", Url.BOARD_POST_REG_PREVIEW_POP).attr("target", popupNm);
            return true;
        }
        if (submitMode === "submit") {
            $("#postRegForm").removeAttr("action").removeAttr("target");
            Swal.fire({
                text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                regAjax();
            });
        }
        return false;
    }

    function regAjax(): void {
        const id = cF.util.getInputValue("#postRegForm input[name='id']");
        const url = isMdf ? cF.util.bindUrl(Url.BOARD_POST, { id }) : Url.BOARD_POSTS;
        const ajaxData = new FormData(document.getElementById("postRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message }).then(function(): void {
                if (res.rslt) list();
            });
        }, "block");
    }

    function list(): void {
        const contentType = $("#contentType").val() as string;
        const listUrl = `${Url.BOARD_POST_LIST}?contentType=${contentType}` + (isMdf ? "&isBackToList=Y" : "");
        cF.ui.blockUIReplace(listUrl);
    }

    function initForm(): void {
        cF.validate.validateForm("#postRegForm", submitHandler);
        cF.tinymce.init("#tinymce_content");
        cF.tagify.initWithCtgr("#postRegForm #tagListStr");
        cF.ui.chckboxLabel(
            "#postRegForm #jandiYn",
            "발송//미발송",
            "blue//gray",
            function(): void { $("#trgetTopicSpan").show(); },
            function(): void { $("#trgetTopicSpan").hide(); }
        );
    }

    initForm();

    return {
        preview(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "preview";
            $("#postRegForm").submit();
        },
        submit(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "submit";
            $("#postRegForm").submit();
        },
        list,
    };
}

function bindEventBridge(actions: { preview: () => void; submit: () => void; list: () => void }): void {
    window.addEventListener("board-post:form-preview", function(): void {
        actions.preview();
    });
    window.addEventListener("board-post:form-submit", function(): void {
        actions.submit();
    });
    window.addEventListener("board-post:form-list", function(): void {
        actions.list();
    });
}

runWhenDomReady(function(): void {
    if (!document.getElementById("board_post_reg_form_app")) {
        console.error("[BoardPostRegFormApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    bindEventBridge(actions);
    Vue.createApp({}).mount("#board_post_reg_form_app");
});
