<#--
 * 게시판 > 태그 전체 목록 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="board_tag_list" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "태그" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="board_tag_list_div">
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
<#include "/view/global/_common/_clsf/tag/_modal/_tag_list_modal_template.hbs">