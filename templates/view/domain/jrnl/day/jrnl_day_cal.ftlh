<#--
 * 일정 > 전체 일정 화면 (달력)
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/jrnl/day/_jrnl_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
    <style>

    </style>
<link rel="stylesheet" type="text/css" href="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.css" />

<!--begin::Advance form-->
<div class="collapse px-10 py-4 bg-light w-100" id="schdul_advanced_search_div">
    <div class="d-flex flex-lg-row flex-column align-items-lg-center align-items-start justify-content-end text-muted">
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내 일정 정보만 조회합니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_myPaprChk" class="me-2 text-muted cursor-help">내 일정만 보기</label>
            <input type="checkbox" name="myPaprChk" id="schdul_chk_myPaprChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>

        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_vcatnChk" class="me-2 text-muted cursor-help">휴가</label>
            <input type="checkbox" name="vcatnChk" id="schdul_chk_vcatnChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내부일정 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_indtChk" class="me-2 text-muted cursor-help">내부일정</label>
            <input type="checkbox" name="indtChk" id="schdul_chk_indtChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="외근 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_outdtChk" class="me-2 text-muted cursor-help">외근</label>
            <input type="checkbox" name="outdtChk" id="schdul_chk_outdtChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="재택근무 정보 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_tlcmmtChk" class="me-2 text-muted cursor-help">재택근무</label>
            <input type="checkbox" name="tlcmmtChk" id="schdul_chk_tlcmmtChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>
        <div class="mx-3"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="개인용 일정 표시여부를&#10;켜고 끕니다.&#10;(해당 정보는 브라우저 캐시에&#10;저장됩니다.)">
            <label for="schdul_chk_prvtChk" class="me-2 text-muted cursor-help">개인용 일정</label>
            <input type="checkbox" name="prvtChk" id="schdul_chk_prvtChk" class="form-check-input" value="Y" onclick="Page.chkbxProp(this);">
        </div>
    </div>
</div>
<!--end::Advance form-->

<div class="card post">
    <#--begin::Card header-->
    <div class="card-header min-h-auto mb-7">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_1"
                   onclick="Page.listPage();">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text">목록 VIEW</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_2"
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
<#include "/view/domain/jrnl/day/_jrnl_day_aside_base.ftlh">

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
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_module.js?${releaseDate!}"></script>

<#--include::jrnl_diary_module-->
<#include "/view/domain/jrnl/diary/_modal/_jrnl_diary_reg_modal.ftlh">
<#include "/view/domain/jrnl/diary/_jrnl_diary_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/diary/jrnl_diary_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_module-->
<#include "/view/domain/jrnl/dream/_modal/_jrnl_dream_reg_modal.ftlh">
<#include "/view/domain/jrnl/dream/_jrnl_dream_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/jrnl_dream_module.js?${releaseDate!}"></script>

<#--include::jrnl_day_tag_module-->
<#include "/view/domain/jrnl/diary/tag/_modal/_jrnl_diary_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/diary/tag/jrnl_diary_tag_module.js?${releaseDate!}"></script>

<#--include::jrnl_day_tag_module-->
<#include "/view/domain/jrnl/day/tag/_modal/_jrnl_day_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/day/tag/jrnl_day_tag_module.js?${releaseDate!}"></script>

<#--include::jrnl_dream_tag_module-->
<#include "/view/domain/jrnl/dream/tag/_modal/_jrnl_dream_tag_dtl_modal.ftlh">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_list_template.hbs">
<script type="text/javascript" src="/js/view/domain/jrnl/dream/tag/jrnl_dream_tag_module.js?${releaseDate!}"></script>

<#--include::tags-->
<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">

<#--include::comment-->
<#include "/view/global/_common/_clsf/comment/_modal/_comment_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/global/_common/_clsf/comment/comment_modal_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_cal.js?${releaseDate!}"></script>

</@layout.layout>