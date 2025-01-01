<#--
 * 공지사항(notice) > 상세 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--페이지:: 헤더: 제목 영역-->
            <@component.post_header_title post />
            <#--페이지:: 헤더: 메타 영역-->
            <@component.post_header_meta post />
            <#--페이지:: 조치자 표시 영역-->
            <@component.post_header_managtr_area post />
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#--begin::cn-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${post.markdownCn?no_esc}
        </div>

        <#--begin::meta-->
        <div class="mb-0">
            <#--페이지:: 첨부파일 표시 영역-->
            <#include "/view/global/_common/file/_atch_file_page_dtl_area.ftlh">

            <#--페이지:: 파일시스템 참조 표시 영역-->

            <#--페이지:: 노션 참조 표시 영역-->

            <#--페이지:: 태그 표시 영역-->
            <#include "/view/global/_common/_clsf/tag/_tag_page_dtl_area.ftlh">

            <#--페이지:: 열람자 표시 영역-->
            <@component.post_viewer_area post />
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 수정하기-->
            <@component.post_btn_modify "Notice.mdfForm();" post isMngr />
            <#--페이지:: 버튼: 삭제하기-->
            <@component.post_btn_delete "Notice.delAjax(${post.postNo});" post isMngr />
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "Notice.list();" />
        </div>
        <#--페이지:: 댓글 영역-->
        <#include "/view/global/_common/_clsf/comment/_comment_page_area.ftlh">
    </div>
</div>

<#if post.sectn?? && post.sectn.hasSectn>
    <div class="card post">
        <#--begin::Card body-->
        <div class="card-body">
            <#--페이지:: 단락 영역-->
            <#include "/view/global/_common/_clsf/sectn/_sectn_page_dtl_area.ftlh">
        </div>
    </div>
</#if>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="${post.postNo!}" />
</form>

<#--include::modals-->
<#include "/view/global/_common/_clsf/comment/_comment_reg_modal.ftlh">
<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/notice/notice_module.js?${releaseDate!}"></script>
<#include "/view/global/_common/_clsf/sectn/_sectn_scripts.ftlh">
<#include "/view/global/_common/_clsf/comment/_comment_modal_scripts.ftlh">
<#--include:: component:: tinymce 이미지 추가 영역 template-->
<#include "/view/global/_tinymce_img.ftlh">

</@layout.layout>