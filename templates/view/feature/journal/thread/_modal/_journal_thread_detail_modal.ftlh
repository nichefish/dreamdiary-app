<#--
 * journal-thread detail quick view modal.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "저널 스레드 조회" />
<@modal.layout id="journal_thread_detail" size="xxl">
    <@component.modal_header_with_back modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="journal_thread_detail_div"></div>
        <div id="journal_thread_detail_vue_app" class="d-none"></div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<script>
    window.JournalThreadDetailVueApp = window.JournalThreadDetailVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/thread/JournalThreadDetailModalApp.js?${releaseDate!}"></script>
