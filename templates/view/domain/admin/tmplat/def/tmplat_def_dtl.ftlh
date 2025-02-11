<#--
 -- 템플릿 관리(tmplat) > 상세 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--begin::Head-->
            <div class="d-flex align-items-center mb-9">
                <#--begin::title-->
                <div class="col-12">
                    <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                        <#if (((tmplat.imprtcYn)!"N") == "Y")!false><span class="badge badge-danger me-2">중요</span></#if>
                        <span class="vertical-middle<#if (((tmplat.imprtcYn)!"N") == "Y")!false> fw-bold</#if>">
                            <#if (tmplat.ctgrNm!"") != "">[${tmplat.ctgrNm!}]</#if>
                            ${tmplat.title!}
                         </span>
                    </h1>
                </div>
            </div>
            <!--begin::Foot-->
            <div class="d-flex flex-stack flex-wrap mb-3">
                <!--begin::Author-->
                <div class="d-flex align-items-center">
                    <!--begin::Name-->
                    <div class="d-flex flex-column align-items-start justify-content-center">
                        <span class="text-gray-800 fs-6 fw-bold lh-1 mb-2">${tmplat.regstrNm!"-"}</span>
                        <span class="text-muted fs-7 lh-1">${tmplat.regDt!}</span>
                    </div>
                </div>
                <!--begin::Info-->
                <div class="d-flex align-items-center py-1">
                    <a href="#answers" data-kt-scroll-toggle="true"
                       class="btn btn-sm btn-outline btn-outline-dashed btn-outline-default px-4 me-2"><@spring.message 'comment'/> ${tmplat.commentCnt!}</a>
                    <button class="btn btn-sm btn-light px-4 me-2 disabled"><@spring.message 'hit-cnt'/> ${tmplat.hitCnt}</button>
                </div>
            </div>
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <!--begin::cn-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${tmplat.cn?no_esc}
        </div>

        <#--begin::meta-->
        <div class="mb-0">
            <#if (tmplat.atchFileInfo.atchFileList)?has_content>
                <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
                    <div class="col-xl-1">
                        <div class="fs-6 fw-bold col-form-label px-5">
                            첨부파일
                        </div>
                    </div>
                    <div class="col-xl-11 my-3">
                        <#list (tmplat.atchFileInfo.atchFileList)![] as file>
                            <div class="row my-1">
                                <div class="col-xl-8">
                                    <a href="javascript:void(0);" onclick="cF.util.fileDownload('${file.atchFileNo!}', '${file.atchFileDtlNo!}');"
                                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.file.download'/>">
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

            <#--페이지:: 열람자 표시 영역-->
            <@component.post_viewer_area post />
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 수정하기-->
            <@component.post_btn_modify "Page.mdfForm();" post isMngr />
            <#--페이지:: 버튼: 삭제하기-->
            <@component.post_btn_delete "Page.delAjax(${post.postNo});" post isMngr />
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "Page.list();" />
        </div>
    </div>

    <#--페이지:: 댓글 영역-->
    <#assign post = tmplat>
    <#include "/view/extension/clsf/comment/_comment_page_area.ftlh">
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="${tmplat.postNo!}" />
</form>

<#--include::tmplat_def_module-->
<script type="text/javascript" src="/js/view/domain/admin/tmplat/def/tmplat_def_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/admin/tmplat/def/tmplat_def_dtl.js?${releaseDate!}"></script>

</@layout.layout>