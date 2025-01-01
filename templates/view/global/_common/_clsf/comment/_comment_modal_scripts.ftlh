<#--
 * 컴포넌트 > 댓글 모듈
 * @included_in : 댓글 기능이 있는 페이지들
 -->

<script>
    if (typeof Comment === 'undefined') { var Comment = {}; }
    Comment.modal = (function() {
        return {
            /**
             * Comment.modal 객체 초기화
             * @param {Object} options - 초기화 옵션 객체.
             * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
             */
            init: function({ refreshFunc } = {}) {
                if (refreshFunc !== undefined) Comment.modal.refreshFunc = refreshFunc;
            },

            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "comment_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#commentRegForm", Comment.modal.regAjax);
            },

            /**
             * 등록 모달 호출
             * @param {string|number} refPostNo - 참조할 게시물 번호.
             * @param {string} refContentType - 참조할 콘텐츠 타입.
             */
            regModal: function(refPostNo, refContentType) {
                if (isNaN(refPostNo) || !refContentType) return;

                const obj = { "refPostNo": refPostNo, "refContentType": refContentType };
                /* initialize form. */
                Comment.modal.initForm(obj);
            },

            /**
             * form submit
             */
            submit: function() {
                $("#commentRegForm").submit();
            },

            /**
             * 댓글 입력(등록/수정) 처리 (Ajax)
             */
            regAjax: function() {
                const isReg = $("#commentRegForm #postNo").val() === "";
                Swal.fire({
                    text: <@spring.message "view.cnfm.save"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = isReg ? "${Url.COMMENT_REG_AJAX!}" : "${Url.COMMENT_MDF_AJAX!}";
                    const ajaxData = new FormData(document.getElementById("commentRegForm"));
                    cF.util.blockUIMultipartAjax(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (Comment.modal.refreshFunc !== undefined) {
                                    Comment.modal.refreshFunc();
                                } else {
                                    cF.util.blockUIReload();
                                }
                            });
                    });
                });
            },

            /**
             * 수정 모달 호출
             * @param {string|number} postNo - 댓글 번호.
             */
            mdfModal: function(postNo) {
                if (isNaN(postNo)) return;

                const url = "${Url.COMMENT_DTL_AJAX!}";
                const ajaxData = { "postNo" : postNo };
                cF.util.blockUIAjax(url, "get", ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    /* initialize form. */
                    Comment.modal.initForm(rsltObj);
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
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (Comment.modal.refreshFunc !== undefined) {
                                    Comment.modal.refreshFunc();
                                } else {
                                    cF.util.blockUIReload();
                                }
                            });
                    });
                });
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Comment.modal.init();
    });
</script>
