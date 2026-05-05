import codeAdminUiService from "./codeAdminUiService.js";
import { CodeAdminActions, CodeAdminState } from "../types.js";

type EventBridgeParams = {
    actions: CodeAdminActions;
    state: CodeAdminState;
};

export default function bindCodeAdminEventBridge(params: EventBridgeParams): void {
    const { actions, state } = params;
    const map: Array<{ eventName: string; handler: () => void }> = [
        { eventName: "code-admin:request-search", handler: (): void => actions.search() },
        { eventName: "code-admin:request-open-group-regist", handler: (): void => actions.openGroupRegist() },
        { eventName: "code-admin:request-submit-group-regist", handler: (): void => actions.submitGroupRegist() },
        { eventName: "code-admin:request-open-group-modify-current", handler: (): void => actions.openGroupModifyCurrent() },
        { eventName: "code-admin:request-submit-item-regist", handler: (): void => actions.submitItemRegist() },
    ];
    map.forEach((item): void => {
        window.addEventListener(item.eventName, item.handler as EventListener);
    });

    window.addEventListener("code-admin:request-open-group-regist-with-payload", ((e: CustomEvent<Record<string, any>>): void => {
        actions.openGroupRegist(e.detail || {});
    }) as EventListener);

    window.addEventListener("code-admin:request-open-item-regist-with-payload", ((e: CustomEvent<Record<string, any>>): void => {
        actions.openItemRegist(e.detail || {});
    }) as EventListener);

    window.addEventListener("code-admin:request-open-group-detail-with-payload", ((e: CustomEvent<Record<string, any>>): void => {
        const payload: Record<string, any> = e.detail || {};
        if (!payload || typeof payload !== "object") return;

        state.detail.id = payload.id;
        state.detail.groupCode = payload.groupCode || "";
        state.detail.groupName = payload.groupName || "";
        state.detail.description = payload.description || "";
        state.detail.codeItems = Array.isArray(payload.codeItems) ? payload.codeItems : [];

        // 상세 그룹코드는 procForm(#code_proc_groupCode)에만 반영한다.
        // #codeGroupRegistForm #groupCode 는 그룹 등록 모달의 입력으로, 여기에 쓰면 Vue groupForm 과 DOM 이 불일치(오염)한다.
        const procGroupCode: HTMLInputElement | null = document.querySelector("#code_proc_groupCode");
        if (procGroupCode) procGroupCode.value = state.detail.groupCode;

        codeAdminUiService.openModal("code_group_detail_modal");
        setTimeout((): void => codeAdminUiService.initCodeItemDraggable(), 0);
    }) as EventListener);
}

