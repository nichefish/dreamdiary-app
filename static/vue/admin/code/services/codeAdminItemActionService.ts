import codeAdminUiService from "./codeAdminUiService.js";
import { CodeAdminActions, CodeAdminState } from "../types.js";

type ItemActionParams = {
    state: CodeAdminState;
    t: (key: string) => string;
    resetItemForm: (payload: Record<string, any>) => void;
    getActions: () => CodeAdminActions;
    formValue: (formSelector: string, fieldId: string, fallback?: string) => string;
    confirmThen: (message: string, onConfirm: () => void) => void;
    showAjaxError: (res: AjaxResponse) => boolean;
};

export default function createCodeAdminItemActions(params: ItemActionParams): Pick<CodeAdminActions,
    "openItemRegist"
    | "openItemModify"
    | "submitItemRegist"
    | "registItemAjax"
    | "deleteItem"
> {
    const { state, t, resetItemForm, getActions, formValue, confirmThen, showAjaxError } = params;

    function configureItemFormValidation(actions: CodeAdminActions): void {
        cF.validate.validateForm("#codeItemRegistForm", actions.registItemAjax);
        cF.ui.chckboxLabel("#codeItemRegistForm #useYn", `${t("txt.comm.use")}//${t("txt.status.unuse")}`, "blue//gray");
        cF.validate.replaceBlankIfMatches("#codeItemRegistForm #code", cF.regex.nonCd);
        cF.validate.toUpperCase("#codeItemRegistForm #code");
    }

    return {
        openItemRegist(payload?: Record<string, any>): void {
            const p: Record<string, any> = payload || {};
            // procForm 을 그룹 등록 폼(#groupCode)보다 먼저 본다. 상세 직후 등록 폼 DOM 이 오염된 적이 있어 proc 쪽이 더 신뢰 가능하다.
            const groupCode: string = p.groupCode
                || state.detail.groupCode
                || (document.querySelector("#code_proc_groupCode") as HTMLInputElement | null)?.value
                || (document.querySelector("#codeGroupRegistForm #groupCode") as HTMLInputElement | null)?.value
                || "";
            resetItemForm({ ...p, groupCode });
            if (state.detail.id) state.modalReturnTarget = "code_group_detail_modal";
            codeAdminUiService.hideModal("code_group_detail_modal");
            codeAdminUiService.openModal("code_item_regist_modal");
            setTimeout((): void => configureItemFormValidation(getActions()), 0);
        },
        openItemModify(id: number): void {
            cF.ajax.get(Url.CODE_ITEM, { id }, (res: AjaxResponse): void => {
                if (showAjaxError(res)) return;
                const obj: Record<string, any> = res.rsltObj || {};
                obj.isModify = true;
                resetItemForm(obj);
                if (state.detail.id) state.modalReturnTarget = "code_group_detail_modal";
                codeAdminUiService.hideModal("code_group_detail_modal");
                codeAdminUiService.openModal("code_item_regist_modal");
                setTimeout((): void => configureItemFormValidation(getActions()), 0);
            });
        },
        submitItemRegist(): void {
            if (typeof window.jQuery !== "undefined" && window.jQuery("#codeItemRegistForm").length) {
                window.jQuery("#codeItemRegistForm").trigger("submit");
                return;
            }
            (document.querySelector("#codeItemRegistForm") as HTMLFormElement | null)?.requestSubmit?.();
        },
        registItemAjax(): void {
            confirmThen(t("view.cnfm.save"), (): void => {
                const registYn: string = formValue("#codeItemRegistForm", "regYn", "Y").toUpperCase();
                const url: string = registYn === "Y" ? Url.CODE_ITEMS : Url.CODE_ITEM;
                cF.$ajax.post(url, cF.util.getJsonFormData("#codeItemRegistForm"), (res: AjaxResponse): void => {
                    if (res.rslt) state.modalReturnTarget = null;
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
        deleteItem(id: number): void {
            confirmThen(t("view.cnfm.del"), (): void => {
                cF.$ajax.delete(Url.CODE_ITEM, { id }, (res: AjaxResponse): void => {
                    if (res.rslt) state.modalReturnTarget = null;
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
    };
}

