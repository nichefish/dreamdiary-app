/**
 * user_my_module.ts
 * 사용자 마이페이지 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.UserMy = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.UserMy.initialized) return;

            dF.UserMy.initialized = true;
            console.log("'dF.UserMy' module initialized.");
        },

        /**
         * 프로필 이미지 첨부
         */
        uploadProflImg: function(): void {
            const $fileInput: JQuery<HTMLInputElement> = $("#atchFile0");
            $fileInput.click();
            $fileInput.on("change", function(): void {
                if ((this as HTMLInputElement).value === "") return;

                if (!cF.validate.fileSizeChck(this)) return;      // fileSizeChck
                if (!cF.validate.fileImgExtnChck(this)) return;      // fileExtnChck

                const url: string = Url.USER_MY_UPLOAD_PROFL_IMG_AJAX;
                const ajaxData: FormData = new FormData(document.getElementById("profllImgForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                    if (cF.util.isEmpty(res.message)) return;

                    Swal.fire({ "text": res.message })
                        .then(function(): void {
                            Swal.fire({ "text": "변경된 프로필은 재접속 이후에 적용됩니다." })
                                .then(function(): void {
                                    if (res.rslt) cF.ui.blockUIReload();
                                });
                        });
                }, "block");
            });
        },

        /**
         * 프로필 이미지 삭제
         */
        removeProflImg: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.USER_MY_REMOVE_PROFL_IMG_AJAX;
                cF.$ajax.multipart(url, null, function(res: AjaxResponse): void {
                    if (cF.util.isEmpty(res.message)) return;

                    Swal.fire({ "text": res.message })
                        .then(function(): void {
                            Swal.fire({ "text": "변경된 프로필은 재접속 이후에 적용됩니다." })
                                .then(function(): void {
                                    if (res.rslt) cF.ui.blockUIReload();
                                });
                        });
                }, "block");
            });
        },

        /**
         * TODO: 원본 프로필 이미지 조회
         */
        viewOrgnlProflImg: function(): void {
            //
        },

        /**
         * 내 휴가 사용현황 조회
         */
        myVcatnList: function(): void {
            $("#user_my_dtl_vcatn_list").modal("show");
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.UserMy.init();
});