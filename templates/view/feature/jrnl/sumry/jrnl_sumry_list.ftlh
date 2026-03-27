<#--
 * 저널 결산(jrnl_sumry) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/jrnl/sumry/_jrnl_sumry_list_header.ftlh">
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
                        <@spring.message 'txt.total-dreams'/>
                    </span>
                    <i class="bi bi-moon-stars fs-4 me-2"></i>
                    (<span class="text-info fw-bold mx-1">${totalSumry.dreamDayCnt!}</span><@spring.message 'txt.dd'/>
                    /
                    <span class="text-info fw-bold mx-1">${totalSumry.dreamCnt!}</span><@spring.message 'txt.ea'/>)
                </div>
            </div>
        </div>
    </div>
</div>
<div id="jrnl_sumry_list_div"></div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value="" />
</form>

<#--include::aside-->
<#include "/view/feature/jrnl/sumry/_jrnl_sumry_aside_base.ftlh">

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::jrnl_sumry_module-->
<#include "/view/feature/jrnl/sumry/_jrnl_sumry_list_template.hbs">
<#include "/view/feature/jrnl/sumry/_modal/_jrnl_sumry_reg_modal.ftlh">
<#include "/view/feature/jrnl/sumry/_partial/_jrnl_sumry_context_btn_partial.hbs">

<#--include::jrnl_dream_module-->
<#include "/view/feature/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_tag_module-->
<script type="text/javascript" src="/js/view/feature/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tag-->
<#include "/view/feature/jrnl/tag/_jrnl_tag_list_modal.ftlh">
<#include "/view/feature/clsf/tag/_partial/_tag_list_partial.hbs">

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/jrnl/sumry/jrnl_sumry_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/jrnl/sumry/jrnl_sumry_list.js?${releaseDate!}"></script>

</@layout.layout>
