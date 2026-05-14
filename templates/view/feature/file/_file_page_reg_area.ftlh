<#--
 * 레이아웃 > 첨부파일 모듈
 * @included_in : 파일 첨부 기능이 있는 페이지들
 * 변경(D): file_module.ts(jQuery 기반 dF.FileGroup) → Vue FileGroupSection 컴포넌트로 전환.
 -->

<#--inject::기존 파일 목록 데이터-->
<script type="application/json" id="file_group_section_data">
[<#if post.fileGroupInfo??><#list post.fileGroupInfo.fileRecordList![] as file><#sep>,</#sep>{"id":${file.id?c},"fileGroupId":"${file.fileGroupId!}","orgnFileNm":"${file.orgnFileNm!?js_string}","fileSize":${(file.fileSize!0)?c}}</#list></#if>]
</script>
<#--inject::라벨-->
<script type="application/json" id="file_group_section_labels">
{
    "atchFile": "${springMacroRequestContext.getMessage('txt.atch-file')?js_string}",
    "fileSizeNote": "※ 첨부파일은 개별 50MB, 최대 100MB까지 업로드 가능합니다.",
    "downloadTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.file.download')?js_string}",
    "del": "삭제",
    "delTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.file.del')?js_string}"
}
</script>

<#--begin::Vue mount-->
<div id="file_group_section_app"></div>

<#--include::vue module-->
<script type="module" src="/vue/feature/file/FileGroupSection.js?${releaseDate!}"></script>