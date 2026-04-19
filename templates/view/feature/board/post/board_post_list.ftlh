<#--
 * 寃뚯떆??board_post) > 紐⑸줉 ?붾㈃
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::踰꾪듉 異붽?-->
<#assign toolbar>
    <#include "/view/feature/board/post/_board_post_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--#include:: component:: ?쒓렇 ?대┃ ?꾪꽣留?->
<#include "/view/feature/attachable/tag/_tag_list_header.ftlh">

<#if board == "cmpyLife">
    <button type="button" class="btn btn-sm btn-light btn-active-secondary mx-1"
            onclick="$('#cmpy_life_modal').modal('show');"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="?ш린濡쒖슫 ?뚯궗?앺솢 ?덈궡 ?앹뾽???몄텧?⑸땲??">
        <span class="blink"><i class="bi bi-lightbulb-fill text-noti"></i></span>?ш린濡쒖슫 ?뚯궗?앺솢
    </button>
</#if>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">踰덊샇</th>
                    <th class="col-lg-8 col-9 text-center wb-keepall"><@spring.message 'txt.title'/></th>
                    <th class="col-lg-1 text-center wb-keepall hidden-table"><@spring.message 'txt.atch-file'/></th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody>
                <#list postList![] as post>
                    <tr class="bg-hover-secondary cursor-default">
                        <td class="text-center hidden-table">
                            ${post.rnum!}
                        </td>
                        <td class="text-start <#if post.tagStrList??>pb-4</#if>">
                            <div class="row d-flex align-items-center justify-content-between align-items-center">
                                <div class="col-xl-8 col-12 d-flex flex-column">
                                    <div class="my-1">
                                        <a href="${Url.BOARD_POST_DTL!}?board=${post.board!}&id=${post.id!}&notionPageId=${post.notionPageId!}"
                                           class="text-dark vertical-middle text-underline-dotted"
                                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.page.dtl'/>">
                                            <#if (post.ctgrNm!"") != "">
                                                <span class="ctgr-span ctgr-gray">${post.ctgrNm!}</span>
                                            </#if>
                                            ${post.title!}
                                        </a>
                                        <#--紐⑸줉:: ?볤? ?쒖떆-->
                                        <@component.list_comment post />
                                        <#--紐⑸줉:: ??湲?대㈃ 'N' ?쒖떆-->
                                        <#if post.isNew>
                                            <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                        </#if>
                                    </div>
                                    <div class="ms-1">
                                        <#--紐⑸줉:: ?쒓렇 ?쒖떆-->
                                        <@component.list_tag post />
                                    </div>
                                </div>
                                <div class="col-xl-4 col-12 d-flex justify-content-end align-items-center">
                                    <#--紐⑸줉:: 紐⑤떖 ?앹뾽 ?닿린 踰꾪듉 ?쒖떆-->
                                    <@component.list_dtl_modal post "dF.BoardPost.dtlModal" />
                                </div>
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
                            <#--紐⑸줉:: 泥⑤??뚯씪 紐⑤떖 ?닿린 踰꾪듉 ?쒖떆-->
                            <@component.list_file_group post />
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan="3" class="text-center"><@spring.message 'view.list.empty'/></td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value=""/>
    <input type="hidden" name="board" id="board" value="${board!}"/>
</form>

<#--include::modals-->
<#include "/view/feature/attachable/comment/_modal/_comment_list_modal.ftlh">
<#include "/view/feature/file/_modal/_file_group_list_modal.ftlh">

<#--include::board_post_module-->
<#include "/view/feature/board/post/_modal/_board_post_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/board/post/board_post_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/board/post/board_post_list.js?${releaseDate!}"></script>

</@layout.layout>
