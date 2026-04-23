<#--
 * journal_entry > register/modify modal shell
 * @included_in : journal pages
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign entryRegType = (journalEntryRegType!"")?upper_case>
<#assign entryRegModalId = "journal_entry_reg">
<#assign entryRegFormId = "journalEntryRegForm">
<#assign entryRegHeaderDivId = "journal_entry_reg_modal_header_div">
<#assign entryRegBodyDivId = "journal_entry_reg_div">
<#assign entryRegTinymceId = "tinymce_journalEntryCn">
<#assign entryRegModalTitleKey = "txt.content.save">
<#assign entryRegSubmitHandler = "">
<#assign entryRegHeaderTemplateId = "journal_entry_reg_modal_header">
<#assign entryRegBodyTemplateId = "journal_entry_reg_modal">
<#assign entryRegUseLegacyStateHidden = false>
<#assign entryRegChapterCol = 1>
<#assign entryRegShowCategorySelect = false>
<#assign entryRegTitleColEdit = 10>
<#assign entryRegTitleColNew = 11>
<#assign entryRegSortMaxLength = 3>
<#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-day">
<#assign entryRegHideSortForElseDream = false>
<#assign entryRegShowTagify = false>

<#if entryRegType == "DIARY">
    <#assign entryRegModalId = "journal_diary_reg">
    <#assign entryRegFormId = "journalDiaryRegForm">
    <#assign entryRegHeaderDivId = "journal_diary_reg_modal_header_div">
    <#assign entryRegBodyDivId = "journal_diary_reg_div">
    <#assign entryRegTinymceId = "tinymce_journalDiaryCn">
    <#assign entryRegModalTitleKey = "txt.journal.diary.save">
    <#assign entryRegSubmitHandler = "dF.JournalEntry.get('JOURNAL_DIARY').submit();">
    <#assign entryRegHeaderTemplateId = "journal_diary_reg_modal_header">
    <#assign entryRegBodyTemplateId = "journal_diary_reg_modal">
    <#assign entryRegUseLegacyStateHidden = true>
    <#assign entryRegChapterCol = 1>
    <#assign entryRegShowCategorySelect = true>
    <#assign entryRegTitleColEdit = 7>
    <#assign entryRegTitleColNew = 9>
    <#assign entryRegSortMaxLength = 3>
    <#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-day">
    <#assign entryRegShowTagify = true>
<#elseif entryRegType == "DREAM">
    <#assign entryRegModalId = "journal_dream_reg">
    <#assign entryRegFormId = "journalDreamRegForm">
    <#assign entryRegHeaderDivId = "journal_dream_reg_modal_header_div">
    <#assign entryRegBodyDivId = "journal_dream_reg_div">
    <#assign entryRegTinymceId = "tinymce_journalDreamCn">
    <#assign entryRegModalTitleKey = "txt.journal.dream.save">
    <#assign entryRegSubmitHandler = "dF.JournalEntry.get('JOURNAL_DREAM').submit();">
    <#assign entryRegHeaderTemplateId = "journal_dream_reg_modal_header">
    <#assign entryRegBodyTemplateId = "journal_dream_reg_modal">
    <#assign entryRegUseLegacyStateHidden = false>
    <#assign entryRegChapterCol = 2>
    <#assign entryRegShowCategorySelect = true>
    <#assign entryRegTitleColEdit = 7>
    <#assign entryRegTitleColNew = 8>
    <#assign entryRegSortMaxLength = 2>
    <#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-dream-chapter">
    <#assign entryRegHideSortForElseDream = true>
    <#assign entryRegShowTagify = true>
</#if>

<#assign entryRegModalTitle = springMacroRequestContext.getMessage(entryRegModalTitleKey)>

<@modal.layout id=entryRegModalId size="xl">
    <@component.modal_header entryRegModalTitle />
    <div class="modal-body modal-mbl-body my-5">
        <form name="${entryRegFormId}" id="${entryRegFormId}" class="form" enctype="multipart/form-data">
            <div id="${entryRegHeaderDivId}">
                <#--Handlebars template goes in here-->
            </div>
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="${entryRegTinymceId}">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="${entryRegTinymceId}" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="${entryRegTinymceId}_validate_span"></div>
                </div>
            </div>
            <div id="${entryRegBodyDivId}">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save entryRegSubmitHandler />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal_header_template.hbs">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal_body_template.hbs">
