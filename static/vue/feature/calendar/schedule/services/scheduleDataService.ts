/**
 * scheduleDataService.ts
 * 일정 데이터 조회 서비스
 *
 * 변경(D): schedule_cal.ts의 getEventList / dtlModal AJAX 로직을 서비스로 분리.
 *
 * @author nichefish
 */
import type { ScheduleCalEvent, ScheduleCalFilter, ScheduleDetail } from "../types.js";

declare const cF: any;
declare const Url: any;

export default {
    /**
     * 달력 이벤트 목록 조회 (AJAX)
     * 기존 Page.getEventList() 대응.
     * @param {Date} calDt - 기준 달력 날짜.
     * @param {ScheduleCalFilter} filter - 체크박스 필터 상태.
     * @param {string} searchKeyword - 검색어.
     */
    getEventList(calDt: Date, filter: ScheduleCalFilter, searchKeyword: string): Promise<ScheduleCalEvent[]> {
        return new Promise((resolve): void => {
            const yy: number   = calDt.getFullYear();
            const bgnDt: string = (cF as any).date.getDateAddDayStr(calDt, -35, "yyyy-MM-dd");
            const endDt: string = (cF as any).date.getDateAddDayStr(calDt,  45, "yyyy-MM-dd");
            // 체크박스 이름 → 서버 파라미터명 규칙: name + "ed" (예: myPaprChk → myPaprChked)
            const ajaxData: Record<string, unknown> = {
                yy, bgnDt, endDt,
                myPaprChked:  filter.myPaprChk  ? "Y" : "N",
                vcatnChked:   filter.vcatnChk   ? "Y" : "N",
                indtChked:    filter.indtChk     ? "Y" : "N",
                outdtChked:   filter.outdtChk    ? "Y" : "N",
                tlcmmtChked:  filter.tlcmmtChk  ? "Y" : "N",
                prvtChked:    filter.prvtChk     ? "Y" : "N",
                searchKeyword,
            };
            (cF as any).ajax.get((Url as any).SCHEDULE_CAL_LIST_AJAX, ajaxData, (res: any): void => {
                resolve(res.rslt ? (res.rsltList as ScheduleCalEvent[] || []) : []);
            });
        });
    },

    /**
     * 일정 상세 조회 (AJAX)
     * 기존 dF.Schedule.dtlModal / mdfModal 내 Ajax 로직 대응.
     * @param {string|number} id - 일정 번호.
     */
    getDetail(id: string | number): Promise<ScheduleDetail> {
        return new Promise((resolve, reject): void => {
            (cF as any).ajax.get((Url as any).SCHEDULE_DTL_AJAX, { id }, (res: any): void => {
                if (res.rslt) resolve(res.rsltObj as ScheduleDetail);
                else reject(res.message as string);
            });
        });
    },
};
