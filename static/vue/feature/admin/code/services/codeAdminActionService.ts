import { CodeAdminActions, CodeAdminState } from "../types.js";
import createCodeAdminGroupActions from "./codeAdminGroupActionService.js";
import createCodeAdminItemActions from "./codeAdminItemActionService.js";

function formValue(formSelector: string, fieldId: string, fallback: string = ""): string {
    const input: HTMLInputElement | null = document.querySelector(`${formSelector} #${fieldId}`);
    return String(input?.value || fallback);
}

function showAjaxError(res: AjaxResponse): boolean {
    if (res.rslt) return false;
    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
    return true;
}

function confirmThen(message: string, onConfirm: () => void): void {
    Swal.fire({ text: message, showCancelButton: true }).then((result: SwalResult): void => {
        if (!result.value) return;
        onConfirm();
    });
}

type CreateActionsParams = {
    state: CodeAdminState;
    t: (key: string) => string;
    resetGroupForm: (payload: Record<string, any>) => void;
    resetItemForm: (payload: Record<string, any>) => void;
};

export default function createCodeAdminActions(params: CreateActionsParams): CodeAdminActions {
    const { state, t, resetGroupForm, resetItemForm } = params;
    const actions = {
        page(pageNo: number, pageSize?: number): void {
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
            cF.form.blockUISubmit("#listForm", listForm.dataset.url || Url.CODE_ADMIN_PAGE);
        },
    };
    const getActions = (): CodeAdminActions => actions as CodeAdminActions;
    Object.assign(actions,
        createCodeAdminGroupActions({ state, t, resetGroupForm, getActions, formValue, confirmThen, showAjaxError }),
        createCodeAdminItemActions({ state, t, resetItemForm, getActions, formValue, confirmThen, showAjaxError }),
    );
    return actions as CodeAdminActions;
}

