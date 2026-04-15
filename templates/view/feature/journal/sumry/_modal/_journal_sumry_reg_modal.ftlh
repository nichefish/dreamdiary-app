<#--
 * 저널 결산(journal_sumry) > 등록 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.sumry.save") />
<@modal.layout id="journal_sumry_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalSumryRegForm" id="journalSumryRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_sumry_reg_modal_header_div">
                <#--Handlebars template goes in here-->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalSumryCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.cn'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="cn" id="tinymce_journalSumryCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalSumryCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_sumry_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.JournalSumry.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_journal_sumry_reg_modal_header_template.hbs">
<#include "_journal_sumry_reg_modal_template.hbs">
