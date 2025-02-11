<#--
 * 휴가계획서(vcatn_papr) > 상세 화면
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
                <#--begin::Title-->
                <div class="col-12">
                    <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                        <#list post.schdulList![] as item>
                            <span class="fw-bolder fs-2 ctgr-span ctgr-gray">
                                ${item.vcatnNm!}
                            </span>
                            <a href="javascript:void(0);" class="fw-bolder mx-3">
                                ${(item.bgnDt)!}<#if item.bgnDt != item.endDt> ~ ${(item.endDt)!}</#if>
                            </a>
                        </#list>
                    </h1>
                </div>
            </div>
            <#--페이지:: 헤더: 메타 (+프린트) 영역-->
            <@component.post_header_meta_with_print post />
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#--begin::cn-->
        <#if post.schdulList?has_content>
            <div class="row mb-4 min-h-150px">
                <div class="col-form-label">
                    <#list post.schdulList![] as item>
                        <div class="row d-flex flex-column mx-5 my-4">
                            <div class="d-flex align-items-center position-relative mb-1">
                                <div class="position-absolute top-0 start-0 rounded h-100 bg-secondary w-4px"></div>
                                <div class="fw-bold ms-5">
                                    <span class="fs-5 fw-bolder text-dark">
                                        ${(item.bgnDt)!}<#if item.bgnDt != item.endDt> ~ ${(item.endDt)!}</#if>
                                    </span>
                                    <span href="javascript:void(0);" class="fs-5 fw-bolder text-gray-700 mx-3">
                                        ${item.vcatnNm!}
                                    </span>
                                    <div class="fs-7 text-muted">${item.resn!}</div>
                                </div>
                            </div>
                        </div>
                    </#list>
                    <span class="text-noti fs-7 ps-5">※주말 및 공휴일은 자동으로 휴가일자 산정에서 제외됩니다.</span>
                </div>
            </div>
        </#if>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#--begin::meta-->
        <div class="mb-0">
            <#--페이지:: 첨부파일 표시 영역-->
            <#include "/view/extension/file/_atch_file_page_dtl_area.ftlh">

            <#--페이지:: 열람자 표시 영역-->
            <@component.post_viewer_area post />

            <#--페이지:: 관리자 확인 영역-->
            <#if isMngr>
                <div class="row mb-5">
                    <div class="col-xl-2">
                        <div class="col-form-label fs-6 fw-bold mx-5">관리자 확인</div>
                    </div>
                    <div class="col-xl-10">
                        <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                            <#if post.cfYn == "N">
                                <span class="text-muted me-10">
                                    미확인<i class="bi bi-dash text-muted"></i>
                                </span>
                                <button class="btn btn-sm btn-primary" onclick="dF.VcatnPapr.cfAjax();">확인하기</button>
                            <#else>
                                <button class="btn btn-sm btn-success" disabled>
                                    확인완료<i class="bi bi-check success"></i>
                                </button>
                                <span class="ms-3 text-success">확인 처리되었습니다.</span>
                            </#if>
                        </div>
                    </div>
                </div>
            </#if>
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 수정하기-->
            <@component.post_btn_modify "dF.VcatnPapr.mdfForm();" post isMngr />
            <#--페이지:: 버튼: 삭제하기-->
            <@component.post_btn_delete "dF.VcatnPapr.delAjax(${post.postNo});" post isMngr />
            <!-- 등록자: 수정,삭제 가능, 관리자: 삭제 가능, 확인 시: 삭제만 가능 -->
    <#--        <#if post.isRegstr!false && (post.cfYn)! == "N">
                <button type="button" class="btn btn-light btn-active-primary me-2 btn-sm" onclick="Page.mdfForm();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.page.mdf'/>">
                    <i class="bi bi-pencil-square"></i>수정
                </button>
            </#if>-->
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "dF.VcatnPapr.list();" />
        </div>
    </div>

    <#--페이지:: 댓글 영역-->
    <#include "/view/extension/clsf/comment/_comment_page_area.ftlh">
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="${post.postNo!}"/>
    <input type="hidden" name="contentType" id="contentType" value="${post.contentType!}"/>
</form>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_dtl.js?${releaseDate!}"></script>

</@layout.layout>