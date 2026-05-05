/**
 * JournalDayAsideEntryFiltersApp.ts
 * 저널 Aside — TAGCLOUD 토글, 일기/꿈 필터 블록(키워드·챕터 카테고리), 고급 필터 아코디언 Vue 렌더.
 *
 * 변경 전: _journal_day_aside_base.ftlh 정적 마크업 + _journal_day_aside_entry_filter.ftlh 2중 include + 아코디언 별도 블록.
 * 변경 후: 동일 id/class·<code>data-journal-day-action</code> 유지(<code>journalDayRuntimeShell</code> 위임·jQuery SSOT 호환).
 *         챕터 옵션은 FTL이 <code>window.__journalAsideEntryFiltersBootstrap.chapterCtgrOptions</code>에 적재한다.
 */

type ChapterCtgrOption = { code: string; codeName: string };
/** @keepInSync static/js/view/feature/journal/day/journal_day_aside_module.ts */
const journalDayResolveListBridge = (): JournalDayListAppBridge | undefined =>
    window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function asideMsg(key: string): string {
    return typeof Message !== "undefined" && typeof Message.get === "function" ? Message.get(key) : key;
}

function readChapterCtgrOptions(): ChapterCtgrOption[] {
    const boot = (window as Window & { __journalAsideEntryFiltersBootstrap?: { chapterCtgrOptions?: ChapterCtgrOption[] } })
        .__journalAsideEntryFiltersBootstrap;
    const list = boot?.chapterCtgrOptions;
    return Array.isArray(list) ? list : [];
}

function initEntryFilterTooltips(root: HTMLElement): void {
    const win = globalThis as unknown as {
        bootstrap?: { Tooltip?: new (el: HTMLElement) => unknown };
    };
    const TooltipCtor = win.bootstrap?.Tooltip;
    Vue.nextTick(function(): void {
        root.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            try {
                if (TooltipCtor) {
                    void new TooltipCtor(el as HTMLElement);
                }
            } catch (e) {
                console.warn("[JournalDayAsideEntryFiltersApp] tooltip init 실패:", e);
            }
        });
    });
}

function entryFilterBlockTemplate(block: "diary" | "dream"): string {
    const isDiary = block === "diary";
    const toggleId = isDiary ? "toggleDiaries" : "toggleDreams";
    const tooltipKey = isDiary ? "bs.tooltip.journal.aside-diary-toggle" : "bs.tooltip.journal.aside-dream-toggle";
    const titleLabel = isDiary ? "DIARIES" : "DREAMS";
    const kwId = isDiary ? "diaryFilterKeyword" : "dreamFilterKeyword";
    const kwName = kwId;
    const kwLabel = isDiary ? "- DIARY KEYWORDS" : "- DREAM KEYWORDS";
    const phKey = isDiary ? "txt.journal.day.filter.diary.placeholder" : "txt.journal.day.filter.dream.placeholder";
    const showChapter = isDiary;

    const chapterBlock = showChapter
        ? `
        <div id="chapterCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
            <div class="d-flex align-items-center justify-content-between">
                <label for="chapterCtgrFilter" class="text-muted mb-0">- CHAPTER CATEGORIES</label>
                <input type="checkbox"
                       id="toggleChapterCtgr"
                       class="form-check-input cursor-pointer m-0"
                       data-journal-day-action="toggle-chapter-ctgr"
                       checked>
            </div>
            <select id="chapterCtgrFilter" class="form-select form-select-sm w-100"
                    multiple size="4"
                    data-bs-toggle="tooltip" data-bs-placement="top" :title="multiSelectTooltip"
                    data-journal-day-action="chapter-ctgr-select">
                <option value="__ALL__">{{ ctgrAllLabel }}</option>
                <option v-for="ct in chapterCtgrOptions" :key="ct.code" :value="ct.code">[{{ ct.codeName }}]</option>
            </select>
        </div>
        `
        : "";

    return `
    <div class="d-flex flex-column gap-2 mb-2 px-2">
        <div class="d-flex align-items-center justify-content-between"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('${tooltipKey}')">
            <label for="${toggleId}" class="text-muted cursor-help mb-0">${titleLabel}</label>
            <input type="checkbox"
                   id="${toggleId}"
                   class="form-check-input cursor-pointer m-0"
                   data-journal-day-action="toggle-param"
                   checked>
        </div>
        ${chapterBlock}
        <div class="d-flex flex-column ps-3 gap-1">
            <div class="d-flex align-items-center justify-content-between">
                <label for="${kwId}" class="text-muted mb-0">${kwLabel}</label>
            </div>
            <div class="d-flex gap-1">
                <input type="text" name="${kwName}" id="${kwId}" class="form-control form-control-sm"
                       value="" :placeholder="tooltip('${phKey}')" maxlength="200"
                       @keyup.enter="applyKeywordFiltersByVueBridge"/>
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                    <i class="bi bi-funnel pe-0"></i>
                </button>
            </div>
        </div>
    </div>
    `;
}

const JournalDayAsideEntryFiltersRoot = {
    name: "JournalDayAsideEntryFiltersRoot",
    data(): { chapterCtgrOptions: ChapterCtgrOption[] } {
        return {
            chapterCtgrOptions: readChapterCtgrOptions(),
        };
    },
    computed: {
        ctgrAllLabel(): string {
            return asideMsg("txt.ctgr.all");
        },
        multiSelectTooltip(): string {
            return asideMsg("bs.tooltip.select.multi");
        },
    },
    methods: {
        tooltip(key: string): string {
            return asideMsg(key);
        },
        applyKeywordFiltersByVueBridge(): void {
            const bridge = journalDayResolveListBridge();
            if (bridge?.mounted === true && typeof bridge.applyKeywordFilters === "function") {
                bridge.applyKeywordFilters();
                return;
            }
            console.error("[JournalDayAsideEntryFiltersApp] applyKeywordFilters 브리지 미등록.");
        },
    },
    mounted(): void {
        const root = this.$el as HTMLElement;
        if (root) initEntryFilterTooltips(root);
        window.JournalDayAsideEntryFiltersVueApp = { mounted: true };
    },
    template: `
    <div class="journal-day-aside-entry-filters-vue-root">
        <div class="d-flex flex-column gap-2 px-2">
            <div class="d-flex align-items-center justify-content-between"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                 :title="tooltip('bs.tooltip.journal.aside-tagcloud-toggle')">
                <label for="toggleTagCloud" class="text-muted cursor-help mb-0">TAGCLOUD</label>
                <input type="checkbox"
                       id="toggleTagCloud"
                       class="form-check-input cursor-pointer m-0"
                       data-journal-day-action="toggle-param"
                       checked>
            </div>
        </div>
        <div class="separator"></div>
        ${entryFilterBlockTemplate("diary")}
        <div class="separator"></div>
        ${entryFilterBlockTemplate("dream")}
        <div class="separator"></div>
        <div class="accordion accordion-flush" id="journal_day_filter_accordion">
            <div class="accordion-item">
                <h2 class="accordion-header" id="journal_day_filter_heading_advanced">
                    <button class="accordion-button collapsed fw-semibold" type="button"
                            data-bs-toggle="collapse" data-bs-target="#journal_day_filter_advanced"
                            aria-expanded="false" aria-controls="journal_day_filter_advanced">
                        {{ tooltip('txt.journal.day.filter.advanced') }}
                    </button>
                </h2>
                <div id="journal_day_filter_advanced" class="accordion-collapse collapse"
                     aria-labelledby="journal_day_filter_heading_advanced"
                     data-bs-parent="#journal_day_filter_accordion">
                    <div class="accordion-body pt-4">
                        <div class="text-muted fs-7">
                            {{ tooltip('txt.journal.day.filter.more') }}
                        </div>
                        <div class="mt-4" data-filter-slot="advanced"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.querySelector("#journal_day_aside_entry_filters_mount") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayAsideEntryFiltersApp] 마운트 루트 #journal_day_aside_entry_filters_mount 없음.");
        return;
    }
    try {
        const app = Vue.createApp(JournalDayAsideEntryFiltersRoot);
        app.mount(mountEl);
    } catch (e) {
        console.error("[JournalDayAsideEntryFiltersApp] Vue 마운트 실패:", e);
    }
});

export {};
