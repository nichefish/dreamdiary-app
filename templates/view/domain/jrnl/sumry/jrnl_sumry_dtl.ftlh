<#--
 * 저널 결산(jrnl_sumry) : 페이지
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

<!--begin::Card-->
<div id="jrnl_sumry_dtl_div" class="card post">
    <!-- handlebars template goes in here-->
</div>

<#--begin::Card header-->
<div class="card-header min-h-auto mb-7">
    <#--begin::Tab-->
    <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DIARY')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_1"
               onclick="dF.JrnlSumry.dtlViewWithSection('DIARY');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-book"></i>
                </span>
                <span class="nav-text">일기 결산</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DREAM')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_2"
               onclick="dF.JrnlSumry.dtlViewWithSection('DREAM');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-moon-stars"></i>
                </span>
                <span class="nav-text">꿈 결산</span>
            </a>
        </li>
    </ul>
</div>

<!-- 태그 헤더 :: 파일 분리 -->
<#include "/view/domain/jrnl/sumry/tag/_jrnl_sumry_dtl_tag_header.ftlh">
<hr class="my-6 text-muted" />

<div class="jrnl_sumry_day_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="jrnl_sumry_diary_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="jrnl_sumry_dream_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="card-post p-10">
    <!-- 중요 일기 -->
    <div id="jrnl_sumry_imprtc_diary_list_div">
        <!-- handlebars template goes in here -->
    </div>

    <!-- 중요 꿈 -->
    <div id="jrnl_sumry_imprtc_dream_list_div">
        <!-- handlebars template goes in here -->
    </div>
</div>

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
<script type="text/javascript" src="/js/view/domain/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tag-->
<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">

<#include "/view/domain/jrnl/day/_partial/_jrnl_day_stdrd_dt_partial.hbs">
<#include "/view/domain/jrnl/day/_partial/_jrnl_day_context_btn_partial.hbs">

<#include "/view/domain/jrnl/day/_partial/_jrnl_day_meta_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_reg_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_context_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_copy_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_toggle_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_cn_partial.hbs">

<#--include::jrnl_sumry_modules-->
<#include "/view/domain/jrnl/sumry/_modal/_jrnl_sumry_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_module.js?${releaseDate!}"></script>

<#--include::sectn-->
<#include "/view/extension/clsf/sectn/_modal/_sectn_reg_modal.ftlh">
<#include "/view/extension/clsf/sectn/_sectn_list_template.hbs">
<script type="text/javascript" src="/js/view/extension/clsf/sectn/sectn_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">
<#include "/view/extension/clsf/tag/_partial/_tag_list_partial.hbs">
<#include "/view/extension/clsf/tag/_partial/_tag_list_sized_partial.hbs">

<#--include::comment-->
<#include "/view/extension/clsf/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/extension/clsf/comment/_partial/_comment_list_partial.hbs">
<#include "/view/extension/clsf/comment/_partial/_comment_reg_btn_partial.hbs">
<script type="text/javascript" src="/js/view/extension/clsf/comment/comment_modal_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_dtl_template.hbs">
<#include "/view/domain/jrnl/sumry/tag/_jrnl_sumry_day_tag_list_template.hbs">
<#include "/view/domain/jrnl/sumry/tag/_jrnl_sumry_diary_tag_list_template.hbs">
<#include "/view/domain/jrnl/sumry/tag/_jrnl_sumry_dream_tag_list_template.hbs">
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_imprtc_diary_list_template.hbs">
<#include "/view/domain/jrnl/sumry/_jrnl_sumry_imprtc_dream_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/sumry/jrnl_sumry_dtl.js?${releaseDate!}"></script>

</@layout.layout>
