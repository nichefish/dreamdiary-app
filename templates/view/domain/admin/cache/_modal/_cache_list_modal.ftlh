<#--
 * 캐시 목록 Modal
 * @included_in : admin_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="cache_list" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "사이트 캐시 목록 조회" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="cache_list_div">
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
<#include "/view/domain/admin/cache/_modal/_cache_list_modal_template.hbs">