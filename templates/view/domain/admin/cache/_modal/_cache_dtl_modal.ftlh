<#--
 * 캐시 상세 Modal
 * @included_in : admin_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="cache_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header_with_back "사이트 캐시 목록 조회" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="cache_dtl_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "dF.Cache.closeModal();" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_cache_dtl_modal_template.hbs">
<#include "_cache_entry_partial.hbs">
