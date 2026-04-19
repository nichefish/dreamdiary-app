<#--
 * 저널 일자(journal_day) > 등록/수정 Modal
 * @included_in : journal_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.day.save") />
<@modal.layout id="journal_day_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalDayRegForm" id="journalDayRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_day_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.JournalDay.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "dF.JournalDay.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_journal_day_reg_modal_template.hbs">
