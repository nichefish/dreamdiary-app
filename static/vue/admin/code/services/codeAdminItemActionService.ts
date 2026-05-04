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
            const groupCode: string = (document.querySelector("#groupCode") as HTMLInputElement | null)?.value || "";
            resetItemForm(payload || { groupCode });
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
                codeAdminUiService.hideModal("code_group_detail_modal");
                codeAdminUiService.openModal("code_item_regist_modal");
                setTimeout((): void => configureItemFormValidation(getActions()), 0);
            });
        },
        submitItemRegist(): void {
            (document.querySelector("#codeItemRegistForm") as HTMLFormElement | null)?.submit();
        },
        registItemAjax(): void {
            confirmThen(t("view.cnfm.save"), (): void => {
                const registYn: string = formValue("#codeItemRegistForm", "regYn", "Y").toUpperCase();
                const url: string = registYn === "Y" ? Url.CODE_ITEMS : Url.CODE_ITEM;
                cF.$ajax.post(url, cF.util.getJsonFormData("#codeItemRegistForm"), (res: AjaxResponse): void => {
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
        deleteItem(id: number): void {
            confirmThen(t("view.cnfm.del"), (): void => {
                cF.$ajax.delete(Url.CODE_ITEM, { id }, (res: AjaxResponse): void => {
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
    };
}

