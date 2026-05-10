<#--
 * 저널 일자 (journal_day) > 태그 상세 Modal
 * @included_in : journal_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="journal_day_tag_dtl" size="xxl">
    <#--begin::Modal Header-->
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">
            [<@spring.message 'txt.journal.day'/> 태그: <span class="header_tag_nm text-primary"></span>] -- <span class="header_tag_cnt text-muted"></span><span>개</span>
        </h5>
        <div>
            <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
                 onclick="ModalHistory.pop(); ModalHistory.prev();"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message 'bs.tooltip.modal.prev'/>">
                <i class="bi bi-box-arrow-in-left"></i>
            </div>
            <div class="btn btn-sm btn-icon btn-active-light-primary" aria-label="Close"
                 onClick="ModalHistory.pop();"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message 'bs.tooltip.modal.close'/>">
                <i class="fas fa-times"></i>
            </div>
        </div>
    </div>
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="journal_day_tag_dtl_div">
            <div class="d-flex flex-column mb-4">
                <div class="d-flex justify-content-start align-items-center gap-3 mb-4">
                    <label for="journal_day_tag_yy" class="form-label mb-0 fw-bold"><@spring.message 'txt.yy'/></label>
                    <select id="journal_day_tag_yy" class="form-select form-select-sm w-auto"></select>
                </div>
                <div id="journal_day_tag_dtl_list" class="d-flex flex-column"></div>
            </div>
        </div>
        <div id="journal_day_tag_dtl_app" class="d-none"></div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "this.setAttribute('data-journal-day-action','tag-close-modal'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
        </div>
    </div>
</@modal.layout>
