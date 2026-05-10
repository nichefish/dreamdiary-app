<#--
 * 저널 해석(journal_interpretation) > 등록/수정 Modal
 * @included_in : journal pages
 *
 * 변경(I-2):
 *   - Handlebars 헤더/본문 템플릿(_journal_interpretation_regist_modal_header_template.hbs / _journal_interpretation_regist_modal_template.hbs) 을
 *     Vue JournalInterpretationRegistModalApp + Header 로 이전했다.
 *   - footer 저장 버튼은 JournalInterpretationRegistVueApp.submit() 브리지가 dF.JournalInterpretation.submit() 로 위임.
 * 변경(I-3):
 *   - ES module 적재 순서: journalInterpretationStateService → journalInterpretationCrudService → JournalInterpretationRegistModalApp.
 *     (`dF.JournalInterpretation` thin 위임이 Crud/State 를 참조하므로 부트 시점보다 사용자 조작 시점이 안전하며, 동일 가드에서 순서 고정.)
 *   - Vue 모듈 스크립트는 include 반복 시 1회만 적재한다(journalInterpretationRegistVueScriptDone).
 * 변경(I-4):
 *   - classic `journal_interpretation_module.js` 적재(5개 ftlh 의 `<script type="text/javascript">` 1줄)를 제거하고,
 *     본 가드 안에서 `journalInterpretationService.js` 를 *최우선* 적재한다(`dF.JournalInterpretation` 표면 등록).
 *   - 적재 순서: journalInterpretationService → State → Crud → RegistModalApp.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.interpretation.save") />
<@modal.layout id="journal_interpretation_regist" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalInterpretationRegistForm" id="journalInterpretationRegistForm" class="form" enctype="multipart/form-data">
            <div id="journal_interpretation_regist_modal_header_div">
                <#-- Vue JournalInterpretationRegistModalHeader teleport 대상 -->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalInterpretationCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="tinymce_journalInterpretationCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalInterpretationCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_interpretation_regist_div">
                <#-- 빈 자리 — 기존 Handlebars 빈 body 동등(dead body 흡수). -->
            </div>
            <div id="journal_interpretation_regist_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "window.JournalInterpretationRegistVueApp && window.JournalInterpretationRegistVueApp.submit && window.JournalInterpretationRegistVueApp.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#assign journalInterpretationRegistVueScriptDone = journalInterpretationRegistVueScriptDone!false>
<#if !journalInterpretationRegistVueScriptDone>
<script>
    window.JournalInterpretationRegistVueApp = window.JournalInterpretationRegistVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/interpretation/services/journalInterpretationService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/interpretation/services/journalInterpretationStateService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/interpretation/services/journalInterpretationCrudService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/interpretation/JournalInterpretationRegistModalApp.js?${releaseDate!}"></script>
<#assign journalInterpretationRegistVueScriptDone = true>
</#if>
