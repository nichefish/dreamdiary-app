<#--
 * 휴가계획서(vcatn_papr) : 스크립트 모듈 분리
 -->

<script>
    const VcatnPapr = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* jquery validation */
                cF.validate.validateForm("#vcatnPaprRegForm", VcatnPapr.submitHandler);
                // 잔디발송여부 클릭시 글씨 변경
                cF.util.chckboxLabel("jandiYn", "발송//미발송", "#0095E8//gray", function() {
                    $("#trgetTopicSpan").show();
                }, function() {
                    $("#trgetTopicSpan").hide();
                });
            },

            /**
             * Custom SubmitHandler
             */
            submitHandler: function() {
                Swal.fire({
                    text: <#if isMdf!false><@spring.message "view.cnfm.mdf"/><#else><@spring.message "view.cnfm.reg"/></#if>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    VcatnPapr.regAjax();
                });
            },

            /**
             * 목록 검색
             */
            search: function() {
                $("#listForm #pageNo").val(1);
                cF.util.blockUISubmit("#listForm", "${Url.VCATN_PAPR_LIST!}?actionTyCd=SEARCH");
            },

            /**
             * 내가 작성한 글 목록 보기
             */
            myPaprList: function() {
                const url = "${Url.VCATN_PAPR_LIST!}";
                let param = "?searchType=nickNm&searchKeyword=${authInfo.nickNm!}&regstrId=${authInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR";
                cF.util.blockUIReplace(url + param);
            },

            /**
             * 등록 화면으로 이동
             */
            regForm: function() {
                cF.util.blockUISubmit("#procForm", "${Url.VCATN_PAPR_REG_FORM!}");
            },

            /**
             * form submit
             */
            submit: function() {
                $("#vcatnPaprRegForm").submit();
            },

            /**
             * 등록/수정 처리(Ajax)
             */
            regAjax: function() {
                const url = "<#if isReg!false>${Url.VCATN_PAPR_REG_AJAX!}</#if><#if isMdf!false>${Url.VCATN_PAPR_MDF_AJAX!}</#if>";
                const ajaxData = new FormData(document.getElementById("vcatnPaprRegForm"));
                cF.util.blockUIMultipartAjax(url, ajaxData, function(res) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) VcatnPapr.list();
                        });
                }, "block");
            },

            /**
             * 상세 화면으로 이동
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtl: function(postNo) {
                if (isNaN(postNo)) return;

                $("#procForm #postNo").val(postNo);
                cF.util.blockUISubmit("#procForm", "${Url.VCATN_PAPR_DTL!}");
            },

            /**
             * 상세 모달 호출
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtlModal: function(postNo) {
                event.stopPropagation();
                if (isNaN(postNo)) return;

                const url = "${Url.VCATN_PAPR_DTL_AJAX!}";
                const ajaxData = { "postNo": postNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltObj, "vcatn_papr_dtl", "show");
                });
            },

            /**
             * 수정 화면으로 이동
             */
            mdfForm: function() {
                cF.util.blockUISubmit("#procForm", "${Url.VCATN_PAPR_MDF_FORM!}");
            },

            /**
             * 관리자 확인 여부 변경 (Ajax)
             */
            cfAjax: function() {
                const url = "${Url.VCATN_PAPR_CF_AJAX!}";
                const ajaxData = $("#procForm").serializeArray();
                cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            },

            /**
             * 삭제 (Ajax)
             */
            delAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.VCATN_PAPR_DEL_AJAX!}";
                    const ajaxData = $("#procForm").serializeArray();
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) VcatnPapr.list();
                            });
                    }, "block");
                });
            },

            /**
             * 목록 화면으로 이동
             */
            list: function() {
                cF.util.blockUIReplace("${Url.VCATN_PAPR_LIST!}?isBackToList=Y");
            }
        }
    })();
</script>