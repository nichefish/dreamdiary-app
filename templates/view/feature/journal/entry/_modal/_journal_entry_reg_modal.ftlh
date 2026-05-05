<#--
 * journal_entry > register/modify modal shell
 * @included_in : journal pages
 *
 * 변경(E-2):
 *   - Handlebars 헤더/본문 템플릿(_journal_entry_reg_modal_*_template.hbs) 을
 *     Vue JournalEntryRegModalApp + Header/Body 로 이전했다.
 *   - footer 저장/미리보기 는 JournalEntryRegVueApp.submit/preview(contentType) 브리지.
 *   - Vue 모듈 스크립트는 include 반복 시 1회만 적재한다(journalEntryRegVueScriptDone).
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
<#assign entryRegPreviewHandler = "">
<#assign entryRegHeaderTemplateId = "journal_entry_reg_modal_header">
<#assign entryRegBodyTemplateId = "journal_entry_reg_modal">
<#assign entryRegUseLegacyStateHidden = false>
<#assign entryRegShowChapterSelect = true>
<#assign entryRegChapterCol = 1>
<#assign entryRegShowCategorySelect = false>
<#assign entryRegTitleColEdit = 10>
<#assign entryRegTitleColNew = 11>
<#assign entryRegSortMaxLength = 3>
<#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-day">
<#assign entryRegHideSortForElseDream = false>
<#assign entryRegShowTagify = false>
<#assign entryRegVueAppId = "journal_entry_reg_vue_app">

<#if entryRegType == "DIARY">
    <#assign entryRegModalId = "journal_diary_reg">
    <#assign entryRegFormId = "journalDiaryRegForm">
    <#assign entryRegHeaderDivId = "journal_diary_reg_modal_header_div">
    <#assign entryRegBodyDivId = "journal_diary_reg_div">
    <#assign entryRegVueAppId = "journal_diary_reg_vue_app">
    <#assign entryRegTinymceId = "tinymce_journalDiaryCn">
    <#assign entryRegModalTitleKey = "txt.journal.diary.save">
    <#assign entryRegSubmitHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.submit && window.JournalEntryRegVueApp.submit('JOURNAL_DIARY');">
    <#assign entryRegPreviewHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.preview && window.JournalEntryRegVueApp.preview('JOURNAL_DIARY');">
    <#assign entryRegHeaderTemplateId = "journal_diary_reg_modal_header">
    <#assign entryRegBodyTemplateId = "journal_diary_reg_modal">
    <#assign entryRegUseLegacyStateHidden = true>
    <#assign entryRegShowChapterSelect = true>
    <#assign entryRegChapterCol = 2>
    <#assign entryRegShowCategorySelect = true>
    <#assign entryRegTitleColEdit = 7>
    <#assign entryRegTitleColNew = 8>
    <#assign entryRegSortMaxLength = 3>
    <#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-day">
    <#assign entryRegShowTagify = true>
<#elseif entryRegType == "DREAM">
    <#assign entryRegModalId = "journal_dream_reg">
    <#assign entryRegFormId = "journalDreamRegForm">
    <#assign entryRegHeaderDivId = "journal_dream_reg_modal_header_div">
    <#assign entryRegBodyDivId = "journal_dream_reg_div">
    <#assign entryRegVueAppId = "journal_dream_reg_vue_app">
    <#assign entryRegTinymceId = "tinymce_journalDreamCn">
    <#assign entryRegModalTitleKey = "txt.journal.dream.save">
    <#assign entryRegSubmitHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.submit && window.JournalEntryRegVueApp.submit('JOURNAL_DREAM');">
    <#assign entryRegPreviewHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.preview && window.JournalEntryRegVueApp.preview('JOURNAL_DREAM');">
    <#assign entryRegHeaderTemplateId = "journal_dream_reg_modal_header">
    <#assign entryRegBodyTemplateId = "journal_dream_reg_modal">
    <#assign entryRegUseLegacyStateHidden = false>
    <#assign entryRegShowChapterSelect = false>
    <#assign entryRegChapterCol = 0>
    <#assign entryRegShowCategorySelect = false>
    <#assign entryRegTitleColEdit = 11>
    <#assign entryRegTitleColNew = 12>
    <#assign entryRegSortMaxLength = 2>
    <#assign entryRegSortTooltipKey = "bs.tooltip.journal.sort-order.in-dream-chapter">
    <#assign entryRegHideSortForElseDream = true>
    <#assign entryRegShowTagify = true>
<#elseif entryRegType == "NOTE">
    <#assign entryRegVueAppId = "journal_entry_reg_vue_app">
    <#assign entryRegSubmitHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.submit && window.JournalEntryRegVueApp.submit('JOURNAL_NOTE');">
    <#assign entryRegPreviewHandler = "window.JournalEntryRegVueApp && window.JournalEntryRegVueApp.preview && window.JournalEntryRegVueApp.preview('JOURNAL_NOTE');">
</#if>

<#assign entryRegModalTitle = springMacroRequestContext.getMessage(entryRegModalTitleKey)>

<@modal.layout id=entryRegModalId size="xl">
    <@component.modal_header entryRegModalTitle />
    <div class="modal-body modal-mbl-body my-5">
        <form name="${entryRegFormId}" id="${entryRegFormId}" class="form" method="post" enctype="multipart/form-data">
            <input type="hidden" name="type" value="${entryRegType}">
            <div id="${entryRegHeaderDivId}">
                <#-- Vue JournalEntryRegModalHeader teleport 대상 -->
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
                <#-- Vue JournalEntryRegModalBody teleport 대상 -->
            </div>
            <div id="${entryRegVueAppId}" class="d-none"></div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <button type="button" class="btn btn-sm btn-light-primary me-2"
                    onclick="${entryRegPreviewHandler!}"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    title="<@spring.message 'bs.tooltip.journal.entry.preview'/>">
                <i class="bi bi-eye"></i>미리보기
            </button>
            <@component.modal_btn_save entryRegSubmitHandler />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#assign journalEntryRegVueScriptDone = journalEntryRegVueScriptDone!false>
<#if !journalEntryRegVueScriptDone>
<script>
    window.JournalEntryRegVueApp = window.JournalEntryRegVueApp || {
        mounted: false,
        pendingPayloads: {}
    };
</script>
<#-- 변경(E-4-δ-3): journalEntryService 적재를 본 가드 안으로 단일 수렴.
     변경 전: 8개 페이지 ftlh 가 _journal_entry_module_script.ftlh 를 include 해 별도 적재.
     변경 후: 본 reg_modal 의 ES module 묶음에 포함하여 적재 그래프(import) 와 한곳에서 관리한다.
     적재 순서: journalEntryService(`dF.JournalEntry`) → 의존 service(SearchPopup/State/Crud) → Tag(`dF.JournalEntryTag`) → RegModalApp.
-->
<script type="module" src="/vue/feature/journal/entry/services/journalEntryService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/entry/services/journalEntrySearchPopupService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/entry/services/journalEntryStateService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/entry/services/journalEntryCrudService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/entry/services/journalEntryTagService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/entry/JournalEntryRegModalApp.js?${releaseDate!}"></script>
<#assign journalEntryRegVueScriptDone = true>
</#if>