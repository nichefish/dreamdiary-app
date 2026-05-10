import { LogStatsUserRow } from "../types.js";

function parseRows(scriptId: string): LogStatsUserRow[] {
    const dataElement = document.getElementById(scriptId);
    if (!dataElement) return [];
    try {
        const parsed: unknown = JSON.parse(dataElement.textContent || "[]");
        return Array.isArray(parsed) ? (parsed as LogStatsUserRow[]) : [];
    } catch (error) {
        console.error("[LogStatsUserPlaceholderApp] " + scriptId + " parse failed", error);
        return [];
    }
}

export default {
    parseUserRows(): LogStatsUserRow[] {
        return parseRows("log_stats_user_list_data");
    },
    parseAnonymousRows(): LogStatsUserRow[] {
        return parseRows("log_stats_not_user_list_data");
    },
};
