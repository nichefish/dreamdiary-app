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

                const id = cF.util.getInputValue("#procForm #id");
                const url: string = cF.util.bindUrl(Url.USER_PASSWORD_RESET, { id });
                cF.$ajax.post(url, null, function(res: AjaxResponse): void {
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
                const id = cF.util.getInputValue("#procForm #id");
                const url: string = cF.util.bindUrl(Url.USER, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
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
