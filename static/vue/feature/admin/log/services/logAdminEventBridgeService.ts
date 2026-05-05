type LogAdminBridgeActions = {
    searchLogs: () => void;
    downloadLogsAsExcel: () => void;
};

declare global {
    interface Window {
        LogAdminBridge?: {
            searchLogs: () => void;
            downloadLogsAsExcel: () => void;
        };
    }
}

export default function bindLogAdminEventBridge(actions: LogAdminBridgeActions): void {
    window.LogAdminBridge = {
        searchLogs(): void {
            actions.searchLogs();
        },
        downloadLogsAsExcel(): void {
            actions.downloadLogsAsExcel();
        },
    };
}

