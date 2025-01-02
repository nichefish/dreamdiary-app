<#--
 * 저널 꿈(jrnl_dream) : 스크립트 모듈 분리
 -->

<#--include::script modules-->
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_ctgr_map.ftlh">

<script>
    const JrnlDream = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "jrnl_dream_reg_header");
                cF.handlebars.template(obj, "jrnl_dream_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#jrnlDreamRegForm", JrnlDream.regAjax, {
                    rules: {
                        elseDreamerNm: {
                            required: function() {
                                return $("#jrnlDreamRegForm #elseDreamYn").prop(":checked", false);
                            }
                        },
                    },
                    ignore: undefined
                });
                // 체크박스 상태 변경시 필드 재검증
                $("#elseDreamYn").change(function() {
                    $("#elseDreamerNm").valid();
                });
                /* tagify */
                cF.tagify.initWithCtgr("#jrnlDreamRegForm #tagListStr", TagCtgrMap.jrnlDream);
                // checkbox init
                cF.util.chckboxLabel("imprtcYn", "중요//해당없음", "red//gray");
                // checkbox init
                cF.util.chckboxLabel("nhtmrYn", "악몽//해당없음", "red//gray");
                // checkbox init
                cF.util.chckboxLabel("hallucYn", "입면환각//해당없음", "blue//gray");
                // checkbox init
                cF.util.chckboxLabel("elseDreamYn", "해당//미해당", "blue//gray", function() {
                    $("#elseDreamerNmDiv").removeClass("d-none");
                }, function() {
                    $("#elseDreamerNmDiv").addClass("d-none");
                });
                /* tinymce editor reset */
                cF.tinymce.init('#tinymce_jrnlDreamCn');
                cF.tinymce.setContentWhenReady("tinymce_jrnlDreamCn", obj.cn || "");
            },

            /**
             * 목록 조회 (Ajax)
             */
            keywordListAjax: function() {
                const keyword = $("#jrnl_aside #dreamKeyword").val();
                if (cF.util.isEmpty(keyword)) return;
                const url = "${Url.JRNL_DREAM_LIST_AJAX!}";
                const ajaxData = { "dreamKeyword": $("#jrnl_aside #dreamKeyword").val() };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    $("#jrnl_aside #yy").val("");
                    $("#jrnl_aside #mnth").val("");
                    // 목록 영역 클리어
                    $("#jrnl_aside #diaryKeyword").val("");
                    $("#jrnl_day_list_div").empty();
                    $("#jrnl_diary_list_div").empty();
                    // 태그 헤더 클리어
                    $("#jrnl_day_tag_list_div").empty();
                    $("#jrnl_diary_tag_list_div").empty();
                    $("#jrnl_dream_tag_list_div").empty();
                    cF.util.closeModal();
                    cF.handlebars.template(res.rsltList, "jrnl_dream_list");
                    JrnlDream.inKeywordSearchMode = true;
                }, "block");
            },

            /**
             * 등록 모달 호출
             * @param {string|number} jrnlDayNo - 저널 일자 번호.
             * @param {string} stdrdDt - 기준 날짜.
             */
            regModal: function(jrnlDayNo, stdrdDt) {
                if (isNaN(jrnlDayNo)) return;

                const obj = { "jrnlDayNo": jrnlDayNo, "stdrdDt": stdrdDt };
                /* initialize form. */
                JrnlDream.initForm(obj);
            },

            /**
             * form submit
             */
            submit: function() {
                tinymce.get("tinymce_jrnlDreamCn").save();
                $("#jrnlDreamRegForm").submit();
            },

            /**
             * 등록 (Ajax)
             */
            regAjax: function() {
                const isReg = $("#jrnlDreamRegForm #postNo").val() === "";
                Swal.fire({
                    text: isReg ? <@spring.message "view.cnfm.reg"/> : <@spring.message "view.cnfm.mdf"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = isReg ? "${Url.JRNL_DREAM_REG_AJAX!}" : "${Url.JRNL_DREAM_MDF_AJAX!}";
                    const ajaxData = new FormData(document.getElementById("jrnlDreamRegForm"));
                    cF.ajax.multipart(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (JrnlDream.inKeywordSearchMode) {
                                    JrnlDream.keywordListAjax();
                                } else {
                                    JrnlDay.yyMnthListAjax();
                                    JrnlDreamTag.listAjax();
                                }
                                // TODO: 결산 페이지에서 처리시도 처리해 줘야 한다.
                                cF.util.unblockUI();
                            });
                    }, "block");
                });
            },

            /**
             * 수정 모달 호출
             * @param {string|number} postNo - 글 번호.
             */
            mdfModal: function(postNo) {
                if (isNaN(postNo)) return;

                const url = "${Url.JRNL_DREAM_DTL_AJAX!}";
                const ajaxData = { "postNo" : postNo };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    /* initialize form. */
                    JrnlDream.initForm(rsltObj);
                });
            },

            /**
             * 삭제 (Ajax)
             * @param {string|number} postNo - 글 번호.
             */
            delAjax: function(postNo) {
                if (isNaN(postNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.JRNL_DREAM_DEL_AJAX!}";
                    const ajaxData = { "postNo": postNo };
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (JrnlDream.inKeywordSearchMode) {
                                    JrnlDream.keywordListAjax();
                                } else {
                                    JrnlDay.yyMnthListAjax();
                                    JrnlDreamTag.listAjax();
                                }
                                cF.util.unblockUI();
                            });
                    }, "block");
                });
            },
        }
    })();
</script>