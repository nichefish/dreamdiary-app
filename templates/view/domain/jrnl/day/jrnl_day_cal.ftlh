<#--
 * 일정 > 전체 일정 화면 (달력)
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/schdul/_schdul_cal_header.ftlh">
</#assign>

<#assign isReg = (isReg!false)>
<#assign isMdf = (isMdf!false)>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>
<#include "/view/domain/schdul/_schdul_scripts.ftlh">

<link rel="stylesheet" type="text/css" href="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.css" />

<style>
    #full_calendar_app table {
        table-layout: fixed;
    }
    @media (max-width: 768px) {
        .card-body {
            padding-left: 0 !important;
            padding-right: 0 !important;
        }
    }
</style>
<script>
    const Page = (function() {
        return {
            eventList: [],
            calendar: null,
            calInitDt: null,
            calDt: null,

            /**
             * Page 객체 초기화
             */
            init: function() {
                /* initialize form. */
                Schdul.initForm();

                const chkArr = $("input:checkbox[id^='schdul_chk']");
                chkArr.each(function(_index, item) {
                    let id = $(item).attr("id");
                    if (Page.getChkedCookie(id) === "Y") $(item).prop("checked", true);
                });
                /** TODO : 선택된 년도,달 기준으로 변경해야함 */
                Page.calInitDt = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
                Page.calDt = Page.calInitDt;
                Page.getEventList();
                Page.calendar = cF.fullcalendar.init("full_calendar_app", Page.eventList, function(info) {
                    // 일정 onclick 이벤트
                    let event = info.event;
                    let schdulCd = event.groupId;
                    let isVcatn = schdulCd === "${Constant.SCHDUL_VCATN!}";
                    let isBrthdy = schdulCd === "${Constant.SCHDUL_BRTHDY!}"
                    if (isVcatn || isBrthdy) return false;     // 휴가/생일은 팝업 띄우지 않음 - 휴가관리/사용자 정보 쪽에서 관리
                    Schdul.dtlModal(event.id);
                });
                Page.calendar.render();
                // 좌우탐색버튼 이벤트핸들러 추가
                $(".fc-today-button").on("click", function() {
                    let calDt = new Date(Page.calInitDt.getFullYear(), Page.calInitDt.getMonth(), 1);
                    Page.refreshEventList(calDt);
                });
                $(".fc-prev-button").on("click", function() {
                    let calDt = new Date(Page.calDt.getFullYear(), Page.calDt.getMonth() - 1, 1);
                    Page.refreshEventList(calDt);
                });
                $(".fc-next-button").on("click", function() {
                    let calDt = new Date(Page.calDt.getFullYear(), Page.calDt.getMonth() + 1, 1);
                    Page.refreshEventList(calDt);
                });

                // datepicker 날짜 검색 init
                cF.datepicker.singleDatePicker("#calDt", "yyyy-MM-DD", cF.date.asStr(Page.calInitDt), function(start) {
                    let selectedDt = new Date(start);
                    Page.calendar.gotoDate(selectedDt);
                    Page.refreshEventList(selectedDt);
                });

                // https://github.com/fullcalendar/fullcalendar/issues/6393
                const harnessArr = $(".fc-daygrid-event-harness");
                harnessArr.each(function() {
                    const marginTop = $(this).css("margin-top");
                    if (parseInt(marginTop) < 0) $(this).css("margin-top", "");
                });
            },
            /** 일정 등록 모달 호출 */
            refreshEventList: function(calDt) {
                Page.calDt = calDt;
                Page.getEventList();
                Page.calendar.removeAllEvents();
                Page.eventList.forEach(event => Page.calendar.addEvent(event));
            },
            /** 일정 등록 모달 호출 */
            search: function() {
                Page.getEventList();
                Page.calendar.removeAllEvents();
                Page.eventList.forEach(event => Page.calendar.addEvent(event));
            },
            /** 일정 등록 모달 호출 */
            getEventList: function() {
                const url = '${Url.SCHDUL_CAL_LIST_AJAX!}';
                // 날짜 세팅
                const calDt = Page.calDt;
                const yy  = calDt.getFullYear();
                const bgnDt = cF.date.getDateAddDayStr(calDt, -35, "yyyy-MM-dd");
                const endDt = cF.date.getDateAddDayStr(calDt, 45, "yyyy-MM-dd");
                // 체크박스 값을 읽어와서 필터 요소를 만듬
                const ajaxData = { 'yy': yy, 'bgnDt': bgnDt, 'endDt': endDt };
                const chkArr = $("input:checkbox[id^='schdul_chk_']");
                chkArr.each(function(_index, item) {
                    const itemName = $(item).attr("name").replaceAll("schdul_chk_", "") + "ed";
                    ajaxData[itemName] = $(item).prop("checked") ? "Y" : "N";
                });
                // 검색키워드 설정
                ajaxData.searchKeyword = $("#searchKeyword").val();
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (res.rslt) Page.eventList = res.rsltList || [];
                });
            },
            /** 팝업 쿠키 존재여부 체크 */
            getChkedCookie: function(key) {
                let cookie = $.cookie(key);
                if (key === "schdul_chk_myPaprChk") return (cookie === undefined) ? "N" : cookie;
                return (cookie === undefined) ? "Y" : cookie;
            },
            /** 체크박스 쿠키 설정 */
            chkbxProp: function(obj) {
                const checkedYn = $(obj).prop("checked") ? "Y" : "N";
                let cookieOptions = { "expires": cF.date.getCurrDateAddDay(36135) };
                $.cookie($(obj).attr("id"), checkedYn, cookieOptions);
                Page.refreshEventList(Page.calDt);
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

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
    <div class="card-header min-h-auto mb-5">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_1">
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

<#--include:: Aside-->
<#include "/view/domain/jrnl/day/_jrnl_day_aside_base.ftlh">

<#include "/view/domain/schdul/_schdul_reg_modal.ftlh">
<#include "/view/domain/schdul/_schdul_dtl_modal.ftlh">

</@layout.layout>