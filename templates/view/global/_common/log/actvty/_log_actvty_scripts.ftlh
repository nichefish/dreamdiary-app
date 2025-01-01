<#--
 * 활동 로그(log_actvty) : 스크립트 모듈 분리
 -->

<script>
    const LogActvty = (function() {
        return {
            /**
             * 목록 검색
             */
            search: function() {
                $("#listForm #pageNo").val(1);
                cF.util.blockUISubmit("#listForm", "${Url.LOG_ACTVTY_LIST!}?actionTyCd=SEARCH");
            },
            /**
             * 엑셀 다운로드
             */
            xlsxDownload: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.download"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    cF.util.blockUIFileDownload();
                    $("#listForm").attr("action", "${Url.LOG_ACTVTY_LIST_XLSX_DOWNLOAD!}").submit();
                });
            },

            /**
             * 상세 모달 호출
             * @param {string|number} logActvtyNo - 조회할 로그 번호.
             */
            dtlModal: function(logActvtyNo) {
                event.stopPropagation();
                if (isNaN(logActvtyNo)) return;

                const url = "${Url.LOG_ACTVTY_DTL_AJAX!}";
                const ajaxData = { "logActvtyNo": logActvtyNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltObj, "log_actvty_dtl", "show");
                });
            },
        }
    })();
</script>