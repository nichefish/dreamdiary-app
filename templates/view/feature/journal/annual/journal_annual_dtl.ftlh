<#--
 * 저널 결산(journal_annual) : 페이지
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<!--begin::Card-->
<div id="journal_annual_dtl_div" class="card post">
    <!-- handlebars template goes in here-->
</div>

<#--begin::Card header-->
<div class="card-header min-h-auto mb-7">
    <#--begin::Tab-->
    <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DIARY')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_1"
               onclick="dF.JournalAnnual.dtlViewWithSection('DIARY');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-book"></i>
                </span>
                <span class="nav-text"><@spring.message 'txt.diary.sumry'/></span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DREAM')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_2"
               onclick="dF.JournalAnnual.dtlViewWithSection('DREAM');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-moon-stars"></i>
                </span>
                <span class="nav-text"><@spring.message 'txt.dream.sumry'/></span>
            </a>
        </li>
    </ul>
</div>

<!-- 태그 헤더 :: 파일 분리 -->
<#include "/view/feature/journal/annual/tag/_journal_annual_dtl_tag_header.ftlh">
<hr class="my-6 text-muted" />

<div class="d-flex align-items-end justify-content-end mb-2">
    <div class="mx-3"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="중요 토글">
        <label for="toggleImprtc" class="me-2 text-muted cursor-help">IMPORTANT</label>
        <input type="checkbox" id="toggleImprtc" class="form-check-input cursor-pointer" onclick="dF.JournalAnnual.toggleParam();" checked>
    </div>
    <div class="mx-3"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="참조 토글">
        <label for="toggleRefrnc" class="me-2 text-muted cursor-help">REFERENCE</label>
        <input type="checkbox" id="toggleRefrnc" class="form-check-input cursor-pointer" onclick="dF.JournalAnnual.toggleParam();" checked>
    </div>
</div>

<div class="journal_annual_day_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="journal_annual_diary_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="journal_annual_dream_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="card-post p-10">
    <!-- 중요 일기 -->
    <div id="journal_annual_diary_list_div">
        <!-- handlebars template goes in here -->
    </div>

    <!-- 중요 꿈 -->
    <div id="journal_annual_imprtc_dream_list_div">
        <!-- handlebars template goes in here -->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="" />
    <input type="hidden" name="yy" id="yy" value="" />
    <input type="hidden" name="mnth" id="mnth" value="99" />
</form>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::journal_day_module-->
<#include "/view/feature/journal/day/_partial/_journal_day_stdrd_dt_partial.hbs">
<#include "/view/feature/journal/day/_partial/_journal_day_context_btn_partial.hbs">
<#include "/view/feature/journal/day/_partial/_journal_day_meta_btn_partial.hbs">

<#--include::journal_diary_module-->
<#include "/view/feature/journal/entry/_partial/_journal_entry_reg_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_context_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_copy_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_toggle_btn_partial.hbs">
<#include "/view/feature/journal/entry/_partial/_journal_entry_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/entry/journal_entry_module.js?${releaseDate!}"></script>

<#--include::journal_dream_module-->
<#assign journalEntryRegType = "DREAM">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<#--include::journal_entry_tag_module-->
<script type="text/javascript" src="/js/view/feature/journal/entry/tag/journal_entry_tag_module.js?${releaseDate!}"></script>

<#--include::tag-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/attachable/tag/_partial/_tag_list_partial.hbs">
<#include "/view/feature/attachable/tag/_partial/_tag_list_sized_partial.hbs">

<#--include::comment-->
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/feature/attachable/comment/_partial/_comment_list_partial.hbs">
<#include "/view/feature/attachable/comment/_partial/_comment_reg_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/attachable/state/state_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/lifecycle/lifecycle_module.js?${releaseDate!}"></script>

<#--include::journal_annual_modules-->
<#include "/view/feature/journal/annual/_partial/_journal_annual_context_btn_partial.hbs">
<#include "/view/feature/journal/annual/_partial/_journal_annual_review_item_partial.hbs">
<#include "/view/feature/journal/annual/_partial/_journal_annual_review_content_partial.hbs">
<#include "/view/feature/journal/annual/_partial/_journal_annual_review_context_btn_partial.hbs">
<#include "/view/feature/journal/annual/_modal/_journal_annual_review_reg_modal.ftlh">
<#include "/view/feature/journal/annual/_journal_annual_dtl_template.hbs">
<#include "/view/feature/journal/annual/tag/_journal_annual_entry_tag_list_template.hbs">
<#include "/view/feature/journal/annual/_journal_annual_entry_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/annual/journal_annual_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/journal/annual/journal_annual_review_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/journal/annual/journal_annual_dtl.js?${releaseDate!}"></script>

</@layout.layout>
