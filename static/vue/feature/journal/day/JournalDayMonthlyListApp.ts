/**
 * JournalDayMonthlyListApp.ts
 * 저널 일자 월간 목록 진입 — <code>mountJournalDayMonthlyListApp()</code> 만 호출한다.
 */

import { mountJournalDayMonthlyListApp } from "./journalDayListAppMount.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    void mountJournalDayMonthlyListApp();
});
