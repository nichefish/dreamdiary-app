/**
 * jrnl_sumry_module.ts
 *
 * @author nichefish
 */
const JrnlSumry = (function() {
    return {
        /**
         * form init
         * @param {Object} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj = {}) {
            /* show modal */
            cF.handlebars.template(obj, "jrnl_sumry_reg_header");
            cF.handlebars.template(obj, "jrnl_sumry_reg", "show");

            /* jquery validation */
            cF.validate.validateForm("#jrnlSumryRegForm", JrnlSumry.regAjax);
            /* tagify */
            cF.tagify.initWithCtgr("#jrnlSumryRegForm #tagListStr", undefined);
            // tinymce editor reset
            cF.tinymce.init('#tinymce_jrnlSumryCn');
            cF.tinymce.setContentWhenReady("tinymce_jrnlSumryCn", obj.cn || "");
        },

        /**
         * 상세 화면으로 이동 (key로 조회)
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo) {
            if (isNaN(postNo)) return;

            $("#procForm #postNo").val(postNo);
            cF.util.blockUISubmit("#procForm", "${Url.JRNL_SUMRY_DTL!}");
        },

        /**
         * 상세 화면으로 이동 (년도로 조회)
         * @param {string|number} yy - 조회할 년도.
         */
        dtlByYy: function(yy) {
            if (isNaN(yy)) return;

            document.querySelector("#procForm #yy").value = yy;
            cF.util.blockUISubmit("#procForm", "${Url.JRNL_SUMRY_DTL!}");
        },

        /**
         * 목록 화면으로 이동
         */
        list: function() {
            cF.util.blockUIReplace("${Url.JRNL_SUMRY_LIST!}");
        },

        /**
         * 특정 년도 결산 생성 (Ajax)
         * @param {string|number} yy - 결산을 생성할 년도.
         */
        makeYySumryAjax: function(yy) {
            if (yy === undefined) yy = document.querySelector("#listForm #yy")?.value;
            if (cF.util.isEmpty(yy)) {
                cF.util.swalOrAlert("yy는 필수 항목입니다.");
                return false;
            }
            const url = "${Url.JRNL_SUMRY_MAKE_AJAX!}";
            const ajaxData = { "yy": yy };
            cF.ajax.post(url, ajaxData, function(res) {
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 전체 년도 결산 갱신 (Ajax)
         */
        makeTotalSumryAjax: function() {
            const url = "${Url.JRNL_SUMRY_MAKE_TOTAL_AJAX!}";
            cF.ajax.post(url, null, function(res) {
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
        },

        /**
         * 꿈 기록 완료 처리 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        comptAjax: function(postNo) {
            if (isNaN(postNo)) return;

            const url = "${Url.JRNL_SUMRY_DREAM_COMPT_AJAX!}";
            const ajaxData = { "postNo": postNo };
            cF.ajax.post(url, ajaxData, function(res) {
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rslt) cF.util.blockUIReload();
                    });
            }, "block");
        },

        /**
         * form submit
         */
        submit: function() {
            tinymce.get("tinymce_jrnlSumryCn").save();
            $("#jrnlSumryRegForm").submit();
        },

        /**
         * 등록(수정) 모달 호출
         * @param {string|number} postNo - 글 번호.
         */
        regModal: function(postNo) {
            if (isNaN(postNo)) return;

            const url = "${Url.JRNL_SUMRY_DTL_AJAX!}";
            const ajaxData = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                const { rsltObj } = res;
                /* initialize form. */
                JrnlSumry.initForm(rsltObj);
            });
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function() {
            Swal.fire({
                text: <@spring.message "view.cnfm.save"/>,
                showCancelButton: true,
        }).then(function(result) {
                if (!result.value) return;
                const url = "${Url.JRNL_SUMRY_REG_AJAX!}";
                const ajaxData = new FormData(document.getElementById("jrnlSumryRegForm"));
                cF.ajax.multipart(url, ajaxData, function(res) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (!res.rslt) return;
                            cF.util.blockUIReload();
                        });
                }, "block");
            });
        }
    }
})();