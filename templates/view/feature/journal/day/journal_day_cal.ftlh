<#--
 * 저널 일자(journal_day) : 페이지 (달력)
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/journal/day/_journal_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<style>
    .tooltip {
        min-width: 900px !important;      /* 툴팁의 최소 너비 설정 */
        max-width: 1200px !important;     /* 최대 너비 설정 */
        width: auto !important;           /* 툴팁의 너비 자동으로 설정 */
    }
    .tooltip-inner {
        text-align: left;
        min-width: 900px !important;      /* 툴팁의 최소 너비 설정 */
        max-width: 1200px !important;     /* 최대 너비 설정 */
        max-height: 900px !important;     /* 툴팁 최대 높이 설정 */
        overflow-y: auto !important;      /* 세로 스크롤 추가 */
        word-break: break-word !important; /* 긴 단어가 줄 바꿈되도록 */
        white-space: normal !important;    /* 툴팁 내에서 텍스트가 여러 줄로 나올 수 있도록 설정 */
        word-wrap: break-word !important;  /* 긴 단어가 화면을 넘지 않도록 */
    }
</style>
<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />
<link rel="stylesheet" type="text/css" href="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.css" />

<!--begin::Advance form-->
<div class="collapse px-10 py-4 bg-light w-100" id="schedule_advanced_search_div">
    <div class="d-flex flex-lg-row flex-column align-items-lg-center align-items-start justify-content-end text-muted">
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내 일정 정보만 조회합니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_myPaprChk" class="me-2 text-muted cursor-help">내 일정만 보기</label>
            <input type="checkbox" name="myPaprChk" id="schedule_chk_myPaprChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>

        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_vcatnChk" class="me-2 text-muted cursor-help">휴가</label>
            <input type="checkbox" name="vcatnChk" id="schedule_chk_vcatnChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내부일정 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_indtChk" class="me-2 text-muted cursor-help">내부일정</label>
            <input type="checkbox" name="indtChk" id="schedule_chk_indtChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="외근 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_outdtChk" class="me-2 text-muted cursor-help">외근</label>
            <input type="checkbox" name="outdtChk" id="schedule_chk_outdtChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="재택근무 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_tlcmmtChk" class="me-2 text-muted cursor-help">재택근무</label>
            <input type="checkbox" name="tlcmmtChk" id="schedule_chk_tlcmmtChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="개인용 일정 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schedule_chk_prvtChk" class="me-2 text-muted cursor-help">개인용 일정</label>
            <input type="checkbox" name="prvtChk" id="schedule_chk_prvtChk" class="form-check-input cursor-pointer" value="Y" onclick="Page.chkbxProp(this);">
        </div>
    </div>
</div>
<!--end::Advance form-->

<div class="journal-day-calendar-page">
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
                <a class="nav-link px-6 cursor-pointer active" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_CAL);">
                    <span class="nav-icon">
                        <i class="bi bi-sort-down-alt"></i>
                    </span>
                    <span class="nav-text"><@spring.message 'txt.journal.day.view.cal'/></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 cursor-pointer" data-bs-toggle="tab"
                   onclick="Page.changeView(Url.JOURNAL_DAY_META_VIEW);">
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
        <div class="card-header min-h-auto mb-7">
            <!-- 태그 헤더 :: 파일 분리 -->
            <#include "/view/feature/journal/day/tag/_journal_day_tag_header.ftlh">
        </div>

        <#--begin::Card body-->
        <div class="card-body">
            <!--begin::Calendar-->
            <div id="full_calendar_app">
                <!--FullCalendar goes in here!!!-->
            </div>
            <!--end::Calendar-->
        </div>
        <!--end::Card body-->
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

<#--include::fullcalendar-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>

<#--include::journal_day_module-->
<#include "/view/feature/journal/day/_modal/_journal_day_reg_modal.ftlh">
<#include "/view/feature/journal/day/_modal/_journal_day_dtl_modal.ftlh">
<#include "/view/feature/journal/day/_journal_day_list_template.hbs">
<#include "/view/feature/journal/day/_partial/_journal_day_context_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_module.js?${releaseDate!}"></script>

<#--include::journal_diary_module-->
<#include "/view/feature/journal/diary/_modal/_journal_diary_reg_modal.ftlh">
<#include "/view/feature/journal/diary/_journal_diary_list_template.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_item_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_copy_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_context_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_toggle_btn_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_states_partial.hbs">
<#include "/view/feature/journal/diary/_partial/_journal_diary_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/diary/journal_diary_module.js?${releaseDate!}"></script>

<#--include::journal_dream_module-->
<#include "/view/feature/journal/dream/_modal/_journal_dream_reg_modal.ftlh">
<#include "/view/feature/journal/dream/_journal_dream_list_template.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_item_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_reg_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_copy_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_context_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_toggle_btn_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_states_partial.hbs">
<#include "/view/feature/journal/dream/_partial/_journal_dream_content_partial.hbs">
<script type="text/javascript" src="/js/view/feature/journal/dream/journal_dream_module.js?${releaseDate!}"></script>

<#--include::journal_todo_module-->
<#include "/view/feature/journal/todo/_journal_todo_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/todo/journal_todo_module.js?${releaseDate!}"></script>

<#--include::journal_day_tag_module-->
<#include "/view/feature/journal/diary/tag/_journal_diary_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/diary/tag/journal_diary_tag_module.js?${releaseDate!}"></script>

<#--include::journal_day_tag_module-->
<#include "/view/feature/journal/day/tag/_modal/_journal_day_tag_dtl_modal.ftlh">
<#include "/view/feature/journal/day/tag/_journal_day_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/day/tag/journal_day_tag_module.js?${releaseDate!}"></script>

<#--include::journal_day_meta_module-->
<script type="text/javascript" src="/js/view/feature/journal/day/meta/journal_day_meta_module.js?${releaseDate!}"></script>

<#--include::journal_dream_tag_module-->
<#include "/view/feature/journal/dream/tag/_journal_dream_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/dream/tag/journal_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#include "/view/feature/attachable/tag/_modal/_tag_profile_modal.ftlh">
<#include "/view/feature/attachable/tag/_partial/_tag_list_partial.hbs">
<#include "/view/feature/attachable/tag/_partial/_tag_list_sized_partial.hbs">

<#include "/view/feature/attachable/meta/_partial/_meta_list_partial.hbs">

<#--include::comment-->
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#include "/view/feature/attachable/history/_history_modal.ftlh">
<#include "/view/feature/attachable/comment/_partial/_comment_list_partial.hbs">
<#include "/view/feature/attachable/comment/_partial/_comment_reg_btn_partial.hbs">
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/attachable/state/state_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_cal.js?${releaseDate!}"></script>

</@layout.layout>

