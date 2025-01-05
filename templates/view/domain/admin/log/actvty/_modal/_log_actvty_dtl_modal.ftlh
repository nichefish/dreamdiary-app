<#--
 * 활동 로그(log_actvty) > 상세 Modal
 * @included_in : log_actvty_list.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="log_actvty_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "활동 로그 조회" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="log_actvty_dtl_div">
                <#--Handlebars template goes in here-->
            </div>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/admin/log/actvty/_modal/_log_actvty_dtl_modal_template.hbs">