/**
 * board_def_module.ts
 *
 * @author nichefish
 */
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
            cF.validate.replaceBlankIfMatches("#boardDefRegForm .cddata", cF.regex.nonCd);
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
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url = Url.BOARD_DEF_REG_AJAX;
                const ajaxData = $("#boardDefRegForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 수정 모달 호출
         * @param {string} boardCd - 게시판 코드 (key).
         */
        mdfModal: function(boardCd: string) {
            const url = Url.BOARD_DEF_DTL_AJAX;
            const ajaxData = { "boardCd": boardCd };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse) {
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
         * @param {string} key - 게시판 코드 (key).
         */
        useAjax: function(key: string) {
            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url = Url.BOARD_DEF_USE_AJAX;
                const ajaxData = { "boardCd": key };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 미사용으로 변경 (Ajax)
         * @param {string} key - 게시판 코드 (key).
         */
        unuseAjax: function(key: string) {
            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url = Url.BOARD_DEF_UNUSE_AJAX;
                const ajaxData = { "boardCd": key };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string} key - 게시판 코드 (key).
         */
        delAjax: function(key: string) {
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult) {
                if (!result.value) return;

                const url = Url.BOARD_DEF_DEL_AJAX;
                const ajaxData = { "boardCd": key };
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            });
        },
    }
})();