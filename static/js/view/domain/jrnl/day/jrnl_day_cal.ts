/**
 * jrnl_day_cal.ts
 * 저널 달력 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        calendar: null,
        calDt: null,

        init: function(): void {
            /* initialize modules. */
            dF.JrnlDay.init();
            dF.JrnlDiary.init();
            dF.JrnlDream.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });

            dF.JrnlDayAside.setYyMnthCookie();
            dF.JrnlDayAside.init();
            // 태그 조회
            dF.JrnlDayTag.listAjax();
            dF.JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            // 일기/꿈 키워드 검색에 엔터키 처리
            cF.util.enterKey("#diaryKeyword", dF.JrnlDiary.keywordListAjax);
            cF.util.enterKey("#dreamKeyword", dF.JrnlDream.keywordListAjax);

            // 초기 일자 설정
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const selectedMonth: number = Number(monthElement.value);
            const selectedYear: number = Number(yearElement.value);
            Page.calDt = new Date(selectedYear, selectedMonth - 1, 1);
            
            // 달력 생성
            Page.calendar = cF.fullcalendar.init("full_calendar_app", { initDt: Page.calDt }, function(info): void {
                // 일정 onclick 이벤트
                const event = info.event;
                const schdulCd: string = event.groupId;
                const isVcatn: boolean = schdulCd === `${Constant?.SCHDUL_VCATN!}`;
                const isBrthdy: boolean = schdulCd === `${Constant?.SCHDUL_BRTHDY!}`;
                if (isVcatn || isBrthdy) return;     // 휴가/생일은 팝업 띄우지 않음 - 휴가관리/사용자 정보 쪽에서 관리
                switch (schdulCd) {
                    case "JRNL_DAY":
                        dF.JrnlDay.dtlModal(event.id);
                        break;
                    case "JRNL_DIARY":
                    case "JRNL_DREAM":
                        dF.JrnlDay.dtlModal(event.extendedProps.jrnlDayNo);
                        break;
                    default:
                        // TODO:
                        break;
                }
            }, {
                headerToolbar: {
                    left: "",
                    center: "title",
                    right: "",
                },
                eventContent: function(arg) {
                    // 아이콘 추가
                    const icon: string = arg.event.extendedProps.icon;  // 아이콘 클래스 (예: FontAwesome, Bootstrap Icons 등)
                    const title: string = arg.event.title;
                    const titleWithIcon: string = icon + ' ' + title;  // 아이콘과 타이틀 결합
                    return icon ? { html: titleWithIcon } : title;  // html을 반환하여 FullCalendar가 렌더링하도록 함
                },
                eventDidMount: function(info) {
                    const eventElmt = info.el;
                    $(eventElmt).attr('title', info.event.title);
                    setTimeout(function(): void {
                        $(eventElmt).tooltip({
                            trigger: 'hover', // 마우스 오버 시 툴팁 활성화
                            placement: 'top'  // 툴팁의 위치 설정
                        });
                    }, 0);
                },
                eventOverlap: false
            });
            Page.calendar.render();

            // 달력 데이터 load
            Page.refreshEventList(Page.calDt);

            // https://github.com/fullcalendar/fullcalendar/issues/6393
            const harnessArr: JQuery<HTMLElement> = $(".fc-daygrid-event-harness");
            harnessArr.each(function(): void {
                const marginTop: string = $(this).css("margin-top");
                if (parseInt(marginTop) < 0) $(this).css("margin-top", "");
            });
        },

        /** 일정 목록 조회 호출 */
        refreshEventList: function(calDt: Date): void {
            const url: string = Url.JRNL_DAY_CAL_LIST_AJAX;
            // 날짜 세팅
            if (calDt !== undefined) Page.calDt = calDt;
            console.log("calDt: ", Page.calDt);
            const yy: number  = Page.calDt.getFullYear();
            const mnth: number  = Page.calDt.getMonth() + 1;
            const bgnDt: string = cF.date.getDateAddDayStr(Page.calDt, -15, "yyyy-MM-dd");
            const endDt: string = cF.date.getDateAddDayStr(Page.calDt, 45, "yyyy-MM-dd");
            // 체크박스 값을 읽어와서 필터 요소를 만듬
            const ajaxData: Record<string, any> = { 'yy': yy, mnth: mnth, 'searchStartDt': bgnDt, 'searchEndDt': endDt };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) {
                    Page.calendar.removeAllEvents();
                    res.rsltList?.forEach(event => Page.calendar.addEvent(event));
                }
            });
        },

        /** 팝업 쿠키 존재여부 체크 */
        getChkedCookie: function(key: string): string {
            const cookie = $.cookie(key);
            if (key === "schdul_chk_myPaprChk") return (cookie === undefined) ? "N" : cookie;
            return (cookie === undefined) ? "Y" : cookie;
        },

        /** 체크박스 쿠키 설정 */
        chkbxProp: function(obj: HTMLInputElement): void {
            const checkedYn: "Y"|"N" = $(obj).prop("checked") ? "Y" : "N";
            const cookieOptions: Record<string, any> = { "expires": cF.date.getCurrDateAddDay(36135) };
            $.cookie($(obj).attr("id"), checkedYn, cookieOptions);
            Page.refreshEventList(Page.calDt);
        },

        /**
         * 목록 화면으로 이동
         */
        listPage: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_DAY_PAGE);
        },

        /**
         * 달력 화면으로 이동
         */
        calPage: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_DAY_CAL);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});