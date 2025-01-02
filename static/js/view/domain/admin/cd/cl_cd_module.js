/**
 * cl_cd_module.ts
 *
 * @author nichefish
 */
const ClCd = (function () {
    return {
        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function (obj = {}) {
            /* show modal */
            cF.handlebars.template(obj, "cl_cd_reg", "show");
            /* jquery validation */
            cF.validate.validateForm("#clCdRegForm", ClCd.regAjax);
            // checkbox init
            cF.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
            cF.validate.replaceBlankIfMatches("#clCdRegForm #clCd", cF.regex.nonCd);
            cF.validate.toUpperCase("#clCdRegForm #clCd");
        },
        /**
         * Draggable 컴포넌트 init
         */
        initDraggable: function () {
            const keyExtractor = (item) => ({ "clCd": $(item).attr("id") });
            const url = Url.CL_CD_SORT_ORDR_AJAX;
            ClCd.swappable = cF.draggable.init("", keyExtractor, url);
        },
        /**
         * 목록 검색
         */
        search: function () {
            event.stopPropagation();
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", Url.CL_CD_LIST + "?actionTyCd=SEARCH");
        },
        /**
         * 등록 모달 호출
         */
        regModal: function () {
            event.stopPropagation();
            /* initialize form. */
            ClCd.initForm({});
        },
        /**
         * form submit
         */
        submit: function () {
            event.stopPropagation();
            $("#clCdRegForm").submit();
        },
        /**
         * 등록/수정 (Ajax)
         */
        regAjax: function () {
            event.stopPropagation();
            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function (result) {
                if (!result.value)
                    return;
                $("#clCdRegForm #regYn").val("Y");
                const url = Url.CL_CD_REG_AJAX;
                const ajaxData = $("#clCdRegForm").serializeArray();
                cF.ajax.post(url, ajaxData, function (res) {
                    Swal.fire({ text: res.message })
                        .then(function () {
                        if (res.rslt)
                            cF.util.blockUIReload();
                    });
                }, "block");
            });
        },
        /**
         * 상세 화면으로 이동
         * @param {string} clCd - 조회할 분류 코드.
         */
        dtl: function (clCd) {
            event.stopPropagation();
            cF.util.blockUIRequest();
            $("#procForm #clCd").val(clCd);
            cF.util.blockUISubmit("#procForm", Url.CL_CD_DTL);
        },
        /**
         * 상세 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        dtlModal: function (clCd) {
            event.stopPropagation();
            const url = Url.CL_CD_DTL_AJAX;
            const ajaxData = { "clCd": clCd };
            cF.ajax.get(url, ajaxData, function (res) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message))
                        Swal.fire({ text: res.message });
                    return false;
                }
                cF.handlebars.template(res.rsltObj, "cl_cd_dtl", "show");
                ClCd.key = clCd;
            });
        },
        /**
         * 수정 모달 호출
         * @param {string} clCd - 조회할 분류 코드.
         */
        mdfModal: function (clCd) {
            event.stopPropagation();
            const url = Url.CL_CD_DTL_AJAX;
            const ajaxData = { "clCd": clCd };
            cF.ajax.get(url, ajaxData, function (res) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message))
                        Swal.fire({ text: res.message });
                    return false;
                }
                const { rsltObj } = res;
                rsltObj.isMdf = true;
                /* initialize form. */
                ClCd.initForm(rsltObj);
                $('#cl_cd_dtl_modal').modal('hide');
            });
        },
        /**
         * 사용으로 변경 (Ajax)
         * @param {string} clCd - 변경할 분류 코드.
         */
        useAjax: function (clCd) {
            event.stopPropagation();
            Swal.fire({
                text: Message.get("view.cnfm.use"),
                showCancelButton: true,
            }).then(function (result) {
                if (!result.value)
                    return;
                const url = Url.CL_CD_USE_AJAX;
                const ajaxData = { "clCd": clCd };
                cF.ajax.post(url, ajaxData, function (res) {
                    Swal.fire({ text: res.message })
                        .then(function () {
                        if (res.rslt)
                            cF.util.blockUIReload();
                    });
                }, "block");
            });
        },
        /**
         * 미사용으로 변경 (Ajax)
         * @param {string} clCd - 변경할 분류 코드.
         */
        unuseAjax: function (clCd) {
            event.stopPropagation();
            Swal.fire({
                text: Message.get("view.cnfm.unuse"),
                showCancelButton: true,
            }).then(function (result) {
                if (!result.value)
                    return;
                const url = Url.CL_CD_UNUSE_AJAX;
                const ajaxData = { "clCd": clCd };
                cF.ajax.post(url, ajaxData, function (res) {
                    Swal.fire({ text: res.message })
                        .then(function () {
                        if (res.rslt)
                            cF.util.blockUIReload();
                    });
                }, "block");
            });
        },
        /**
         * 삭제 (Ajax)
         * @param {string} clCd - 삭제할 분류 코드.
         */
        delAjax: function (clCd) {
            event.stopPropagation();
            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function (result) {
                if (!result.value)
                    return;
                const url = Url.CL_CD_DEL_AJAX;
                const ajaxData = { "clCd": clCd };
                cF.ajax.post(url, ajaxData, function (res) {
                    Swal.fire({ text: res.message })
                        .then(function () {
                        if (res.rslt)
                            cF.util.blockUIReload();
                    });
                }, "block");
            });
        },
        /**
         * 목록 화면으로 이동
         */
        list: function () {
            const listUrl = Url.CL_CD_LIST + "?isBackToList=Y";
            cF.util.blockUIReplace(listUrl);
        },
    };
})();
