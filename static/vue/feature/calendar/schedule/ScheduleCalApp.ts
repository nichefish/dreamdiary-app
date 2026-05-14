/**
 * ScheduleCalApp.ts
 * 일정 달력 페이지 진입 모듈
 *
 * 변경(D): schedule_cal.ts(jQuery 기반 Page 객체)를 TypeScript 모듈로 전환.
 *          - FullCalendar 초기화 / 이벤트 목록 갱신 / 체크박스 필터 / datepicker 로직 흡수.
 *          - 모달 트리거는 window CustomEvent('schedule:open-dtl-modal' 등)로 위임.
 *          - window.Page 를 유지해 서버 렌더 HTML(header, checkbox onclick 등) 호환성 보존.
 *
 * @author nichefish
 */
import scheduleDataService from "./services/scheduleDataService.js";
import type { ScheduleCalFilter, ScheduleCalEvent } from "./types.js";

declare const cF: any;
declare const Url: any;

// --- 체크박스 ID → filter key 매핑 ---
const FILTER_KEYS: (keyof ScheduleCalFilter)[] = [
    "myPaprChk", "vcatnChk", "indtChk", "outdtChk", "tlcmmtChk", "prvtChk",
];

// --- 쿠키 헬퍼 (기존 Page.getChkedCookie / Page.chkbxProp 대응) ---
function getCookieYn(key: string, defaultChecked: boolean): boolean {
    const val: string | undefined = (window as any)?.jQuery?.cookie?.(key)
        ?? (window as any)?.$?.cookie?.(key);
    if (val == null) return defaultChecked;
    return val === "Y";
}

function setCookieYn(key: string, checked: boolean): void {
    const expires = (cF as any).date?.getCurrDateAddDay(36135);
    ((window as any)?.jQuery ?? (window as any)?.$)?.cookie?.(key, checked ? "Y" : "N", { expires });
}

// --- 모듈 레벨 상태 ---
const calInitDt: Date = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
let calDt: Date = calInitDt;
let calendar: any = null;

/** 휴가/생일 코드 (schedule_cal.ftlh JSON 주입에서 읽음) */
let vcatnCd: string = "";
let brthdyCd: string = "";

/** 체크박스 필터 상태 (쿠키 기반 초기화) */
const filter: ScheduleCalFilter = {
    myPaprChk: getCookieYn("schedule_chk_myPaprChk", false),   // default: 미체크
    vcatnChk:  getCookieYn("schedule_chk_vcatnChk",  true),
    indtChk:   getCookieYn("schedule_chk_indtChk",   true),
    outdtChk:  getCookieYn("schedule_chk_outdtChk",  true),
    tlcmmtChk: getCookieYn("schedule_chk_tlcmmtChk", true),
    prvtChk:   getCookieYn("schedule_chk_prvtChk",   true),
};

// --- 이벤트 목록 갱신 ---

/** AJAX로 이벤트 목록을 가져와 FullCalendar에 반영 */
async function fetchEventList(): Promise<void> {
    const keyword: string =
        (document.getElementById("searchKeyword") as HTMLInputElement | null)?.value || "";
    const events: ScheduleCalEvent[] =
        await scheduleDataService.getEventList(calDt, filter, keyword);
    if (!calendar) return;
    calendar.removeAllEvents();
    events.forEach((ev: ScheduleCalEvent) => calendar.addEvent(ev));
}

/**
 * 달력 날짜 변경 후 이벤트 목록 갱신
 * @param {Date} newCalDt - 새 기준 달력 날짜.
 */
function refreshEventList(newCalDt: Date): void {
    calDt = newCalDt;
    fetchEventList();
}

// --- FullCalendar 초기화 ---
function initCalendar(): void {
    // 체크박스 DOM 상태를 filter 와 동기화
    FILTER_KEYS.forEach((key: keyof ScheduleCalFilter): void => {
        const el = document.getElementById("schedule_chk_" + key) as HTMLInputElement | null;
        if (el) el.checked = filter[key] as boolean;
    });

    calendar = (cF as any).fullcalendar.init(
        "full_calendar_app",
        [],
        function(info: any): void {
            const event = info.event;
            const scheduleCd: string = event.groupId || "";
            // 휴가·생일 코드는 클릭 시 상세 모달 진입 생략 (기존 동작 유지)
            if (scheduleCd === vcatnCd || scheduleCd === brthdyCd) return;
            window.dispatchEvent(
                new CustomEvent("schedule:open-dtl-modal", { detail: { id: event.id } })
            );
        }
    );
    calendar.render();

    // 달력 네비게이션 버튼 이벤트 바인딩 (render 후에 DOM 생성됨)
    document.querySelector(".fc-today-button")?.addEventListener("click", (): void => {
        refreshEventList(new Date(calInitDt.getFullYear(), calInitDt.getMonth(), 1));
    });
    document.querySelector(".fc-prev-button")?.addEventListener("click", (): void => {
        refreshEventList(new Date(calDt.getFullYear(), calDt.getMonth() - 1, 1));
    });
    document.querySelector(".fc-next-button")?.addEventListener("click", (): void => {
        refreshEventList(new Date(calDt.getFullYear(), calDt.getMonth() + 1, 1));
    });

    // datepicker 초기화 (날짜 선택 시 달력 이동 + 이벤트 갱신)
    (cF as any).datepicker.singleDatePicker(
        "#calDt",
        "yyyy-MM-DD",
        (cF as any).date.asStr(calInitDt),
        function(start: any): void {
            const selectedDt: Date = new Date(start);
            calendar.gotoDate(selectedDt);
            refreshEventList(selectedDt);
        }
    );

    // FullCalendar 음수 margin-top 이슈 보정 (github.com/fullcalendar/fullcalendar/issues/6393)
    document.querySelectorAll(".fc-daygrid-event-harness").forEach((el: Element): void => {
        const htmlEl = el as HTMLElement;
        if (parseInt(htmlEl.style.marginTop || "0") < 0) {
            htmlEl.style.marginTop = "";
        }
    });

    fetchEventList();
}

// --- 전역 Page 객체 등록 (서버 렌더 HTML 호환) ---
function registerPageGlobal(): void {
    (window as any).Page = {
        /** 헤더 검색 버튼 onclick="Page.search();" */
        search(): void {
            fetchEventList();
        },
        /** 체크박스 onclick="Page.chkbxProp(this);" */
        chkbxProp(obj: HTMLInputElement): void {
            const key: string = obj.id;                                   // "schedule_chk_myPaprChk" 등
            const filterKey = key.replace("schedule_chk_", "") as keyof ScheduleCalFilter;
            filter[filterKey] = obj.checked as any;
            setCookieYn(key, obj.checked);
            refreshEventList(calDt);
        },
    };
}

// --- window 이벤트 리스너 ---
function bindWindowEvents(): void {
    /** 등록/수정/삭제 성공 후 달력 갱신 */
    window.addEventListener("schedule:calendar-refresh", (): void => {
        refreshEventList(calDt);
    });

}

// --- 진입 ---
function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    // 주입된 코드값 읽기
    const codeEl = document.getElementById("schedule_cal_code_data");
    if (codeEl) {
        try {
            const codeData: { vcatnCd?: string; brthdyCd?: string } =
                JSON.parse(codeEl.textContent || "{}");
            vcatnCd  = codeData.vcatnCd  || "";
            brthdyCd = codeData.brthdyCd || "";
        } catch { /* ignore parse error */ }
    }

    registerPageGlobal();
    bindWindowEvents();
    initCalendar();
});
