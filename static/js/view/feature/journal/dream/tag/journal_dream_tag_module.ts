/**
 * journal_dream_tag_module.ts
 * 꿈 태그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.JournalDreamTag = (function(): dfModule {
    return {
        initialized: false,
        initPromise: null,
        ctgrMap: new Map(),
        list: [],

        /**
         * initializes module.
         * @return Promise<void>
         */
        init: async function(): Promise<void> {
            if (this.initPromise) return this.initPromise;

            this.initPromise = (async () => {
                await dF.JournalDreamTag.getCtgrMap();
                await dF.JournalDreamTag.getNmList();
                this.initialized = true;
                console.log("'dF.JournalDreamTag' module initialized.");
            })();

            return this.initPromise;
        },

        /**
         * 태그 카테고리 맵 조회
         * @return Promise<void>
         */
        getCtgrMap: async function(): Promise<void> {
            const url: string = Url.JOURNAL_DREAM_TAG_CTGR_MAP;
            return cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JournalDreamTag.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 태그 이름 맵 조회
         * @return Promise<void>
         */
        getNmList: async function(): Promise<void> {
            const url: string = Url.JOURNAL_DREAM_TAGS;
            return cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltList) dF.JournalDreamTag.list = res.rsltList;
            });
        },

        getCurrentWeekStartDt: function(): string {
            const currentWeekStartDt: string = dF.JournalDay?.currentSearchParams?.weekStartDt;
            if (cF.util.isNotEmpty(currentWeekStartDt)) return currentWeekStartDt;

            if (dF.JournalDay?.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.weekStartDt)) return Page.weekStartDt;

            const stdrdDt: string = dF.JournalDay?.currentSearchParams?.stdrdDt
                ?? Page?.stdrdDt
                ?? cF.date.getCurrDateStr(cF.date.ptnDate);
            return cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
        },

        /**
         * 목록 상단의 꿈 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.JOURNAL_DREAM_TAGS;
            const ajaxData: Record<string, any> = {};
            if (dF.JournalDay?.viewType === "WEEKLY") {
                const weekStartDt: string = dF.JournalDreamTag.getCurrentWeekStartDt();
                if (cF.util.isEmpty(weekStartDt)) return;
                ajaxData.weekStartDt = weekStartDt;
            } else {
                const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "9999";
                if (cF.util.isEmpty(yy)) return;
                const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "99";
                if (cF.util.isEmpty(mnth)) return;
                ajaxData.yy = yy;
                ajaxData.mnth = mnth;
            }

            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "journal_dream_tag_list");
            });
        },

        /**
         * 목록 상단의 꿈 태그 (전체) 조회 (Ajax)
         */
        listAllAjax: function(): void {
            const url: string = Url.JOURNAL_DREAM_TAGS;
            const ajaxData: Record<string, any> = { yy: 9999, mnth: 99 };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const ctgrSet: Set<string> = new Set();
                res.rsltList.forEach((item: Record<string, string>): void => {
                    if (item.ctgr) ctgrSet.add(item.ctgr);
                });
                cF.handlebars.template(ctgrSet, "journal_tag_ctgr");
                cF.handlebars.modal(res.rsltList, "journal_tag_list");
            });
        },

        /**
         * 목록 상단의 꿈 태그 (전체) 조회 (Ajax)
         */
        dreamTagGroupListAllAjax: function(): void {
            const url: string = Url.JOURNAL_DREAM_TAGS;
            const ajaxData: Record<string, any> = { yy: 9999, mnth: 99 };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const groupedList = dF.Tag.groupTagsByCategory(res.rsltList);
                for (const ctgr in groupedList) {
                    if (!Object.prototype.hasOwnProperty.call(groupedList, ctgr)) continue;
                    const eachList = groupedList[ctgr];
                    cF.handlebars.append({ ctgr, tagList: eachList }, "journal_tag_list");
                }
                $("#journal_tag_list_modal").modal("show");
            });
        },

        /**
         * 태그 검색 팝업 호출
         * @param {string|number} tagId - 조회할 태그 ID.
         */
        openSearch: function(tagId: string|number): void {
            let url: string = `${Url.JOURNAL_DREAM_SEARCH}?tagIds=${tagId}`;
            if (dF.JournalDay?.viewType === "WEEKLY") {
                const weekStartDt: string = dF.JournalDreamTag.getCurrentWeekStartDt();
                if (cF.util.isNotEmpty(weekStartDt)) url += `&weekStartDt=${encodeURIComponent(weekStartDt)}`;
            }

            const popupNm: string = "꿈 검색";
            const options: string = "width=1960,height=1440,top=0,left=270";
            const popup: Window = cF.ui.openPopup(url, popupNm, options);
            if (popup) popup.focus();
        },

        select: function(tagId: string|number, tagNm?: string, ctgr: string = ""): void {
            if (dF.JournalDayTag?.isContextMenuEnabled?.()) {
                dF.JournalDayTag.openContextMenu(tagId, tagNm ?? "", ctgr, function(): void {
                    dF.JournalDreamTag.openSearch(tagId);
                }, "JOURNAL_DREAM");
                return;
            }

            dF.JournalDreamTag.openSearch(tagId);
        },
    };
})();
