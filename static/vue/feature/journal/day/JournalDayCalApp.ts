/**
 * JournalDayCalApp.ts
 * 저널 일자 달력 Vue 마이그레이션 앵커.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import journalDaySearchStateService from "./services/journalDaySearchStateService.js";
import journalDayCrudService from "./services/journalDayCrudService.js";
import journalTodoCrudService from "../todo/services/journalTodoCrudService.js";

type FullCalendarEventInfo = {
    event: {
        id: string;
        groupId: string;
        title: string;
        extendedProps: Record<string, any>;
    };
    el?: HTMLElement;
};

type JournalDayCalState = {
    calendar: FullCalendar.Calendar | null;
    calDt: Date | null;
};

const state: JournalDayCalState = {
    calendar: null,
    calDt: null,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveInitialCalendarDate(): Date {
    const yearElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
    const monthElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
    const selectedYear = Number(yearElement?.value ?? cF.date.getCurrYyStr());
    const selectedMonth = Number(monthElement?.value ?? cF.date.getCurrMnthStr());

    return new Date(selectedYear, selectedMonth - 1, 1);
}

function handleEventClick(info: FullCalendarEventInfo): void {
    const event = info.event;
    const scheduleCd: string = event.groupId;
    const isVcatn: boolean = scheduleCd === `${Code?.SCHEDULE_VCATN!}`;
    const isBrthdy: boolean = scheduleCd === `${Code?.SCHEDULE_BRTHDY!}`;
    if (isVcatn || isBrthdy) return;

    switch (scheduleCd) {
        case "JOURNAL_DAY":
            journalDayCrudService.openDtlModal(event.id);
            break;
        case "JOURNAL_DIARY":
        case "JOURNAL_DREAM":
            journalDayCrudService.openDtlModal(event.extendedProps.journalDayId);
            break;
        default:
            break;
    }
}

function renderEventContent(info: FullCalendarEventInfo): { html: string } | string {
    const event = info.event;
    const scheduleCd: string = event.groupId;
    const icon: string = event.extendedProps.icon;
    const title: string = event.title;
    const titleWithIcon: string = icon + " " + title;

    switch (scheduleCd) {
        case "JOURNAL_DAY":
            return { html: `<div class='cursor-pointer text-truncate'>${titleWithIcon}</div>` };
        case "JOURNAL_DIARY":
        case "JOURNAL_DREAM": {
            const isImprtc: boolean = event.extendedProps.imprtcYn === "Y";
            const classStr: string = isImprtc ? "text-magenta blink fw-bold text-truncate" : "";
            return { html: `<div class='${classStr}'>${titleWithIcon}</div>` };
        }
        default:
            return icon ? { html: `<div class='text-truncate'>${titleWithIcon}</div>` } : title;
    }
}

function mountEventTooltip(info: FullCalendarEventInfo): void {
    const event = info.event;
    const scheduleCd: string = event.groupId;
    let tooltipContent: string | undefined;

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

    const eventElement = info.el;
    if (!eventElement) return;

    $(eventElement).attr("title", tooltipContent);
    setTimeout(function(): void {
        $(eventElement).tooltip({
            trigger: "hover",
            placement: "top",
            html: true,
        });
    }, 0);
}

function normalizeFullCalendarHarness(): void {
    const harnessArr: JQuery<HTMLElement> = $(".fc-daygrid-event-harness");
    harnessArr.each(function(): void {
        const marginTop: string = $(this).css("margin-top");
        if (parseInt(marginTop, 10) < 0) $(this).css("margin-top", "");
    });
}

function refreshEventList(calDt?: Date | null): void {
    if (calDt != null) state.calDt = calDt;
    if (!state.calDt || !state.calendar) return;

    const yy: number = state.calDt.getFullYear();
    const mnth: number = state.calDt.getMonth() + 1;
    const bgnDt: string = cF.date.getDateAddDayStr(state.calDt, -15, "yyyy-MM-dd");
    const endDt: string = cF.date.getDateAddDayStr(state.calDt, 45, "yyyy-MM-dd");
    const url: string = Url.JOURNAL_DAYS + `?viewType=cal&yy=${yy}&mnth=${mnth}`;
    const ajaxData: Record<string, any> = { yy, mnth, searchStartDt: bgnDt, searchEndDt: endDt };

    cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt || !state.calendar) return;

        state.calendar.removeAllEvents();
        res.rsltList?.forEach(function(event: object): void {
            state.calendar?.addEvent(event);
        });
        normalizeFullCalendarHarness();
    });
}

function chkbxProp(obj: HTMLInputElement): void {
    const checkedYn: "Y" | "N" = $(obj).prop("checked") ? "Y" : "N";
    const cookieOptions: Record<string, any> = { expires: cF.date.getCurrDateAddDay(36135) };
    $.cookie($(obj).attr("id"), checkedYn, cookieOptions);
    refreshEventList(state.calDt);
}

function moveMonth(yy: string, mnth: string | number): void {
    const nextDate = new Date(Number(yy), Number(mnth) - 1, 1);
    state.calDt = nextDate;
    state.calendar?.gotoDate(nextDate);
    refreshEventList(nextDate);
}

function syncCalVueBridge(): void {
    window.JournalDayCalVueApp = {
        mounted: true,
        get calendar(): FullCalendar.Calendar | null {
            return state.calendar;
        },
        get calDt(): Date | null {
            return state.calDt;
        },
        refresh: function(): void {
            refreshEventList(state.calDt);
        },
        refreshEventList,
        chkbxProp,
        moveMonth,
        regModal: journalDayCrudService.openRegModal,
        mdfModal: journalDayCrudService.openMdfModal,
        dtlModal: journalDayCrudService.openDtlModal,
        delAjax: journalDayCrudService.delAjax,
        getCalendarDate: function(): Date | null {
            return state.calDt;
        },
    };
}

function init(): void {
    /* 변경(Phase 17): bootstrap_service 제거. 변경(통합): Tag/Meta/delegation → bootstrapDfJournalDayShell. */
    dF.JournalDayRuntimeService.bootstrapDfJournalDayShell();
    journalDaySearchStateService.initFromUrl();
    journalDaySearchStateService.syncSearchFilterDomFromParams();
    void dF.JournalEntry.initAll("CAL");
    /* 변경(T-2-β): dF.JournalTodo.init() → journalTodoCrudService.yyMnthListAjax() 단일 진입.
     * 기존 init 의 initialized 플래그는 페이지 진입 시점 1회 호출 보장으로 자연 소멸. */
    journalTodoCrudService.yyMnthListAjax();
    window.addEventListener("comment:modal-refresh", function(): void { refreshEventList(); });
    window.addEventListener("related-content:refresh", function(): void { refreshEventList(); });

    dF.JournalDayRuntimeService.initJournalDayAsideShell();
    journalDayUiBridgeService.syncTagCloud(true, true, true);
    dF.JournalEntry.bindSearchPopupEnterKeys();

    state.calDt = resolveInitialCalendarDate();
    state.calendar = cF.fullcalendar.init("full_calendar_app", {
        initDt: cF.date.dateToStr(state.calDt, cF.date.ptnDate),
    }, handleEventClick, {
        headerToolbar: {
            left: "",
            center: "title",
            right: "",
        },
        eventContent: renderEventContent,
        eventDidMount: mountEventTooltip,
        eventOverlap: false,
    });
    state.calendar?.render();

    refreshEventList(state.calDt);
    normalizeFullCalendarHarness();
    syncCalVueBridge();
}

const JournalDayCalApp = {
    name: "JournalDayCalApp",
    mounted(): void {
        init();
    },
    render(): null {
        return null;
    },
};

syncCalVueBridge();

runWhenDomReady(function(): void {
    const root = document.getElementById("journal_day_cal_app");
    if (!root) return;

    Vue.createApp(JournalDayCalApp).mount(root);
});
