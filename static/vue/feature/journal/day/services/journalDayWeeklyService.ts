/**
 * journalDayWeeklyService.ts
 * 주간 저널 일자 상태와 이동 보조 함수.
 */

import journalDaySearchStateService from "./journalDaySearchStateService.js";

export type JournalDayWeeklyState = {
    stdrdDt: string;
    weekStartDt: string;
    weekEndDt: string;
    targetDt: string | null;
};

function resolveWeeklyState(stdrdDt: string, targetDt?: string): JournalDayWeeklyState {
    const weekStartDt = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
    const weekEndDt = cF.date.getDateAddDayStr(weekStartDt, 6, cF.date.ptnDate) ?? weekStartDt;

    return {
        stdrdDt,
        weekStartDt,
        weekEndDt,
        targetDt: cF.util.isNotEmpty(targetDt) ? targetDt : null,
    };
}

function syncAsidePeriodState(weeklyState: JournalDayWeeklyState): void {
    if (cF.util.isEmpty(weeklyState.stdrdDt)) return;

    const yy = weeklyState.stdrdDt.substring(0, 4);
    const mnth = String(parseInt(weeklyState.stdrdDt.substring(5, 7), 10));
    /* Vue 검색 상태에 현재 주간 기준 일자를 반영한다. */
    journalDaySearchStateService.patchSearchParams({
        yy,
        mnth,
        stdrdDt: weeklyState.stdrdDt,
        weekStartDt: weeklyState.weekStartDt,
        sort: localStorage.getItem("journal_day_sort") ?? "DESC",
    });

    const yyElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
    const mnthElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
    if (yyElement) yyElement.value = yy;
    if (mnthElement) mnthElement.value = mnth;
}

function normalizeWeekDays(rsltList: Record<string, any>[], sortValue: string = "DESC"): Record<string, any>[] {
    return (rsltList ?? [])
        .map(function(day: Record<string, any>): Record<string, any> {
            const journalChapterList = Array.isArray(day?.journalChapterList) ? day.journalChapterList : [];
            const journalDreamList = Array.isArray(day?.journalDreamList) ? day.journalDreamList : [];
            const journalElseDreamList = Array.isArray(day?.journalElseDreamList) ? day.journalElseDreamList : [];

            return {
                ...day,
                journalDateWeekDay: day?.journalDateWeekDay ?? cF.date.getDayweekStr(day?.stdrdDt, "KO"),
                tag: day?.tag ?? { list: [] },
                journalChapterList,
                journalDreamList,
                journalElseDreamList,
                hasDream: (journalDreamList.length + journalElseDreamList.length) > 0,
            };
        })
        .sort(function(a: Record<string, any>, b: Record<string, any>): number {
            const dateA = new Date(a?.stdrdDt ?? "").getTime();
            const dateB = new Date(b?.stdrdDt ?? "").getTime();
            return sortValue === "ASC" ? dateA - dateB : dateB - dateA;
        });
}

function scrollToTarget(targetDt?: string | null): void {
    if (cF.util.isEmpty(targetDt)) return;

    Vue.nextTick(function(): void {
        const targetElement = document.querySelector(`.journal-day[data-stdrd-dt="${targetDt}"]`) as HTMLElement | null;
        if (targetElement == null) return;

        window.requestAnimationFrame(function(): void {
            targetElement.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    });
}

const journalDayWeeklyService = {
    resolveWeeklyState,
    syncAsidePeriodState,
    normalizeWeekDays,
    scrollToTarget,
};

export default journalDayWeeklyService;
