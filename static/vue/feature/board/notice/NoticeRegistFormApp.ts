/**
 * NoticeRegistFormApp.ts
 * 공지사항 등록/수정 Vue 엔트리 (이벤트 브리지)
 *
 * 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
 */
import { resolveMessage } from "../../../common/messageHelper.js";

export {};

type SubmitMode = "preview" | "submit" | "";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function createActions(): { preview: () => void; submit: () => void; list: () => void } {
    let submitMode: SubmitMode = "";
    const isModify = String($("#noticeRegForm").data("mode") || "") === "modify";

    function submitHandler(): boolean {
        if (submitMode === "preview") {
            const popupNm = "preview";
            const options = "width=1280,height=1440,top=0,left=270";
            const popup = cF.ui.openPopup("", popupNm, options);
            if (popup) popup.focus();
            $("#noticeRegForm").attr("action", Url.NOTICE_REGIST_PREVIEW_POP).attr("target", popupNm);
            return true;
        }
        if (submitMode === "submit") {
            $("#noticeRegForm").removeAttr("action").removeAttr("target");
            Swal.fire({
                text: resolveMessage(isModify ? "view.cnfm.mdf" : "view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                registAjax();
            });
        }
        return false;
    }

    function registAjax(): void {
        const formIdValue = String(($("input[name='id']").val() ?? "")).trim();
        const url = isModify ? cF.util.bindUrl(Url.NOTICE, { id: formIdValue }) : Url.NOTICES;
        const ajaxData = new FormData(document.getElementById("noticeRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message }).then(function(): void {
                if (res.rslt) list();
            });
        }, "block");
    }

    function list(): void {
        const listUrl = Url.NOTICE_LIST + (isModify ? "?isBackToList=Y" : "");
        cF.ui.blockUIReplace(listUrl);
    }

    cF.validate.validateForm("#noticeRegForm", submitHandler);
    cF.tinymce.init("#tinymce_content");
    cF.tagify.initWithCtgr("#noticeRegForm #tagListStr");
    cF.ui.chckboxLabel("#noticeRegForm #jandiYn", "발송//미발송", "blue//gray", function(): void {
        $("#trgetTopicSpan").show();
    }, function(): void {
        $("#trgetTopicSpan").hide();
    });

    return {
        preview(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "preview";
            $("#noticeRegForm").submit();
        },
        submit(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "submit";
            $("#noticeRegForm").submit();
        },
        list,
    };
}

runWhenDomReady(function(): void {
    if (!document.getElementById("notice_regist_form_app")) {
        console.error("[NoticeRegistFormApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    window.addEventListener("notice:form-preview", function(): void { actions.preview(); });
    window.addEventListener("notice:form-submit", function(): void { actions.submit(); });
    window.addEventListener("notice:form-list", function(): void { actions.list(); });
    Vue.createApp({}).mount("#notice_regist_form_app");
});
