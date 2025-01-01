<#--
 * 게시판 정의(board_def) : 스크립트 모듈 분리
 -->

<script>
    const BoardDef = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "board_def_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#boardDefRegForm", BoardDef.regAjax);
                // checkbox init
                cF.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
                cF.validate.replaceBlankIfMatches("#boardDefRegForm .cddata", cF.validate.nonCdRegex);
                cF.validate.onlyNum(".number");
            },

            /**
             * Draggable 컴포넌트 init
             */
            initDraggable: function() {
                const keyExtractor = (item) => ({ "boardCd": $(item).attr("id") });
                const url = "${Url.BOARD_DEF_SORT_ORDR_AJAX!}";
                BoardDef.swappable = cF.draggable.init(keyExtractor, url);
            },

            /**
             * 등록 모달 호출
             */
            regModal: function() {
                /* initialize form. */
                BoardDef.initForm();
            },

            /**
             * form submit
             */
            submit: function() {
                $("#boardDefRegForm").submit();
            },

            /**
             * 등록 (Ajax)
             */
            regAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.reg"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = '${Url.BOARD_DEF_REG_AJAX!}';
                    const ajaxData = $("#boardDefRegForm").serializeArray();
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 수정 모달 호출
             * @param {string|number} boardCd - 게시판 코드 (key).
             */
            mdfModal: function(boardCd) {
                const url = "${Url.BOARD_DEF_DTL_AJAX!}";
                const ajaxData = { "boardCd": boardCd };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    rsltObj.isMdf = true;
                    /* initialize form. */
                    BoardDef.initForm(rsltObj);
                });
            },

            /**
             * 사용으로 변경 (Ajax)
             * @param {string|number} Ajax - 게시판 코드 (key).
             */
            useAjax: function(key) {
                Swal.fire({
                    text: <@spring.message "view.cnfm.use"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = '${Url.BOARD_DEF_USE_AJAX!}';
                    const ajaxData = { "boardCd": key };
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 미사용으로 변경 (Ajax)
             * @param {string|number} Ajax - 게시판 코드 (key).
             */
            unuseAjax: function(key) {
                Swal.fire({
                    text: <@spring.message "view.cnfm.unuse"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = '${Url.BOARD_DEF_UNUSE_AJAX!}';
                    const ajaxData = { "boardCd": key };
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 삭제 (Ajax)
             * @param {string|number} Ajax - 게시판 코드 (key).
             */
            delAjax: function(key) {
                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.BOARD_DEF_DEL_AJAX!}";
                    const ajaxData = { "boardCd": key };
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            },
        }
    })();
</script>