/**
 * BoardPostListApp.ts
 * 일반게시판 목록 화면 Vue 엔트리 (액션 브리지)
 */
import createBoardPostListActions from "./services/boardPostListActionService.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function bindBoardPostListEventBridge(actions: ReturnType<typeof createBoardPostListActions>): void {
    window.addEventListener("board-post:list-search", function(): void {
        actions.search();
    });
    window.addEventListener("board-post:list-mypapr", function(): void {
        actions.myPaprList();
    });
    window.addEventListener("board-post:list-all", function(): void {
        actions.list();
    });
    window.addEventListener("board-post:open-reg-form", function(): void {
        actions.regForm();
    });
    window.addEventListener("board-post:list-xlsx-download", function(): void {
        actions.xlsxDownload();
    });
    window.addEventListener("board-post:open-dtl-modal", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null) return;
        actions.dtlModal(id);
    });
}

runWhenDomReady(function(): void {
    if (!document.getElementById("board_post_list_app")) {
        console.error("[BoardPostListApp] Vue mount root not found.");
        return;
    }

    const actions = createBoardPostListActions();
    bindBoardPostListEventBridge(actions);
    cF.table.initSort();

    Vue.createApp({}).mount("#board_post_list_app");
});
