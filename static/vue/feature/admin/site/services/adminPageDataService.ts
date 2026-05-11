import { AdminPageMeta, EmbeddingStats, RoleRow } from "../types.js";

const DEFAULT_META: AdminPageMeta = {
    authMngrKey: "MNGR",
    authUserKey: "USER",
    authDevKey: "DEV",
    currYy: new Date().getFullYear(),
};

export default {
    parseMeta(): AdminPageMeta {
        const dataEl: HTMLElement | null = document.getElementById("admin_page_meta_data");
        if (!dataEl) return DEFAULT_META;
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "{}");
            const o = parsed as Partial<AdminPageMeta>;
            return {
                authMngrKey: String(o.authMngrKey || DEFAULT_META.authMngrKey),
                authUserKey: String(o.authUserKey || DEFAULT_META.authUserKey),
                authDevKey: String(o.authDevKey || DEFAULT_META.authDevKey),
                currYy: Number(o.currYy || DEFAULT_META.currYy),
            };
        } catch (e) {
            console.error("[AdminPageApp] admin_page_meta_data parse failed", e);
            return DEFAULT_META;
        }
    },

    parseRoles(): RoleRow[] {
        const dataEl: HTMLElement | null = document.getElementById("admin_page_role_list_data");
        if (!dataEl) return [];
        try {
            const parsed: unknown = JSON.parse(dataEl.textContent || "[]");
            return Array.isArray(parsed) ? (parsed as RoleRow[]) : [];
        } catch (e) {
            console.error("[AdminPageApp] admin_page_role_list_data parse failed", e);
            return [];
        }
    },

    async fetchEmbeddingStats(): Promise<EmbeddingStats> {
        const urlMap = globalThis as { Url?: { ADMIN_JOURNAL_ENTRY_EMBEDDING_STATS?: string } };
        const url = urlMap.Url?.ADMIN_JOURNAL_ENTRY_EMBEDDING_STATS || "/api/admin/journal-entry-embeddings/stats";
        const response = await fetch(url, { credentials: "same-origin" });
        if (!response.ok) throw new Error(`Embedding stats request failed: ${response.status}`);

        const payload = await response.json() as AjaxResponse;
        if (!payload.rslt) throw new Error(payload.message || "Embedding stats request failed");

        return normalizeEmbeddingStats(payload.rsltObj as Partial<EmbeddingStats>);
    },
};

export function createEmptyEmbeddingStats(): EmbeddingStats {
    return normalizeEmbeddingStats({});
}

function normalizeEmbeddingStats(stats: Partial<EmbeddingStats> | null | undefined): EmbeddingStats {
    return {
        total: Number(stats?.total || 0),
        pending: Number(stats?.pending || 0),
        processing: Number(stats?.processing || 0),
        embedded: Number(stats?.embedded || 0),
        failed: Number(stats?.failed || 0),
        skipped: Number(stats?.skipped || 0),
        remaining: Number(stats?.remaining || 0),
        completed: Number(stats?.completed || 0),
        completionRate: Number(stats?.completionRate || 0),
        vectorizedRate: Number(stats?.vectorizedRate || 0),
    };
}
