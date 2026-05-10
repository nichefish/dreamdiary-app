type LogAdminBridgeActions = {
    searchLogs: () => void;
    downloadLogsAsExcel: () => void;
    openLogDetailModal: (logId: number) => void;
};

declare global {
    interface Window {
        LogAdminBridge?: {
            searchLogs: () => void;
            downloadLogsAsExcel: () => void;
            openLogDetailModal: (logId: number) => void;
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
        openLogDetailModal(logId: number): void {
            actions.openLogDetailModal(logId);
        },
    };
}

