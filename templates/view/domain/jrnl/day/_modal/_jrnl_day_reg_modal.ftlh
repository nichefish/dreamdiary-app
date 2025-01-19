<#--
 * 저널 일자(jrnl_day) > 등록/수정 Modal
 * @included_in : jrnl_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="jrnl_day_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "저널 일자 저장"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="jrnlDayRegForm" id="jrnlDayRegForm" class="form" enctype="multipart/form-data">
            <div id="jrnl_day_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.JrnlDay.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "dF.JrnlDay.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/jrnl/day/_modal/_jrnl_day_reg_modal_template.hbs">