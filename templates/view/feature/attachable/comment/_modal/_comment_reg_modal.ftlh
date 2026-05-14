<#--
 * 댓글 등록/수정 Modal
 * @included_in : 댓글 등록 기능이 있는 페이지들
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "댓글 저장" />
<@modal.layout id="comment_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <form name="commentRegForm" id="commentRegForm" enctype="multipart/form-data">
            <div id="comment_reg_modal_app"></div>
            <!--begin::Label-->
            <label class="mb-2" for="tinymce_commentCn">
                <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                <span class="text-gray-500 fs-9">&nbsp;</span>
            </label>
            <!--end::Label-->
            <textarea name="content" id="tinymce_commentCn" class="form-control required w-100" rows="3" maxlength="1000" placeholder="내용을 입력하세요."></textarea>
            <div id="commentCn_validate_span"></div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "window.dispatchEvent(new CustomEvent('comment:submit-reg-form'));" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--begin::JSON injection-->
<script type="application/json" id="comment_reg_modal_data">{"actvtyCtgrCd":"${actvtyCtgrCd!?js_string}"}</script>

<script type="module" src="/vue/feature/attachable/comment/CommentRegModal.js?${releaseDate!}"></script>