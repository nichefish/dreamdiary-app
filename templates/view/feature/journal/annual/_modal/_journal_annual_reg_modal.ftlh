<#--
 * 저널 결산(journal_annual) > 등록/수정 Modal
 * @included_in : journal_annual_list.ftlh
 *
 * 변경(A-3):
 *   - Handlebars 헤더/본문 템플릿(_journal_annual_reg_modal_header_template.hbs / _journal_annual_reg_modal_template.hbs) 을
 *     Vue JournalAnnualRegModalApp + Header 로 이전했다.
 *   - footer 저장 버튼은 JournalAnnualRegVueApp.submit() 브리지가 dF.JournalAnnual.submit() 로 위임.
 *   - Vue 모듈 스크립트는 include 반복 시 1회만 적재한다(journalAnnualRegVueScriptDone).
 *   - tagify 본문(_tag_tagify_partial.hbs)은 Vue 컴포넌트(JournalAnnualRegModalBody) 로 옮겨 model 의 `tag.tagListStrWithCtgr` 등을 v-bind 한다. 기존 hbs body 호출 시 Handlebars 가 컴파일하던 동일 데이터 흐름을 보존한다.
 *
 * 변경(A-4-β):
 *   - classic `journal_annual_module.js` 적재를 제거하고, 본 가드 안에서 의존 ES module 을 단일 수렴한다.
 *   - 적재 순서: journalAnnualCrudService → journalAnnualStateService → journalAnnualService(`dF.JournalAnnual` 표면) → JournalAnnualRegModalApp.
 *   - `journal_annual_list.ftlh` 하단의 동일 묶음 제거 — 결산 등록 모달 include 가 list 페이지의 유일 진입점이다.
 *   - `journal_annual_dtl.ftlh` 는 본 모달을 포함하지 않으며, 페이지 하단에서 동일 의존 순으로 journalAnnualService 만 적재한다(등록 모달 스텁 없음은 기존과 동일).
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.annual.save") />
<@modal.layout id="journal_annual_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalAnnualRegForm" id="journalAnnualRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_annual_reg_modal_header_div">
                <#-- Vue JournalAnnualRegModalHeader teleport 대상 -->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalAnnualCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="tinymce_journalAnnualCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalAnnualCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_annual_reg_div">
                <#-- Vue JournalAnnualRegModalBody teleport 대상 — 기존 Handlebars body(_tag_tagify_partial.hbs)는 본 컴포넌트가 동일 DOM 으로 옮겨 렌더한다(model 의 `{{tag.tagListStrWithCtgr}}` 바인딩 유지). -->
            </div>
            <div id="journal_annual_reg_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "window.JournalAnnualRegVueApp && window.JournalAnnualRegVueApp.submit && window.JournalAnnualRegVueApp.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#assign journalAnnualRegVueScriptDone = journalAnnualRegVueScriptDone!false>
<#if !journalAnnualRegVueScriptDone>
<script>
    window.JournalAnnualRegVueApp = window.JournalAnnualRegVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualCrudService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualStateService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/JournalAnnualRegModalApp.js?${releaseDate!}"></script>
<#assign journalAnnualRegVueScriptDone = true>
</#if>
