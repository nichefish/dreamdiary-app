<#--
 * 저널 일자 (jrnl_day) > 태그 상세 Modal
 * @included_in : jrnl_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="jrnl_day_tag_dtl" size="xxl" module="dF.JrnlDayTag">
    <#--begin::Modal Header-->
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">
            [저널 일자 태그: <span class="header_tag_nm text-primary"></span> 전체보기]
        </h5>
        <div>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.JrnlDreamTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message "bs.tooltip.modal.close"/>">
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
            <@component.modal_btn_close_safe "${module!}.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/jrnl/day/tag/_modal/_jrnl_day_tag_dtl_modal_template.hbs">