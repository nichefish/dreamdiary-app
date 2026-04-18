<#--
 * 저널 주제(journal_sbjct) > 등록 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = isReg!false>
<#assign isMdf = isMdf!false>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <div class="card-body">
        <form name="journalSbjctRegForm" id="journalSbjctRegForm" class="form" data-mode="${formMode!}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="id" value="${post.id!}">
            <input type="hidden" name="fileGroupId" value="${post.fileGroupId!}">

            <div class="row d-flex mb-8">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="title">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.title'/></span>
                        <span class="text-gray-500 fs-9">(최대 50자)</span>
                    </label>
                </div>
                <div class="col-lg-2">
                    <select name="categoryCode" id="categoryCode" class="form-select form-select-solid">
                        <option value="">--글분류--</option>
                        <#list JOURNAL_SBJCT_CTGR_CD![] as ctgr>
                            <option value="${ctgr.code!}" <#if ctgr.code == (post.categoryCode)!>selected="selected"</#if>>[${ctgr.codeName!}]</option>
                        </#list>
                    </select>
                </div>
                <div class="col-lg-10">
                    <input type="text" name="title" id="title" class="form-control required"
                           value="${post.title!}" maxlength="100" />
                    <span id="title_validate_span"></span>
                </div>
            </div>

            <#if isMdf!false>
                <div class="row">
                    <div class="col-lg-1 col-3 d-flex align-items-center">
                        <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" title="미체크시 수정 일자가 기존 일자로 유지됩니다. (기본: ON)">
                            수정일자 변경 <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                    <div class="col-lg-2 col-9 d-flex align-items-center">
                        <@component.checkbox post "managt.managtDtUpdtYn" "변경//미변경", "blue//gray", "defaultOn" />
                    </div>
                </div>
            </#if>

            <div class="separator my-8"></div>

            <div class="d-flex flex-column mb-8">
                <label class="mb-2 d-flex align-items-center" for="tinymce_content">
                    <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    <span class="text-gray-500 fs-9">&nbsp;</span>
                </label>
                <div class="mb-3">
                    <textarea name="content" id="tinymce_content" class="form-control required" rows="10" placeholder="내용을 입력하세요.">${post.content!}</textarea>
                </div>
                <div id="tinymce_content_validate_span"></div>
            </div>

            <div class="separator separator-dashed border-gray-300 mb-8"></div>

            <div class="mb-0">
                <#include "/view/feature/file/_file_page_reg_area.ftlh">
                <#include "/view/feature/attachable/tag/_tag_page_reg_area.ftlh">
            </div>

            <div class="separator my-8"></div>

            <@component.jandi_reg_area 1 "NOTICE" />
        </form>
    </div>

    <div class="card-footer">
        <div class="d-flex justify-content-between">
            <@component.post_btn_preview_pop "dF.JournalSbjct.preview();" />
            <div>
                <@component.post_btn_submit "dF.JournalSbjct.submit();" />
                <@component.post_btn_toList "dF.JournalSbjct.list();" />
            </div>
        </div>
    </div>
</div>

<#if isMdf!false>
    <div class="card post">
        <div class="card-body pt-0">
            <#include "/view/feature/attachable/sectn/_sectn_page_reg_area.ftlh">
        </div>
    </div>
</#if>

<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#include "/view/feature/attachable/sectn/_modal/_sectn_reg_modal.ftlh">
<#include "/view/feature/attachable/sectn/_sectn_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/attachable/sectn/sectn_module.js?${releaseDate!}"></script>

<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/feature/attachable/comment/_partial/_comment_list_partial.hbs">
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>

<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_reg_form.js?${releaseDate!}"></script>

</@layout.layout>
