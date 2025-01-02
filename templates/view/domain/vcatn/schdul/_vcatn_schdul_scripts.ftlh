<#--
 * 휴가일자(vcatn_schdul) : 스크립트 모듈 분리
 -->

<script>
    const VcatnSchdul = (function() {
        return {
            /** 조회년도 변경 */
            chgYy: function(obj) {
                cF.util.blockUIReplace("${Url.VCATN_SCHDUL_LIST!}?statsYy=" + $(obj).val());
            },

            /**
             * 등록 모달 호출
             */
            regModal: function() {
                cF.handlebars.template({}, "vcatn_schdul_reg", "show");
                cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", null, function() {
                    VcatnSchdul.propEndDt();
                    VcatnSchdul.noBefore("#bgnDt", "#endDt");
                });
                cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", null, function() {
                    if ($("#endDt").val() !== "") VcatnSchdul.noBefore("#bgnDt", "#endDt");
                });
            },

            /**
             * 반차 여부 체크
             */
            isHalf: function(idx) {
                if (idx === undefined) idx = "";
                const vcatnCd = $("#vcatnCd" + idx).children('option:selected').val();
                return vcatnCd === "${Constant.VCATN_AM_HALF}" || vcatnCd === "${Constant.VCATN_PM_HALF}";
            },

            /**
             * 시작일 이전 날짜 선택 불가
             */
            noBefore: function(beginSelectorStr, endSelectorStr, idx) {
                if (VcatnSchdul.isHalf(idx)) {
                    $(endSelectorStr).val($(beginSelectorStr).val());
                } else {
                    const isBefore = cF.date.isBefore(beginSelectorStr, endSelectorStr);
                    if (!isBefore && $(endSelectorStr).val() !== "") {
                        Swal.fire("휴가일을 다시 설정해 주세요.");
                        $(endSelectorStr).val("");
                    }
                }
            },

            /**
             * form submit
             */
            submit: function() {
                $("#vcantSchdulRegForm").submit();
            },

            /**
             * 등록/수정 처리(Ajax)
             */
            regAjax: function() {
                const isReg = ($("#vcatnSchdulNo").val() === "");
                const url = isReg ? '${Url.VCATN_SCHDUL_REG_AJAX!}' : '${Url.VCATN_SCHDUL_MDF_AJAX!}';
                const ajaxData = new FormData(document.getElementById("vcantSchdulRegForm"));
                cF.ajax.multipart(url, ajaxData, function(res) {
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rslt) cF.util.blockUIReload();
                        });
                }, "block");
            },

            /**
             * 반차 선택 시 종료일 안보이게
             */
            propEndDt: function(idx) {
                if (idx === undefined) idx = "";
                if (VcatnSchdul.isHalf(idx)) {
                    $("#lineDiv"+idx).addClass("d-none");
                    $("#endDtDiv"+idx).addClass("d-none");
                    $("#endDt"+idx).val($("#bgnDt"+idx).val());
                    $("#endDt"+idx+"_validate_span").empty();
                } else {
                    $("#lineDiv"+idx).removeClass("d-none");
                    $("#endDtDiv"+idx).removeClass("d-none");
                }
            },

            /**
             * 수정 팝업 호출
             */
            mdfModal: function(vcatnSchdulNo) {
                if (isNaN(vcatnSchdulNo)) return;

                $("#vcatnSchdulNoProc").val(vcatnSchdulNo);
                const url = "${Url.VCATN_SCHDUL_DTL_AJAX!}";
                const ajaxData = $("#procForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res) {
                    if (!res.rslt) return;
                    const { rsltObj } = res;
                    // 수정 modal_form에 값 세팅
                    $("#vcatnSchdulNo").val(vcatnSchdulNo);
                    $("#userId").val(rsltObj.userId);
                    $("#vcatnCd").val(rsltObj.vcatnCd);
                    $("#bgnDt").val(rsltObj.bgnDt);
                    $("#endDt").val(rsltObj.endDt);
                    $("#resn").val(rsltObj.resn);
                    $("#vcatnRm").val(rsltObj.vcatnRm);
                    $("#vcatn_schdul_reg_modal").modal("show");
                });
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
                    $("#procForm").attr("action", "${Url.VCATN_SCHDUL_XLSX_DOWNLOAD!}").submit();
                });
            },

            /**
             * 삭제 (Ajax)
             * @param {string|number} vcatnSchdulNo - 일정 번호.
             */
            delAjax: function(vcatnSchdulNo) {
                if (isNaN(vcatnSchdulNo)) return;

                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    $("#vcatnSchdulNoProc").val(vcatnSchdulNo);
                    const url = "${Url.VCATN_SCHDUL_DEL_AJAX!}";
                    const ajaxData = $("#procForm").serializeArray();
                    cF.ajax.post(url, ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReload();
                            });
                    }, "block");
                });
            }
        }
    })();
</script>