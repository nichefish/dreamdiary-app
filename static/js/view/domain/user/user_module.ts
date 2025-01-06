/**
 * user_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.User = (function(): Module {
    return {
        isMdf: $("#userRegForm").data("mode") === "modify",

        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'User' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* jquery validation */
            cF.validate.validateForm("#userRegForm", dF.User.submitHandler);
            $.validator.addMethod("dupChck", function(value: string): boolean {
                return (value === "Y");
            });
            // 자동 대문자->소문자처리
            cF.validate.toLowerCase("#userId");
            // 연락처 포맷
            cF.validate.cttpc("#cttpc");
            // 권한 변경시 필드 재검증
            $("#authCd").change(function(): void {
                $("#authCd").valid();
            });
            // 접속IP 사용 여부 클릭시 글씨 변경 + 입력창 토글 :: 메소드 분리
            cF.util.chckboxLabel("useAcsIpYn", "사용//미사용", "blue//gray", function(): void{
                $("#acsIpListSpan").show()
            }, function(){
                $("#acsIpListSpan").hide()
            });
            /* 접속IP tagify */
            cF.tagify.init("#acsIpListStr");
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if ($("#useAcsIpYn").is(":checked") && $("#acsIpListStr").val() === "") {
                Swal.fire("접속 IP는 최소 한 개 이상 입력해야 합니다.");
                return false;
            }
            Swal.fire({
                text: dF.User.isMdf ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                dF.User.regAjax();
            });
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", Url.USER_LIST + "?actionTyCd=SEARCH");
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
                $("#listForm").attr("action", Url.USER_LIST_XLSX_DOWNLOAD).submit();
            });
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.USER_REG_FORM);
        },

        /**
         * 아이디 중복 체크(Ajax)
         */
        idDupChckAjax: function(): boolean {
            const userIdValidSpan = $("#userId_validate_span");
            const userIdElement: HTMLInputElement = document.getElementById("userId") as HTMLInputElement;
            const userId: string = userIdElement.value || "";
            if (!cF.regex.id.test(userId)) {
                userIdValidSpan.text("아이디가 형식에 맞지 않습니다.").removeClass("text-success").addClass("text-danger");
                return false;
            }

            const url: string = Url.USER_ID_DUP_CHK_AJAX;
            const ajaxData: Record<string, any> = { "userId": userId };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                userIdValidSpan.text(res.message);
                if (res.rslt) {
                    userIdValidSpan.removeClass("text-danger").addClass("text-success");
                    $("#ipDupChckPassed").val("Y");
                    $("#ipDupChckPassed_validate_span").text("");
                    $("#idDupChckBtn").removeClass("blink").addClass("btn-success").removeClass("btn-secondary").attr("disabled", "disabled");
                } else {
                    userIdValidSpan.removeClass("text-success").addClass("text-danger");
                    $("#ipDupChckPassed").val("N");
                }
            });
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = dF.User.isMdf ? Url.USER_MDF_AJAX : Url.USER_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("userRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) dF.User.list();
                    });
            }, "block");
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} userNo - 사용자 번호
         */
        dtl: function(userNo: string|number): void {
            if (isNaN(Number(userNo))) return;

            $("#procForm #userNo").val(userNo);
            cF.util.blockUISubmit("#procForm", Url.USER_DTL);
        },

        /**
         * 승인 처리 (Ajax)
         */
        cfAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.cf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.USER_REQST_CF_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 승인취소 처리 (Ajax)
         */
        uncfAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.uncf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.USER_REQST_UNCF_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 패스워드 초기화 (Ajax)
         */
        pwResetAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.resetPw"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = Url.USER_PW_RESET_AJAX;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 수정 화면으로 이동
         */
        mdfForm: function(): void {
            cF.util.blockUISubmit("#procForm", Url.USER_MDF_FORM);
        },

        /**
         * 삭제 (Ajax)
         */
        delAjax: function(): void {
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
                            if (res.rslt) dF.User.list();
                        });
                }, "block");
            });
        },

        /**
         * 목록 화면으로 이동
         */
        list: function(): void {
            const listUrl: string = Url.USER_LIST + (dF.User.isMdf ? "?isBackToList=Y" : "");
            cF.util.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.User.init();
});