import { UserFormActions } from "../types.js";

export default function createUserFormActions(): UserFormActions {
    function isMdf(): boolean {
        return $("#userRegForm").data("mode") === "modify";
    }

    function resetUsernameDupCheck(): void {
        $("#username_validate_span").empty();
        $("#ipDupChckPassed").val("N");
        $("#idDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
    }

    function resetEmailDupCheck(): void {
        $("#emailId_validate_span").empty();
        $("#emailDomain_validate_span").empty();
        $("#emailDupChckPassed").val("N");
        $("#emailDupChckBtn").addClass("blink").removeClass("btn-success").addClass("btn-secondary").removeAttr("disabled");
    }

    return {
        /**
         * 등록/수정 화면 모드 여부
         */
        isMdf,
        /**
         * form init
         */
        initForm(): void {
            /* jquery validation */
            cF.validate.validateForm("#userRegForm", this.submitHandler, {
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
            $("#username").on("input keyup keydown", resetUsernameDupCheck);
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailId").on("input", resetEmailDupCheck);
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailDomain").on("input", resetEmailDupCheck);
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#emailDomainSelect").on("change", function(): void {
                const selected = String($(this).val() || "");
                if (selected) $("#emailDomain").val(selected).trigger("input");
                resetEmailDupCheck();
            });
            // 접속IP 사용 여부 클릭시 글씨 변경 + 입력창 토글 :: 메소드 분리
            cF.ui.chckboxLabel("#useAllowedIpYn", "사용//미사용", "blue//gray", function(): void {
                $("#allowedIpListSpan").show();
            }, function(): void {
                $("#allowedIpListSpan").hide();
            });
            /* 접속IP tagify */
            cF.tagify.init("#allowedIpListStr");
        },
        /**
         * Custom SubmitHandler
         */
        submitHandler(): boolean {
            if ($("#useAllowedIpYn").is(":checked") && $("#allowedIpListStr").val() === "") {
                Swal.fire("접속 IP는 최소 한 개 이상 입력해야 합니다.");
                return false;
            }
            Swal.fire({
                text: isMdf() ? Message.get("view.cnfm.mdf") : Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;
                window.dF.User.regAjax();
            });
            return false;
        },
        /**
         * 아이디 중복 체크(Ajax)
         */
        idDupChckAjax(): boolean {
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
            return true;
        },
        /**
         * 이메일 중복 체크(Ajax)
         */
        emailDupChckAjax(): boolean {
            const emailValidSpan = $("#emailId_validate_span");
            const emailId: string = cF.util.getInputValue("#emailId");
            const emailDomain: string = cF.util.getInputValue("#emailDomain");
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
            return true;
        },
        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax(): void {
            const url: string = isMdf() ? Url.USER_MDF_AJAX : Url.USER_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("userRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) window.dF.User.list();
                    });
            }, "block");
        },
        /**
         * 목록 화면으로 이동
         */
        list(): void {
            const listUrl: string = Url.USER_LIST + (isMdf() ? "?isBackToList=Y" : "");
            cF.ui.blockUIReplace(listUrl);
        },
    };
}
