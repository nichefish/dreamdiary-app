/**
 * noticeListActionService.ts
 * 공지사항 목록 화면 액션 서비스
 */
export type NoticeListActions = {
    search: () => void;
    myPaprList: () => void;
    xlsxDownload: () => void;
    registForm: () => void;
    detailModal: (id: string | number) => void;
};

export default function createNoticeListActions(): NoticeListActions {
    return {
        search(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", Url.NOTICE_LIST + "?actionTyCd=SEARCH");
        },
        myPaprList(): void {
            const param = `?searchType=nickname&searchKeyword=${AuthInfo.nickname!}&createdBy=${AuthInfo.username!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.ui.blockUIReplace(Url.NOTICE_LIST + param);
        },
        xlsxDownload(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.NOTICES_XLSX_DOWNLOAD).submit();
            });
        },
        registForm(): void {
            cF.form.blockUISubmit("#procForm", Url.NOTICE_REGIST_FORM);
        },
        detailModal(id: string | number): void {
            if (isNaN(Number(id))) return;
            const e = window.event as Event | undefined;
            if (e?.stopPropagation) e.stopPropagation();
            cF.ajax.get(cF.util.bindUrl(Url.NOTICE, { id }), null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "notice_detail");
            });
        },
    };
}
