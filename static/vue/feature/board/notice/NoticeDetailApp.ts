/**
 * NoticeDetailApp.ts
 * 공지사항 상세 Vue 엔트리 (이벤트 브리지)
 */
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
        cF.ui.blockUIReplace(Url.NOTICE_LIST);
    }

    return {
        modifyForm(): void {
            cF.form.blockUISubmit("#procForm", Url.NOTICE_MODIFY_FORM);
        },
        deleteAjax(id: string | number): void {
            if (isNaN(Number(id))) return;
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.$ajax.delete(cF.util.bindUrl(Url.NOTICE, { id }), null, function(res: AjaxResponse): void {
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
    if (!document.getElementById("notice_detail_app")) {
        console.error("[NoticeDetailApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    window.addEventListener("notice:detail-modify-form", function(): void { actions.modifyForm(); });
    window.addEventListener("notice:detail-list", function(): void { actions.list(); });
    window.addEventListener("notice:detail-delete", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null) return;
        actions.deleteAjax(id);
    });
    Vue.createApp({}).mount("#notice_detail_app");
});
