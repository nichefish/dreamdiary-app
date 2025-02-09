<#--
 * 게시판 > 태그 상세 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="tag_admin_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "태그"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div class="row" id="tag_admin_dtl_div">
                <#--Handlebars template goes in here-->
            </div>
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
<#include "_tag_admin_dtl_modal_template.hbs">