/**
 * BoardPostDetailApp.ts
 * 일반게시판 상세 화면 Vue 엔트리 (액션 브리지)
 */
export {};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function createActions(): {
    mdfForm: () => void;
    delAjax: (id: string | number) => void;
    list: () => void;
} {
    function list(): void {
        const board = $("#procForm #board").val() as string;
        cF.ui.blockUIReplace(`${Url.BOARD_POST_LIST}?contentType=${board}`);
    }

    return {
        mdfForm(): void {
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_MDF_FORM);
        },
        delAjax(id: string | number): void {
            if (isNaN(Number(id))) return;
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const ajaxData = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(cF.util.bindUrl(Url.BOARD_POST, { id }), ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (res.rslt) list();
                    });
                }, "block");
            });
        },
        list,
    };
}

function bindEventBridge(actions: { mdfForm: () => void; delAjax: (id: string | number) => void; list: () => void }): void {
    window.addEventListener("board-post:dtl-mdf-form", function(): void {
        actions.mdfForm();
    });
    window.addEventListener("board-post:dtl-list", function(): void {
        actions.list();
    });
    window.addEventListener("board-post:dtl-del", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null) return;
        actions.delAjax(id);
    });
}

runWhenDomReady(function(): void {
    if (!document.getElementById("board_post_dtl_app")) {
        console.error("[BoardPostDetailApp] Vue mount root not found.");
        return;
    }
    const actions = createActions();
    bindEventBridge(actions);
    Vue.createApp({}).mount("#board_post_dtl_app");
});
