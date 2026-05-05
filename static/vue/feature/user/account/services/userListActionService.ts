import { UserListActions } from "../types.js";

export default function createUserListActions(): UserListActions {
    function setListFormPaging(pageNo: number, pageSize?: number): void {
        $("#listForm #pageNo").val(pageNo);
        if (pageSize !== undefined) $("#listForm #pageSize").val(pageSize);
    }

    return {
        /**
         * 목록 페이지 이동
         */
        page(pageNo: number, pageSize?: number): void {
            setListFormPaging(pageNo, pageSize);
            cF.form.blockUISubmit("#listForm", Url.USER_LIST);
        },
        /**
         * 목록 검색
         */
        search(): void {
            setListFormPaging(1);
            cF.form.blockUISubmit("#listForm", Url.USER_LIST + "?actionTyCd=SEARCH");
        },
        /**
         * 엑셀 다운로드
         */
        xlsxDownload(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.util.blockUIFileDownload();
                $("#listForm").attr("action", Url.USER_LIST_XLSX_DOWNLOAD).submit();
            });
        },
        /**
         * 등록 화면으로 이동
         */
        regForm(): void {
            cF.form.blockUISubmit("#procForm", Url.USER_REG_FORM);
        },
        /**
         * 상세 화면으로 이동
         * @param id 사용자 번호
         */
        dtl(id: string | number): void {
            if (isNaN(Number(id))) return;

            $("#procForm #id").val(id);
            cF.form.blockUISubmit("#procForm", Url.USER_DTL);
        },
    };
}
