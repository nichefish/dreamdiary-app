<#--
 * 레이아웃 > 파일시스템 참조 표시 영역
 -->

<#--begin::Row-->
<#if (post.flsysRefList)?has_content>
    <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
        <div class="col-xl-1 col-form-label fs-6 fw-bold px-5 cursor-help"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
             title="파일서버 파일에 대한&#10;참조 링크 목록입니다.">
            파일 참조
        </div>
        <div class="col-xl-11 my-3">
            <#list (post.flsysRefList)![] as flsysRef>
                <div class="row my-1">
                    <div class="col-xl-8">
                        <a href="javascript:void(0);" onclick="Flsys.fileDownload('${flsysRef.filePath!}');"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                           title="<@spring.message "bs.tooltip.file.download"/>">
                            <i class="fas fa-file-download fs-15 me-1"></i>
                            ${flsysRef.filePath!}
                        </a>
                    </div>
                </div>
            <#else>
                <span>첨부된 파일이 없습니다.</span>
            </#list>
        </div>
    </div>
</#if>

<#include "/view/flsys/_flsys_home_modal.ftlh">