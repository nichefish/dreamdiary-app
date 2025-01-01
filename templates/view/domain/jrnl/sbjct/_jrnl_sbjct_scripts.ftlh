<#--
 * 저널 주제(jrnl_sbjct) : 스크립트 모듈 분리
 -->

<script>
    const JrnlSbjct = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* jquery validation */
                cF.validate.validateForm("#jrnlSbjctRegForm", JrnlSbjct.submitHandler);
                /* tinymce init */
                cF.tinymce.init("#tinymce_cn");
                /* tagify */
                cF.tagify.initWithCtgr("#tagListStr", undefined);
                // 잔디발송여부 클릭시 글씨 변경
                cF.util.chckboxLabel("jandiYn", "발송//미발송", "blue//gray", function() {
                    $("#trgetTopicSpan").show();
                }, function() {
                    $("#trgetTopicSpan").hide();
                });
            },

            /**
             * Custom SubmitHandler
             */
            submitHandler: function() {
                if (JrnlSbjct.submitMode === "preview") {
                    const popupNm = "preview";
                    const options = 'width=1280,height=1440,top=0,left=270';
                    const popup = cF.util.openPopup("", popupNm, options);
                    if (popup) popup.focus();
                    const popupUrl = "${Url.JRNL_SBJCT_REG_PREVIEW_POP!}";
                    $("#jrnlSbjctRegForm").attr("action", popupUrl).attr("target", popupNm);
                    return true;
                } else if (JrnlSbjct.submitMode === "submit") {
                    $("#jrnlSbjctRegForm").removeAttr("action");
                    Swal.fire({
                        text: <#if isMdf!false><@spring.message "view.cnfm.mdf"/><#else><@spring.message "view.cnfm.reg"/></#if>,
                        showCancelButton: true,
                    }).then(function(result) {
                        if (!result.value) return;
                        JrnlSbjct.regAjax();
                    });
                }
            },

            /**
             * 목록 검색
             */
            search: function() {
                $("#listForm #pageNo").val(1);
                cF.util.blockUISubmit("#listForm", "${Url.JRNL_SBJCT_LIST!}?actionTyCd=SEARCH");
            },

            /**
             * 내가 작성한 글 목록 보기
             */
            myPaprList: function() {
                const url = "${Url.JRNL_SBJCT_LIST!}";
                let param = "?searchType=nickNm&searchKeyword=${authInfo.nickNm!}&regstrId=${authInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR";
                cF.util.blockUIReplace(url + param);
            },

            /**
             * 등록 화면으로 이동
             */
            regForm: function() {
                cF.util.blockUISubmit("#procForm", "${Url.JRNL_SBJCT_REG_FORM!}");
            },

            /**
             * form submit
             */
            submit: function() {
                if (tinymce !== undefined) tinymce.activeEditor.save();
                JrnlSbjct.submitMode = "submit";
                $("#jrnlSbjctRegForm").submit();
            },

            /**
             * 미리보기 팝업 호출
             */
            preview: function() {
                if (tinymce !== undefined) tinymce.activeEditor.save();
                JrnlSbjct.submitMode = "preview";
                $("#jrnlSbjctRegForm").submit();
            },

            /**
             * 등록/수정 처리(Ajax)
             */
            regAjax: function() {
                const url = " <#if isMdf!false>${Url.JRNL_SBJCT_MDF_AJAX!}<#else>${Url.JRNL_SBJCT_REG_AJAX!}</#if>";
                const ajaxData = new FormData(document.getElementById("jrnlSbjctRegForm"));
                cF.util.blockUIMultipartAjax(url, ajaxData, function(res) {
                    Swal.fire({text: res.message})
                        .then(function() {
                            if (!res.rslt) return;
                            if (res.rsltObj === undefined) JrnlSbjct.list();
                            const postNo = res.rsltObj.postNo;
                            cF.util.blockUIReplace("${Url.JRNL_SBJCT_DTL!}?postNo=" + postNo);
                        });
                }, "block");
            },

            /**
             * 상세 화면으로 이동
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtl: function(postNo) {
                if (isNaN(postNo)) return;

                event.stopPropagation();
                if (isNaN(postNo)) return;

                $("#procForm #postNo").val(postNo);
                cF.util.blockUISubmit("#procForm", "${Url.JRNL_SBJCT_DTL!}");
            },

            /**
             * 상세 모달 호출
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtlModal: function(postNo) {
                event.stopPropagation();
                if (isNaN(postNo)) return;

                const url = "${Url.JRNL_SBJCT_DTL_AJAX!}";
                const ajaxData = {"postNo": postNo};
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({text: res.message});
                        return false;
                    }
                    cF.handlebars.template(res.rsltObj, "jrnlSbjct_dtl", "show");
                });
            },

            /**
             * 수정 화면으로 이동
             */
            mdfForm: function() {
                cF.util.blockUISubmit("#procForm", "${Url.JRNL_SBJCT_MDF_FORM!}");
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
                    const url = "${Url.JRNL_SBJCT_DEL_AJAX!}";
                    const ajaxData = $("#procForm").serializeArray();
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({text: res.message})
                            .then(function() {
                                if (res.rslt) JrnlSbjct.list();
                            });
                    }, "block");
                });
            },

            /**
             * 목록 화면으로 이동
             */
            list: function() {
                const listUrl = "${Url.JRNL_SBJCT_LIST!}<#if isMdf!false>?isBackToList=Y</#if>";
                cF.util.blockUIReplace(listUrl);
            }
        }
    })();
</script>