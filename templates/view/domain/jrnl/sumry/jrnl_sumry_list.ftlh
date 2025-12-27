<#--
 * 저널 결산(jrnl_sumry) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/jrnl/sumry/_jrnl_sumry_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <div class="d-flex-between fs-5">
            <div class="d-flex fs-5">
                <div class="text-gray-700 d-flex-center me-5">
                    <span class="fw-bold me-2">
                        전체 꿈
                    </span>
                    <i class="bi bi-moon-stars fs-4 me-2"></i>
                    (<span class="text-info fw-bold mx-1">${totalSumry.dreamDayCnt!}</span>일
                    /
                    <span class="text-info fw-bold mx-1">${totalSumry.dreamCnt!}</span>개)
                </div>
                <div class="text-gray-700 d-flex-center">
                    <span class="fw-bold me-2">
                        전체 일기
                    </span>
                    <i class="bi bi-book fs-4 mt-1 me-2"></i>
                    (<span class="text-info fw-bold mx-1">${totalSumry.diaryDayCnt!}</span>일)
                </div>
            </div>

            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.JrnlDreamTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
</div>
<div id="jrnl_sumry_list_div"></div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="" />
</form>

<#--include::aside-->
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_aside_base.ftlh">

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::jrnl_sumry_module-->
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_list_template.hbs">
<#include "/view/domain/jrnl/sumry/_modal/_jrnl_sumry_reg_modal.ftlh">
<#include "/view/domain/jrnl/sumry/_partial/_jrnl_sumry_context_btn_partial.hbs">

<#--include::jrnl_dream_module-->
<#include "/view/domain/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_tag_module-->
<#include "/view/domain/jrnl/dream/tag/_modal/_jrnl_dream_tag_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tag-->
<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">
<#include "/view/extension/clsf/tag/_partial/_tag_list_partial.hbs">

<#--include::comment-->
<#include "/view/extension/clsf/comment/_modal/_comment_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/extension/clsf/comment/comment_modal_module.js?${releaseDate!}"></script>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_list.js?${releaseDate!}"></script>

</@layout.layout>