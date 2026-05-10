<#--
 * journal_day: aside (right sidebar)
 * @included_in : journal_day_page.ftlh
 -->

<#--begin::aside-->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <#--begin::Aside wrapper-->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y ps-5 pe-2 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <#--begin::Aside-->
        <div class="card card-reset card-p-0">
            <#--begin::aside filter header (Vue JournalDayAsideFilterHeaderApp)-->
            <div id="journal_day_aside_filter_header_mount" style="display:contents"></div>
            <!--end::Header-->
            <#--begin::Body-->
            <div id="journal_aside" class="card-body">
                <input type="hidden" name="sort" id="sort">

                <div class="d-flex flex-column gap-4">
                    <!-- 년월 필터 부분 분리 -->
                    <#include "/view/feature/journal/day/_aside/_journal_day_aside_yy_mnth_section.ftlh">
                    <!-- separator -->
                    <div class="separator"></div>
                    <!-- TAGCLOUD·일기·꿈·고급필터 아코디언: JournalDayAsideEntryFiltersApp (+ 챕터 옵션은 FTL 적재 SSOT). -->
                    <script>
                    (function() {
                        var chapterCtgrOptions = [
                            <#list JOURNAL_CHAPTER_CTGR_CD![] as ctgr>
                            { code: "${ctgr.code!''?js_string}", codeName: "${ctgr.codeName!''?js_string}" }<#if ctgr_has_next>,</#if>
                            </#list>
                        ];
                        window.__journalAsideEntryFiltersBootstrap = window.__journalAsideEntryFiltersBootstrap || { chapterCtgrOptions: chapterCtgrOptions };
                    })();
                    </script>
                    <div id="journal_day_aside_entry_filters_mount" style="display:contents"></div>
                    <script type="module" src="/vue/feature/journal/day/JournalDayAsideEntryFiltersApp.js?${releaseDate!}"></script>
                </div>
            </div>
            <script type="module" src="/vue/feature/journal/day/JournalDayAsideFilterHeaderApp.js?${releaseDate!}"></script>
        </div>
        <!--end::Aside-->

        <div class="separator my-8"></div>

        <#--begin::Aside TODO card — Vue JournalDayAsideTodoCardApp (헤더·목록 단일 카드 SSOT).-->
        <div id="journal_day_aside_todo_card_mount" style="display:contents"></div>
        <script>
            window.JournalDayAsideTodoVueApp = window.JournalDayAsideTodoVueApp || {
                mounted: false,
                pendingTodoListPayload: null
            };
        </script>
        <script type="module" src="/vue/feature/journal/day/JournalDayAsideTodoCardApp.js?${releaseDate!}"></script>
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->
<div id="journal_day_aside_week_nav_app" class="d-none"></div>

<#--include::script modules-->
<script>
    window.JournalDayAsideWeekNavigatorVueApp = window.JournalDayAsideWeekNavigatorVueApp || {
        mounted: false,
        pendingPayload: null
    };
</script>
<script type="module" src="/vue/feature/journal/day/JournalDayAsideWeekNavigatorApp.js?${releaseDate!}"></script>

<#include "/view/feature/journal/todo/_modal/_journal_todo_regist_modal.ftlh">
