<#--
 * 로그인 정책(lgn_policy) : 스크립트 모듈 분리
 -->

<script>
    const LgnPolicy = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* jquery validation */
                cF.validate.validateForm("#lgnPolicyForm", LgnPolicy.regAjax, {
                    rules: {
                        lgnLockDy: { maxlength: 3 },
                        lgnTryLmt: { maxlength: 3 },
                        pwChgDy: { maxlength: 3 },
                        pwForReset: { minlength: 8, maxlength: 20, regex: cF.regex.pw },
                    },
                    messages: {
                        pwForReset: { regex: "비밀번호가 형식에 맞지 않습니다." },
                    },
                });
            },

            /**
             * form submit
             */
            submit: function() {
                $("#lgnPolicyForm").submit();
            },

            /**
             * 등록/수정 (Ajax)
             */
            regAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.mdf"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.LGN_POLICY_REG_AJAX!}";
                    const ajaxData = $("#lgnPolicyForm").serializeArray();
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReplace("${Url.LGN_POLICY_FORM!}");
                            });
                    }, "block");
                });
            }
        }
    })();
</script>