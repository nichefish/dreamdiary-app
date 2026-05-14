<#--
 * 레이아웃 > 첨부파일 모듈
 * @included_in : 파일 첨부 기능이 있는 페이지들
 * 변경(D): Freemarker 직접 렌더링 → Vue FileGroupDetail 컴포넌트로 전환.
 -->

<#--inject::기존 파일 목록 데이터-->
<script type="application/json" id="file_group_detail_data">
[<#if post.fileGroupInfo??><#list post.fileGroupInfo.fileRecordList![] as file><#sep>,</#sep>{"id":${file.id?c},"fileGroupId":"${file.fileGroupId!}","orgnFileNm":"${file.orgnFileNm!?js_string}","fileSize":${(file.fileSize!0)?c}}</#list></#if>]
</script>
<#--inject::라벨-->
<script type="application/json" id="file_group_detail_labels">
{
    "atchFile": "${springMacroRequestContext.getMessage('txt.atch-file')?js_string}",
    "downloadTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.file.download')?js_string}"
}
</script>

<#--begin::Vue mount-->
<div id="file_group_detail_app"></div>

<#--include::vue module-->
<script type="module" src="/vue/feature/file/FileGroupDetail.js?${releaseDate!}"></script>