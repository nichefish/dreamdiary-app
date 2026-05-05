import { UserDetailActions } from "../types.js";

export default function createUserDetailActions(): UserDetailActions {
    return {
        /**
         * 패스워드 초기화 (Ajax)
         */
        pwResetAjax(): void {
            Swal.fire({
                text: Message.get("view.cnfm.reset-pw"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.USER_PW_RESET_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },
        /**
         * 수정 화면으로 이동
         */
        mdfForm(): void {
            cF.form.blockUISubmit("#procForm", Url.USER_MDF_FORM);
        },
        /**
         * 삭제 (Ajax)
         */
        delAjax(): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                const url: string = Url.USER_DEL_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReplace(Url.USER_LIST);
                        });
                }, "block");
            });
        },
        /**
         * 목록 화면으로 이동
         */
        list(): void {
            cF.ui.blockUIReplace(Url.USER_LIST);
        },
    };
}
