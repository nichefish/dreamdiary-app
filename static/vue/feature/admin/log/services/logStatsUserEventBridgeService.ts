type LogStatsUserBridgeActions = {
    logList: () => void;
    search: () => void;
    downloadLogsAsExcel: () => void;
};

declare global {
    interface Window {
        LogStatsUserBridge?: {
            logList: () => void;
            search: () => void;
            downloadLogsAsExcel: () => void;
        };
    }
}

export default function bindLogStatsUserEventBridge(actions: LogStatsUserBridgeActions): void {
    window.LogStatsUserBridge = {
        logList(): void {
            actions.logList();
        },
        search(): void {
            actions.search();
        },
        downloadLogsAsExcel(): void {
            actions.downloadLogsAsExcel();
        },
    };
}
