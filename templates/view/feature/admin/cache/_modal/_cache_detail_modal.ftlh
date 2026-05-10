<#--
 * 캐시 상세 Modal
 * @included_in : admin_page.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "사이트 캐시 목록 조회" />
<@modal.layout id="cache_detail" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header_with_back modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="cache_detail_div">
            <#-- Vue(CacheAdminApp)가 캐시 상세를 렌더링 -->
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
