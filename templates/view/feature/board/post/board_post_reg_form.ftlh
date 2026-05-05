<#--
 * 게시판(board_post) > 등록/수정 화면
 * tinyMCE 에디터 적용 때문에 문서 최상단에 <!DOCTYPE html> 추가
-->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<div id="board_post_reg_form_app" class="d-none"></div>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <!--begin:Form-->
        <form name="postRegForm" id="postRegForm" class="form" data-mode="${formMode!}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="id" value="${post.id!}">
            <input type="hidden" name="board" id="board" value="<#if isMdf!false>${post.board!}<#else>${board!}</#if>">
            <input type="hidden" name="fileGroupId" value="${post.fileGroupId!}">
            <#--begin::Row-->
            <div class="row d-flex mb-8">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="title">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.title'/></span>
                        <span class="text-gray-500 fs-9">(理쒕? 50??</span>
                    </label>
                </div>
                <div class="col-lg-2">
                    <select name="categoryCode" id="categoryCode" class="form-select form-select-solid"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="湲遺꾨쪟瑜??좏깮?⑸땲??">
                        <option value="">--湲遺꾨쪟--</option>
                        <#list POST_CTGR_CD![] as ctgr>
                            <option value="${ctgr.code!}" id="${ctgr.code!}"<#if ctgr.code == (post.categoryCode)!> selected="selected"</#if>>[${ctgr.codeName!}]</option>
                        </#list>
                    </select>
                </div>
                <div class="col-lg-10">
                    <input type="text" name="title" id="title" class="form-control required"
                           value="${post.title!}" maxlength="100" />
                    <span id="title_validate_span"></span>
                </div>
            </div>
            <div class="separator my-8"></div>

            <div class="d-flex flex-column mb-8">
                <!--begin::Label-->
                <label class="mb-2" for="tinymce_content">
                    <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    <span class="text-gray-500 fs-9">&nbsp;</span>
                </label>
                <!--end::Label-->
                <!--begin::Form control-->
                <div class="mb-3">
                    <textarea name="content" id="tinymce_content" class="form-control required" rows="10" placeholder="내용을 입력하세요.">${post.content!}</textarea>
                </div>
                <div id="tinymce_content_validate_span"></div>
                <!--end::Form control-->
            <#--페이지:: 첨부파일 추가 영역-->

            <#--페이지:: 첨부파일 추가 영역-->
            <#include "/view/feature/file/_file_page_reg_area.ftlh">

            <!-- #include:: component:: 파일소스 참조 -->

            <!-- #include:: component:: NOTION 참조 ID -->

            <!-- #include:: component:: 태그 영역 -->
            <#include "/view/feature/attachable/tag/_tag_page_reg_area.ftlh">

            <div class="separator my-8"></div>

            <#--페이지:: 잔디 알림 발송 영역-->
            <@component.jandi_reg_area 1 "" />
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-between">
            <#--페이지:: 버튼: 미리보기-->
            <@component.post_btn_preview_pop "window.dispatchEvent(new CustomEvent('board-post:form-preview'));" />
            <div>
                <#--페이지:: 버튼: 저장하기-->
                <@component.post_btn_submit "window.dispatchEvent(new CustomEvent('board-post:form-submit'));" />
                <#--페이지:: 버튼: 목록으로 가기-->
                <@component.post_btn_toList "window.dispatchEvent(new CustomEvent('board-post:form-list'));" />
            </div>
        </div>
    </div>
</div>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::vue module-->
<script type="module" src="/vue/feature/board/post/BoardPostRegFormApp.js?${releaseDate!}"></script>

</@layout.layout>
