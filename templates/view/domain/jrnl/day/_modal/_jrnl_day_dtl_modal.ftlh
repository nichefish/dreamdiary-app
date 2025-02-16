<#--
 * 저널 일자(jrnl_day) > 등록/수정 Modal
 * @included_in : jrnl_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "저널 일자 조회" />
<@modal.layout id="jrnl_day_dtl" size="xxl">
    <#--begin::Modal Header-->
    <@component.modal_header_with_back modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <div id="jrnl_day_dtl_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "dF.JrnlDay.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_jrnl_day_dtl_modal_template.hbs">