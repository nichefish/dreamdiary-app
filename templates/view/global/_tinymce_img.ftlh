<#--
 * 레이아웃 > tinyMCE 첨부이미지 업로드 모듈
 * @included_in : tinyMCE를 사용하는 페이지들
 -->

<div id="tinymceImageTemplate" hidden>
    <#--begin::Form-->
    <form name="tinymceImageForm" id="tinymceImageForm" class="form" enctype="multipart/form-data">
        <input type="file" name="fileGroup0" id="fileGroup0" class="file"
               onchange="return false;"
               accept=".png, .jpg, .jpeg" />
    </form>
</div>