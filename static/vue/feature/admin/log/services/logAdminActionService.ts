type LogAdminActions = {
    searchLogs: () => void;
    downloadLogsAsExcel: () => void;
    openLogDetailModal: (logId: number) => void;
};

export default function createLogAdminActions(): LogAdminActions {
    return {
        searchLogs(): void {
            const pageNoElement: HTMLInputElement | null = document.querySelector("#listForm #pageNo");
            if (pageNoElement) pageNoElement.value = "1";
            cF.form.blockUISubmit("#listForm", `${Url.LOG_LIST!}?actionTyCd=SEARCH`);
        },
        downloadLogsAsExcel(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                window.jQuery("#listForm").attr("action", Url.LOG_LIST_XLSX_DOWNLOAD).trigger("submit");
            });
        },
        openLogDetailModal(logId: number): void {
            if (Number.isNaN(Number(logId))) return;
            const url: string = cF.util.bindUrl(Url.LOG, { id: logId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "log_detail");
            });
        },
    };
}

