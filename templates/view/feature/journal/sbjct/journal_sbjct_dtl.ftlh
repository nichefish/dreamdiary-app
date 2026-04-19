<#--
 * 저널 주제(journal_sbjct) > 상세 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <div class="card-body">
        <div class="mb-0">
            <@component.post_header_title post />
            <@component.post_header_meta post />
            <@component.post_header_managtr_area post />
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${post.markdownContent?no_esc}
        </div>

        <div class="mb-0">
            <#include "/view/feature/file/_file_page_dtl_area.ftlh">
            <#include "/view/feature/attachable/tag/_tag_page_dtl_area.ftlh">
            <@component.post_viewer_area post />
        </div>
    </div>

    <div class="card-footer">
        <div class="d-flex justify-content-end">
            <#if post.isCreatedBy!false>
                <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                        onclick="dF.JournalSbjct.mdfForm();"
                        data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                    <i class="bi bi-pencil-square"></i>(내글)수정
                </button>
                <button type="button" class="btn btn-sm btn-light btn-active-danger me-2"
                        onclick="dF.JournalSbjct.delAjax(${post.id});"
                        data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.del'/>">
                    <i class="bi bi-trash"></i>(내글)삭제
                </button>
            </#if>
            <@component.post_btn_toList "dF.JournalSbjct.list();" />
        </div>
        <#include "/view/feature/attachable/comment/_comment_page_area.ftlh">
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="${post.id!}" />
</form>

<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_module.js?${releaseDate!}"></script>

<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>

<script type="text/javascript" src="/js/view/feature/journal/sbjct/journal_sbjct_dtl.js?${releaseDate!}"></script>

</@layout.layout>
