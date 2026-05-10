// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
import { resolveMessage } from "../../../../common/messageHelper.js";
import { LogDetail } from "../types.js";

type LogAdminActions = {
    searchLogs: () => void;
    downloadLogsAsExcel: () => void;
    openLogDetailModal: (logId: number) => void;
};

function showLogDetailModal(): void {
    const modalEl = document.getElementById("log_detail_modal");
    if (!modalEl) return;
    const bootstrapRef = (window as any).bootstrap;
    if (bootstrapRef?.Modal?.getOrCreateInstance) {
        bootstrapRef.Modal.getOrCreateInstance(modalEl).show();
        return;
    }
    $("#log_detail_modal").modal("show");
}

export default function createLogAdminActions(onLogDetailLoaded?: (detail: LogDetail) => void): LogAdminActions {
    return {
        searchLogs(): void {
            const pageNoElement: HTMLInputElement | null = document.querySelector("#listForm #pageNo");
            if (pageNoElement) pageNoElement.value = "1";
            cF.form.blockUISubmit("#listForm", `${Url.LOG_LIST!}?actionTyCd=SEARCH`);
        },
        downloadLogsAsExcel(): void {
            Swal.fire({
                text: resolveMessage("view.cnfm.download"),
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
                if (onLogDetailLoaded) onLogDetailLoaded((res.rsltObj || {}) as LogDetail);
                requestAnimationFrame(showLogDetailModal);
            });
        },
    };
}
