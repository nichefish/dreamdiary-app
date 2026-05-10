import { PaginationState, UserLabels, UserRow } from "../types.js";
import { applyPaginationFromPageData } from "../../../../global/services/paginationDataService.js";

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
        applyPaginationFromPageData("user_pagination_data", pagination, "[UserListApp]");
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
