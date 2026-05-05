export default function createUserMyActions(t: (key: string) => string) {
    function uploadProflImg(): void {
        const $fileInput: JQuery<HTMLInputElement> = $("#fileGroup0");
        $fileInput.off("change.userMyVue").on("change.userMyVue", function(): void {
            if ((this as HTMLInputElement).value === "") return;

            if (!cF.validate.fileSizeChck(this)) return;      // fileSizeChck
            if (!cF.validate.fileImgExtnChck(this)) return;      // fileExtnChck

            const url: string = Url.USER_MY_UPLOAD_PROFL_IMG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("profllImgForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                if (cF.util.isEmpty(res.message)) return;

                Swal.fire({ text: res.message })
                    .then(function(): void {
                        Swal.fire({ text: t("msg.user.my.changed-profile-notice") })
                            .then(function(): void {
                                if (res.rslt) cF.ui.blockUIReload();
                            });
                    });
            }, "block");
        });
        $fileInput.trigger("click");
    }

    function removeProflImg(): void {
        Swal.fire({
            text: Message.get("view.cnfm.del"),
            showCancelButton: true,
        }).then(function(result: SwalResult): void {
            if (!result.value) return;

            const url: string = Url.USER_MY_REMOVE_PROFL_IMG_AJAX;
            cF.$ajax.multipart(url, null, function(res: AjaxResponse): void {
                if (cF.util.isEmpty(res.message)) return;

                Swal.fire({ text: res.message })
                    .then(function(): void {
                        Swal.fire({ text: t("msg.user.my.changed-profile-notice") })
                            .then(function(): void {
                                if (res.rslt) cF.ui.blockUIReload();
                            });
                    });
            }, "block");
        });
    }

    function initPwChangeForm(): void {
        /* jquery validation */
        cF.validate.validateForm("#myPwChgForm", submitPwChangeHandler, {
            rules: {
                newPw: { regex: cF.regex.pw },
                newPwCf: { equalTo: "#newPw" },
            },
            messages: {
                newPw: { regex: "변경할 비밀번호가 형식에 맞지 않습니다." },
                newPwCf: { equalTo: "변경할 비밀번호에 입력한 값과 동일하게 입력해야 합니다." },
            },
        });
    }

    function openPwChangeModal(): void {
        (document.getElementById("myPwChgForm") as HTMLFormElement | null)?.reset();
        $("#user_my_pw_chg").modal("show");
    }

    function submitPwChange(): void {
        $("#myPwChgForm").submit();
    }

    function submitPwChangeHandler(): void {
        Swal.fire({
            text: Message.get("view.cnfm.chg-pw"),
            showCancelButton: true,
        }).then(function(result: SwalResult): void {
            if (!result.value) return;
            myPwChgAjax();
        });
    }

    function myPwChgAjax(): void {
        const url: string = Url.USER_MY_PW_CHG_AJAX;
        const ajaxData: Record<string, any> = {
            username: AuthInfo.username,
            currPw: $("#currPw").val(),
            newPw: $("#newPw").val(),
        };
        cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (res.rslt) cF.ui.blockUIReload();
                });
        });
    }

    return {
        uploadProflImg,
        removeProflImg,
        initPwChangeForm,
        openPwChangeModal,
        submitPwChange,
    };
}
