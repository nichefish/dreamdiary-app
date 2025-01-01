<#--
 * 파일시스템 메타(flsys_meta) : 스크립트 모듈 분리
 -->

<script>
    const FlsysMeta = (function() {
        return {
            /**
             * FlsysMeta 객체 초기화
             */
            init: function() {

            },

            /**
             * form init
             * @param {Object} obj - 폼에 바인딩할 데이터
             */
            initForm: function(obj = {}) {
                /* show modal */
                cF.handlebars.template(obj, "flsys_meta_reg", "show");

                /* jquery validation */
                cF.validate.validateForm("#flsysMetaRegForm", FlsysMeta.regAjax);
                /* tagify */
                cF.tagify.init("#tagListStr");
            },

            /**
             * 등록 모달 조회
             */
            regModal: function(filePath) {
                const obj = { "filePath": filePath, "upperFilePath": Flsys.currPath };
                /* initialize form. */
                FlsysMeta.initForm(obj);
            },

            /**
             * 메타정보 등록 (Ajax)
             */
            submit: function() {
                $("#flsysMetaRegForm").submit();
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
                    const url = "${Url.FLSYS_META_REG_AJAX!}";
                    const ajaxData = $("#flsysMetaRegForm").serializeArray();
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) Flsys.flsysRefreshAjax();
                            });
                    });
                });
            },

            /**
             * 상세 모달 호출
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtlModal: function(postNo) {
                event.stopPropagation();
                if (isNaN(postNo)) return;

                const url = "${Url.FLSYS_META_DTL_AJAX!}";
                const ajaxData = { "postNo": postNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltObj, "flsys_meta_dtl", "show");
                });
            },

            /**
             * 수정 모달 호출
             */
            mdfModal: function(postNo) {
                if (isNaN(postNo)) return;

                const url = "${Url.FLSYS_META_DTL_AJAX!}";
                const ajaxData = { "postNo": postNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    /* initialize form. */
                    FlsysMeta.initForm(rsltObj);
                    $("#flsys_meta_dtl_modal").modal("hide");
                    FlsysMeta.key = postNo;
                });
            },

            /**
             * 삭제 (Ajax)
             */
            delAjax: function(postNo) {
                if (isNaN(postNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    const url = "${Url.FLSYS_META_DEL_AJAX!}";
                    const ajaxData = { "postNo": postNo };
                    cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        if (res.rslt) Flsys.flsysListAjax(Flsys.currPath);
                    });
                });
            },
        }
    })();
</script>