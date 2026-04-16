<#--
 * 저널 일기(journal_diary) : 검색
 -->

<#import "/layout/layout_popup.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">
    <#--begin::Card header-->
    <div class="card-header min-h-auto">
        <#include "/view/feature/journal/diary/_journal_diary_search_header.ftlh">
    </div>
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table body-->
        <div id="journal_diary_search_div">
            <#--handlebars template goes in here-->
        </div>
    </div>
</div>
<!--end::Card-->

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
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_context_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_toggle_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/chapter/journal_chapter_module.js?${releaseDate!}"></script>

<#--include::journal_diary_module-->
<#include "/view/feature/journal/diary/_journal_diary_search_template.hbs">
<#include "/view/feature/journal/diary/_modal/_journal_diary_reg_modal.ftlh">
<#include "/view/feature/journal/diary/_partial/_journal_diary_item_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_reg_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_copy_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_search_context_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_toggle_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_states_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_content_search_partial.hbs">
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

<#--include::journal_diary_tag_module-->
<#include "/view/feature/journal/diary/tag/_journal_diary_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/diary/tag/journal_diary_tag_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/attachable/tag/_partial/_tag_list_partial.hbs">
<#include "/view/feature/attachable/tag/_partial/_tag_list_sized_partial.hbs">

<#--include::comment-->
<#include "/view/feature/attachable/history/_history_modal.ftlh">
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/feature/attachable/comment/_partial/_comment_list_partial.hbs">
<#include "/view/feature/attachable/comment/_partial/_comment_reg_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/attachable/state/state_module.js?${releaseDate!}"></script>

<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/journal/diary/journal_diary_search.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/journal/diary/journal_diary_search_module.js?${releaseDate!}"></script>

</@layout.layout>
