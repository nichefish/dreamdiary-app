/**
 * JournalThreadRegistFormApp.ts
 * Journal thread register/modify page Vue entry.
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
    const isModify = String($("#journalThreadRegistForm").data("mode") || "") === "modify";

    function list(): void {
        cF.ui.blockUIReplace(Url.JOURNAL_THREAD_LIST + (isModify ? "?isBackToList=Y" : ""));
    }

    function registAjax(): void {
        const id = cF.util.getInputValue("#journalThreadRegistForm input[name='id']");
        const url = isModify ? cF.util.bindUrl(Url.JOURNAL_THREAD_API, { id }) : Url.JOURNAL_THREAD_API_LIST;
        const formEl = document.getElementById("journalThreadRegistForm") as HTMLFormElement | null;
        if (!formEl) {
            console.error("[JournalThreadRegistFormApp] #journalThreadRegistForm not found.");
            return;
        }
        const ajaxData = new FormData(formEl);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message }).then(function(): void {
                if (!res.rslt) return;
                if (res.rsltObj == null) {
                    list();
                    return;
                }
                cF.ui.blockUIReplace(`${Url.JOURNAL_THREAD_DETAIL}?id=${res.rsltObj.id}`);
            });
        }, "block");
    }

    function submitHandler(): boolean {
        if (submitMode === "preview") {
            const popupNm = "preview";
            const options = "width=1280,height=1440,top=0,left=270";
            const popup = cF.ui.openPopup("", popupNm, options);
            if (popup) popup.focus();
            $("#journalThreadRegistForm").attr("action", Url.JOURNAL_THREAD_REGIST_PREVIEW_POP).attr("target", popupNm);
            return true;
        }
        if (submitMode === "submit") {
            $("#journalThreadRegistForm").removeAttr("action").removeAttr("target");
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

    cF.validate.validateForm("#journalThreadRegistForm", submitHandler);
    cF.tinymce.init("#tinymce_content");
    cF.tagify.initWithCtgr("#tagListStr", undefined);
    cF.ui.chckboxLabel("#journalThreadRegistForm #jandiYn", "발송//미발송", "blue//gray", function(): void {
        $("#trgetTopicSpan").show();
    }, function(): void {
        $("#trgetTopicSpan").hide();
    });

    if (!isModify) {
        $("#jandiYn").click();
    }

    return {
        preview(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "preview";
            $("#journalThreadRegistForm").submit();
        },
        submit(): void {
            if (tinymce != null) tinymce.activeEditor.save();
            submitMode = "submit";
            $("#journalThreadRegistForm").submit();
        },
        list,
    };
}

runWhenDomReady(function(): void {
    if (!document.getElementById("journal_thread_regist_form_app")) {
        console.error("[JournalThreadRegistFormApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    window.addEventListener("journal-thread:form-preview", function(): void { actions.preview(); });
    window.addEventListener("journal-thread:form-submit", function(): void { actions.submit(); });
    window.addEventListener("journal-thread:form-list", function(): void { actions.list(); });
    Vue.createApp({}).mount("#journal_thread_regist_form_app");
});
