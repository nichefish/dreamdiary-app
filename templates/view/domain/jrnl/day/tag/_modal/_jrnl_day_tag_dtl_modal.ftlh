<#--
 * 저널 일자 (jrnl_day) > 태그 상세 Modal
 * @included_in : jrnl_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="jrnl_day_tag_dtl" size="xxl">
    <#--begin::Modal Header-->
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">
            [<@spring.message 'jrnl.day'/> 태그: <span class="header_tag_nm text-primary"></span>]
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
        <div id="jrnl_day_tag_dtl_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "dF.JrnlDayTag.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_jrnl_day_tag_dtl_modal_template.hbs">