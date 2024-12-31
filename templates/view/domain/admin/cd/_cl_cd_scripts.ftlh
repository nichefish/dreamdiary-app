<#--
  -- 분류 코드(cl_cd) : 스크립트 모듈 분리
  -->

<script>
    const ClCd = (function() {
        return {
            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                commons.util.handlebarsTemplate(obj, "cl_cd_reg", "show");

                /* jquery validation */
                commons.validate.validateForm("#clCdRegForm", ClCd.regAjax);
                // checkbox init
                commons.util.chckboxLabel("useYn", "사용//미사용", "blue//gray");
                commons.validate.replaceBlankIfMatches("#clCdRegForm #clCd", commons.validate.nonCdRegex);
                commons.validate.toUpperCase("#clCdRegForm #clCd");
            },

            /**
             * Draggable 컴포넌트 init
             */
            initDraggable: function() {
                const keyExtractor = (item) => ({ "clCd": $(item).attr("id") });
                const url = "${Url.CL_CD_SORT_ORDR_AJAX!}";
                ClCd.swappable = commons.util.initDraggable("", keyExtractor, url);
            },

            /**
             * 목록 검색
             */
            search: function() {
                event.stopPropagation();

                $("#listForm #pageNo").val(1);
                commons.util.blockUISubmit("#listForm", "${Url.CL_CD_LIST!}?actionTyCd=SEARCH");
            },

            /**
             * 등록 모달 호출
             */
            regModal: function() {
                event.stopPropagation();

                /* initialize form. */
                ClCd.initForm({});
            },

            /**
             * form submit
             */
            submit: function() {
                event.stopPropagation();

                $("#clCdRegForm").submit();
            },

            /**
             * 등록/수정 (Ajax)
             */
            regAjax: function() {
                event.stopPropagation();

                Swal.fire({
                    text: <@spring.message "view.cnfm.reg"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    $("#clCdRegForm #regYn").val("Y");
                    const url = "${Url.CL_CD_REG_AJAX!}";
                    const ajaxData = $("#clCdRegForm").serializeArray();
                    commons.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) commons.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 상세 화면으로 이동
             * @param {string} clCd - 조회할 분류 코드.
             */
            dtl: function(clCd) {
                event.stopPropagation();

                commons.util.blockUIRequest();
                $("#procForm #clCd").val(clCd);
                commons.util.blockUISubmit("#procForm", "${Url.CL_CD_DTL!}");
            },

            /**
             * 상세 모달 호출
             * @param {string} clCd - 조회할 분류 코드.
             */
            dtlModal: function(clCd) {
                event.stopPropagation();

                const url = "${Url.CL_CD_DTL_AJAX!}";
                const ajaxData = { "clCd": clCd };
                commons.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (commons.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    commons.util.handlebarsTemplate(res.rsltObj, "cl_cd_dtl", "show");
                    ClCd.key = clCd;
                });
            },

            /**
             * 수정 모달 호출
             * @param {string} clCd - 조회할 분류 코드.
             */
            mdfModal: function(clCd) {
                event.stopPropagation();

                const url = "${Url.CL_CD_DTL_AJAX!}";
                const ajaxData = { "clCd": clCd };
                commons.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (commons.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
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
            useAjax: function(clCd) {
                event.stopPropagation();

                Swal.fire({
                    text: <@spring.message "view.cnfm.use"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = '${Url.CL_CD_USE_AJAX!}';
                    const ajaxData = { "clCd": clCd };
                    commons.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) commons.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 미사용으로 변경 (Ajax)
             * @param {string} clCd - 변경할 분류 코드.
             */
            unuseAjax: function(clCd) {
                event.stopPropagation();

                Swal.fire({
                    text: <@spring.message "view.cnfm.unuse"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = '${Url.CL_CD_UNUSE_AJAX!}';
                    const ajaxData = { "clCd": clCd };
                    commons.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) commons.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 삭제 (Ajax)
             * @param {string} clCd - 삭제할 분류 코드.
             */
            delAjax: function(clCd) {
                event.stopPropagation();

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.CL_CD_DEL_AJAX!}";
                    const ajaxData = { "clCd" : clCd };
                    commons.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) commons.util.blockUIReload();
                            });
                    }, "block");
                });
            },

            /**
             * 목록 화면으로 이동
             */
            list: function() {
                const listUrl = "${Url.CL_CD_LIST!}?isBackToList=Y";
                commons.util.blockUIReplace(listUrl);
            },
        }
    })();
</script>