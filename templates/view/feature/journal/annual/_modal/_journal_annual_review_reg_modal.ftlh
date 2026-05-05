<#--
 * 저널 결산 리뷰(journal_annual_review) > 등록/수정 Modal
 * @included_in : journal_annual_dtl.ftlh
 *
 * 변경(A-3):
 *   - Handlebars 헤더/본문 템플릿(_journal_annual_review_reg_modal_header_template.hbs / _journal_annual_review_reg_modal_template.hbs) 을
 *     Vue JournalAnnualReviewRegModalApp + Header + Body 로 이전했다.
 *   - footer 저장 버튼은 JournalAnnualReviewRegVueApp.submit() 브리지가 dF.JournalAnnualReview.submit() 로 위임.
 *   - tagify 본문(_tag_tagify_partial.hbs)은 Vue 컴포넌트(JournalAnnualReviewRegModalBody) 로 옮겨 model 의 `tag.tagListStrWithCtgr` 등을 v-bind 한다.
 *   - Vue 모듈 스크립트는 include 반복 시 1회만 적재한다(journalAnnualReviewRegVueScriptDone).
 *
 * 변경(A-4-β):
 *   - classic `journal_annual_review_module.js` 적재를 제거하고, 본 가드 안에서 의존 ES module 을 단일 수렴한다.
 *   - 적재 순서: journalAnnualReviewCrudService → journalAnnualReviewService(`dF.JournalAnnualReview` 표면) → JournalAnnualReviewRegModalApp.
 *   - `journal_annual_dtl.ftlh` 하단의 review classic / ReviewCrud 중복 적재 제거.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.annual.review.save") />
<@modal.layout id="journal_annual_review_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalAnnualReviewRegForm" id="journalAnnualReviewRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_annual_review_reg_modal_header_div">
                <#-- Vue JournalAnnualReviewRegModalHeader teleport 대상 -->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalAnnualReviewCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="tinymce_journalAnnualReviewCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalAnnualReviewCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_annual_review_reg_div">
                <#-- Vue JournalAnnualReviewRegModalBody teleport 대상 — 기존 hbs body(_tag_tagify_partial.hbs) 동등(model 바인딩 보존). -->
            </div>
            <div id="journal_annual_review_reg_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "window.JournalAnnualReviewRegVueApp && window.JournalAnnualReviewRegVueApp.submit && window.JournalAnnualReviewRegVueApp.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#assign journalAnnualReviewRegVueScriptDone = journalAnnualReviewRegVueScriptDone!false>
<#if !journalAnnualReviewRegVueScriptDone>
<script>
    window.JournalAnnualReviewRegVueApp = window.JournalAnnualReviewRegVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualReviewCrudService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualReviewService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/JournalAnnualReviewRegModalApp.js?${releaseDate!}"></script>
<#assign journalAnnualReviewRegVueScriptDone = true>
</#if>
