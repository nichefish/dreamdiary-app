/**
 * journalDayTagDataShell.ts
 * 저널 일자 태그 데이터 조회 서비스 — 전역 <code>dF.JournalDayTagDataService</code>.
 *
 * 변경(P7): <code>static/js/view/feature/journal/day/tag/journal_day_tag_data_service.ts</code> 본문을 Vue 축으로 이전.
 * 로드: <code>registerJournalDayShellServices</code> side-effect import.
 */
const __journalDayGlobal: any = typeof globalThis !== "undefined" ? globalThis : (window as any);
if (__journalDayGlobal.dF == null) {
    __journalDayGlobal.dF = {};
}
const dfNs: any = __journalDayGlobal.dF;
dfNs.JournalDayTagDataService = (function(): Module {
    return {
        getCtgrMap: function(callback: (map: Record<string, any>) => void): void {
            cF.ajax.get(Url.JOURNAL_DAY_TAG_CTGR_MAP, {}, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                callback((res.rsltMap as Record<string, any>) ?? {});
            });
        },

        getYyList: function(tagId: string|number, callback: (yyList: any[]) => void): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_TAG_YYS, { tagId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                callback(Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        listTags: function(params: Record<string, any>, callback: (list: Record<string, any>[]) => void): void {
            cF.ajax.get(Url.JOURNAL_DAY_TAGS, params, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                callback(Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        getTagDetail: function(tagId: string|number, params: Record<string, any>, callback: (list: Record<string, any>[]) => void): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_TAG, { tagId });
            cF.ajax.get(url, params, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                callback(Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        getTagProfile: function(tagId: string|number, contentType: string, callback: (obj: Record<string, any>) => void): void {
            const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId });
            cF.ajax.get(url, { contentType }, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                callback((res.rsltObj as Record<string, any>) ?? {});
            });
        },
    };
})();

export {};
