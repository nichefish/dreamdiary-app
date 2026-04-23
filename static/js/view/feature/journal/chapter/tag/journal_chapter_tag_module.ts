/**
 * journal_chapter_tag_module.ts
 * 저널 챕터 태그 스크립트 모듈
 * 
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalChapterTag = (function(): dfModule {
    const getEntryTagMeta = function(): Record<string, any> {
        return dF.JournalEntry.getMeta("JOURNAL_DIARY");
    };

    return {
        initialized: false,
        ctgrMap: new Map(),

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalChapterTag.initialized) return;

            dF.JournalChapterTag.getCtgrMap();

            dF.JournalChapterTag.initialized = true;
            console.log("'dF.JournalChapterTag' module initialized.");
        },

        /**
         * 태그 카테고리 맵 조회
         */
        getCtgrMap: function(): void {
            const url: string = getEntryTagMeta().tagCtgrMapUrl;
            cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JournalChapterTag.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 목록에 따른 일기 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "9999";
            if (cF.util.isEmpty(yy)) return;
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "99";
            if (cF.util.isEmpty(mnth)) return;

            const url: string = getEntryTagMeta().tagsUrl;
            const ajaxData: Record<string, any> = { yy, mnth };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "journal_chapter_tag_list");
            });
        },

        /**
         * 목록에 따른 일기 태그 (전체) 조회 (Ajax)
         */
        listAllAjax: function(): void {
            const url: string = getEntryTagMeta().tagsUrl;
            const ajaxData: Record<string, any> = { "yy": 9999, "mnth":99 };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 상단에 태그 카테고리 메뉴 생성
                const ctgrSet = new Set();
                res.rsltList.forEach((item: Record<string, any>): void => {
                    if (item.ctgr) ctgrSet.add(item.ctgr);
                });
                cF.handlebars.template(ctgrSet, "journal_tag_ctgr");
                cF.handlebars.modal(res.rsltList, "journal_tag_list");
            });
        },
    }
})();
