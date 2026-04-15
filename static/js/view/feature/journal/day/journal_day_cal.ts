/**
 * journal_day_cal.ts
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
            dF.JournalDay.init('CAL');
            dF.JournalDiary.init('CAL');
            dF.JournalDream.init('CAL');
            dF.JournalTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.yyMnthListAjax
            });
            dF.State.init();

            dF.JournalDayAside.init();
            // 태그 조회
            dF.JournalDayTag.listAjax();
            dF.JournalDiaryTag.listAjax();
            dF.JournalDreamTag.listAjax();
            // 일기/꿈 키워드 검색창에 엔터키 처리
            cF.util.enterKey("#diarySearchKeyword", dF.JournalDiary.searchPopup);
            cF.util.enterKey("#dreamSearchKeyword", dF.JournalDream.searchPopup);

            // 초기 날짜 설정
            const yearElement: HTMLSelectElement = document.querySelector("#journal_aside #yy");
            const monthElement: HTMLSelectElement = document.querySelector("#journal_aside #mnth");
            const selectedMonth: number = Number(monthElement.value);
            const selectedYear: number = Number(yearElement.value);
            Page.calDt = new Date(selectedYear, selectedMonth - 1, 1);

            // 달력 생성
            Page.calendar = cF.fullcalendar.init("full_calendar_app", { initDt: Page.calDt }, function(info): void {
                const event = info.event;
                const scheduleCd: string = event.groupId;
                const isVcatn: boolean = scheduleCd === `${Code?.SCHEDULE_VCATN!}`;
                const isBrthdy: boolean = scheduleCd === `${Code?.SCHEDULE_BRTHDY!}`;
                if (isVcatn || isBrthdy) return;

                switch (scheduleCd) {
                    case "JOURNAL_DAY":
                        dF.JournalDay.dtlModal(event.id);
                        break;
                    case "JOURNAL_DIARY":
                    case "JOURNAL_DREAM":
                        dF.JournalDay.dtlModal(event.extendedProps.journalDayId);
                        break;
                    default:
                        break;
                }
            }, {
                /* 툴바 설정 */
                headerToolbar: {
                    left: "",
                    center: "title",
                    right: "",
                },
                /**
                 *  이벤트 표시내용
                 */
                eventContent: function(info) {
                    const event = info.event;
                    const scheduleCd: string = event.groupId;
                    const icon: string = event.extendedProps.icon;
                    const title: string = event.title;
                    const titleWithIcon: string = icon + ' ' + title;

                    switch (scheduleCd) {
                        case "JOURNAL_DAY":
                            return { html: `<div class='cursor-pointer text-truncate'>${titleWithIcon}</div>` };
                        case "JOURNAL_DIARY":
                        case "JOURNAL_DREAM":
                            const isImprtc: boolean = event.extendedProps.imprtcYn === "Y";
                            const classStr: string = isImprtc ? "text-magenta blink fw-bold text-truncate" : "";
                            return { html: `<div class='${classStr}'>${titleWithIcon}</div>` };
                        default:
                            return icon ? { html: `<div class='text-truncate'>${titleWithIcon}</div>` } : title;
                    }
                },
                /**
                 * 이벤트 렌더링 후처리
                 */
                eventDidMount: function(info): void {
                    const event = info.event;
                    const scheduleCd: string = event.groupId;
                    let tooltipContent;
                    switch (scheduleCd) {
                        case "JOURNAL_DAY":
                            tooltipContent = event.title;
                            break;
                        case "JOURNAL_DIARY":
                            tooltipContent = `<div class="journal-diary-content">${event.extendedProps.markdownContent}</div>`;
                            break;
                        case "JOURNAL_DREAM":
                            tooltipContent = `<div class="journal-dream-content">${event.extendedProps.markdownContent}</div>`;
                            break;
                        default:
                            break;
                    }
                    const eventElmt = info.el;
                    $(eventElmt).attr('title', tooltipContent);
                    setTimeout(function(): void {
                        $(eventElmt).tooltip({
                            trigger: 'hover',
                            placement: 'top',
                            html: true,
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
            if (calDt != null) Page.calDt = calDt;
            const yy: number = Page.calDt.getFullYear();
            const mnth: number = Page.calDt.getMonth() + 1;
            const bgnDt: string = cF.date.getDateAddDayStr(Page.calDt, -15, "yyyy-MM-dd");
            const endDt: string = cF.date.getDateAddDayStr(Page.calDt, 45, "yyyy-MM-dd");
            const url: string = Url.JOURNAL_DAYS + `?viewType=cal&yy=${yy}&mnth=${mnth}`;
            const ajaxData: Record<string, any> = { 'yy': yy, mnth: mnth, 'searchStartDt': bgnDt, 'searchEndDt': endDt };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) {
                    Page.calendar.removeAllEvents();
                    res.rsltList?.forEach(event => Page.calendar.addEvent(event));
                }
            });
        },

        /**
         * 팝업 쿠키 존재여부 체크
         *
         * @param {string} key
         */
        getChkedCookie: function(key: string): string {
            const cookie = $.cookie(key);
            if (key === "schedule_chk_myPaprChk") return (cookie == null) ? "N" : cookie;
            return (cookie == null) ? "Y" : cookie;
        },

        /**
         * 체크박스 쿠키 설정
         *
         * @param {HTMLInputElement} obj
         */
        chkbxProp: function(obj: HTMLInputElement): void {
            const checkedYn: "Y"|"N" = $(obj).prop("checked") ? "Y" : "N";
            const cookieOptions: Record<string, any> = { "expires": cF.date.getCurrDateAddDay(36135) };
            $.cookie($(obj).attr("id"), checkedYn, cookieOptions);
            Page.refreshEventList(Page.calDt);
        },

        /**
         * VIEW 변경
         *
         * @param {string} url
         */
        changeView: function(url: string): void {
            cF.ui.blockUIReplace(dF.JournalDay.buildViewUrl(url));
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});

