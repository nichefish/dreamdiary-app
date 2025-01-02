<#--
 * 레이아웃 > 댓글 모듈
 * @included_in : 댓글 기능이 있는 페이지들
 -->

<script>
    if (typeof Comment === 'undefined') { let Comment = {}; }
    Comment.page = (function() {
        return {
            /**
             * Comment.page 객체 초기화
             */
            init: function() {
                /* initialize form. */
                Comment.page.initForm();
            },

            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show reg area */
                cF.handlebars.template(obj, "comment_page_reg");

                /* jquery validation */
                cF.validate.validateForm("#commentPageRegForm", Comment.page.regAjax);
            },

            /**
             * form submit
             */
            submit: function() {
                $("#commentPageRegForm").submit();
            },

            /**
             * 댓글 입력 처리 (Ajax)
             */
            regAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.reg"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.COMMENT_REG_AJAX!}";
                    const ajaxData = new FormData(document.getElementById("commentPageRegForm"));
                    cF.ajax.multipart(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    });
                });
            },

            /**
             * 댓글 수정 폼 생성
             * @param {string|number} postNo - 댓글 번호.
             */
            mdfForm: function(postNo) {
                if (isNaN(postNo)) return;

                $("#commentDtlSpan" + postNo).hide();
                let str = $("#commentMdfTemplate").html().replace(/__INDEX__/g, postNo);
                $("#commentSpan" + postNo).append(str);
                $("#commentMdfCn" + postNo).html($("#commentCnSpanDiv" + postNo).html());
                $("#showMdfBtnDiv" + postNo).hide();
                $("#mdfSaveBtnDiv" + postNo).show();
                cF.validate.validateForm("#commentPageMdfForm", Comment.page.mdfAjax);
            },

            /**
             * 댓글 수정 폼 닫기
             * @param {string|number} postNo - 댓글 번호.
             */
            closeMdfForm: function(postNo) {
                if (isNaN(postNo)) return;

                $("#commentDtlSpan" + postNo).show();
                $("#commentSpan" + postNo).empty();
                $("#commentMdfCn" + postNo).empty();
                $("#showMdfBtnDiv" + postNo).show();
                $("#mdfSaveBtnDiv" + postNo).hide();
            },

            /**
             * 댓글 수정 (Ajax)
             * @param {string|number} postNo - 댓글 번호.
             */
            mdfAjax: function(postNo) {
                if (isNaN(postNo)) return;

                if (cF.util.isEmpty($("#commentMdfCn" + postNo), "value")) {
                    Swal.fire("댓글 내용을 입력해주세요.");
                    return false;
                }
                Swal.fire({
                    text: <@spring.message "view.cnfm.mdf"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.COMMENT_MDF_AJAX!}";
                    const ajaxData = $("#commentPageMdfForm" + postNo).serializeArray();
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    });
                });
            },

            /**
             * 댓글 삭제 (Ajax)
             * @param {string|number} postNo - 댓글 번호.
             */
            delAjax: function(postNo) {
                if (isNaN(postNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.COMMENT_DEL_AJAX!}";
                    const ajaxData = { "postNo": postNo, "actvtyCtgrCd": "${actvtyCtgrCd!}" };
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    });
                });
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Comment.page.init();
    });
</script>
