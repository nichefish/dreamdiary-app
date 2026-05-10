/**
 * JournalThreadDetailApp.ts
 * Journal thread detail page Vue entry.
 */
import { resolveMessage } from "../../../common/messageHelper.js";

export {};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function createActions(): { modifyForm: () => void; deleteAjax: (id: string | number) => void; list: () => void } {
    function list(): void {
        cF.ui.blockUIReplace(Url.JOURNAL_THREAD_LIST);
    }

    return {
        modifyForm(): void {
            cF.form.blockUISubmit("#procForm", Url.JOURNAL_THREAD_MODIFY_FORM);
        },
        deleteAjax(id: string | number): void {
            if (isNaN(Number(id))) return;
            Swal.fire({
                text: resolveMessage("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.$ajax.delete(cF.util.bindUrl(Url.JOURNAL_THREAD_API, { id }), null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (res.rslt) list();
                    });
                }, "block");
            });
        },
        list,
    };
}

runWhenDomReady(function(): void {
    if (!document.getElementById("journal_thread_detail_app")) {
        console.error("[JournalThreadDetailApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    window.addEventListener("journal-thread:detail-modify-form", function(): void { actions.modifyForm(); });
    window.addEventListener("journal-thread:detail-list", function(): void { actions.list(); });
    window.addEventListener("journal-thread:detail-delete", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null) return;
        actions.deleteAjax(id);
    });
    Vue.createApp({}).mount("#journal_thread_detail_app");
});
