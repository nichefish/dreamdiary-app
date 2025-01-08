<#--
 * 저널 일자(jrnl_day) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/jrnl/day/_jrnl_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">
    <#--begin::Card header-->
    <div class="card-header min-h-auto mb-7">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_1">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text">목록 VIEW</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_2"
                   onclick="Page.calPage();">
                    <span class="nav-icon">
                        <i class="bi bi-sort-down-alt"></i>
                    </span>
                    <span class="nav-text">달력 VIEW</span>
                </a>
            </li>
        </ul>
    </div>

    <!-- 태그 헤더 :: 파일 분리 -->
    <#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_header.ftlh">

    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table body-->
        <div id="jrnl_day_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div id="jrnl_diary_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div id="jrnl_dream_list_div">
            <#--handlebars template goes in here-->
        </div>
    </div>
</div>
<!--end::Card-->

<#--include:: Aside-->
<#include "/view/domain/jrnl/day/_jrnl_day_aside_base.ftlh">

<#--include::modals-->
<#include "/view/domain/jrnl/day/_jrnl_day_list_template.hbs">
<#include "/view/domain/jrnl/day/_modal/_jrnl_day_reg_modal.ftlh">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_list_template.hbs">
<#include "/view/domain/jrnl/day/tag/_modal/_jrnl_day_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/diary/_modal/_jrnl_diary_reg_modal.ftlh">
<#include "/view/domain/jrnl/diary/_jrnl_diary_list_template.hbs">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_list_template.hbs">
<#include "/view/domain/jrnl/diary/tag/_modal/_jrnl_diary_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<#include "/view/domain/jrnl/dream/_jrnl_dream_list_template.hbs">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_list_template.hbs">
<#include "/view/domain/jrnl/dream/tag/_modal/_jrnl_dream_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">
<#include "/view/global/_common/_clsf/comment/_modal/_comment_reg_modal.ftlh">

<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_ctgr_map.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_page.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/day/tag/jrnl_day_tag_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/diary/jrnl_diary_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/jrnl/diary/tag/jrnl_diary_tag_module.js?${releaseDate!}"></script>
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_ctgr_map.ftlh">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_ctgr_map.ftlh">
<#include "/view/domain/jrnl/tag/_jrnl_tag_scripts.ftlh">
<script type="text/javascript" src="/js/view/global/_common/_clsf/comment/comment_modal_module.js?${releaseDate!}"></script>

</@layout.layout>