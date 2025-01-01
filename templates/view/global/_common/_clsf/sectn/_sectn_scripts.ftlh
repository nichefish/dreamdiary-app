<#--
 * 레이아웃 > 단락(section) 모듈
 * @included_in : 단락 기능이 있는 페이지들
 -->

<script>
    const Sectn = (function() {
        return {
            /**
             * Sectn 객체 초기화
             * @param {Object} options - 초기화 옵션 객체.
             * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
             */
            init: function({ refreshFunc } = {}) {
                if (refreshFunc !== undefined) Sectn.refreshFunc = refreshFunc;
            },

            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "sectn_reg_header");
                cF.handlebars.template(obj, "sectn_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#sectnRegForm", Sectn.regAjax);
                // checkbox init
                cF.util.chckboxLabel("deprcYn", "만료//해당없음", "red//gray");
                /* tagify */
                cF.tagify.initWithCtgr("#sectnRegForm #tagListStr", undefined);
                // tinymce editor reset
                cF.tinymce.init('#tinymce_sectnCn');
                cF.tinymce.setContentWhenReady("tinymce_sectnCn", obj.cn || "");
            },

            /**
             * Draggable 컴포넌트 init
             * @param {Object} options - 초기화 옵션 객체.
             * @param {Function} [options.refreshFunc] - 드래그 완료 후 호출할 함수 (선택적).
             */
            initDraggable: function({ refreshFunc } = {}) {
                const keyExtractor = (item) => ({ "postNo": $(item).attr("id") });
                const url = "${Url.SECTN_SORT_ORDR_AJAX!}";
                Sectn.swappable = cF.draggable.init(keyExtractor, url, refreshFunc);
            },

            /**
             * 전체 태그 목록 조회 (Ajax)
             * @param {Object} options - 조회 옵션 객체.
             * @param {string|number} options.refPostNo - 참조할 포스트 번호.
             * @param {string} options.refContentType - 참조할 콘텐츠 유형.
             */
            listAjax: function({ refPostNo, refContentType }) {
                const url = "${Url.SECTN_LIST_AJAX!}";
                const ajaxData = { "refPostNo": refPostNo, "refContentType": refContentType };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    // 태그 상세 정보
                    cF.handlebars.template(res.rsltList, "sectn_list");
                });
            },

            /**
             * 등록 모달 호출
             * @param {string|number} refPostNo - 참조할 포스트 번호.
             * @param {string} refContentType - 참조할 콘텐츠 유형.
             */
            regModal: function(refPostNo, refContentType) {
                if (isNaN(refPostNo) || !refContentType) return;

                const obj = { "refPostNo": refPostNo, "refContentType": refContentType };
                /* initialize form. */
                Sectn.initForm(obj);
            },

            /**
             * form submit
             */
            submit: function() {
                if (tinymce !== undefined) tinymce.activeEditor.save();
                $("#sectnRegForm").submit();
            },

            /**
             * 단락 등록 (Ajax)
             */
            regAjax: function() {
                const isReg = $("#sectnRegForm #postNo").val() === "";
                Swal.fire({
                    text: isReg ? <@spring.message "view.cnfm.reg"/> : <@spring.message "view.cnfm.mdf"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = isReg ? "${Url.SECTN_REG_AJAX!}" : "${Url.SECTN_MDF_AJAX!}";
                    const ajaxData = new FormData(document.getElementById("sectnRegForm"));
                    cF.util.blockUIMultipartAjax(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (Sectn.refreshFunc !== undefined) {
                                    Sectn.refreshFunc();
                                } else {
                                    cF.util.blockUIReload();
                                }
                            });
                    });
                });
            },

            /**
             * 단락 수정 모달 호출
             * @param {string|number} postNo - 단락 번호.
             */
            mdfModal: function(postNo) {
                if (isNaN(postNo)) return;

                const url = "${Url.SECTN_DTL_AJAX!}";
                const ajaxData = { "postNo" : postNo };
                cF.util.blockUIAjax(url, "get", ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    /* initialize form. */
                    Sectn.initForm(rsltObj);
                });
            },

            /**
             * 단락 삭제 (Ajax)
             * @param {string|number} postNo - 단락 번호.
             */
            delAjax: function(postNo) {
                if (isNaN(postNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.SECTN_DEL_AJAX!}";
                    const ajaxData = { "postNo": postNo, "actvtyCtgrCd": "${actvtyCtgrCd!}" };
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (!res.rslt) return;
                                if (Sectn.refreshFunc !== undefined) {
                                    Sectn.refreshFunc();
                                } else {
                                    cF.util.blockUIReload();
                                }
                            });
                    });
                });
            },
        }
    })();
</script>
