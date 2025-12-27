<#--
 * 저널 일자(jrnl_day) : 페이지
 -->

<#import "/layout/layout_popup.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/jrnl/day/_jrnl_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<script>
    window.JRNL = {
        stdrdDt: "${stdrdDt?js_string}"
    };
</script>

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

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
        <div id="jrnl_day_list_div">
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

<#--include::fullcalendar-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>

<#--include::jrnl_day_module-->
<#include "/view/domain/jrnl/day/_modal/_jrnl_day_reg_modal.ftlh">
<#include "/view/domain/jrnl/day/_modal/_jrnl_day_dtl_modal.ftlh">
<#include "/view/domain/jrnl/day/_jrnl_day_list_template.hbs">
<#include "/view/domain/jrnl/day/_partial/_jrnl_day_stdrd_dt_partial.hbs">
<#include "/view/domain/jrnl/day/_partial/_jrnl_day_context_btn_partial.hbs">
<#include "/view/domain/jrnl/day/_partial/_jrnl_day_meta_btn_partial.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_module.js?${releaseDate!}"></script>

<#--include::jrnl_entry_module-->
<#include "/view/domain/jrnl/entry/_modal/_jrnl_entry_reg_modal.ftlh">
<#include "/view/domain/jrnl/entry/_jrnl_entry_list_template.hbs">
<#include "/view/domain/jrnl/entry/_partial/_jrnl_entry_reg_btn_partial.hbs">
<#include "/view/domain/jrnl/entry/_partial/_jrnl_entry_context_btn_partial.hbs">
<#include "/view/domain/jrnl/entry/_partial/_jrnl_entry_toggle_btn_partial.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/entry/jrnl_entry_module.js?${releaseDate!}"></script>

<#--include::jrnl_diary_module-->
<#include "/view/domain/jrnl/diary/_modal/_jrnl_diary_reg_modal.ftlh">
<#include "/view/domain/jrnl/diary/_jrnl_diary_list_template.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_reg_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_copy_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_context_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_toggle_btn_partial.hbs">
<#include "/view/domain/jrnl/diary/_partial/_jrnl_diary_cn_partial.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/diary/jrnl_diary_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_module-->
<#include "/view/domain/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<#include "/view/domain/jrnl/dream/_jrnl_dream_list_template.hbs">
<#include "/view/domain/jrnl/dream/_partial/_jrnl_dream_reg_btn_partial.hbs">
<#include "/view/domain/jrnl/dream/_partial/_jrnl_dream_copy_btn_partial.hbs">
<#include "/view/domain/jrnl/dream/_partial/_jrnl_dream_context_btn_partial.hbs">
<#include "/view/domain/jrnl/dream/_partial/_jrnl_dream_toggle_btn_partial.hbs">
<#include "/view/domain/jrnl/dream/_partial/_jrnl_dream_cn_partial.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>

<#--include::jrnl_intrpt_module-->
<#include "/view/domain/jrnl/intrpt/_modal/_jrnl_intrpt_reg_modal.ftlh">
<#include "/view/domain/jrnl/intrpt/_jrnl_intrpt_list_template.hbs">
<#include "/view/domain/jrnl/intrpt/_partial/_jrnl_intrpt_reg_btn_partial.hbs">
<#include "/view/domain/jrnl/intrpt/_partial/_jrnl_intrpt_copy_btn_partial.hbs">
<#include "/view/domain/jrnl/intrpt/_partial/_jrnl_intrpt_context_btn_partial.hbs">
<#include "/view/domain/jrnl/intrpt/_partial/_jrnl_intrpt_toggle_btn_partial.hbs">
<#include "/view/domain/jrnl/intrpt/_partial/_jrnl_intrpt_cn_partial.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/intrpt/jrnl_intrpt_module.js?${releaseDate!}"></script>

<#--include::jrnl_todo_module-->
<#include "/view/domain/jrnl/todo/_jrnl_todo_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/todo/jrnl_todo_module.js?${releaseDate!}"></script>

<#--include::jrnl_day_tag_module-->
<#include "/view/domain/jrnl/day/tag/_modal/_jrnl_day_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/day/tag/jrnl_day_tag_module.js?${releaseDate!}"></script>

<#--include::jrnl_day_meta_module-->
<#include "/view/domain/jrnl/day/meta/_modal/_jrnl_day_meta_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/jrnl/day/meta/jrnl_day_meta_module.js?${releaseDate!}"></script>

<#--include::jrnl_diary_tag_module-->
<#include "/view/domain/jrnl/diary/tag/_modal/_jrnl_diary_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/diary/tag/jrnl_diary_tag_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_tag_module-->
<#include "/view/domain/jrnl/dream/tag/_modal/_jrnl_dream_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::jrnl_intrpt_tag_module-->
<#include "/view/domain/jrnl/intrpt/tag/_modal/_jrnl_intrpt_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/intrpt/tag/_jrnl_intrpt_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/intrpt/tag/jrnl_intrpt_tag_module.js?${releaseDate!}"></script>

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
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_view.js?${releaseDate!}"></script>

</@layout.layout>