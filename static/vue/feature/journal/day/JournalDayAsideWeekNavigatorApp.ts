/**
 * JournalDayAsideWeekNavigatorApp.ts
 * 저널 사이드바 주간 일자 네비게이터 Vue 브리지.
 */

type WeekDayItem = {
    label: string;
    dateStr: string;
    hasDay: boolean;
    isActive: boolean;
};

type WeekNavigatorPayload = {
    stdrdDt: string;
    days: WeekDayItem[];
};

type WeekNavigatorBridge = {
    mounted?: boolean;
    setWeekDays?: (payload: WeekNavigatorPayload) => void;
    syncWeekRangeLabel?: (stdrdDt?: string) => void;
    syncWeekNavigator?: (stdrdDt?: string, weeklyList?: Record<string, any>[]) => void;
    loadWeekNavigator?: (stdrdDt: string) => void;
    pendingPayload?: WeekNavigatorPayload | null;
    pendingSyncRequest?: { stdrdDt?: string; weeklyList?: Record<string, any>[] } | null;
};
/** @keepInSync static/js/view/feature/journal/day/journalDayListBridge.ts */
const journalDayResolveListBridge = (): JournalDayListAppBridge | undefined =>
    window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;

const state = Vue.reactive({
    stdrdDt: "",
    days: [] as WeekDayItem[],
    weekRangeLabel: "----",
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function applyPayload(payload: WeekNavigatorPayload): void {
    state.stdrdDt = String(payload?.stdrdDt ?? "");
    state.days = Array.isArray(payload?.days) ? payload.days : [];
}

function resolveAnchorDateByBridge(): string {
    const bridge = journalDayResolveListBridge();
    return String(bridge?.getCurrentAnchorDate?.() ?? "");
}

function syncWeekRangeLabel(stdrdDt?: string): void {
    const targetDate: string = String(stdrdDt ?? resolveAnchorDateByBridge() ?? "");
    if (cF.util.isEmpty(targetDate)) {
        state.weekRangeLabel = "----";
        return;
    }
    const weekStartDt: string = cF.date.getWeekdayDateStr(targetDate, 1, cF.date.ptnDate) ?? targetDate;
    const weekEndDt: string = cF.date.getDateAddDayStr(weekStartDt, 6, cF.date.ptnDate) ?? weekStartDt;
    state.weekRangeLabel = `${weekStartDt.substring(5)} ~ ${weekEndDt.substring(5)}`;
}

function renderWeekNavigator(stdrdDt: string, weeklyList: Record<string, any>[] | null): void {
    if (cF.util.isEmpty(stdrdDt)) {
        applyPayload({ stdrdDt: "", days: [] });
        return;
    }
    const weekStartDt: string = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
    const weekDayLabels: string[] = ["월", "화", "수", "목", "금", "토", "일"];
    const dayMap: Map<string, Record<string, any>> = new Map<string, Record<string, any>>();
    const loaded: boolean = Array.isArray(weeklyList);
    if (loaded) {
        weeklyList.forEach(function(day: Record<string, any>): void {
            const dateKey: string = String(day?.stdrdDt ?? "");
            if (cF.util.isNotEmpty(dateKey)) dayMap.set(dateKey, day);
        });
    }
    const dayItems: WeekDayItem[] = [];
    weekDayLabels.forEach(function(label: string, idx: number): void {
        const dateStr: string = cF.date.getDateAddDayStr(weekStartDt, idx, cF.date.ptnDate) ?? weekStartDt;
        const hasDay: boolean = loaded && dayMap.has(dateStr);
        const isActive: boolean = dateStr === stdrdDt;
        dayItems.push({ label, dateStr, hasDay, isActive });
    });
    applyPayload({ stdrdDt, days: dayItems });
}

let weekNavRequestKey: string | null = null;
function loadWeekNavigator(stdrdDt: string): void {
    if (cF.util.isEmpty(stdrdDt)) return;
    const weekStartDt: string = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
    const requestKey: string = `${weekStartDt}:${stdrdDt}`;
    weekNavRequestKey = requestKey;
    renderWeekNavigator(stdrdDt, null);
    cF.ajax.get(Url.JOURNAL_DAYS, {
        viewType: "weekly",
        weekStartDt,
        stdrdDt,
    }, function(res: AjaxResponse): void {
        if (weekNavRequestKey !== requestKey) return;
        if (!res.rslt) {
            renderWeekNavigator(stdrdDt, []);
            return;
        }
        renderWeekNavigator(stdrdDt, Array.isArray(res.rsltList) ? res.rsltList : []);
    });
}

function syncWeekNavigator(stdrdDt?: string, weeklyList?: Record<string, any>[]): void {
    const targetDate: string = String(stdrdDt ?? resolveAnchorDateByBridge() ?? "");
    syncWeekRangeLabel(targetDate);
    if (cF.util.isEmpty(targetDate)) {
        renderWeekNavigator("", []);
        return;
    }
    if (Array.isArray(weeklyList)) {
        renderWeekNavigator(targetDate, weeklyList);
        return;
    }
    loadWeekNavigator(targetDate);
}

const JournalDayAsideWeekNavigatorRootApp = {
    name: "JournalDayAsideWeekNavigatorRootApp",
    data(): { state: typeof state } {
        return { state };
    },
    methods: {
        goWeekDay(day: WeekDayItem): void {
            if (!day.hasDay) return;
            const bridge = journalDayResolveListBridge();
            if (bridge?.mounted !== true) {
                console.error("[JournalDayAsideWeekNavigatorApp] goWeekDay: JournalDay*App 브리지 미등록.");
                return;
            }
            if (String(bridge.getSearchParams?.()?.viewType ?? "") === "weekly") {
                bridge.runSetAnchorDateForCurrentView?.(day.dateStr, true);
                return;
            }
            bridge.navigateToWeekDay?.(day.dateStr);
        },
    },
    template: `
    <teleport to="#journal_aside #journalAsideWeekDays">
        <button
            v-for="day in state.days"
            :key="day.dateStr"
            type="button"
            :class="'journal-aside-week-day' + (day.isActive ? ' is-active' : '')"
            :disabled="!day.hasDay"
            @click="goWeekDay(day)"
        >
            <span class="journal-aside-week-day__label">{{ day.label }}</span>
            <span class="journal-aside-week-day__date">{{ day.dateStr.substring(8, 10) }}</span>
        </button>
    </teleport>
    <teleport to="#journal_aside #journalAsideWeekRange">{{ state.weekRangeLabel }}</teleport>
    `,
};

runWhenDomReady(function(): void {
    const rootEl = document.querySelector("#journal_day_aside_week_nav_app");
    if (!rootEl) {
        console.error("[JournalDayAsideWeekNavigatorApp] Vue mount root not found.");
        return;
    }

    const queuedBridge = window.JournalDayAsideWeekNavigatorVueApp as WeekNavigatorBridge | undefined;
    const app = Vue.createApp(JournalDayAsideWeekNavigatorRootApp);
    app.mount("#journal_day_aside_week_nav_app");

    window.JournalDayAsideWeekNavigatorVueApp = {
        mounted: true,
        setWeekDays: function(payload: WeekNavigatorPayload): void {
            applyPayload(payload);
        },
        syncWeekRangeLabel,
        syncWeekNavigator,
        loadWeekNavigator,
        pendingSyncRequest: null,
    };

    if (queuedBridge?.pendingPayload) window.JournalDayAsideWeekNavigatorVueApp.setWeekDays?.(queuedBridge.pendingPayload);
    if (queuedBridge?.pendingSyncRequest) {
        window.JournalDayAsideWeekNavigatorVueApp.syncWeekNavigator?.(
            queuedBridge.pendingSyncRequest.stdrdDt,
            queuedBridge.pendingSyncRequest.weeklyList
        );
    }
});

export {};
