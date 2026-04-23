<#--
 * 저널 일자(journal_day) : 페이지
 -->

<#import "/layout/layout_popup.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    window.JOURNAL = {
        stdrdDt: "${stdrdDt?js_string}"
    };
</script>

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">

    <#--begin::Card header-->
    <div class="card-header min-h-auto">
        <#-- datepicker-->
        <div class="d-flex-align-center mb-4">
            <div class="menu-icon me-md-2 vertical-middle">
                <label for="stdrdDt"><i class="bi bi-calendar3 fs-3 cursor-pointer"></i></label>
            </div>
            <div class="datepicker">
                <input type="text" name="stdrdDt" id="stdrdDt" class="form-control form-control-sm form-control-solid w-150px d-inline cursor-pointer"
                       value="${stdrdDt!}" placeholder="Pick date" maxlength="25" readonly />
            </div>
        </div>
    </div>
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table body-->
        <div id="journal_day_list_div">
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
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_export_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_context_btn_partial.hbs">
<#include "/view/feature/journal/chapter/_partial/_journal_chapter_toggle_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/chapter/journal_chapter_module.js?${releaseDate!}"></script>

<#--include::journal_diary_module-->
<#assign journalEntryRegType = "DIARY">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">
<#include "/view/feature/journal/entry/_partial/_journal_entry_item_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_reg_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_context_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_copy_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_toggle_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_states_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/entry/journal_entry_module.js?${releaseDate!}"></script>

<#--include::journal_note_module-->
<#assign journalEntryRegType = "NOTE">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<#--include::journal_dream_module-->
<#assign journalEntryRegType = "DREAM">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<#--include::journal_interpretation_module-->
<#include "/view/feature/journal/interpretation/_journal_interpretation_list_template.hbs">
<#include "/view/feature/journal/interpretation/_modal/_journal_interpretation_reg_modal.ftlh">
<#include "/view/feature/journal/interpretation/_partial/_journal_interpretation_item_partial.hbs">
<#include "/view/feature/journal/interpretation/_partial/_journal_interpretation_copy_btn_partial.hbs">
<#include "/view/feature/journal/interpretation/_partial/_journal_interpretation_context_btn_partial.hbs">
<#include "/view/feature/journal/interpretation/_partial/_journal_interpretation_toggle_btn_partial.hbs">
<#include "/view/feature/journal/interpretation/_partial/_journal_interpretation_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/interpretation/journal_interpretation_module.js?${releaseDate!}"></script>

<#--include::journal_todo_module-->
<#include "/view/feature/journal/todo/_journal_todo_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/todo/journal_todo_module.js?${releaseDate!}"></script>

<#--include::journal_day_tag_module-->
<#include "/view/feature/journal/day/tag/_journal_day_tag_list_template.hbs">
<#include "/view/feature/journal/day/tag/_modal/_journal_day_tag_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/journal/day/tag/journal_day_tag_module.js?${releaseDate!}"></script>

<#--include::journal_day_meta_module-->
<#include "/view/feature/journal/day/meta/_modal/_journal_day_meta_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/journal/day/meta/journal_day_meta_module.js?${releaseDate!}"></script>

<#--include::journal_entry_tag_module-->
<#include "/view/feature/journal/entry/tag/_journal_entry_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/entry/tag/journal_entry_tag_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/attachable/tag/_modal/_tag_profile_modal.ftlh">
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
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_view.js?${releaseDate!}"></script>

</@layout.layout>
