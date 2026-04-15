<#--
 * 저널 일자(journal_day) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/journal/day/_journal_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />
<div class="journal-day-meta-page">
    <div class="d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_WEEKLY);">
                    <span class="nav-icon">
                        <i class="bi bi-calendar-week"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.week'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_MONTHLY);">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.list'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_CAL);">
                    <span class="nav-icon">
                        <i class="bi bi-sort-down-alt"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.cal'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer active" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_META_VIEW);">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.meta'/></span>
                </a>
            </li>
        </ul>
    </div>
    <!--begin::Card-->
    <div class="card post" style="margin-top: 0 !important;">
        <#--begin::Card header-->
        <div class="card-header min-h-auto mb-7">
            <!-- 메타 헤더 :: 파일 분리 -->
            <#include "/view/feature/journal/day/meta/_journal_day_meta_header.ftlh">
        </div>
        <#--begin::Card body-->
        <div class="card-body">
            <#--begin::form-->
            <div id="journal_day_meta_config_div" class="d-flex-align-center mb-2 ps-2 min-h-42px">
                <span class="text-muted">
                    메타를 선택해 주세요.
                </span>
                <#--handlebars template goes in here-->
            </div>
            <#--begin::Table body-->
            <div id="journal_day_meta_graph_div" class="border border-1 border-primary d-flex-align-center min-h-42px" style="min-height: 200px;">
                graph goes in here
                <#--handlebars template goes in here-->
            </div>
        </div>
    </div>
    <!--end::Card-->

    <#--include::aside-->
    <#include "/view/feature/journal/day/_aside/_journal_day_aside_base.ftlh">
</div>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::journal_day_module-->
<#include "/view/feature/journal/day/_journal_day_list_template.hbs">
<#include "/view/feature/journal/day/_modal/_journal_day_reg_modal.ftlh">
<#include "/view/feature/journal/day/_modal/_journal_day_dtl_modal.ftlh">
<#include "/view/feature/journal/day/_partial/_journal_day_stdrd_dt_partial.hbs">
<#include "/view/feature/journal/day/_partial/_journal_day_context_btn_partial.hbs">
<#include "/view/feature/journal/day/_partial/_journal_day_meta_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_module.js?${releaseDate!}"></script>

<#--include::journal_chapter_module-->
<#include "/view/feature/journal/chapter/_journal_chapter_list_template.hbs">
<#include "/view/feature/journal/chapter/_modal/_journal_chapter_reg_modal.ftlh">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_item_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_reg_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_copy_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_export_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_context_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_toggle_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/chapter/journal_chapter_module.js?${releaseDate!}"></script>

<#--include::journal_diary_module-->
<#include "/view/feature/journal/diary/_journal_diary_list_template.hbs">
<#include "/view/feature/journal/diary/_modal/_journal_diary_reg_modal.ftlh">
<#include "/view/feature/journal/diary/_partial/_journal_diary_item_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_reg_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_copy_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_context_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_toggle_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_states_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/diary/journal_diary_module.js?${releaseDate!}"></script>

<#--include::journal_dream_module-->
<#include "/view/feature/journal/dream/_journal_dream_list_template.hbs">
<#include "/view/feature/journal/dream/_modal/_journal_dream_reg_modal.ftlh">
<#include "/view/feature/journal/dream/_partial/_journal_dream_item_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_reg_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_copy_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_context_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_toggle_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_states_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/dream/journal_dream_module.js?${releaseDate!}"></script>

<#--include::journal_intrpt_module-->
<#include "/view/feature/journal/intrpt/_journal_intrpt_list_template.hbs">
<#include "/view/feature/journal/intrpt/_modal/_journal_intrpt_reg_modal.ftlh">
<#include "/view/feature/journal/intrpt/_partial/_journal_intrpt_item_partial.hbs">
<#include "/view/feature/journal/intrpt/_partial/_journal_intrpt_copy_btn_partial.hbs">
<#include "/view/feature/journal/intrpt/_partial/_journal_intrpt_context_btn_partial.hbs">
<#include "/view/feature/journal/intrpt/_partial/_journal_intrpt_toggle_btn_partial.hbs">
<#include "/view/feature/journal/intrpt/_partial/_journal_intrpt_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/intrpt/journal_intrpt_module.js?${releaseDate!}"></script>

<#--include::journal_todo_module-->
<#include "/view/feature/journal/todo/_journal_todo_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/todo/journal_todo_module.js?${releaseDate!}"></script>

<#--include::journal_day_tag_module-->
<#include "/view/feature/journal/day/tag/_journal_day_tag_list_template.hbs">
<#include "/view/feature/journal/day/tag/_modal/_journal_day_tag_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/journal/day/tag/journal_day_tag_module.js?${releaseDate!}"></script>

<#--include::journal_day_meta_module-->
<#include "/view/feature/journal/day/meta/_journal_day_meta_config.hbs">
<#include "/view/feature/journal/day/meta/_modal/_journal_day_meta_modal.ftlh">
<#include "/view/feature/journal/day/meta/_modal/_journal_day_meta_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/day/meta/journal_day_meta_module.js?${releaseDate!}"></script>

<#--include::journal_diary_tag_module-->
<#include "/view/feature/journal/diary/tag/_journal_diary_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/diary/tag/journal_diary_tag_module.js?${releaseDate!}"></script>

<#--include::journal_dream_tag_module-->
<#include "/view/feature/journal/dream/tag/_journal_dream_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/dream/tag/journal_dream_tag_module.js?${releaseDate!}"></script>

<#--include::journal_intrpt_tag_module-->
<#include "/view/feature/journal/intrpt/tag/_journal_intrpt_tag_list_template.hbs">
<#include "/view/feature/journal/intrpt/tag/_modal/_journal_intrpt_tag_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/journal/intrpt/tag/journal_intrpt_tag_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/clsf/tag/_modal/_tag_profile_modal.ftlh">
<#include "/view/feature/clsf/tag/_partial/_tag_list_partial.hbs">
<#include "/view/feature/clsf/tag/_partial/_tag_list_sized_partial.hbs">

<#include "/view/feature/clsf/meta/_partial/_meta_list_partial.hbs">

<#--include::comment-->
<#include "/view/feature/clsf/history/_history_modal.ftlh">
<#include "/view/feature/clsf/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/feature/clsf/comment/_partial/_comment_list_partial.hbs">
<#include "/view/feature/clsf/comment/_partial/_comment_reg_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/clsf/comment/comment_modal_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/clsf/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/clsf/state/state_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_meta.js?${releaseDate!}"></script>

</@layout.layout>
