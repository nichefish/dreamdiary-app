import boardAdminUiService from "./boardAdminUiService.js";
import { BoardAdminActions, BoardAdminState } from "../types.js";
import { confirmThen, formValue, showAjaxError } from "./boardAdminActionHelpers.js";

type CreateParams = {
    state: BoardAdminState;
    t: (key: string) => string;
    resetBoardForm: (payload: Record<string, any>) => void;
};

export default function createBoardAdminActions(params: CreateParams): BoardAdminActions {
    const { state, t, resetBoardForm } = params;

    const actions: BoardAdminActions = {} as BoardAdminActions;

    function getActions(): BoardAdminActions {
        return actions;
    }

    function configureBoardFormValidation(): void {
        cF.validate.validateForm("#boardRegForm", (): void => actions.registBoardAjax());
        cF.ui.chckboxLabel("#boardRegForm #useYn", `${t("txt.comm.use")}//${t("txt.status.unuse")}`, "blue//gray");
        cF.validate.replaceBlankIfMatches("#boardRegForm .cddata", cF.regex.nonCd);
        cF.validate.onlyNum(".number");
    }

    actions.page = function(pageNo: number, pageSize?: number): void {
        const listForm: HTMLFormElement | null = document.querySelector("#listForm");
        if (!listForm) return;

        const pageNoEl: HTMLInputElement | null = listForm.querySelector("#pageNo");
        const pageSizeEl: HTMLInputElement | null = listForm.querySelector("#pageSize");
        const prevPageSize: number = Number(pageSizeEl?.value || state.pagination.pageSize || 10);
        let targetPageNo: number = Number(pageNo || 1);

        if (typeof pageSize === "number" && pageSize > 0 && pageSize !== prevPageSize) {
            const offset: number = (targetPageNo - 1) * prevPageSize;
            targetPageNo = Math.floor(offset / pageSize) + 1;
            if (pageSizeEl) pageSizeEl.value = String(pageSize);
        }

        if (pageNoEl) pageNoEl.value = String(targetPageNo);
        const listUrl: string = listForm.dataset.url || (typeof Url !== "undefined" ? (Url as any).BOARD_ADMIN_PAGE : "");
        if (listUrl) cF.form.blockUISubmit("#listForm", listUrl);
    };

    actions.openBoardRegist = function(payload?: Record<string, any>): void {
        resetBoardForm(payload || {});
        boardAdminUiService.openModal("board_reg_modal");
        setTimeout((): void => configureBoardFormValidation(), 0);
    };

    actions.submitBoardRegist = function(): void {
        if (typeof window.jQuery !== "undefined" && window.jQuery("#boardRegForm").length) {
            window.jQuery("#boardRegForm").trigger("submit");
            return;
        }
        (document.querySelector("#boardRegForm") as HTMLFormElement | null)?.requestSubmit?.();
    };

    actions.openBoardModify = function(id: number): void {
        const url: string = (Url as any).BOARD_GROUP_DTL_AJAX;
        if (!url) return;
        cF.ajax.get(url, { id }, (res: AjaxResponse): void => {
            if (showAjaxError(res)) return;
            const obj: Record<string, any> = res.rsltObj || {};
            resetBoardForm(obj);
            boardAdminUiService.openModal("board_reg_modal");
            setTimeout((): void => configureBoardFormValidation(), 0);
        });
    };

    actions.registBoardAjax = function(): void {
        confirmThen(t("view.cnfm.reg"), (): void => {
            const regYn: string = formValue("#boardRegForm", "regYn", "Y").toUpperCase();
            const url: string = regYn === "Y"
                ? (Url as any).BOARD_GROUP_REG_AJAX
                : (Url as any).BOARD_GROUP_MDF_ITEM_AJAX;
            cF.$ajax.post(url, cF.util.getJsonFormData("#boardRegForm"), (res: AjaxResponse): void => {
                Swal.fire({ text: res.message }).then((): void => {
                    if (res.rslt) cF.ui.blockUIReload();
                });
            }, "block");
        });
    };

    actions.toggleBoardUse = function(id: number, currentlyUse: boolean): void {
        const msgKey: string = currentlyUse ? "view.cnfm.unuse" : "view.cnfm.use";
        confirmThen(t(msgKey), (): void => {
            const url: string = currentlyUse
                ? (Url as any).BOARD_GROUP_UNUSE_AJAX
                : (Url as any).BOARD_GROUP_USE_AJAX;
            cF.$ajax.post(url, { id }, (res: AjaxResponse): void => {
                Swal.fire({ text: res.message }).then((): void => {
                    if (res.rslt) cF.ui.blockUIReload();
                });
            }, "block");
        });
    };

    actions.deleteBoard = function(id: number): void {
        confirmThen(t("view.cnfm.del"), (): void => {
            cF.$ajax.post((Url as any).BOARD_GROUP_DEL_AJAX, { id }, (res: AjaxResponse): void => {
                Swal.fire({ text: res.message }).then((): void => {
                    if (res.rslt) cF.ui.blockUIReload();
                });
            }, "block");
        });
    };

    return actions;
}
