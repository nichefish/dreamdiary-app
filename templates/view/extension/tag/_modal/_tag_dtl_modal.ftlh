<#--
 * 게시판 > 태그 상세 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="tag_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "태그"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div class="row" id="tag_dtl_div">
                <#--Handlebars template goes in here-->
            </div>

            <div class="separator separator-dashed border-gray-300 my-2"></div>

            <div class="row" id="tag_dtl_post_list_div">
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
<#include "_tag_dtl_modal_template.hbs">