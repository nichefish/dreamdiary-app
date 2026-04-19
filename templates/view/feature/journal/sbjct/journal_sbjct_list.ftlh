<#--
 * 저널 주제(journal_sbjct) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/journal/sbjct/_journal_sbjct_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#include "/view/feature/attachable/tag/_tag_list_header.ftlh">

<div class="card post">
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-lg-9 col-9 text-center wb-keepall"><@spring.message 'txt.title'/></th>
                    <th class="col-lg-1 text-center wb-keepall hidden-table"><@spring.message 'txt.atch-file'/></th>
                </tr>
            </thead>
            <tbody>
                <#list journalSbjctList![] as post>
                    <tr class="bg-hover-secondary cursor-default">
                        <td class="text-center hidden-table">
                            ${post.rnum!}
                        </td>
                        <td class="text-start <#if post.tagStrList??>pb-4</#if>">
                            <div class="row d-flex align-items-center justify-content-between">
                                <div class="col-xl-8 col-12 d-flex flex-column">
                                    <div class="my-1">
                                        <a href="${Url.JOURNAL_SBJCT_DTL!}?id=${post.id!}"
                                           class="text-dark vertical-middle text-underline-dotted"
                                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.page.dtl'/>">
                                            <#if (post.categoryName!"") != "">
                                                <span class="ctgr-span ctgr-gray">${post.categoryName!}</span>
                                            </#if>
                                            ${post.title!}
                                        </a>
                                        <@component.list_comment post />
                                        <#if post.isNew>
                                            <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                        </#if>
                                    </div>
                                    <div class="ms-1">
                                        <@component.list_tag post />
                                    </div>
                                </div>
                                <div class="col-xl-4 col-12 d-flex justify-content-end align-items-center">
                                    <@component.list_dtl_modal post "dF.JournalSbjct.dtlModal" />
                                </div>
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
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
    <div class="card-footer">
        <#include "/view/global/_pagination.ftlh">
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="" />
</form>

<#include "/view/feature/attachable/comment/_modal/_comment_list_modal.ftlh">
<#include "/view/feature/file/_modal/_file_group_list_modal.ftlh">

<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_list.js?${releaseDate!}"></script>

</@layout.layout>
