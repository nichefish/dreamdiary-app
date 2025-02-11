/**
 * user_reqst_module.ts
 * 사용자 등록 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.UserReqst = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.UserReqst.initialized) return;

            dF.UserReqst.initialized = true;
            console.log("'dF.UserReqst' module initialized.");
        },

        /**
         * form init
         */
        initForm: function(): void {
            /* jquery validation */
            cF.validate.validateForm("#userReqstForm", dF.UserReqst.submitHandler, {
                rules: {
                    userId: { minlength: 4, maxlength: 16 },
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
            cF.validate.toLowerCase("#userId");
            // 전화번호 형식 유효성 검사
            cF.validate.cttpc("#cttpc");
            // 이메일 도메인 select시 자동입력
            $("#emailDomainSelect").on("change", function(): void {
                $("#emailDomain").val($(this).val());
            });
            // 권한 변경시 필드 재검증
            $("#authCd").change(function(): void {
                $("#authCd").valid(); // 체크박스 상태 변경시 details 필드 재검증
            });
            // 등록화면:: 사용자 ID 변경입력시 중복체크 통과여부 초기화
            $("#userId").on("input", function(): void {
                $("#userId_validate_span").empty();
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
            cF.ui.chckboxLabel("useAcsIpYn", "사용//미사용", "blue//gray", function(): void{
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
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                dF.UserReqst.regAjax();
            });
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("userReqstForm").submit();
        },

        /**
         * 등록/수정 처리(Ajax)
         */
        regAjax: function(): void {
            const url: string = Url.USER_REQST_REG_AJAX;
            const ajaxData: FormData = new FormData(document.getElementById("userReqstForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) dF.UserReqst.main();
                    });
            }, "block");
        },

        /**
         * 로그인 화면으로 돌아가기
         */
        return: function(): void {
            Swal.fire({
                text: "<@spring.message 'bs.tooltip.return-to-main'/>\n(현재까지 입력한 내용은 사라집니다.)\n계속하겠습니까?",
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (result.value) dF.UserReqst.main();
            });
        },

        /**
         * 로그인 화면으로 이동
         */
        main: function(): void {
            cF.ui.blockUIReplace(Url.AUTH_LGN_FORM);
        }
    }
})();