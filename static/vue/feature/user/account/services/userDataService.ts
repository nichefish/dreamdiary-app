import { PaginationState, UserLabels, UserRow } from "../types.js";

export default {
    parseRowsFromPageData(): UserRow[] {
        const dataEl: HTMLElement | null = document.getElementById("user_list_data");
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as UserRow[]) : [];
        } catch (e) {
            console.error("[UserListApp] user_list_data parse failed", e);
            return [];
        }
    },
    applyPaginationFromPageData(pagination: PaginationState): void {
        const dataEl: HTMLElement | null = document.getElementById("user_pagination_data");
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
            console.error("[UserListApp] user_pagination_data parse failed", e);
        }
    },
    parseLabels(): UserLabels {
        const fallback: UserLabels = {
            noProfile: "",
            retired: "",
            activeEmployee: "",
            probation: "",
            locked: "잠김",
            use: "사용",
            emptyList: "",
            totalPrefix: "Total",
            unuse: "미사용",
            modifyTooltip: "",
            deleteTooltip: "",
            listTooltip: "",
            passwordResetTooltip: "",
        };
        const dataEl: HTMLElement | null = document.getElementById("user_label_data");
        if (!dataEl) return fallback;
        try {
            return { ...fallback, ...JSON.parse(dataEl.textContent || "{}") };
        } catch (e) {
            console.error("[UserListApp] user_label_data parse failed", e);
            return fallback;
        }
    },
};
