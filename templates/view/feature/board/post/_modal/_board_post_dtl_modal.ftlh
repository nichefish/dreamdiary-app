<#--
 * 게시판(board_post) > 상세 Modal
 * @included_in : board_post_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "modal_title" />
<@modal.layout id="board_post_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="board_post_dtl_div">
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
<#include "_board_post_dtl_modal_template.hbs">