import codeAdminUiService from "./codeAdminUiService.js";
import { CodeAdminActions, CodeAdminState } from "../types.js";

type GroupActionParams = {
    state: CodeAdminState;
    t: (key: string) => string;
    resetGroupForm: (payload: Record<string, any>) => void;
    getActions: () => CodeAdminActions;
    formValue: (formSelector: string, fieldId: string, fallback?: string) => string;
    confirmThen: (message: string, onConfirm: () => void) => void;
    showAjaxError: (res: AjaxResponse) => boolean;
};

export default function createCodeAdminGroupActions(params: GroupActionParams): Pick<CodeAdminActions,
    "search"
    | "openGroupRegist"
    | "submitGroupRegist"
    | "registGroupAjax"
    | "openGroupDetail"
    | "openGroupModifyCurrent"
    | "toggleGroupUse"
    | "deleteGroup"
> {
    const { state, t, resetGroupForm, getActions, formValue, confirmThen, showAjaxError } = params;

    function configureGroupFormValidation(actions: CodeAdminActions): void {
        cF.validate.validateForm("#codeGroupRegistForm", actions.registGroupAjax);
        cF.ui.chckboxLabel("#codeGroupRegistForm #useYn", `${t("txt.comm.use")}//${t("txt.status.unuse")}`, "blue//gray");
        cF.validate.replaceBlankIfMatches("#codeGroupRegistForm #groupCode", cF.regex.nonCd);
        cF.validate.toUpperCase("#codeGroupRegistForm #groupCode");
    }

    return {
        search(): void {
            const pageNoEl: HTMLInputElement | null = document.querySelector("#listForm #pageNo");
            if (pageNoEl) pageNoEl.value = "1";
            cF.form.blockUISubmit("#listForm", `${Url.CODE_GROUP_LIST}?actionTyCd=SEARCH`);
        },
        openGroupRegist(payload?: Record<string, any>): void {
            resetGroupForm(payload || {});
            codeAdminUiService.openModal("code_group_regist_modal");
            setTimeout((): void => configureGroupFormValidation(getActions()), 0);
        },
        submitGroupRegist(): void {
            (document.querySelector("#codeGroupRegistForm") as HTMLFormElement | null)?.submit();
        },
        registGroupAjax(): void {
            confirmThen(t("view.cnfm.reg"), (): void => {
                const registYn: string = formValue("#codeGroupRegistForm", "regYn", "Y").toUpperCase();
                const id: number = Number(formValue("#codeGroupRegistForm", "id", "0"));
                const url: string = registYn === "Y" ? Url.CODE_GROUPS : cF.util.bindUrl(Url.CODE_GROUP, { id });
                cF.$ajax.post(url, cF.util.getJsonFormData("#codeGroupRegistForm"), (res: AjaxResponse): void => {
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
        openGroupDetail(id: number): void {
            if (isNaN(Number(id))) return;
            cF.ajax.get(cF.util.bindUrl(Url.CODE_GROUP, { id }), null, (res: AjaxResponse): void => {
                if (showAjaxError(res)) return;
                const obj: Record<string, any> = res.rsltObj || {};
                state.detail.id = obj.id;
                state.detail.groupCode = obj.groupCode || "";
                state.detail.groupName = obj.groupName || "";
                state.detail.description = obj.description || "";
                state.detail.codeItems = Array.isArray(obj.codeItems) ? obj.codeItems : [];
                (document.querySelector("#groupCode") as HTMLInputElement | null)!.value = state.detail.groupCode;
                codeAdminUiService.openModal("code_group_detail_modal");
                setTimeout((): void => codeAdminUiService.initCodeItemDraggable(), 0);
            });
        },
        openGroupModifyCurrent(): void {
            const id: number = Number(state.detail.id || 0);
            if (!id) return;
            cF.ajax.get(cF.util.bindUrl(Url.CODE_GROUP, { id }), { id }, (res: AjaxResponse): void => {
                if (showAjaxError(res)) return;
                const obj: Record<string, any> = res.rsltObj || {};
                obj.isModify = true;
                resetGroupForm(obj);
                codeAdminUiService.hideModal("code_group_detail_modal");
                codeAdminUiService.openModal("code_group_regist_modal");
                setTimeout((): void => configureGroupFormValidation(getActions()), 0);
            });
        },
        toggleGroupUse(id: number): void {
            if (isNaN(Number(id))) return;
            const item: HTMLElement | null = document.querySelector(`.code-group-item[data-id='${id}']`);
            const currentUseYn: string = (item?.dataset?.useYn || "N").toUpperCase();
            const url: string = cF.util.bindUrl(Url.CODE_GROUP, { id });
            cF.$ajax.patch(url, { useYn: currentUseYn === "Y" ? "N" : "Y" }, (res: AjaxResponse): void => {
                if (showAjaxError(res)) return;
                cF.ui.blockUIReload();
            });
        },
        deleteGroup(id: number): void {
            if (isNaN(Number(id))) return;
            confirmThen(t("view.cnfm.del"), (): void => {
                cF.$ajax.delete(cF.util.bindUrl(Url.CODE_GROUP, { id }), null, (res: AjaxResponse): void => {
                    Swal.fire({ text: res.message }).then((): void => { if (res.rslt) cF.ui.blockUIReload(); });
                }, "block");
            });
        },
    };
}

