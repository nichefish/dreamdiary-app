<#--
 * 저널 결산(jrnl_sumry) : 페이지
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::header row-->
        <div class="row">
            <div class="col-2 d-flex align-items-center fs-5">
                <i class="bi bi-calendar3 fs-6 me-2"></i>
                <#if post.title??>
                    <span class="fs-5 me-1">${post.title!}</span>
                <#else>
                    <span class="fs-5 fw-bold me-1">${post.yy!}</span>
                    년 결산
                </#if>
            </div>
            <div class="col d-flex align-items-center fs-5">
                <div class="text-gray-700 d-flex-center me-5">
                    <span class="fw-bold me-2">
                        꿈
                    </span>
                    <#if post.dreamComptYn! == "Y">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈 기록을 완료했습니다.">
                            <i class="bi bi-moon-stars-fill fs-4 me-2 text-success"></i>
                            <i class="bi bi-check text-success" style="margin-left:-0.8rem"></i>
                        </span>
                    <#else>
                        <span>
                            <i class="bi bi-moon-stars fs-4 me-2"></i>
                        </span>
                    </#if>
                    (<span class="text-underline-dotted ms-1">${post.dreamDayCnt!}</span>일 / <span class="text-underline-dotted ms-1">${post.dreamCnt!}</span>개)
                </div>
                <div class="text-gray-700 d-flex-center">
                    <span class="fw-bold me-2">
                        일기
                    </span>
                    <i class="bi bi-book fs-4 me-2"></i>
                    (<span class="text-underline-dotted ms-1">${post.diaryDayCnt!}</span>일)
                </div>
            </div>
            <div class="col-1 d-flex flex-column align-items-center">
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined py-2 px-3 cursor-pointer"
                        onclick="dF.JrnlSumry.comptAjax(${post.postNo!});"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="저널 일자 <@spring.message "bs.tooltip.modal.mdf"/>">
                    <i class="bi bi-book fs-4 pe-1"></i>
                    꿈 기록 완료 처리
                </button>
            </div>
        </div>
    </div>
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::cn-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-50px">
            ${post.markdownCn?no_esc}
        </div>

        <#--begin::meta-->
        <div class="mb-0">
            <#--페이지:: 태그 표시 영역-->
            <#include "/view/global/_common/_clsf/tag/_tag_page_dtl_area.ftlh">
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end gap-3">
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.header_btn_reg_modal "dF.JrnlSumry.regModal(${post.postNo!})" />
            <#--페이지:: 버튼: 조회년도 변경-->
            <div class="col-1 w-auto">
                <select id="yy" class="form-select form-select-solid fs-7 text-end"
                        onchange="dF.JrnlSumry.dtlByYy(this.value)"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="조회 년도를 변경합니다.">>
                    <#assign currYy = DateUtils.getCurrYy()?number />
                    <#assign startYy = 2011 />
                    <#list currYy..startYy as year>
                        <option value="${year}" ${fn.selected(post.yy!, year)}>${year}년 결산</option>
                    </#list>
                    <option value="2010">2010년 이전 결산</option>
                </select>
            </div>
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "dF.JrnlSumry.list();" />
        </div>
    </div>
</div>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body pt-0">
        <#--페이지:: 단락 영역-->
        <#include "/view/global/_common/_clsf/sectn/_sectn_page_reg_area.ftlh">
    </div>
</div>

<hr class="my-6 text-muted" />
<div class="d-flex align-items-center mb-6 ms-4">
    <div class="col-1 ms-4 me-6 text-center fs-6">
        <b>일자 태그 : </b>
    </div>
    <div class="col" id="jrnl_diary_tag_list_div">
        <#list dayTagList![] as tag>
            <span class="py-2 me-3 cursor-pointer opacity-hover"
                  onclick="JrnlTag.dtlModal(${tag.tagNo!}, '${tag.tagNm!}');"
                  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="해당 태그로 게시물 목록을&#10;필터링합니다.">
                <span class="${tag.tagClass!}">
                    <#if tag.ctgr?? && tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                    ${tag.tagNm!}
                </span>
                <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">${tag.contentSize!}</span>
            </span>
        </#list>
    </div>
    <div class="col-1 ms-4 me-6 text-center fs-6 w-50px">
        <!-- -->
    </div>
</div>

<!-- 중요 일기 -->
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_dtl_imprtc_diary.ftlh">

<!-- 중요 꿈 -->
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_dtl_imprtc_dream.ftlh">

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="" />
    <input type="hidden" name="yy" id="yy" value="" />
    <input type="hidden" name="mnth" id="mnth" value="99" />
</form>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::draggable-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#--include::jrnl_dream_module-->
<#include "/view/domain/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_tag_module-->
<#include "/view/domain/jrnl/dream/tag/_modal/_jrnl_dream_tag_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tag-->
<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">

<#--include::jrnl_sumry_modules-->
<#include "/view/domain/jrnl/sumry/_modal/_jrnl_sumry_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_module.js?${releaseDate!}"></script>

<#--include::sectn-->
<#include "/view/global/_common/_clsf/sectn/_modal/_sectn_reg_modal.ftlh">
<#include "/view/global/_common/_clsf/sectn/_sectn_list_template.hbs">
<script type="text/javascript" src="/js/view/global/_common/_clsf/sectn/sectn_module.js?${releaseDate!}"></script>

<#--include::comment-->
<#include "/view/global/_common/_clsf/comment/_modal/_comment_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/global/_common/_clsf/comment/comment_modal_module.js?${releaseDate!}">

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_dtl.js?${releaseDate!}"></script>

</@layout.layout>
