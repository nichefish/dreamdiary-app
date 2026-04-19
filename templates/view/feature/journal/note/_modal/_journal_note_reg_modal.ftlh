<#--
 * 저널 노트(journal_note) > 등록/수정 Modal
 * @included_in : journal_day_page.ftlh 등
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.note.save") />
<@modal.layout id="journal_note_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalNoteRegForm" id="journalNoteRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_note_reg_modal_header_div">
                <#--Handlebars template goes in here-->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalNoteCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="tinymce_journalNoteCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalNoteCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_note_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "dF.JournalNote.submit();" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_journal_note_reg_modal_header_template.hbs">
<#include "_journal_note_reg_modal_template.hbs">
