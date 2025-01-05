/**
 * log_actvty_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.LogActvty = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'LogActvty' module initialized.");
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", `${Url.LOG_ACTVTY_LIST!}?actionTyCd=SEARCH`);
        },
        /**
         * 엑셀 다운로드
         */
        xlsxDownload: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.LOG_ACTVTY_LIST_XLSX_DOWNLOAD).submit();
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} logActvtyNo - 조회할 로그 번호.
         */
        dtlModal: function(logActvtyNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(logActvtyNo))) return;

            const url: string = Url.LOG_ACTVTY_DTL_AJAX;
            const ajaxData: Record<string, any> = { "logActvtyNo": logActvtyNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltObj, "log_actvty_dtl", "show");
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.LogActvty.init();
});