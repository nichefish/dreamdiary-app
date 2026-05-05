<#--
 * 寃뚯떆??board_post) > ?깅줉/?섏젙 ?붾㈃
 * tinyMCE ?뺤콉 ?뚮Ц??臾몄꽌 理쒖긽?⑥뿉 <!DOCTYPE html> 異붽?
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

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

            <#--?섏씠吏:: 泥⑤??뚯씪 異붽?異붽? ?곸뿭-->
            <#include "/view/feature/file/_file_page_reg_area.ftlh">

            <!-- #include:: component:: ?뚯씪?쒖뒪??李몄“ -->

            <!-- #include:: component:: NOTION 李몄“ ID -->

            <!-- #include:: component:: ?쒓렇 ?쒓렇 -->
            <#include "/view/feature/attachable/tag/_tag_page_reg_area.ftlh">

            <div class="separator my-8"></div>

            <#--?섏씠吏:: ?붾뵒 ?뚮┝ 諛쒖넚 ?곸뿭-->
            <@component.jandi_reg_area 1 "" />
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--踰꾪듉 ?곸뿭-->
        <div class="d-flex justify-content-between">
            <#--?섏씠吏:: 踰꾪듉: 誘몃━蹂닿린-->
            <@component.post_btn_preview_pop "dF.BoardPost.preview();" />
            <div>
                <#--?섏씠吏:: 踰꾪듉: ??ν븯湲?->
                <@component.post_btn_submit "dF.BoardPost.submit();" />
                <#--?섏씠吏:: 踰꾪듉: 紐⑸줉?쇰줈 媛湲?->
                <@component.post_btn_toList "dF.BoardPost.list();" />
            </div>
        </div>
    </div>
</div>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/board/post/board_post_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/board/post/board_post_reg_form.js?${releaseDate!}"></script>

</@layout.layout>
