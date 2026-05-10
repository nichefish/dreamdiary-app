/**
 * 로그 관리 화면 Vue 앱
 *
 * @author nichefish
 */
import LogListTable from "./components/LogListTable.js";
import LogDetailModalBody from "./components/LogDetailModalBody.js";
import logAdminDataService from "./services/logAdminDataService.js";
import createLogAdminActions from "./services/logAdminActionService.js";
import bindLogAdminEventBridge from "./services/logAdminEventBridgeService.js";
import { LogDetail, LogListRow } from "./types.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

const state = Vue.reactive({
    rows: [],
    detail: null,
}) as { rows: LogListRow[]; detail: LogDetail | null };
const i18n = createScopedI18n();
const actions = createLogAdminActions(function(detail: LogDetail): void {
    state.detail = detail;
});
bindLogAdminEventBridge(actions);

function t(key: string): string {
    return i18n.t(key);
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveLogAdminPageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const locale = w?.Model?.locale;
    if (locale) return locale;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

const LogAdminRootApp = {
    name: "LogAdminRootApp",
    components: {
        LogListTable,
        LogDetailModalBody,
    },
    data(): { state: { rows: LogListRow[]; detail: LogDetail | null } } {
        return { state };
    },
    methods: {
        onOpenDetail(logId: number): void {
            actions.openLogDetailModal(logId);
        },
    },
    template: `
    <teleport to="#log_list_body">
        <LogListTable
            :rows="state.rows"
            @open-detail="onOpenDetail"
        />
    </teleport>
    <teleport to="#log_detail_div">
        <LogDetailModalBody :detail="state.detail" />
    </teleport>
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveLogAdminPageLocale());
    state.rows = logAdminDataService.parseRowsFromPageData();

    if (!document.getElementById("log_admin_app") || !document.getElementById("log_list_body") || !document.getElementById("log_detail_div")) {
        console.error("[LogAdminApp] Vue mount root not found.");
        return;
    }

    const app = Vue.createApp(LogAdminRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#log_admin_app");

    cF.table.initSort();
});

