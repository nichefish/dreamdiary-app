/**
 * 게시판 관리(Board group) Vue 앱
 *
 * @author nichefish
 */
import BoardListTable from "./components/BoardListTable.js";
import BoardRegistForm from "./components/BoardRegistForm.js";
import BoardAdminPagination from "./components/BoardAdminPagination.js";
import boardAdminDataService from "./services/boardAdminDataService.js";
import bindBoardAdminEventBridge from "./services/boardAdminEventBridgeService.js";
import createBoardAdminActions from "./services/boardAdminActionService.js";
import { BoardAdminState } from "./types.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

const state = Vue.reactive({
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
    boardForm: {
        id: undefined as number | undefined,
        boardKey: "",
        boardName: "",
        categoryGroupCode: "",
        description: "",
        useYn: "Y",
        regYn: "Y",
    },
}) as BoardAdminState;
const i18n = createScopedI18n();

function t(key: string): string {
    return i18n.t(key);
}

function resetBoardForm(payload: Record<string, any>): void {
    state.boardForm.id = payload.id;
    state.boardForm.boardKey = payload.boardKey || "";
    state.boardForm.boardName = payload.boardName || "";
    state.boardForm.categoryGroupCode = payload.categoryGroupCode || "";
    state.boardForm.description = payload.description || "";
    state.boardForm.useYn = String(payload.useYn || "Y").toUpperCase();
    state.boardForm.regYn = cF.util.isNotEmpty(payload.id) ? "N" : "Y";
}

const actions = createBoardAdminActions({ state, t, resetBoardForm });

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveBoardAdminPageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

function initBoardListDraggable(): void {
    const keyExtractor: (item: HTMLElement) => { id: number } = (item: HTMLElement) => ({ id: Number(item.dataset.id) });
    const url: string = (Url as any).BOARD_GROUP_SORT_ORDR_AJAX;
    cF.draggable.init("", keyExtractor, url);
}

const BoardAdminRootApp = {
    name: "BoardAdminRootApp",
    components: {
        BoardListTable,
        BoardRegistForm,
        BoardAdminPagination,
    },
    data(): { state: BoardAdminState } {
        return { state };
    },
    methods: {
        onModifyBoard(id: number): void { actions.openBoardModify(id); },
        onToggleUse(id: number, currentlyUse: boolean): void { actions.toggleBoardUse(id, currentlyUse); },
        onDeleteBoard(id: number): void { actions.deleteBoard(id); },
        onGoPage(pageNo: number): void { actions.page(pageNo); },
        onChangePageSize(size: number): void { actions.page(state.pagination.currPageNo, size); },
    },
    template: `
    <teleport to="#board_group_list_div">
        <BoardListTable
            :rows="state.rows"
            @modify-board="onModifyBoard"
            @toggle-use="onToggleUse"
            @delete-board="onDeleteBoard"
        />
    </teleport>
    <teleport to="#board_reg_div">
        <BoardRegistForm :form="state.boardForm" />
    </teleport>
    <teleport to="#board_group_pagination_div">
        <BoardAdminPagination
            :pagination="state.pagination"
            @go-page="onGoPage"
            @change-size="onChangePageSize"
        />
    </teleport>
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveBoardAdminPageLocale());
    state.rows = boardAdminDataService.parseRowsFromPageData();
    boardAdminDataService.applyPaginationFromPageData(state.pagination);

    if (!document.getElementById("board_admin_app")
        || !document.getElementById("board_group_list_div")
        || !document.getElementById("board_reg_div")
        || !document.getElementById("board_group_pagination_div")) {
        console.error("[BoardAdminApp] Vue mount root not found.");
        return;
    }

    const app = Vue.createApp(BoardAdminRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#board_admin_app");

    cF.table.initSort();
    bindBoardAdminEventBridge({ actions });
    setTimeout((): void => initBoardListDraggable(), 0);
});
