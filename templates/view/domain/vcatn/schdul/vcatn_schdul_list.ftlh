<#--
 * 휴가일자(vcatn_schdul) > 휴가사용일자 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/vcatn/schdul/_vcatn_schdul_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<#include "/view/domain/vcatn/schdul/_vcatn_schdul_scripts.ftlh">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                cF.datepicker.singleDatePicker("#bgnDt", "yyyy-MM-DD", null, function() {
                    Page.noBefore("#bgnDt", "#endDt");
                });
                cF.datepicker.singleDatePicker("#endDt", "yyyy-MM-DD", null, function() {
                    if ($("#endDt").val() !== "") Page.noBefore("#bgnDt", "#endDt");
                });

                /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
                cF.util.initSortTable();

                /* jquery validation */
                $(function() {
                    $("#vcantSchdulRegForm").validate({
                        submitHandler: function() {
                            let isReg = ($("#vcatnSchdulNo").val() === "");
                            Swal.fire({
                                text: isReg ? <@spring.message "view.cnfm.reg"/> : <@spring.message "view.cnfm.mdf"/>,
                                showCancelButton: true,
                            }).then(function(result) {
                                if (!result.value) return;
                                VcatnSchdul.regAjax();
                            });
                        },
                        errorPlacement: function(error, element) {
                            cF.validate.errorSpan(error, element);        // 공통 함수로 분리
                        }
                    });
                });
            },

            /**
             * 유효성 검사:: 시작일 이전 날짜 선택 불가하게 막기
             */
            noBefore: function(beginSelectorStr, endSelectorStr) {
                const isBefore = cF.date.isBefore(beginSelectorStr, endSelectorStr);
                const vcatnCd = select = $("#vcatnCd").children('option:selected').val();
                if (select === "AM_HALF" || select === "PM_HALF") {
                    $(endSelectorStr).val($(beginSelectorStr).val());
                } else if (result === false && $(endSelectorStr).val() !== "") {
                    Swal.fire("접속 IP는 최소 한 개 이상 입력해야 합니다.");
                    $(endSelectorStr).val("");
                }
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable">
            <#--begin::Table head-->
            <thead class="cursor-pointer">
                <tr class="text-start text-muted fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="w-90px text-center wb-keepall w-10">번호</th>
                    <th class="min-w-180px text-center wb-keepall w-20">휴가일</th>
                    <th class="min-w-180px text-center wb-keepall w-10">휴가자</th>
                    <th class="min-w-120px text-center wb-keepall w-10">휴가구분</th>
                    <th class="min-w-120px text-center wb-keepall w-10">소진일</th>
                    <th class="min-w-100px text-center wb-keepall w-20">휴가사유</th>
                    <th class="min-w-100px text-center wb-keepall w-20">비고</th>
                    <th class="min-w-100px text-center wb-keepall w-10">관리</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="cursor-pointer">
                <#list vcatnSchdulList![] as vcatn>
                    <tr>
                        <td class="text-center">${vcatn?counter}</td>
                        <td class="text-center">${vcatn.bgnDt!} ~ ${vcatn.endDt!}</td>
                        <td class="text-center">${vcatn.userNm!}</td>
                        <td class="text-center">${vcatn.vcatnNm!}</td>
                        <td class="text-center">${vcatn.vcatnExprDy!}</td>
                        <td class="text-center">${vcatn.resn!}</td>
                        <td class="text-center">${vcatn.rm!}</td>
                        <td class="text-center">
                            <#if (vcatn.vcatnSchdulNo?? && vcatn.userId??)>
                                <button type="button" class="badge badge-primary" onclick="VcatnSchdul.mdfModal(${vcatn.vcatnSchdulNo!});">수정</button>
                                <button type="button" class="badge badge-primary" onclick="VcatnSchdul.delAjax(${vcatn.vcatnSchdulNo!});">삭제</button>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="vcatnSchdulNo" id="vcatnSchdulNoProc" />
    <input type="hidden" name="statsYy" id="statsYy" value="${(vcatnYy.statsYy)!}">
</form>

<#include "/view/domain/vcatn/schdul/_vcatn_schdul_reg_modal.ftlh">

</@layout.layout>