<#--
 * 저널 일자(journal_day) > 등록/수정 Modal
 * @included_in : journal_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("txt.journal.day.save") />
<@modal.layout id="journal_day_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="journalDayRegForm" id="journalDayRegForm" class="form" enctype="multipart/form-data">
            <div id="journal_day_reg_div">
                <#-- Vue JournalDayRegModalBody 가 teleport 로 본문을 채운다. -->
            </div>
            <div id="journal_day_reg_vue_app" class="d-none"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "this.setAttribute('data-journal-day-action','submit-form'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "this.setAttribute('data-journal-day-action','close-modal'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
        </div>
    </div>
</@modal.layout>

<script>
    window.JournalDayRegVueApp = window.JournalDayRegVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/day/JournalDayRegModalApp.js?${releaseDate!}"></script>
