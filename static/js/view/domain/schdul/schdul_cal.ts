/**
 * schdul_cal.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        eventList: [],
        calendar: null,
        calInitDt: null,
        calDt: null,

        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize form. */
            dF.Schdul.initForm();

            const chkArr: JQuery<HTMLElement> = $("input:checkbox[id^='schdul_chk']");
            chkArr.each(function(_index: number, item: HTMLInputElement): void {
                const id: string = $(item).attr("id");
                if (Page.getChkedCookie(id) === "Y") $(item).prop("checked", true);
            });
            /** TODO : 선택된 년도,달 기준으로 변경해야함 */
            Page.calInitDt = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
            Page.calDt = Page.calInitDt;
            Page.getEventList();
            Page.calendar = cF.fullcalendar.init("full_calendar_app", Page.eventList, function(info): void {
                // 일정 onclick 이벤트
                const event = info.event;
                const schdulCd = event.groupId;
                const isVcatn: boolean = schdulCd === "${Constant.SCHDUL_VCATN!}";
                const isBrthdy: boolean = schdulCd === "${Constant.SCHDUL_BRTHDY!}"
                if (isVcatn || isBrthdy) return;     // 휴가/생일은 팝업 띄우지 않음 - 휴가관리/사용자 정보 쪽에서 관리
                dF.Schdul.dtlModal(event.id);
            });
            Page.calendar.render();
            // 좌우탐색버튼 이벤트핸들러 추가
            $(".fc-today-button").on("click", function(): void {
                const calDt = new Date(Page.calInitDt.getFullYear(), Page.calInitDt.getMonth(), 1);
                Page.refreshEventList(calDt);
            });
            $(".fc-prev-button").on("click", function(): void {
                const calDt = new Date(Page.calDt.getFullYear(), Page.calDt.getMonth() - 1, 1);
                Page.refreshEventList(calDt);
            });
            $(".fc-next-button").on("click", function(): void {
                const calDt = new Date(Page.calDt.getFullYear(), Page.calDt.getMonth() + 1, 1);
                Page.refreshEventList(calDt);
            });

            // datepicker 날짜 검색 init
            cF.datepicker.singleDatePicker("#calDt", "yyyy-MM-DD", cF.date.asStr(Page.calInitDt), function(start): void {
                const selectedDt = new Date(start);
                Page.calendar.gotoDate(selectedDt);
                Page.refreshEventList(selectedDt);
            });

            // https://github.com/fullcalendar/fullcalendar/issues/6393
            const harnessArr: JQuery<HTMLElement> = $(".fc-daygrid-event-harness");
            harnessArr.each(function(): void {
                const marginTop: string = $(this).css("margin-top");
                if (parseInt(marginTop) < 0) $(this).css("margin-top", "");
            });

            console.log("Page scripts initialized.");
        },

        /**
         * 일정 등록 모달 호출
         * @param {Date} calDt
         */
        refreshEventList: function(calDt: Date): void {
            Page.calDt = calDt;
            Page.getEventList();
            Page.calendar.removeAllEvents();
            Page.eventList.forEach(event => Page.calendar.addEvent(event));
        },

        /**
         * 일정 등록 모달 호출
         */
        search: function(): void {
            Page.getEventList();
            Page.calendar.removeAllEvents();
            Page.eventList.forEach(event => Page.calendar.addEvent(event));
        },

        /**
         * 일정 등록 모달 호출
         */
        getEventList: function(): void {
            const url: string = Url.SCHDUL_CAL_LIST_AJAX;
            // 날짜 세팅
            const calDt: Date = Page.calDt;
            const yy: number  = calDt.getFullYear();
            const bgnDt: string = cF.date.getDateAddDayStr(calDt, -35, "yyyy-MM-dd");
            const endDt: string = cF.date.getDateAddDayStr(calDt, 45, "yyyy-MM-dd");
            // 체크박스 값을 읽어와서 필터 요소를 만듬
            const ajaxData: Record<string, any> = { 'yy': yy, 'bgnDt': bgnDt, 'endDt': endDt };
            const chkArr: JQuery<HTMLElement> = $("input:checkbox[id^='schdul_chk_']");
            chkArr.each(function(_index: number, item: HTMLElement): void {
                const itemName = $(item).attr("name").replaceAll("schdul_chk_", "") + "ed";
                ajaxData[itemName] = $(item).prop("checked") ? "Y" : "N";
            });
            // 검색키워드 설정
            ajaxData.searchKeyword = $("#searchKeyword").val();
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Page.eventList = res.rsltList || [];
            });
        },

        /**
         * 팝업 쿠키 존재여부 체크
         */
        getChkedCookie: function(key): string {
            const cookie: string = $.cookie(key);
            if (key === "schdul_chk_myPaprChk") return (cookie === undefined) ? "N" : cookie;
            return (cookie === undefined) ? "Y" : cookie;
        },

        /**
         * 체크박스 쿠키 설정
         */
        chkbxProp: function(obj: HTMLElement): void {
            const checkedYn: 'Y'|'N' = $(obj).prop("checked") ? "Y" : "N";
            const cookieOptions: Record<string, any> = { "expires": cF.date.getCurrDateAddDay(36135) };
            $.cookie($(obj).attr("id"), checkedYn, cookieOptions);
            Page.refreshEventList(Page.calDt);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});