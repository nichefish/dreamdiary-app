<#--
 * 레이아웃 > 첨부파일 모듈
 * @included_in : 파일 첨부 기능이 있는 페이지들
 -->

<#--begin::Row-->
<div class="row">
    <div>
        <label class="mb-2">
            <span class="text-gray-700 fs-6 fw-bolder">첨부파일</span>
            <span class="text-gray-500 fs-9 mx-2">※ 첨부파일은 개별 50MB, 최대 100MB까지 업로드 가능합니다.</span>
        </label>
        <input type="file" id="formFile" class="form-control"
               onclick="dF.AtchFile.addFileItem();">
    </div>
    <div class="col-xl-10 mb-8 text-sm-start" id="atchFileSpan">
        <#if post.atchFileInfo??>
            <#list post.atchFileInfo.atchFileList![] as file>
                <div class="row mt-2" id="atchFile${file.atchFileDtlNo!}">
                    <div class="col-xl-8 text-sm-start" id="itemContainer${file.atchFileDtlNo!}">
                        <i class="fas fa-file-download fs-15 me-3"></i>
                        <a href="javascript:void(0);" onclick="cF.util.fileDownload('${file.atchFileNo!}', '${file.atchFileDtlNo!}');"
                           data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message "bs.tooltip.file.download"/>">
                            ${file.orgnFileNm!} (${file.fileSize!}byte)
                        </a>
                        <div class="badge badge-light btn-primary badge-outlined mx-2 cursor-pointer"
                             onclick="dF.AtchFile.delExistingFile(${file.atchFileDtlNo!});"
                             data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message "bs.tooltip.file.del"/>">
                            삭제
                        </div>
                        <input type="hidden" name="atchCtrl${file.atchFileDtlNo!}" id="atchCtrl${file.atchFileDtlNo!}" value="R">
                    </div>
                </div>
            </#list>
        </#if>
    </div>
</div>

<!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
<div id="atchFileTemplate" style="display:none;">
    <!--atchCtrl :: CRUD -->
    <input type="hidden" name="atchCtrl__INDEX__" id="atchCtrl__INDEX__" value="C" size="5"/>
    <!--fileSn :: nullable-->
    <input type="hidden" name="fileSn__INDEX__" value="fileSn__INDEX__" size="5"/>
    <div hidden>
        <input type="file" name="atchFile__INDEX__" id="atchFile__INDEX__" class="file"
               onchange="return false;"/>
    </div>
    <div class="col-xl-10 text-sm-start">
        <!--fileNm :: to display-->
        <i class="fas fa-file-download fs-15 me-3"></i> <span id="fileNm__INDEX__" ></span>
        <button type="button" class="badge badge-light btn-primary badge-outlined mx-2"
                onclick="dF.AtchFile.delNewFileSpan('__INDEX__');"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.file.del"/>">
            삭제
        </button>
    </div>
</div>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/global/_common/file/atch_file_module.js?${releaseDate!}"></script>
