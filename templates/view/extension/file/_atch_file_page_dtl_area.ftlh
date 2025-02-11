<#--
 * 레이아웃 > 첨부파일 모듈
 * @included_in : 파일 첨부 기능이 있는 페이지들
 -->

<#--begin::Row-->
<#if (post.atchFileInfo.atchFileList)?has_content>
    <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
        <div class="col-xl-1 col-form-label fs-6 fw-bold px-5">
            <@spring.message 'atch-file'/>
        </div>
        <div class="col-xl-11 my-3">
            <#list (post.atchFileInfo.atchFileList)![] as file>
                <div class="row my-1">
                    <div class="col-xl-8" id="itemContainer${file.atchFileDtlNo!}">
                        <a href="javascript:void(0);"
                           onclick="cF.util.fileDownload('${file.atchFileNo!}', '${file.atchFileDtlNo!}');"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                           title="<@spring.message 'bs.tooltip.file.download'/>">
                            <i class="fas fa-file-download fs-15 me-1"></i>
                            ${file.orgnFileNm!}(${file.fileSize!}byte)
                        </a>
                    </div>
                </div>
            <#else>
                <span>첨부된 파일이 없습니다.</span>
            </#list>
        </div>
    </div>
</#if>