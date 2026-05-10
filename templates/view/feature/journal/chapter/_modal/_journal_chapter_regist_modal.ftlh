<#--
 * 저널 챕터(journal_chapter) > 등록/수정 Modal
 * @included_in : journal_day_*.ftlh / journal_entry_search.ftlh
 *
 * 변경(Phase B):
 *   - Handlebars 템플릿(_journal_chapter_regist_modal_template.hbs) 본문을
 *     Vue JournalChapterRegistModalApp + JournalChapterRegistModalBody 로 이전했다.
 *   - dF.JournalChapter.registModal/modifyModal/initForm/submit/registAjax 진입은 모두 제거되고
 *     window.JournalChapterRegistVueApp.open(model) / .submit() 큐 단일 진입으로 수렴한다.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.chapter.save") />
<@modal.layout id="journal_chapter_regist" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalChapterRegistForm" id="journalChapterRegistForm" class="form" enctype="multipart/form-data">
            <div id="journal_chapter_regist_div">
                <#-- Vue JournalChapterRegistModalBody 가 teleport 로 본문을 채운다. -->
            </div>
            <div id="journal_chapter_regist_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기 (Vue 모달 큐의 submit 진입)-->
            <@component.modal_btn_save "window.JournalChapterRegistVueApp && window.JournalChapterRegistVueApp.submit && window.JournalChapterRegistVueApp.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<script>
    window.JournalChapterRegistVueApp = window.JournalChapterRegistVueApp || {
        mounted: false,
        pendingPayload: null
    };
    /* 변경(Phase B): JOURNAL_CHAPTER_CTGR_CD 모델을 Vue 측에 노출 (FTL → JS 단일 적재 SSOT). */
    window.__journalChapterRegistBootstrap = window.__journalChapterRegistBootstrap || (function() {
        var categoryOptions = [
            <#list JOURNAL_CHAPTER_CTGR_CD![] as ctgr>
            { code: "${ctgr.code!''?js_string}", codeName: "${ctgr.codeName!''?js_string}" }<#if ctgr_has_next>,</#if>
            </#list>
        ];
        return { categoryOptions: categoryOptions };
    })();
</script>
<script type="module" src="/vue/feature/journal/chapter/JournalChapterRegistModalApp.js?${releaseDate!}"></script>
