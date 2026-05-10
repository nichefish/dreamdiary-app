import { resolveMessage } from "../../../../common/messageHelper.js";

type LogStatsUserActions = {
    logList: () => void;
    search: () => void;
    downloadLogsAsExcel: () => void;
};

export default function createLogStatsUserActions(): LogStatsUserActions {
    return {
        logList(): void {
            cF.form.blockUISubmit("#listForm", Url.LOG_LIST);
        },
        search(): void {
            const pageNoElement: HTMLInputElement | null = document.querySelector("#listForm #pageNo");
            if (pageNoElement) pageNoElement.value = "1";
            cF.form.blockUISubmit("#listForm", `${Url.LOG_STATS_USER_LIST!}?actionTyCd=SEARCH`);
        },
        downloadLogsAsExcel(): void {
            Swal.fire({
                text: resolveMessage("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.LOG_LIST_XLSX_DOWNLOAD).submit();
            });
        },
    };
}
