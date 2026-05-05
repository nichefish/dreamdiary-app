<#--
 * 게시판(board_post) > 상세 화면
-->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div id="board_post_dtl_app" class="d-none"></div>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--페이지:: 헤더: 제목 영역-->
            <@component.post_header_title post />
            <#--페이지:: 헤더: 메타 영역-->
            <@component.post_header_meta post />
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#--begin::content-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${post.markdownContent?no_esc}
        </div>

        <div class="separator separator-dashed border-gray-300 mb-8"></div>

        <#--begin::meta-->
        <div class="mb-0">
            <#--페이지:: 첨부파일 표시 영역-->
            <#include "/view/feature/file/_file_page_dtl_area.ftlh">

            <#--페이지:: 파일소스 참조 표시 영역-->

            <#--페이지:: 노션 참조 표시 영역-->

            <#--페이지:: 태그 표시 영역-->
            <#include "/view/feature/attachable/tag/_tag_page_dtl_area.ftlh">

            <#--페이지:: 뷰어 표시 영역-->
            <@component.post_viewer_area post />
        </div>
    </div>

    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 수정하기-->
            <@component.post_btn_modify "window.dispatchEvent(new CustomEvent('board-post:dtl-mdf-form'));" post isMngr />
            <#--페이지:: 버튼: 삭제하기-->
            <@component.post_btn_delete "window.dispatchEvent(new CustomEvent('board-post:dtl-del', { detail: { id: ${post.id} } }));" post isMngr />
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "window.dispatchEvent(new CustomEvent('board-post:dtl-list'));" />
        </div>
    </div>

    <#--페이지:: 댓글 영역-->
    <#include "/view/feature/attachable/comment/_comment_page_area.ftlh">
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="${post.id!}" />
    <input type="hidden" name="board" id="board" value="${post.board!}" />
</form>

<#--include::vue module-->
<script type="module" src="/vue/feature/board/post/BoardPostDetailApp.js?${releaseDate!}"></script>

</@layout.layout>
