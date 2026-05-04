/**
 * CodeAdminApp.ts
 * 코드 관리 화면 Vue 앱 (오케스트레이션)
 *
 * @author nichefish
 */
import CodeGroupListTable from "./components/CodeGroupListTable.js";
import CodeGroupRegistForm from "./components/CodeGroupRegistForm.js";
import CodeGroupDetail from "./components/CodeGroupDetail.js";
import CodeItemRegistForm from "./components/CodeItemRegistForm.js";
import CodeGroupPagination from "./components/CodeGroupPagination.js";
import codeAdminI18nService from "./services/codeAdminI18nService.js";
import codeAdminDataService from "./services/codeAdminDataService.js";
import bindCodeAdminEventBridge from "./services/codeAdminEventBridgeService.js";
import createCodeAdminActions from "./services/codeAdminActionService.js";
import { CodeAdminActions, CodeAdminState } from "./types.js";

const state: CodeAdminState = {
    rows: [],
    pagination: {
        currPageNo: 1,
        lastPageNo: 1,
        totalCnt: 0,
        pageSize: 10,
        isFirstPage: true,
        isLastPage: true,
        prevPageNo: 0,
        nextPageNo: 0,
    },
    groupForm: { groupCode: "", groupName: "", description: "", useYn: "Y", registYn: "Y" },
    detail: { groupCode: "", groupName: "", description: "", codeItems: [] },
    itemForm: { groupCode: "", code: "", codeName: "", description: "", useYn: "Y", registYn: "Y" },
};

function t(key: string): string {
    return codeAdminI18nService.t(key);
}

function resetGroupForm(payload: Record<string, any>): void {
    state.groupForm.id = payload.id;
    state.groupForm.groupCode = payload.groupCode || "";
    state.groupForm.groupName = payload.groupName || "";
    state.groupForm.description = payload.description || "";
    state.groupForm.useYn = String(payload.useYn || "Y").toUpperCase();
    state.groupForm.registYn = cF.util.isNotEmpty(payload.id) ? "N" : "Y";
}

function resetItemForm(payload: Record<string, any>): void {
    state.itemForm.id = payload.id;
    state.itemForm.groupCode = payload.groupCode || "";
    state.itemForm.code = payload.code || "";
    state.itemForm.codeName = payload.codeName || "";
    state.itemForm.description = payload.description || "";
    state.itemForm.useYn = String(payload.useYn || "Y").toUpperCase();
    state.itemForm.registYn = cF.util.isNotEmpty(payload.code) ? "N" : "Y";
}

const actions: CodeAdminActions = createCodeAdminActions({
    state,
    t,
    resetGroupForm,
    resetItemForm,
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const CodeAdminRootApp = {
    name: "CodeAdminRootApp",
    components: {
        CodeGroupListTable,
        CodeGroupRegistForm,
        CodeGroupDetail,
        CodeItemRegistForm,
        CodeGroupPagination,
    },
    data(): { state: CodeAdminState } {
        return { state };
    },
    methods: {
        onOpenDetail(id: number): void { actions.openGroupDetail(id); },
        onToggleUse(id: number): void { actions.toggleGroupUse(id); },
        onDeleteGroup(id: number): void { actions.deleteGroup(id); },
        onRegistItem(): void { actions.openItemRegist(); },
        onModifyItem(id: number): void { actions.openItemModify(id); },
        onDeleteItem(id: number): void { actions.deleteItem(id); },
        onGoPage(pageNo: number): void { actions.page(pageNo); },
        onChangePageSize(size: number): void { actions.page(state.pagination.currPageNo, size); },
    },
    template: `
    <teleport to="#code_group_list_div">
        <CodeGroupListTable
            :rows="state.rows"
            @open-detail="onOpenDetail"
            @toggle-use="onToggleUse"
            @delete-group="onDeleteGroup"
        />
    </teleport>
    <teleport to="#code_group_regist_div">
        <CodeGroupRegistForm :form="state.groupForm" />
    </teleport>
    <teleport to="#code_group_detail_div">
        <CodeGroupDetail
            :detail="state.detail"
            @regist-item="onRegistItem"
            @modify-item="onModifyItem"
            @delete-item="onDeleteItem"
        />
    </teleport>
    <teleport to="#code_item_regist_div">
        <CodeItemRegistForm :form="state.itemForm" />
    </teleport>
    <teleport to="#code_group_pagination_div">
        <CodeGroupPagination
            :pagination="state.pagination"
            @go-page="onGoPage"
            @change-size="onChangePageSize"
        />
    </teleport>
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await codeAdminI18nService.load(Model.locale);
    state.rows = codeAdminDataService.parseRowsFromPageData();
    codeAdminDataService.applyPaginationFromPageData(state.pagination);

    if (!document.getElementById("code_admin_app")
        || !document.getElementById("code_group_list_div")
        || !document.getElementById("code_group_regist_div")
        || !document.getElementById("code_group_detail_div")
        || !document.getElementById("code_item_regist_div")
        || !document.getElementById("code_group_pagination_div")) {
        console.error("[CodeAdminApp] Vue mount root not found.");
        return;
    }

    const app = Vue.createApp(CodeAdminRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#code_admin_app");

    cF.table.initSort();
    bindCodeAdminEventBridge({ actions, state });
});
