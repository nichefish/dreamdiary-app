<#--
 * 寃뚯떆??board_post) > ?곸꽭 ?붾㈃
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--?섏씠吏:: ?ㅻ뜑: ?쒕ぉ ?곸뿭-->
            <@component.post_header_title post />
            <#--?섏씠吏:: ?ㅻ뜑: 誘몃━蹂닿린 硫뷀? ?곸뿭-->
            <@component.post_header_meta_preview post />
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#--begin::content-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${post.markdownContent?no_esc}
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--踰꾪듉 ?곸뿭-->
        <div class="d-flex justify-content-end">
            <#--?앹뾽:: 踰꾪듉: ?앹뾽 ?リ린-->
            <@component.popup_btn_preview_close "Layout" />
        </div>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="${post.id!}" />
    <input type="hidden" name="board" id="board" value="${post.board!}" />
</form>

</@layout.layout>
