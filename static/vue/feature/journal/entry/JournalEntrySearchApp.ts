import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalEntrySearchItem from "./components/JournalEntrySearchItem.js";

/**
 * 변경(E-4-γ): journal_entry_search_module.ts 흡수 — `dF.JournalEntrySearch` facade.
 * 변경 전: classic journal_entry_search_module.js 가 JournalEntrySearchVueApp 로 forward.
 * 변경 후: 동일 forward 계약; 본 ES 모듈 평가 시점에 등록한다.
 */
(function registerJournalEntrySearchFacade(): void {
    const g = typeof globalThis !== "undefined" ? globalThis : window;
    const dfNs: any = g.dF ?? {};
    if (g.dF == null) g.dF = dfNs;

    const forward = (method: string, ...args: any[]): any => {
        const app = (g as any).JournalEntrySearchVueApp;
        const fn = app?.[method];
        if (typeof fn !== "function") return;
        return fn(...args);
    };

    dfNs.JournalEntrySearch = (function(): dfModule {
        const createFacade = function(): dfModule {
            return {
                initialized: true,
                initPromise: null,
                init: function(): void {},
                initSearch: function(): void { forward("search"); },
                initKeyword: function(): void {},
                initTag: function(): void {},
                initSort: function(): void {},
                addKeyword: function(value?: string): void { forward("addKeyword", value); },
                removeKeyword: function(value: string): void { forward("removeKeyword", value); },
                clearKeywordFields: function(): void {},
                toggleSort: function(): void { forward("toggleSort"); },
                resetSearch: function(): void { forward("resetSearch"); },
                select: function(tagId: string|number, name: string): void { forward("selectTag", tagId, name); },
                removeTag: function(value: string): void { forward("removeTag", value); },
                search: function(): void { forward("search"); },
                copy: function(): void { forward("copy"); },
                exportTxt: function(): void { forward("exportTxt"); },
                replaceItem: function(id: string|number): void { forward("replaceItem", id); },
                removeItem: function(id: string|number): void { forward("removeItem", id); },
            };
        };

        const modules: Record<string, dfModule> = {
            JOURNAL_DIARY: createFacade(),
            JOURNAL_DREAM: createFacade(),
        };

        return {
            initialized: true,
            init: function(): void {},
            get: function(contentType: string): dfModule {
                return modules[contentType] ?? modules.JOURNAL_DIARY;
            },
        };
    })();
})();

type SearchState = {
    contentType: string;
    entryType: string;
    contentLabel: string;
    emptyLabel: string;
    cssPrefix: string;
    iconIdPrefix: string;
    showDreamStates: boolean;
    highlightImportant: boolean;
    rightBorderClass: string;
    list: Record<string, any>[];
    keywordList: string[];
    tagList: Array<{ id: string; name: string }>;
    sort: "asc" | "desc";
};

const i18n = createScopedI18n();

function mountSearchBridge(searchState: SearchState): void {
    const buildSearchItemModel = (entry: Record<string, any>): Record<string, any> => ({
        ...dF.JournalEntry.get(searchState.contentType).buildViewModel(entry, "SEARCH"),
        contentType: searchState.contentType,
        contentLabel: searchState.contentLabel,
        cssPrefix: searchState.cssPrefix,
        iconIdPrefix: searchState.iconIdPrefix,
        showDreamStates: searchState.showDreamStates,
        highlightImportant: searchState.highlightImportant,
        rightBorderClass: searchState.rightBorderClass,
    });

    const readSearchParams = (): Record<string, any> => {
        const params: Record<string, any> = { type: searchState.entryType, sort: searchState.sort };
        if (searchState.keywordList.length > 0) params.searchKeywords = [...searchState.keywordList];
        if (searchState.tagList.length > 0) params.tagIds = searchState.tagList.map((tag): string => tag.id);
        return params;
    };

    const renderDisplay = (): void => {
        const keywordDisplay = document.getElementById("keywordDisplay");
        const tagDisplay = document.getElementById("tagDisplay");
        if (keywordDisplay) keywordDisplay.innerHTML = searchState.keywordList.map((keyword: string): string => `<div class="badge badge-light-primary keyword-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary">${keyword}</div>`).join("");
        if (tagDisplay) tagDisplay.innerHTML = searchState.tagList.map((tag): string => `<div class="badge badge-light-primary tag-wrapper fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary">#${tag.name}</div>`).join("");
        const sortInput = document.getElementById("sortInput") as HTMLInputElement | null;
        if (sortInput) sortInput.value = searchState.sort;
    };

    const search = (): void => {
        const ajaxData: Record<string, any> = readSearchParams();
        const hasKeyword = Array.isArray(ajaxData.searchKeywords) && ajaxData.searchKeywords.length > 0;
        const hasTag = Array.isArray(ajaxData.tagIds) && ajaxData.tagIds.length > 0;
        if (!hasKeyword && !hasTag) {
            searchState.list = [];
            const msg = document.getElementById("msgDisplay");
            if (msg) msg.textContent = "검색 조건을 하나 이상 입력하세요.";
            renderDisplay();
            return;
        }

        const msg = document.getElementById("msgDisplay");
        if (msg) msg.textContent = "";
        cF.ajax.get(dF.JournalEntry.getMeta(searchState.contentType).listUrl, ajaxData, function(res: AjaxResponse): void {
            if (!res.rslt) return;
            searchState.list = (res.rsltList ?? []).map((entry: Record<string, any>): Record<string, any> => buildSearchItemModel(entry));
            renderDisplay();
            KTMenu.createInstances();
            const params: URLSearchParams = cF.util.buildUrlParams(ajaxData);
            history.replaceState(null, "", `${window.location.pathname}?${params.toString()}`);
        });
    };

    const parseInitial = (): void => {
        const params = new URLSearchParams(window.location.search);
        searchState.keywordList = params.getAll("searchKeywords").map((k: string): string => String(k ?? "").trim()).filter((k: string): boolean => k.length > 0);
        searchState.sort = params.get("sort") === "asc" ? "asc" : "desc";
        const tagIds: string[] = params.getAll("tagIds");
        searchState.tagList = tagIds.map((tagId: string): { id: string; name: string } => {
            const tag = dF.JournalEntryTag.get(searchState.contentType).list.find((item: any): boolean => String(item.id) === String(tagId));
            return { id: tagId, name: String(tag?.name ?? tagId) };
        });
        renderDisplay();
        search();
    };

    (window as any).JournalEntrySearchVueApp = {
        addKeyword: function(value?: string): void {
            const keywordInput = document.getElementById("keywordInput") as HTMLInputElement | null;
            const nextKeyword: string = String(value ?? keywordInput?.value ?? "").trim();
            if (nextKeyword.length === 0) return;
            if (searchState.keywordList.some((keyword: string): boolean => keyword.toLowerCase() === nextKeyword.toLowerCase())) return;
            searchState.keywordList.push(nextKeyword);
            if (keywordInput) keywordInput.value = "";
            search();
        },
        removeKeyword: function(value: string): void {
            searchState.keywordList = searchState.keywordList.filter((keyword: string): boolean => keyword !== value);
            search();
        },
        selectTag: function(tagId: string|number, name: string): void {
            const normalizedTagId: string = String(tagId);
            if (searchState.tagList.some((tag): boolean => tag.id === normalizedTagId)) return;
            searchState.tagList.push({ id: normalizedTagId, name: String(name ?? normalizedTagId) });
            search();
        },
        removeTag: function(tagId: string|number): void {
            const normalizedTagId: string = String(tagId);
            searchState.tagList = searchState.tagList.filter((tag): boolean => tag.id !== normalizedTagId);
            search();
        },
        toggleSort: function(): void {
            searchState.sort = searchState.sort === "desc" ? "asc" : "desc";
            search();
        },
        search,
        resetSearch: function(): void {
            window.location.href = window.location.pathname;
        },
        copy: function(): void {
            if (searchState.list.length === 0) return;
            const textToCopy = searchState.list.map((item: Record<string, any>): string => `${item.stdrdDt} (${item.journalDateWeekDay})\r\n#${item.sortOrder}\r\n${cF.util.htmlToText(item.markdownContent ?? "")}`).join("\r\n\r\n");
            navigator.clipboard.writeText(textToCopy).catch(function(): void {
                cF.util.legacyCopy(textToCopy);
            });
        },
        exportTxt: function(): void {
            const params: URLSearchParams = cF.util.buildUrlParams(readSearchParams());
            window.location.href = `${dF.JournalEntry.getMeta(searchState.contentType).exportUrl}?${params.toString()}`;
        },
        replaceItem: function(id: string|number): void {
            const url = cF.util.bindUrl(dF.JournalEntry.getMeta(searchState.contentType).itemUrl, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) return;
                const viewModel = buildSearchItemModel(res.rsltObj);
                const idx = searchState.list.findIndex((item: Record<string, any>): boolean => Number(item.id) === Number(id));
                if (idx >= 0) searchState.list.splice(idx, 1, viewModel);
                else search();
            });
        },
        removeItem: function(id: string|number): void {
            searchState.list = searchState.list.filter((item: Record<string, any>): boolean => Number(item.id) !== Number(id));
        },
    };

    parseInitial();
}

document.addEventListener("DOMContentLoaded", async function(): Promise<void> {
    const root = document.getElementById("journal_entry_search_app") as HTMLElement | null;
    if (!root) return;

    const contentType: string = String(root.dataset.contentType ?? "JOURNAL_DIARY");

    /**
     * 변경(E-4-γ): journal_entry_search.ts 의 Page.init 흡수.
     * 변경 전/후: DOMContentLoaded 에서 initAll("SEARCH"), Comment.modal.init, State.init,
     * keywordInput 엔터 → addKeyword 동작 동일.
     */
    await dF.JournalEntry.initAll("SEARCH");
    /** @keepInSync static/vue/feature/journal/day/journalDayListBridge.ts */
    const journalDayResolveListBridge = (): JournalDayListAppBridge | undefined =>
        window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;
    window.addEventListener("comment:modal-refresh", function(): void {
        journalDayResolveListBridge()?.applySearchParamsAndReload?.({}, "MONTHLY");
    });
    dF.State.init();

    const input: HTMLElement | null = document.getElementById("keywordInput");
    input?.addEventListener("keydown", (e: KeyboardEvent): void => {
        if (e.key === "Enter" && !e.isComposing) {
            e.preventDefault();
            dF.JournalEntrySearch.get(contentType).addKeyword();
        }
    });

    const meta: Record<string, any> = dF.JournalEntry.getMeta(contentType);
    const searchState = Vue.reactive({
        contentType,
        entryType: String(meta.entryType ?? "DIARY"),
        contentLabel: String(meta.contentLabel ?? ""),
        emptyLabel: String(meta.emptyLabel ?? ""),
        cssPrefix: String(meta.cssPrefix ?? "diary"),
        iconIdPrefix: String(meta.iconIdPrefix ?? ""),
        showDreamStates: Boolean(meta.hasDreamStates),
        highlightImportant: Boolean(meta.highlightImportant),
        rightBorderClass: String(meta.rightBorderClass ?? ""),
        list: [],
        keywordList: [],
        tagList: [],
        sort: "desc",
    }) as SearchState;

    await i18n.load((document.documentElement.lang || "ko").replace(/_/g, "-"));

    const app = Vue.createApp({
        name: "JournalEntrySearchRootApp",
        components: { JournalEntrySearchItem },
        data(): { state: SearchState } {
            return { state: searchState };
        },
        computed: {
            groupedList(): Array<{ key: string; yy: string; mnth: string; items: Record<string, any>[] }> {
                const groups: Array<{ key: string; yy: string; mnth: string; items: Record<string, any>[] }> = [];
                this.state.list.forEach((item: Record<string, any>): void => {
                    const key = `${item.yy}-${item.mnth}`;
                    const existing = groups.find((group): boolean => group.key === key);
                    if (existing) existing.items.push(item);
                    else groups.push({ key, yy: String(item.yy ?? ""), mnth: String(item.mnth ?? ""), items: [item] });
                });
                return groups;
            },
        },
        template: `
        <div>
            <template v-if="state.list.length > 0">
                <template v-for="group in groupedList" :key="group.key">
                    <div class="d-flex-center mt-6 mb-4 fs-5 text-dark">{{ group.yy }}년{{ group.mnth }}월</div>
                    <JournalEntrySearchItem
                        v-for="entry in group.items"
                        :key="'entry-search-' + entry.id"
                        :entry="entry"
                        :content-type="state.contentType"
                        :content-label="state.contentLabel"
                        :css-prefix="state.cssPrefix"
                        :icon-id-prefix="state.iconIdPrefix"
                        :show-dream-states="state.showDreamStates"
                        :highlight-important="state.highlightImportant"
                        :right-border-class="state.rightBorderClass"
                    />
                </template>
            </template>
            <div v-else class="journal-day d-flex-center">
                {{ state.emptyLabel }} 목록이 없습니다.
            </div>
        </div>
        `,
    });
    app.config.globalProperties.$t = (key: string): string => i18n.t(key);
    app.mount("#journal_entry_search_app");
    mountSearchBridge(searchState);
});
