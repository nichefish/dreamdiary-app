<#--
 * 댓글 목록 Modal
 * @included_in : 댓글 미리보기 기능이 있는 페이지들
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "댓글 목록 조회" />
<@modal.layout id="comment_list" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="comment_list_modal_app"></div>
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

<#--begin::JSON injection-->
<script type="application/json" id="comment_list_modal_data">{"actvtyCtgrCd":"${actvtyCtgrCd!?js_string}"}</script>

<script type="module" src="/vue/feature/attachable/comment/CommentListModal.js?${releaseDate!}"></script>