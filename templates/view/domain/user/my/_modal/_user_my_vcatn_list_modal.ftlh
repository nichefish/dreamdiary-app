<#--
 * 내 정보(user_my) > 휴가사용현황 Modal
 * @included_in : user_my_dtl
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="user_my_dtl_vcatn" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "${vcatnYy.statsYy}년 휴가 상세 현황" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <#--begin::Table-->
            <table class="table table-row-bordered table-row-gray-300 gy-2 align-middle fs-small">
                <#--begin::Table body-->
                <tbody>
                    <#list vcatnPaprList![] as vcatnList>
                        <tr>
                            <td class="text-start">
                                <#list vcatnList.schdulList![] as schdul>
                                    <span class="pe-4">
                                        <span class="ctgr-span ctgr-gray">${schdul.vcatnNm!}</span>
                                        ${schdul.bgnDt!}
                                        <#if schdul.endDt?? && schdul.bgnDt != schdul.endDt>~ ${schdul.endDt!}</#if>
                                    </span>
                                </#list>
                            </td>
                        </tr>
                    <#else>
                        <tr>
                            <td colspan=8" class="text-center"><@spring.message "view.list.empty"/></td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
</@modal.layout>
