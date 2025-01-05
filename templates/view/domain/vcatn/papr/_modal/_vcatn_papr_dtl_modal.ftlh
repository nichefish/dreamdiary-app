<#--
 * 휴가계획서(vcatn_papr) > 상세 Modal
 * @included_in : vcatn_papr_list.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="vcatn_papr_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "휴가계획서" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="vcatn_papr_dtl_div">
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
<#include "/view/domain/vcatn/papr/_modal/_vcatn_papr_dtl_modal_template.hbs">