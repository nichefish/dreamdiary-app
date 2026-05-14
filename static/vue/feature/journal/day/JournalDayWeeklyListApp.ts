/**
 * JournalDayWeeklyListApp.ts
 * 저널 일자 주간 목록 진입 — Vue 마운트 후 레거시 Page 객체 없이 주간 전용 셸만 초기화한다.
 */

import { mountJournalDayWeeklyListApp } from "./journalDayListAppMount.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function tryLoadWeeklyByVue(stdrdDt: string, targetDt?: string): boolean {
    const bridge = window.JournalDayWeeklyApp;
    if (typeof bridge?.applySearchParamsAndReload !== "function") return false;
    const patch: Record<string, any> = { stdrdDt };
    if (cF.util.isNotEmpty(targetDt)) patch.target = targetDt;
    bridge.applySearchParamsAndReload(patch, "WEEKLY");
    return true;
}

function requestWeeklyReload(stdrdDt: string, targetDt?: string): void {
    if (tryLoadWeeklyByVue(stdrdDt, targetDt)) return;
    console.error("[JournalDayWeeklyListApp] JournalDayWeeklyApp.applySearchParamsAndReload bridge is not available.");
}

/**
 * 변경 전: <code>journal_day_weekly.ts</code> 의 <code>Page.init</code> 본문.
 * 변경 후: <code>mountJournalDayWeeklyListApp()</code> 완료 뒤 실행(브리지 확보).
 */
function initWeeklyPageShell(): void {
    void dF.JournalEntry.initAll("WEEKLY");
    window.addEventListener("comment:modal-refresh", function(): void {
        const stdrdDt: string = String(window.JournalDayWeeklyApp?.getSearchParams?.()?.stdrdDt
            ?? window.JOURNAL?.stdrdDt
            ?? cF.date.getCurrDateStr(cF.date.ptnDate));
        requestWeeklyReload(stdrdDt);
    });
    dF.State.init();

    const stdrdDt: string = window.JOURNAL?.stdrdDt ?? cF.date.getCurrDateStr(cF.date.ptnDate);
    const targetDt: string = cF.util.getUrlParam("target") ?? "";
    syncAsidePeriodYyMnth(stdrdDt);
    dF.JournalEntry.bindSearchPopupEnterKeys();

    requestWeeklyReload(stdrdDt, cF.util.isNotEmpty(targetDt) ? targetDt : undefined);
}

/**
 * 어사이드 연/월 select 를 기준일에 맞춘다. 변경 전: <code>Page.syncAsidePeriodState</code>.
 * @param {string} stdrdDt
 */
function syncAsidePeriodYyMnth(stdrdDt: string): void {
    if (cF.util.isEmpty(stdrdDt)) return;
    const yyElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
    const mnthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
    if (yyElement) yyElement.value = stdrdDt.substring(0, 4);
    if (mnthElement) mnthElement.value = String(parseInt(stdrdDt.substring(5, 7), 10));
}

runWhenDomReady(function(): void {
    void (async function(): Promise<void> {
        await mountJournalDayWeeklyListApp();
        initWeeklyPageShell();
    })();
});
