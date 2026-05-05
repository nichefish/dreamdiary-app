<#--
 * 저널 일자(journal_day) > 등록/수정 Modal
 * @included_in : journal_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "저널 일자 조회" />
<@modal.layout id="journal_day_meta" size="xxl">
    <#--begin::Modal Header-->
    <@component.modal_header_with_back modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <div id="journal_day_meta_div">
            <#-- Vue JournalDayMetaModalBody 가 teleport 로 본문을 채운다. -->
        </div>
        <div id="journal_day_meta_vue_app" class="d-none"></div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "this.setAttribute('data-journal-day-action','close-modal'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
        </div>
    </div>
</@modal.layout>

<script>
    window.JournalDayMetaVueApp = window.JournalDayMetaVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/day/JournalDayMetaModalApp.js?${releaseDate!}"></script>
