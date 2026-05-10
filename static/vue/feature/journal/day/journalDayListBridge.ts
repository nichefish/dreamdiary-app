/**
 * 저널 일자 목록(월·주·일) Vue 브리지 조회 유틸.
 *
 * 페이지당 활성 브리지는 하나뿐이며 레거시 모듈은 이 함수로 조회한다.
 * 변경 후: 단일 전역 JournalDayVueApp 대신 월·주·일별 전역을 둔다.
 *
 * @keepInSync <code>services/journalDayRuntimeShell.ts</code> 의 <code>journalDayResolveListBridge</code> 와 동일 로직(전역 브리지 선택 규칙).
 */

/**
 * 현재 문서에서 마운트된 저널 일자 목록 Vue 브리지를 반환한다.
 * @returns {JournalDayListAppBridge | undefined}
 */
export function getJournalDayListBridge(): JournalDayListAppBridge | undefined {
    const w = window as Window &
        Partial<Pick<Window, "JournalDayMonthlyApp" | "JournalDayWeeklyApp" | "JournalDayDailyApp">>;
    return w.JournalDayMonthlyApp ?? w.JournalDayWeeklyApp ?? w.JournalDayDailyApp;
}
