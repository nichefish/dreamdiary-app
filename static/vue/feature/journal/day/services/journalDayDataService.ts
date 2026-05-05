/**
 * journalDayDataService.ts
 * 저널 일자 Vue 데이터 조회 헬퍼.
 */

export type JournalDayViewType = "list" | "weekly" | "daily";

export type JournalDaySearchParams = Record<string, any> & {
    viewType: JournalDayViewType;
};

function getList(searchParams: JournalDaySearchParams): Promise<Record<string, any>[]> {
    return new Promise(function(resolve, reject): void {
        cF.ajax.get(Url.JOURNAL_DAYS, searchParams, function(res: AjaxResponse): void {
            if (!res.rslt) {
                reject(res);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        }, "block");
    });
}

const journalDayDataService = {
    listMonthly(searchParams: Omit<JournalDaySearchParams, "viewType">): Promise<Record<string, any>[]> {
        return getList({ ...searchParams, viewType: "list" });
    },

    listWeekly(searchParams: Omit<JournalDaySearchParams, "viewType">): Promise<Record<string, any>[]> {
        return getList({ ...searchParams, viewType: "weekly" });
    },

    listDaily(stdrdDt: string): Promise<Record<string, any>[]> {
        return getList({ viewType: "daily", stdrdDt });
    },
};

export default journalDayDataService;
