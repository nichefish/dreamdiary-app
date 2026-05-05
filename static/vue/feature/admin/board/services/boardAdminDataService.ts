import { BoardRow, PaginationState } from "../types.js";
import { applyPaginationFromPageData } from "../../../../global/services/paginationDataService.js";

export default {
    parseRowsFromPageData(): BoardRow[] {
        const dataEl: HTMLElement | null = document.getElementById("board_group_list_data");
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as BoardRow[]) : [];
        } catch (e) {
            console.error("[BoardAdminApp] board_group_list_data parse failed", e);
            return [];
        }
    },
    applyPaginationFromPageData(pagination: PaginationState): void {
        applyPaginationFromPageData("board_group_pagination_data", pagination, "[BoardAdminApp]");
    },
};
