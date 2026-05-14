/**
 * JournalDayDailyListApp.ts
 * 저널 일자 일간 목록 진입 — Vue 마운트 후 레거시 Page 객체 없이 일간 전용 셸만 초기화한다.
 */

import { mountJournalDayDailyListApp } from "./journalDayListAppMount.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function requestDailyReload(stdrdDt: string): void {
    const dailyBridge = window.JournalDayDailyApp;
    if (typeof dailyBridge?.applySearchParamsAndReload === "function") {
        dailyBridge.applySearchParamsAndReload({ stdrdDt }, "DAILY");
        return;
    }
    const queuedBridge = (dailyBridge ?? {}) as JournalDayListAppBridge;
    queuedBridge.pendingLoad = { type: "daily", stdrdDt };
    window.JournalDayDailyApp = queuedBridge;
}

/**
 * 변경 전: <code>journal_day_daily.ts</code> 의 <code>Page.init</code> 본문.
 * 변경 후: <code>mountJournalDayDailyListApp()</code> 완료 뒤 실행.
 */
function initDailyPageShell(): void {
    void dF.JournalEntry.initAll("DAILY");
    window.addEventListener("comment:modal-refresh", function(): void {
        const stdrdDt: string = String(window.JournalDayDailyApp?.getSearchParams?.()?.stdrdDt
            ?? window.JOURNAL?.stdrdDt
            ?? cF.date.getCurrDateStr(cF.date.ptnDate));
        requestDailyReload(stdrdDt);
    });
    window.addEventListener("related-content:refresh", function(): void {
        const stdrdDt: string = String(window.JournalDayDailyApp?.getSearchParams?.()?.stdrdDt
            ?? window.JOURNAL?.stdrdDt
            ?? cF.date.getCurrDateStr(cF.date.ptnDate));
        requestDailyReload(stdrdDt);
    });
    dF.State.init();

    const stdrdDt: string = window.JOURNAL?.stdrdDt;
    const pattern: string = cF.date.ptnDate.toUpperCase();
    // @ts-ignore
    cF.datepicker.singleDatePicker("#stdrdDt", pattern, stdrdDt, function(date: monent): void {
        const dateStr: string = date.format(pattern);
        history.pushState(null, "", cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt: dateStr }));
        window.JournalDayDailyApp?.patchSearchParams?.({ stdrdDt: dateStr });
        requestDailyReload(dateStr);
    });

    requestDailyReload(stdrdDt);
}

runWhenDomReady(function(): void {
    void (async function(): Promise<void> {
        await mountJournalDayDailyListApp();
        initDailyPageShell();
    })();
});
