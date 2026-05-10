<#--
 * 저널 일자(journal_day) : 일간(daily).
 * 목록 레이아웃(aside)을 쓰는 월간·주간과 달리, 본 페이지는 팝업 전용 레이아웃(`layout_popup.ftlh`)을 사용한다.
 * 마이그레이션 과정에서 범용 `journal_day_view.ftlh` 진입 대신 일간 전용 `journal_day_daily.ftlh` 로 분리·고정되었다(`docs/DEV_NOTES.md` 참고).
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

<div id="journal_day_app" class="d-none" data-view-type="daily"></div>
<script>
    var Model = window.Model || {};
    Model.locale = Model.locale || "${.locale?replace('_', '-')?js_string}";
</script>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::journal_day_module-->
<#-- journal_day_list: 일간(daily) - JournalDayDailyListApp / journalDayListAppMount 가 #journal_day_list_div 에 마운트된다. HBS 일간 목록용 include 는 제거되었다. -->
<#include "/view/feature/journal/day/_modal/_journal_day_reg_modal.ftlh">
<#include "/view/feature/journal/day/_modal/_journal_day_dtl_modal.ftlh">
<#-- 변경(A-8): `_journal_day_stdrd_dt_partial` / `_journal_day_context_btn_partial` / `_journal_day_meta_btn_partial`
     include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건이라 dead partial 로 확정. partial 파일 자체도 삭제. -->
<script type="module" src="/vue/feature/journal/day/registerJournalDayShellServices.js?${releaseDate!}"></script>

<#include "/view/feature/journal/chapter/_modal/_journal_chapter_reg_modal.ftlh">


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

<#--include::comment-->
<#include "/view/feature/attachable/history/_history_modal.ftlh">
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#-- 변경(A-10): `_comment_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. id `comment_list_partial` 자체는
     호출자 없음(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). -->
<#-- 변경(A-8): `_comment_reg_btn_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건이라 dead partial 로 확정. -->
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/attachable/state/state_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/lifecycle/lifecycle_module.js?${releaseDate!}"></script>

<#-- 변경(A-9): 일기/꿈 태그 헤더 Vue 흡수. journalEntryTagService 적재(=_journal_entry_reg_modal.ftlh ESM 묶음) 이후
     본 앱이 마운트되어 `JournalDayEntryTagListVueApp.setList` 브리지를 노출한다.
     마운트 전 호출은 `pendingByType` 큐잉으로 흡수 — 적재 경합 방지. -->
<script type="module" src="/vue/feature/journal/day/JournalDayEntryTagListApp.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script>
    window.JournalDayDailyApp = window.JournalDayDailyApp || {
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
<script type="module" src="/vue/feature/journal/day/JournalDayDailyListApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagDetailModalApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagPanelApp.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/day/JournalDayTagProfileModalApp.js?${releaseDate!}"></script>

</@layout.layout>
