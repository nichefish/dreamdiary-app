<#--
 * 저널 태그(jrnl_tag) > 목록 Modal
 * @included_in : jrnl_day_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="jrnl_tag_list" size="xl">
    <#--begin::Modal Header-->
    <div class="modal-header">
        <h5 id="modal_title" class="modal-title">
            [<@spring.message 'jrnl.dream'/> 태그 전체 목록]
        </h5>
        <div class="d-flex gap-4">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.Tag.hideSingleTag('#jrnl_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <div class="btn btn-sm btn-icon btn-active-light-primary ms-2" aria-label="Close"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="modal" title="<@spring.message 'bs.tooltip.modal.close'/>">
                <i class="fas fa-times"></i>
            </div>
        </div>
    </div>
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="jrnl_tag_ctgr_div">
            <#--Handlebars template goes in here-->
        </div>
        <hr />
        <div id="jrnl_tag_list_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_jrnl_tag_list_ctgr_template.hbs">
<#include "_jrnl_tag_list_modal_template.hbs">
