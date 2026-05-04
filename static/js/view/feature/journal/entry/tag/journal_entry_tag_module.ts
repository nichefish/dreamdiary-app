/**
 * journal_entry_tag_module.ts
 * Common tag module for journal diary/dream entries.
 */
if (typeof dF === "undefined") { var dF = {} as any; }

dF.JournalEntryTag = (function(): dfModule {
    const createConfig = function(contentType: string): Record<string, any> {
        const meta: Record<string, any> = dF.JournalEntry.getMeta(contentType);
        return {
            moduleName: `JournalEntryTag[${contentType}]`,
            contentType,
            entryType: meta.entryType,
            tagCtgrMapUrl: meta.tagCtgrMapUrl,
            tagsUrl: meta.tagsUrl,
            searchUrl: meta.searchUrl,
            tagListTargetId: meta.tagListTargetId,
            tagModule: meta.tagModuleExpr,
            popupName: meta.popupName,
        };
    };

    const modules: Record<string, any> = {};

    const resolveSearchUrl = function(config: Record<string, any>): string {
        if (cF.util.isNotEmpty(config?.searchUrl) && !String(config.searchUrl).includes("undefined")) {
            return String(config.searchUrl);
        }
        const typeSegment: string = String(config?.entryType ?? "DIARY").toLowerCase();
        return cF.util.bindUrl(Url.JOURNAL_EMTRY_SEARCH, { type: typeSegment });
    };

    const create = function(config: Record<string, any>): dfModule {
        const module: dfModule = {
            initialized: false,
            initPromise: null,
            ctgrMap: new Map(),
            list: [],

            init: async function(): Promise<void> {
                if (this.initPromise) return this.initPromise;

                this.initPromise = (async () => {
                    await module.getCtgrMap();
                    await module.getNmList();
                    this.initialized = true;
                    console.log(`'dF.${config.moduleName}' module initialized.`);
                })();

                return this.initPromise;
            },

            getCtgrMap: async function(): Promise<void> {
                return cF.ajax.get(config.tagCtgrMapUrl, { type: config.entryType }, function(res: AjaxResponse): void {
                    if (res.rsltMap) module.ctgrMap = res.rsltMap;
                });
            },

            getNmList: async function(): Promise<void> {
                return cF.ajax.get(config.tagsUrl, { type: config.entryType }, function(res: AjaxResponse): void {
                    if (res.rsltList) module.list = res.rsltList;
                });
            },

            renderList: function(list: Record<string, any>[] = []): void {
                const actual: string = cF.handlebars.compile({
                    list,
                    module: config.tagModule,
                }, "journal_entry_tag_list");
                if (actual == null) return console.error("template compile error: journal_entry_tag_list");

                const targetElement: HTMLElement|null = document.getElementById(config.tagListTargetId);
                if (!targetElement) return console.error(`target element not found: ${config.tagListTargetId}`);

                targetElement.innerHTML = "";
                targetElement.insertAdjacentHTML("beforeend", actual);
                targetElement.querySelectorAll("[data-bs-toggle='tooltip']").forEach((tooltipEl: HTMLElement): void => {
                    new bootstrap.Tooltip(tooltipEl);
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

            listAjax: function(): void {
                const ajaxData: Record<string, any> = {};
                ajaxData.type = config.entryType;
                if (dF.JournalDay?.viewType === "WEEKLY") {
                    const weekStartDt: string = module.getCurrentWeekStartDt();
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

                cF.ajax.get(config.tagsUrl, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    module.renderList(res.rsltList ?? []);
                });
            },

            listAllAjax: function(): void {
                const ajaxData: Record<string, any> = { yy: 9999, mnth: 99, type: config.entryType };
                cF.ajax.get(config.tagsUrl, ajaxData, function(res: AjaxResponse): void {
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

            tagGroupListAllAjax: function(): void {
                const ajaxData: Record<string, any> = { yy: 9999, mnth: 99, type: config.entryType };
                cF.ajax.get(config.tagsUrl, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    const groupedList = dF.Tag.groupTagsByCategory(res.rsltList);
                    for (const ctgr in groupedList) {
                        if (!Object.prototype.hasOwnProperty.call(groupedList, ctgr)) continue;
                        cF.handlebars.append({ ctgr, tagList: groupedList[ctgr] }, "journal_tag_list");
                    }
                    $("#journal_tag_list_modal").modal("show");
                });
            },

            openSearch: function(tagId: string|number, tagNm?: string): void {
                if ((window as any).journalEntrySearchContentType === config.contentType) {
                    const resolvedTagNm: string = tagNm
                        ?? module.list?.find?.((tag: any): boolean => Number(tag.id) === Number(tagId))?.tagNm
                        ?? String(tagId);
                    dF.JournalEntrySearch?.get?.(config.contentType)?.select?.(tagId, resolvedTagNm);
                    return;
                }

                const baseSearchUrl: string = resolveSearchUrl(config);
                let url: string = `${baseSearchUrl}?tagIds=${tagId}`;
                if (dF.JournalDay?.viewType === "WEEKLY") {
                    const weekStartDt: string = module.getCurrentWeekStartDt();
                    if (cF.util.isNotEmpty(weekStartDt)) url += `&weekStartDt=${encodeURIComponent(weekStartDt)}`;
                }

                const options: string = "width=1960,height=1440,top=0,left=270";
                const popup: Window = cF.ui.openPopup(url, config.popupName, options);
                if (popup) popup.focus();
            },

            select: function(tagId: string|number, tagNm?: string, ctgr: string = ""): void {
                if (dF.JournalDayTag?.isContextMenuEnabled?.()) {
                    dF.JournalDayTag.openContextMenu(tagId, tagNm ?? "", ctgr, function(): void {
                        module.openSearch(tagId, tagNm);
                    }, config.contentType);
                    return;
                }

                module.openSearch(tagId, tagNm);
            },
        };

        if (config.contentType === "JOURNAL_DREAM") {
            module.dreamTagGroupListAllAjax = module.tagGroupListAllAjax;
        }

        return module;
    };

    [
        "JOURNAL_DIARY",
        "JOURNAL_DREAM"
    ].forEach(function(contentType: string): void {
        modules[contentType] = create(createConfig(contentType));
    });

    return {
        initialized: true,
        init: function(): void {},
        get: function(contentType: string): dfModule {
            return modules[contentType];
        },
    };
})();
