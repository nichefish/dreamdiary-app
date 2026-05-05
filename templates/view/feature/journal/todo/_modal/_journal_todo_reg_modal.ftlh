<#--
 * 저널 할일(journal_todo) > 등록/수정 Modal
 * @included_in : journal_day_page.ftlh
 *
 * 변경(T-2-α):
 *   - Handlebars 템플릿(_journal_todo_reg_modal_header_template.hbs / _journal_todo_reg_modal_template.hbs) 본문을
 *     Vue JournalTodoRegModalApp + JournalTodoRegModalHeader + JournalTodoRegModalBody 로 이전했다.
 *   - dF.JournalTodo.regModal/mdfModal/initForm/submit/regAjax 진입은 모두 제거되고
 *     window.JournalTodoRegVueApp.open(model) / .submit() 큐 단일 진입으로 수렴한다.
 *   - tinymce textarea 영역(#tinymce_journalTodoCn)은 기존 위치/구조 그대로 보존한다.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.diary.save") />
<@modal.layout id="journal_todo_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalTodoRegForm" id="journalTodoRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_todo_reg_modal_header_div">
                <#-- Vue JournalTodoRegModalHeader 가 teleport 로 헤더를 채운다. -->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_journalTodoCn">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="content" id="tinymce_journalTodoCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_journalTodoCn_validate_span"></div>
                </div>
            </div>
            <div id="journal_todo_reg_div">
                <#-- Vue JournalTodoRegModalBody 가 teleport 로 본문을 채운다. -->
            </div>
            <div id="journal_todo_reg_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기 (Vue 모달 큐의 submit 진입)-->
            <@component.modal_btn_save "window.JournalTodoRegVueApp && window.JournalTodoRegVueApp.submit && window.JournalTodoRegVueApp.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<script>
    /* 변경(T-2-α): Vue 측 진입 큐 stub. JournalTodoRegModalApp 가 마운트되면 mounted=true 로 갱신된다. */
    window.JournalTodoRegVueApp = window.JournalTodoRegVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/todo/JournalTodoRegModalApp.js?${releaseDate!}"></script>