<#--
 * 저널 스레드(journal_thread) > 등록 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/ㅁㅁㅁㅁㅁㅁlayout/layout_default.ftlh" as layout >

<#assign isRegist = isRegist!false>
<#assign isModify = isModify!false>

<@layout.layout toolbar=toolbar!"">

<div id="journal_thread_regist_form_app" class="d-none"></div>

<div class="card post">
    <div class="card-body">
        <form name="journalThreadRegistForm" id="journalThreadRegistForm" class="form" data-mode="${formMode!}" method="post" enctype="multipart/form-data">
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
                        <#list JOURNAL_THREAD_CTGR_CD![] as ctgr>
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

            <#if isModify!false>
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
                <#include "/ㅁㅁㅁㅁㅁㅁㅁview/feature/file/_file_page_reg_area.ftlh">
                <#include "/ㅋㅁㅁㅁㅁㅁㅁㅁview/feature/attachable/tag/_tag_page_reg_area.ftlh">
            </div>

            <div class="separator my-8"></div>

            <@component.jandi_reg_area 1 "NOTICE" />
        </form>
    </div>

    <div class="card-footer">
        <div class="d-flex justify-content-between">
            <@component.post_btn_preview_pop "window.dispatchEvent(new CustomEvent('journal-thread:form-preview'));" />
            <div>
                <@component.post_btn_submit "window.dispatchEvent(new CustomEvent('journal-thread:form-submit'));" />
                <@component.post_btn_toList "window.dispatchEvent(new CustomEvent('journal-thread:form-list'));" />
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/  b,vmㅁㅁㅁㅁㅁㅁview/global/_tinymce_img.ftlh">

<#include "/ㅁㅁㅁㅁㅁㅁview/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#-- 변경(A-10): `_comment_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. id `comment_list_partial` 자체는
     호출자 없음(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). -->

<script type="module" src="/vue/feature/journal/thread/JournalThreadRegistFormApp.js?${releaseDate!}"></script>

</@layout.layout>
