/**
 * log_module.ts
 * 로그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Log = (function(): dfModule {
    return {
        initialized: false,

        init: function(): void {
            if (dF.Log.initialized) return;
            dF.Log.initialized = true;
            console.log("'dF.Log' module initialized.");
        },

        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", `${Url.LOG_LIST!}?actionTyCd=SEARCH`);
        },

        xlsxDownload: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.LOG_LIST_XLSX_DOWNLOAD).submit();
            });
        },

        dtlModal: function(logId: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(logId))) return;

            const url: string = Url.LOG_DTL_AJAX;
            const ajaxData: Record<string, any> = { logId };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "log_dtl");
            });
        },
    }
})();
