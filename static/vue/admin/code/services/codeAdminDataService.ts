import { CodeGroupRow, PaginationState } from "../types.js";

export default {
    parseRowsFromPageData(): CodeGroupRow[] {
        const dataEl: HTMLElement | null = document.getElementById("code_group_list_data");
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as CodeGroupRow[]) : [];
        } catch (e) {
            console.error("[CodeAdminApp] code_group_list_data parse failed", e);
            return [];
        }
    },
    applyPaginationFromPageData(pagination: PaginationState): void {
        const dataEl: HTMLElement | null = document.getElementById("code_group_pagination_data");
        if (!dataEl) return;
        try {
            const parsed: any = JSON.parse(dataEl.textContent || "{}");
            pagination.currPageNo = Number(parsed.currPageNo || 1);
            pagination.lastPageNo = Number(parsed.lastPageNo || 1);
            pagination.totalCnt = Number(parsed.totalCnt || 0);
            pagination.pageSize = Number(parsed.pageSize || 10);
            pagination.isFirstPage = !!parsed.isFirstPage;
            pagination.isLastPage = !!parsed.isLastPage;
            pagination.prevPageNo = Number(parsed.prevPageNo || 0);
            pagination.nextPageNo = Number(parsed.nextPageNo || 0);
        } catch (e) {
            console.error("[CodeAdminApp] code_group_pagination_data parse failed", e);
        }
    },
};

