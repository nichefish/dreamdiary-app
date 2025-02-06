<#--
 * 댓글 목록 Modal
 * @included_in : 댓글 미리보기 기능이 있는 페이지들
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="comment_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "댓글 저장"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <form name="commentRegForm" id="commentRegForm" enctype="multipart/form-data">
            <div class="row" id="comment_reg_div">
                <#--handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.Comment.modal.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/extension/comment/_modal/_comment_reg_modal_template.hbs">