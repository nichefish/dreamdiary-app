import { AdminPageMeta, RoleRow } from "../types.js";

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
};
