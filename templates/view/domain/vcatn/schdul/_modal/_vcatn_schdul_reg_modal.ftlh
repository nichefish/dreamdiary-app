<#--
 * 휴가일자(vcatn_schdul) > 휴가사용일자 > 휴가 관리자 직접 등록 Modal
 * @included_in : vcatn_schdul_list.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="vcatn_schdul_reg" size="md">
    <#--begin::Modal Header-->
    <@component.modal_header_dark "휴가 등록"  />
    <#--begin::Modal Body-->
    <div class="modal-body">
        <#--begin::Row-->
        <div class="row">
            <div class="col-xl-12 col-form-label">
                <div class="text-noti fs-small">※인트라넷 이외 방식(ex.메일)으로 처리된 휴가를 입력해주세요.</div>
                <div class="text-noti fs-small">※주말 및 공휴일은 자동으로 휴가일자 산정에서 제외됩니다.</div>
            </div>
        </div>
        <#--begin::Form-->
        <form name="vcantSchdulRegForm" id="vcantSchdulRegForm" class="form" enctype="multipart/form-data">
            <div id="vcatn_schdul_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.VcatnSchdul.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/vcatn/schdul/_modal/_vcatn_schdul_reg_modal_template.hbs">