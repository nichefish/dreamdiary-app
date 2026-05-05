import { CodeGroupRow, PaginationState } from "../types.js";
import { applyPaginationFromPageData } from "../../../../global/services/paginationDataService.js";

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
        applyPaginationFromPageData("code_group_pagination_data", pagination, "[CodeAdminApp]");
    },
};

