<#--
 * 파일시스템 메타(flsys_meta) > 상세 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="flsys_meta_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "파일시스템 메타 정보 조회"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="flsys_meta_dtl_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 수정 모달 호출-->
            <@component.modal_btn_modify "FlsysMeta.mdfModal(FlsysMeta.key);" />
            <#--모달:: 버튼: 삭제-->
            <@component.modal_btn_delete "dF.Schdul.delAjax(FlsysMeta.key);" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_flsys_meta_dtl_template.hbs">
