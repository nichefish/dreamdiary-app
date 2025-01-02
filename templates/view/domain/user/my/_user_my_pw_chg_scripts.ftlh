<#--
 * 내 정보(user_my) > 내 비밀번호 변경 > 스크립트 모듈 분리
 -->

<script>
    const UserMyPwChg = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* jquery validation */
                cF.validate.validateForm("#myPwChgForm", UserMyPwChg.submitHandler, {
                    rules: {
                        newPw: { regex: cF.regex.pw },
                        newPwCf: { equalTo: "#newPw" }
                    },
                    messages: {
                        newPw: { regex: "변경할 비밀번호가 형식에 맞지 않습니다." },
                        newPwCf: { equalTo: "변경할 비밀번호에 입력한 값과 동일하게 입력해야 합니다." }
                    },
                });
            },

            /**
             * Custom SubmitHandler
             */
            submitHandler: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.chgPw"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    UserMyPwChg.myPwChgAjax();
                });
            },

            /**
             * 내 비밀번호 수정 팝업 호출
             */
            myPwChgModal: function() {
                const data = { "errorMsg": "${errorMsg!}" };
                cF.handlebars.template(data, "user_my_pw_chg", "show");
            },

            /**
             * form submit
             */
            submit: function() {
                $("#myPwChgForm").submit();
            },

            /**
             * 내 비밀번호 수정 (Ajax)
             */
            myPwChgAjax: function() {
                const url = "${Url.USER_MY_PW_CHG_AJAX!}";
                const ajaxData = { "currPw" : $("#currPw").val(), "newPw" : $("#newPw").val() };
                cF.ajax.post(url, ajaxData, function(res) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                });
            },
        }
    })();
</script>
