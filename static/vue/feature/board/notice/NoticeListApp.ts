/**
 * NoticeListApp.ts
 * 공지사항 목록 Vue 엔트리 (이벤트 브리지)
 */
import createNoticeListActions from "./services/noticeListActionService.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function bindEvents(actions: ReturnType<typeof createNoticeListActions>): void {
    window.addEventListener("notice:list-search", function(): void {
        actions.search();
    });
    window.addEventListener("notice:list-mypapr", function(): void {
        actions.myPaprList();
    });
    window.addEventListener("notice:open-regist-form", function(): void {
        actions.registForm();
    });
    window.addEventListener("notice:list-xlsx-download", function(): void {
        actions.xlsxDownload();
    });
    window.addEventListener("notice:open-detail-modal", function(evt: Event): void {
        const customEvt = evt as CustomEvent<{ id?: string | number }>;
        const id = customEvt.detail?.id;
        if (id === undefined || id === null) return;
        actions.detailModal(id);
    });
}

runWhenDomReady(function(): void {
    if (!document.getElementById("notice_list_app")) {
        console.error("[NoticeListApp] Vue mount root not found.");
        return;
    }
    const actions = createNoticeListActions();
    bindEvents(actions);
    cF.table.initSort();
    Vue.createApp({}).mount("#notice_list_app");
});
