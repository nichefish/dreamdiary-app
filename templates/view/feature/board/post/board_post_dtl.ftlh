<#--
 * 寃뚯떆??board_post) > ?곸꽭 ?붾㈃
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--?섏씠吏:: ?ㅻ뜑: ?쒕ぉ ?곸뿭-->
            <@component.post_header_title post />
            <#--?섏씠吏:: ?ㅻ뜑: 誘몃━蹂닿린 硫뷀? ?곸뿭-->
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
            <#--?섏씠吏:: 泥⑤??뚯씪 ?쒖떆 ?곸뿭-->
            <#include "/view/feature/file/_file_page_dtl_area.ftlh">

            <#--?섏씠吏:: ?뚯씪?쒖뒪??李몄“ ?쒖떆 ?곸뿭-->

            <#--?섏씠吏:: ?몄뀡 李몄“ ?쒖떆 ?곸뿭-->

            <#--?섏씠吏:: ?쒓렇 ?쒖떆 ?곸뿭-->
            <#include "/view/feature/attachable/tag/_tag_page_dtl_area.ftlh">

            <#--?섏씠吏:: ?대엺???쒖떆 ?곸뿭-->
            <@component.post_viewer_area post />
        </div>
    </div>

    <#--begin::Card footer-->
    <div class="card-footer">
        <#--踰꾪듉 ?곸뿭-->
        <div class="d-flex justify-content-end">
            <#--?섏씠吏:: 踰꾪듉: ?섏젙?섍린-->
            <@component.post_btn_modify "dF.BoardPost.mdfForm();" post isMngr />
            <#--?섏씠吏:: 踰꾪듉: ??젣?섍린-->
            <@component.post_btn_delete "dF.BoardPost.delAjax(${post.id});" post isMngr />
            <#--?섏씠吏:: 踰꾪듉: 紐⑸줉?쇰줈 媛湲?->
            <@component.post_btn_toList "dF.BoardPost.list();" />
        </div>
    </div>

    <#--?섏씠吏:: ?볤? ?곸뿭-->
    <#include "/view/feature/attachable/comment/_comment_page_area.ftlh">
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="${post.id!}" />
    <input type="hidden" name="board" id="board" value="${post.board!}" />
</form>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/board/post/board_post_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/board/post/board_post_dtl.js?${releaseDate!}"></script>

</@layout.layout>
