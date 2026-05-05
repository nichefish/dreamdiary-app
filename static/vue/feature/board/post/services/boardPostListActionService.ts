/**
 * boardPostListActionService.ts
 * 일반게시판 목록 화면 액션 서비스
 */
export type BoardPostListActions = {
    init: () => void;
    search: () => void;
    myPaprList: () => void;
    xlsxDownload: () => void;
    regForm: () => void;
    dtl: (id: string | number) => void;
    dtlModal: (id: string | number) => void;
    mdfForm: () => void;
    delAjax: (id: string | number) => void;
    list: () => void;
};

export default function createBoardPostListActions(): BoardPostListActions {
    return {
        init(): void {},
        search(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", Url.BOARD_POST_LIST + "?actionTyCd=SEARCH");
        },
        myPaprList(): void {
            const contentTypeElement: HTMLInputElement | null = document.querySelector("#contentType");
            if (!contentTypeElement) return;

            const contentType: string = contentTypeElement.value;
            const param: string = `?contentType=${contentType}&searchType=nickname&searchKeyword=${AuthInfo.nickname!}&createdBy=${AuthInfo.username!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.ui.blockUIReplace(Url.BOARD_POST_LIST + param);
        },
        xlsxDownload(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.BOARD_POST_LIST).submit();
            });
        },
        regForm(): void {
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_REG_FORM);
        },
        dtl(id: string | number): void {
            if (isNaN(Number(id))) return;
            $("#procForm #id").val(id);
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_DTL);
        },
        dtlModal(id: string | number): void {
            if (isNaN(Number(id))) return;
            const e = window.event as Event | undefined;
            if (e?.stopPropagation) e.stopPropagation();

            const ajaxData: Record<string, any> = { id, contentType: $("#contentType").val() };
            cF.ajax.get(cF.util.bindUrl(Url.BOARD_POST, { id }), ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltObj, "board_post_dtl");
            });
        },
        mdfForm(): void {
            cF.form.blockUISubmit("#procForm", Url.BOARD_POST_MDF_FORM);
        },
        delAjax(id: string | number): void {
            if (isNaN(Number(id))) return;
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(cF.util.bindUrl(Url.BOARD_POST, { id }), ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (res.rslt) cF.ui.blockUIReplace(Url.BOARD_POST_LIST);
                    });
                }, "block");
            });
        },
        list(): void {
            const contentType: string = $("#contentType").val() as string;
            cF.ui.blockUIReplace(`${Url.BOARD_POST_LIST}?contentType=${contentType}`);
        },
    };
}
