/**
 * user_module.ts
 * 사용자 관리 페이지 스크립트
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.User = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.User.initialized) return;

            dF.User.initialized = true;
            console.log("'dF.User' module initialized.");
        },

        /**
         * 등록/수정 화면 모드 여부
         */
        isMdf: function(): boolean {
            return $("#userRegForm").data("mode") === "modify";
        },

        /**
         * 페이지에 포함된 사용자 등록 폼 템플릿 렌더링
         */
        renderRegFormFromPageData: function(): void {
            cF.handlebars.template({}, "user_reg_form");
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* jquery validation */
            cF.validate.validateForm("#userReqstForm", dF.UserSignup.submitHandler, {
                rules: {
                    username: { minlength: 4, maxlength: 16 },
                    ipDupChckPassed: { dupChck: true },
                    emailDupChckPassed: { dupChck: true },
                    password: { minlength: 9, maxlength: 20, regex: cF.regex.pw },
                    passwordCf: { equalTo: "#password", maxlength: 20 },
                },
                messages: {
                    ipDupChckPassed: { dupChck: "아이디 중복 체크는 필수 항목입니다." },
                    emailDupChckPassed: { dupChck: "이메일 중복 체크는 필수 항목입니다." },
                    password: { regex: "비밀번호가 형식에 맞지 않습니다." },
                    passwordCf: { equalTo: "비밀번호에 입력한 값과 동일하게 입력해야 합니다." },
                },
            });
            $.validator.addMethod("dupChck", function(value: string): boolean {
                return (value === "Y");
            });
            // 자동 대문자->소문자처리
            cF.validate.toLowerCase("#username");
            // 연락처 포맷
            cF.validate.phoneNumber("#phoneNumber");
            // 권한 변경시 필드 재검증
            $("#roleKey").change(function(): void {
                $("#roleKey").valid();
            });
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#username").on("input", function(): void {
                $("#username_validate_span").empty();
                $("#ipDupChckPassed").val("N");
                $("#idDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
            });
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailId").on("input", function(): void {
                $("#emailId_validate_span").empty();
                $("#emailDupChckPassed").val("N");
                $("#emailDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
            });
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailDomain").on("input", function(): void {
                $("#emailDomain_validate_span").empty();
                $("#emailDupChckPassed").val("N");
                $("#emailDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
            });
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailDomainSelect").on("change", function(): void {
                $("#emailDomain_validate_span").empty();
                $("#emailDupChckPassed").val("N");
                $("#emailDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
            });
            // 접속IP 사용 여부 클릭시 글씨 변경 + 입력창 토글 :: 메소드 분리
            cF.ui.chckboxLabel("#useAllowedIpYn", "사용//미사용", "blue//gray", function(): void{
                $("#allowedIpListSpan").show()
            }, function(){
                $("#allowedIpListSpan").hide()
            });
            /* 접속IP tagify */
            cF.tagify.init("#allowedIpListStr");
        },

        /**
         * Custom SubmitHandler
         */
        submitHandler: function(): boolean {
            if ($("#useAllowedIpYn").is(":checked") && $("#allowedIpListStr").val() === "") {
                Swal.fire("접속 IP는 최소 한 개 이상 입력해야 합니다.");
                return false;
            }
            Swal.fire({
                text: dF.User.isMdf() ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
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
            cF.form.blockUISubmit("#listForm", Url.USER_LIST + "?actionTyCd=SEARCH");
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
            cF.form.blockUISubmit("#procForm", Url.USER_REG_FORM);
        },

        /**
         * 아이디 중복 체크(Ajax)
         */
        idDupChckAjax: function(): boolean {
            const usernameValidSpan = $("#username_validate_span");
            const username: string = cF.util.getInputValue("#username");
            if (!cF.regex.id.test(username)) {
                usernameValidSpan.text("아이디가 형식에 맞지 않습니다.").removeClass("text-success").addClass("text-danger");
                return false;
            }

            const url: string = Url.USERNAME_DUP_CHK_AJAX;
            const ajaxData: Record<string, any> = { "username": username };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                usernameValidSpan.text(res.message);
                if (res.rslt) {
                    usernameValidSpan.removeClass("text-danger").addClass("text-success");
                    $("#ipDupChckPassed").val("Y");
                    $("#ipDupChckPassed_validate_span").text("");
                    $("#idDupChckBtn").removeClass("blink").addClass("btn-success").removeClass("btn-secondary").attr("disabled", "disabled");
                } else {
                    usernameValidSpan.removeClass("text-success").addClass("text-danger");
                    $("#ipDupChckPassed").val("N");
                }
            });
        },

        /**
         * 이메일 중복 체크(Ajax)
         */
        emailDupChckAjax: function(): boolean {
            const emailValidSpan = $("#emailId_validate_span");
            const emailId: string = cF.util.getInputValue("#emailId");
            const emailDomain: string =  cF.util.getInputValue("#emailDomain");
            const email: string = emailId + "@" + emailDomain || "";
            if (!cF.regex.email.test(email)) {
                emailValidSpan.text("이메일이 형식에 맞지 않습니다.").removeClass("text-success").addClass("text-danger");
                return false;
            }

            const url: string = Url.USER_EMAIL_DUP_CHK_AJAX;
            const ajaxData: Record<string, any> = { "email": email };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                emailValidSpan.text(res.message);
                if (res.rslt) {
                    emailValidSpan.removeClass("text-danger").addClass("text-success");
                    $("#emailDupChckPassed").val("Y");
                    $("#emailDupChckPassed_validate_span").text("");
                    $("#emailDupChckBtn").removeClass("blink").addClass("btn-success").removeClass("btn-secondary").attr("disabled", "disabled");
                } else {
                    emailValidSpan.removeClass("text-success").addClass("text-danger");
                    $("#emailDupChckPassed").val("N");
                }
            });
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = dF.User.isMdf() ? Url.USER_MDF_AJAX : Url.USER_REG_AJAX;
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
         * @param {string|number} id - 사용자 번호
         */
        dtl: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            $("#procForm #id").val(id);
            cF.form.blockUISubmit("#procForm", Url.USER_DTL);
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

                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                const reqId = ajaxData.id;
                if (reqId === undefined || reqId === null || String(reqId).trim() === "") return;
                const url: string = Url.USER_SIGNUP_REQUEST_APPROVAL.replace("{id}", String(reqId));
                // 변경 후: 신청 id 는 경로 변수만 사용. 변경 전: 본문(JSON)에도 id 를 실어 보냄.
                cF.$ajax.post(url, {}, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 신청 승인 처리 (Ajax)
         * @param {string|number} reqId - 가입 신청 ID
         */
        cfReqAjax: function(reqId: string|number): void {
            if (isNaN(Number(reqId))) return;
            $("#procForm #id").val(reqId);
            dF.User.cfAjax();
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

                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#procForm");
                const reqId = ajaxData.id;
                if (reqId === undefined || reqId === null || String(reqId).trim() === "") return;
                const url: string = Url.USER_SIGNUP_REQUEST_REJECTION.replace("{id}", String(reqId));
                // 변경 후: 신청 id 는 경로 변수만 사용. 변경 전: 본문(JSON)에도 id 를 실어 보냄.
                cF.$ajax.post(url, {}, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 신청 반려 처리 (Ajax)
         * @param {string|number} reqId - 가입 신청 ID
         */
        uncfReqAjax: function(reqId: string|number): void {
            if (isNaN(Number(reqId))) return;
            $("#procForm #id").val(reqId);
            dF.User.uncfAjax();
        },

        /**
         * 패스워드 초기화 (Ajax)
         */
        pwResetAjax: function(): void {
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
        mdfForm: function(): void {
            cF.form.blockUISubmit("#procForm", Url.USER_MDF_FORM);
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
            const listUrl: string = Url.USER_LIST + (dF.User.isMdf() ? "?isBackToList=Y" : "");
            cF.ui.blockUIReplace(listUrl);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.User.init();
});
