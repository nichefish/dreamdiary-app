<#--
 * 저널 스레드(journal_thread) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/journal/thread/_journal_thread_list_header.ftlh">
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
            <tbody id="journal_thread_list_div">
                <#-- 변경: `JournalThreadListApp` 텔레포트가 행을 주입한다(사용자 목록 페이지 `user_list.ftlh` 과 동등 패턴). -->
            </tbody>
        </table>
    </div>
    <div class="card-footer">
        <#include "/view/global/_pagination.ftlh">
    </div>
</div>

<script id="journal_thread_list_data" type="application/json">
[
<#list journalThreadList![] as post>
{
    "rnum": ${post.rnum!0},
    "id": ${post.id!0},
    "contentType": "${(post.contentType!'')?js_string}",
    "categoryName": "${(post.categoryName!'')?js_string}",
    "title": "${(post.title!'')?js_string}",
    "isNew": ${(post.isNew!false)?c},
    "commentCnt": <#if post.comment??>${post.comment.cnt!0}<#else>0</#if>,
    "fileGroupId": "<#if post.fileGroupId??>${post.fileGroupId?c}<#elseif post.file?? && post.file.fileGroupId??>${post.file.fileGroupId?c}<#else></#if>",
    "hasFiles": ${((post.fileGroupInfo?? && post.fileGroupInfo.fileRecordList?? && post.fileGroupInfo.fileRecordList?has_content))?c},
    "hasTagsLayout": ${(post.tag?? && post.tag.tagStrList??)?c},
    "tags": [
    <#if post.tag?? && post.tag.list??>
        <#list post.tag.list as tag>
        {
            "tagId": "${tag.tagId?c}",
            "ctgr": "${(tag.ctgr!'')?js_string}",
            "name": "${(tag.name!'')?js_string}"
        }<#if tag_has_next>,</#if>
        </#list>
    </#if>
    ]
}<#if post_has_next>,</#if>
</#list>
]
</script>

<script id="journal_thread_label_data" type="application/json">
{
    "pageDetail": "${springMacroRequestContext.getMessage('bs.tooltip.page.dtl')?js_string}",
    "comment": "${springMacroRequestContext.getMessage('txt.comment')?js_string}",
    "atchFile": "${springMacroRequestContext.getMessage('txt.atch-file')?js_string}",
    "tagContentList": "${springMacroRequestContext.getMessage('view.tag.content-list')?js_string}",
    "modalView": "모달로 보기",
    "emptyList": "${springMacroRequestContext.getMessage('view.list.empty')?js_string}"
}
</script>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="" />
</form>

<#include "/view/feature/attachable/comment/_modal/_comment_list_modal.ftlh">
<#include "/view/feature/file/_modal/_file_list_modal.ftlh">
<#include "/view/feature/journal/thread/_modal/_journal_thread_detail_modal.ftlh">

<#--
 변경: 목록 전용 classic `journal_thread_list.js` 제거 — 헤더·모달 버튼은 `JournalThreadListApp` ES module 이 CustomEvent 로 처리한다.
 변경: 목록 `tbody` 는 `journal_thread_list_data` 부트스트랩 후 Vue 가 렌더한다(페이지네이션은 기존 `_pagination.ftlh` 유지).
 상세/등록 화면까지 Vue 엔트리로 수렴한다.
-->
<div id="journal_thread_list_app" class="d-none"></div>
<script type="module" src="/vue/feature/journal/thread/JournalThreadListApp.js?${releaseDate!}"></script>

</@layout.layout>
