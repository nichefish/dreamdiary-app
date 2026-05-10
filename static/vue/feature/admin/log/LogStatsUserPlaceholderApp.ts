/**
 * 로그 통계(사용자별) placeholder Vue 화면.
 * 서버 기능이 아직 완성되지 않은 화면이라, 현재 넘어온 데이터만 얇게 표시한다.
 */
import LogStatsUserPlaceholderTable from "./components/LogStatsUserPlaceholderTable.js";
import logStatsUserDataService from "./services/logStatsUserDataService.js";
import createLogStatsUserActions from "./services/logStatsUserActionService.js";
import bindLogStatsUserEventBridge from "./services/logStatsUserEventBridgeService.js";
import { LogStatsUserRow } from "./types.js";

const state = Vue.reactive({
    userRows: [],
    anonymousRows: [],
}) as { userRows: LogStatsUserRow[]; anonymousRows: LogStatsUserRow[] };

const actions = createLogStatsUserActions();
bindLogStatsUserEventBridge(actions);

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const LogStatsUserPlaceholderRootApp = {
    name: "LogStatsUserPlaceholderRootApp",
    components: {
        LogStatsUserPlaceholderTable,
    },
    data(): { state: { userRows: LogStatsUserRow[]; anonymousRows: LogStatsUserRow[] } } {
        return { state };
    },
    template: `
    <teleport to="#log_stats_user_list_body">
        <LogStatsUserPlaceholderTable
            :user-rows="state.userRows"
            :anonymous-rows="state.anonymousRows"
        />
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    state.userRows = logStatsUserDataService.parseUserRows();
    state.anonymousRows = logStatsUserDataService.parseAnonymousRows();

    if (!document.getElementById("log_stats_user_app") || !document.getElementById("log_stats_user_list_body")) {
        console.error("[LogStatsUserPlaceholderApp] Vue mount root not found.");
        return;
    }

    Vue.createApp(LogStatsUserPlaceholderRootApp).mount("#log_stats_user_app");
    cF.table.initSort();
});
