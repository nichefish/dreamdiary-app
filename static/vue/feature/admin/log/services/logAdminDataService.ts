import { LogListRow } from "../types.js";

export default {
    parseRowsFromPageData(): LogListRow[] {
        const dataElement: HTMLElement | null = document.getElementById("log_list_data");
        if (!dataElement) return [];
        try {
            const parsed: unknown = JSON.parse(dataElement.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as LogListRow[]) : [];
        } catch (error) {
            console.error("[LogAdminApp] log_list_data parse failed", error);
            return [];
        }
    },
};

