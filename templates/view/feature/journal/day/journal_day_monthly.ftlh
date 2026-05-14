<#--
 * 저널 일자(journal_day) : 월간.
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::저널 일자 페이지 공통 헤더-->
<#assign toolbar>
    <#include "/view/feature/journal/day/_journal_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<div class="journal-day-monthly-page">
    <div class="d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_WEEKLY);">
                    <span class="nav-icon">
                        <i class="bi bi-calendar-week"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.week'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer active" data-bs-toggle="tab"
                   onclick="dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_MONTHLY);">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.list'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_CAL);">
                    <span class="nav-icon">
                        <i class="bi bi-sort-down-alt"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.cal'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="dF.JournalDayViewService.changeView(Url.JOURNAL_DAY_META_VIEW);">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.meta'/></span>
                </a>
            </li>
        </ul>
        <#--keyword search (popup)-->
        <#include "/view/feature/journal/day/_journal_day_keyword_search.ftlh">
    </div>
    <!--begin::Card-->
    <div class="card post" style="margin-top: 0 !important;">
        <#--begin::Card header-->
        <div class="card-header min-h-auto mb-7 justify-content-between">
            <!-- ??? ??? :: ??? ?? -->
            <#include "/view/feature/journal/day/tag/_journal_day_tag_header.ftlh">
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

    <#--include::aside-->
    <#include "/view/feature/journal/day/_aside/_journal_day_aside_base.ftlh">
</div>

<div id="journal_day_app" class="d-none" data-view-type="monthly"></div>
<script>
    var Model = window.Model || {};
    Model.locale = Model.locale || "${.locale?replace('_', '-')?js_string}";
</script>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::fullcalendar-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>

<#--include::journal_day_module-->
<#-- journal_day_list: monthly ??JournalDayMonthlyListApp ??journalDayListAppMount ? #journal_day_list_div ????????. HBS journal_day_list ?????include ??? (daily/cal/meta ???? ????). -->
<#include "/view/feature/journal/day/_modal/_journal_day_reg_modal.ftlh">
<#include "/view/feature/journal/day/_modal/_journal_day_dtl_modal.ftlh">
<#-- 변경(A-8): `_journal_day_stdrd_dt_partial` / `_journal_day_context_btn_partial` / `_journal_day_meta_btn_partial`
     include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건이라 dead partial 로 확정. partial 파일 자체도 삭제. -->
<script type="module" src="/vue/feature/journal/day/registerJournalDayShellServices.js?${releaseDate!}"></script>

<#include "/view/feature/journal/chapter/_modal/_journal_chapter_regist_modal.ftlh">


<#--include::journal_diary_module-->
<#assign journalEntryRegType = "DIARY">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">
<#-- 변경(A-8): `_journal_entry_context_btn_partial` / `_journal_entry_copy_btn_partial` / `_journal_entry_content_partial`
     include 제거 — `{{>}}` 호출 0건(자기 자신 `_by_type` 래퍼도 호출자 0건), JS 문자열 참조 0건이라 dead partial 로 확정. -->
<#-- 변경(A-10): `_journal_entry_reg_btn_partial` / `_journal_entry_toggle_btn_partial` / `_journal_entry_states_partial`
     include 제거 — `{{>}}` 호출 0건(자기 자신 `_by_type` 래퍼 포함), JS 문자열 참조 0건. 등록 버튼/토글 버튼/상태 뱃지 마크업은
     Vue 컴포넌트(`JournalDayCard` / `JournalEntryItem` / `JournalChapterItem` / `JournalEntrySearchItem` / `JournalInterpretationItem`)에 흡수됨. -->
<#-- 변경(E-4-δ-3): journalEntryService 적재는 _journal_entry_reg_modal.ftlh 의 ES module 묶음으로 단일 수렴. -->

<#--include::journal_note_module-->
<#assign journalEntryRegType = "NOTE">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<#--include::journal_dream_module-->
<#assign journalEntryRegType = "DREAM">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<#--include::journal_interpretation_module-->
<#include "/view/feature/journal/interpretation/_modal/_journal_interpretation_regist_modal.ftlh">


<#--include::journal_day_tag_module-->
<#include "/view/feature/journal/day/tag/_modal/_journal_day_tag_dtl_modal.ftlh">

<#--include::journal_day_meta_module-->
<#include "/view/feature/journal/day/meta/_modal/_journal_day_meta_modal.ftlh">

<#-- journal_entry_tag_module 제거(E-4-γ): journalEntryTagService가 등록 모달에 포함됨. -->
<#-- 변경(A-9): `_journal_entry_tag_list_template` include 제거 — 일기/꿈 태그 헤더 Vue 흡수(JournalDayEntryTagListApp).
     journalEntryTagService.renderList 가 본 앱의 setList 브리지로 수렴(Handlebars 컴파일 비경유). -->

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/attachable/tag/_modal/_tag_profile_modal.ftlh">
<#-- 변경(A-9): `_tag_list_partial` / `_tag_list_sized_partial` include 제거 — `{{>}}` 호출 0건(이전 유일 호출자
     `_journal_entry_tag_list_template` 도 함께 제거), JS 문자열 참조 0건이라 dead partial 로 확정. -->

<#-- 변경(A-10): `_meta_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. 메타 sized 행 마크업은
     `JournalDayMetaHeaderList` Vue 컴포넌트에 흡수됨. -->

<#--include::comment-->
<#include "/view/feature/attachable/history/_history_modal.ftlh">
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#-- 변경(A-10): `_comment_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. id `comment_list_partial` 자체는
     호출자 없음(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). -->
<#-- 변경(A-8): `_comment_reg_btn_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건이라 dead partial 로 확정. -->
<#include "/view/feature/attachable/related/_related_content_add_modal.ftlh">

<#--include::state-->

<#-- 변경(A-9): 일기/꿈 태그 헤더 Vue 흡수. journalEntryTagService 적재(=_journal_entry_reg_modal.ftlh ESM 묶음) 이후
     본 앱이 마운트되어 `JournalDayEntryTagListVueApp.setList` 브리지를 노출한다.
     마운트 전 호출은 `pendingByType` 큐잉으로 흡수 — 적재 경합 방지. -->
<script type="module" src="/vue/feature/journal/day/JournalDayEntryTagListApp.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script>
    window.JournalDayMonthlyApp = window.JournalDayMonthlyApp || {
        mounted: false,
        pendingModel: null,
        pendingSort: null,
        pendingLoad: null,
        render: function(model) { this.pendingModel = model; },
        clear: function() { this.pendingModel = { list: [] }; },
        sort: function(sort) { this.pendingSort = sort; },
        loadMonthly: function() { this.pendingLoad = { type: 'monthly' }; },
        loadWeekly: function(stdrdDt, targetDt) { this.pendingLoad = { type: 'weekly', stdrdDt: stdrdDt, targetDt: targetDt }; },
        loadDaily: function(stdrdDt) { this.pendingLoad = { type: 'daily', stdrdDt: stdrdDt }; },
        applySearchParamsAndReload: function(patch, scope) { this.pendingLoad = { type: 'reload', patch: patch || {}, scope: scope || 'CURRENT' }; },
        refresh: function() { this.pendingLoad = { type: 'refresh' }; }
    };
    window.JournalDayTagDetailVueApp = window.JournalDayTagDetailVueApp || {
        mounted: false,
        pendingPayload: null
    };
    window.JournalDayTagPanelVueApp = window.JournalDayTagPanelVueApp || {
        mounted: false,
        pendingDayTagList: null,
        pendingModalTagList: null
    };
    window.JournalDayTagProfileVueApp = window.JournalDayTagProfileVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/day/JournalDayMonthlyListApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagDetailModalApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagPanelApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagProfileModalApp.js?${releaseDate!}"></script>

</@layout.layout>
