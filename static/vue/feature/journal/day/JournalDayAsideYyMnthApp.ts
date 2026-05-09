/**
 * JournalDayAsideYyMnthApp.ts
 * 저널 일자 사이드바 — 년월·Week 래퍼·Pinpoint 영역 Vue 렌더.
 *
 * 변경 전: _journal_day_aside_yy_mnth_section.ftlh 정적 마크업 + onclick/onchange → dF.JournalDayAside.
 * 변경 후: 동일 id/클래스·구조를 Vue 템플릿으로 재현(레이아웃·Metronic 훅 유지).
 *         연도 목록은 FTL이 <code>window.__journalAsideYyMnthBootstrap.yyOptions</code>에 싣는다.
 * @see JournalDayAsideWeekNavigatorApp — <code>#journalAsideWeekDays</code> Teleport 대상은 본 앱이 유지한다.
 */

// 변경(D): 글로벌 `Message` 결의를 `resolveMessage` 헬퍼로 위임 — typeof guard 분기 통일(window/globalThis.Message 우선 결의 + key 폴백).
import { resolveMessage } from "../../../common/messageHelper.js";

type YyOption = { value: string; label: string };
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

/** 툴팁 title — 서버 Message 번들과 동일 키 사용. */
function asideTooltip(key: string): string {
    return resolveMessage(key);
}

function readYyOptions(): YyOption[] {
    const boot = (window as Window & { __journalAsideYyMnthBootstrap?: { yyOptions?: YyOption[] } })
        .__journalAsideYyMnthBootstrap;
    const list = boot?.yyOptions;
    if (!Array.isArray(list) || list.length === 0) {
        console.error("[JournalDayAsideYyMnthApp] window.__journalAsideYyMnthBootstrap.yyOptions 가 비어 있음.");
        return [];
    }
    return list;
}

/**
 * 마운트 직후 Tooltip 인스턴스를 붙인다(FTL 정적 마크업과 동등).
 * 변경 전: Metronic/페이지 부트가 일괄 초기화했을 수 있음.
 * 변경 후: Vue가 그린 노드에 한해 <code>bootstrap.Tooltip</code>을 직접 생성한다.
 */
function initAsideYyMnthTooltips(root: HTMLElement): void {
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
                console.warn("[JournalDayAsideYyMnthApp] tooltip init 실패:", e);
            }
        });
    });
}

function monthOptions(): number[] {
    const out: number[] = [];
    for (let m = 1; m <= 12; m += 1) out.push(m);
    return out;
}

function shiftMonth(yy: string, mnth: string, direction: "prev" | "next"): { yy: string; mnth: string } {
    let nextYy: string = yy;
    let nextMnth: string = mnth;
    const monthNo: number = parseInt(mnth, 10);

    if (direction === "prev") {
        if (monthNo === 1) {
            nextYy = String(parseInt(yy, 10) - 1);
            nextMnth = "12";
        } else {
            nextMnth = String(monthNo - 1);
        }
        return { yy: nextYy, mnth: nextMnth };
    }

    if (monthNo === 12) {
        nextYy = String(parseInt(yy, 10) + 1);
        nextMnth = "1";
    } else {
        nextMnth = String(monthNo + 1);
    }
    return { yy: nextYy, mnth: nextMnth };
}

function navigateWeekDate(anchorDate: string, direction: "prev" | "next"): string {
    const currentWeekStartDt: string = cF.date.getWeekdayDateStr(anchorDate, 1, cF.date.ptnDate) ?? anchorDate;
    return cF.date.navigateDateStr("week", currentWeekStartDt, direction, cF.date.ptnDate) ?? currentWeekStartDt;
}

function requireListBridge(action: string): JournalDayListAppBridge | undefined {
    const bridge = journalDayResolveListBridge();
    if (bridge?.mounted === true) return bridge;
    console.error("[JournalDayAsideYyMnthApp] %s: JournalDay*App bridge unavailable.", action);
    return undefined;
}

const JournalDayAsideYyMnthRoot = {
    name: "JournalDayAsideYyMnthRoot",
    data(): { yyOptions: YyOption[]; months: number[] } {
        return {
            yyOptions: readYyOptions(),
            months: monthOptions(),
        };
    },
    mounted(): void {
        const root = this.$el as HTMLElement;
        if (root) initAsideYyMnthTooltips(root);
        window.JournalDayAsideYyMnthVueApp = { mounted: true };
    },
    methods: {
        msgYy(): string {
            return asideTooltip("txt.yy");
        },
        msgMnth(): string {
            return asideTooltip("txt.mnth");
        },
        tooltip(key: string): string {
            return asideTooltip(key);
        },
        onYyChange(): void {
            const bridge = requireListBridge("onYyChange");
            if (!bridge) return;

            const tagListContainer: HTMLElement | null = document.querySelector("#journal_day_tag_list_div");
            if (tagListContainer) tagListContainer.innerHTML = "";
            dF.JournalEntry.get("JOURNAL_DREAM").inKeywordSearchMode = false;

            const yyElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
            const selectedYear: string = String(yyElement?.value ?? "");
            if (selectedYear === "") return;
            const sort: string = String(bridge.getCurrentSort?.() ?? bridge.getSearchParams?.()?.sort ?? "DESC");

            if (selectedYear === "2010") {
                bridge.runYyMnth?.(selectedYear, 99, sort);
            }

            const mnthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
            if (mnthElement) mnthElement.value = "";
        },
        onMnthChange(): void {
            const bridge = requireListBridge("onMnthChange");
            if (!bridge) return;

            const yearElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
            const selectedYear: string = String(yearElement?.value ?? "");
            const monthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
            const selectedMnth: string = String(monthElement?.value ?? "");
            if (selectedMnth === "") return;
            const sort: string = String(bridge.getCurrentSort?.() ?? bridge.getSearchParams?.()?.sort ?? "DESC");
            bridge.runYyMnth?.(selectedYear, selectedMnth, sort);
        },
        todayMonth(): void {
            const bridge = requireListBridge("todayMonth");
            if (!bridge) return;
            const today: string = cF.date.getCurrDateStr(cF.date.ptnDate);
            const yy: string = today.substring(0, 4);
            const mnth: string = String(parseInt(today.substring(5, 7), 10));
            const searchParams: Record<string, any> = bridge.patchSearchParams?.({ stdrdDt: today }) ?? {};
            bridge.runYyMnth?.(yy, mnth, searchParams.sort);
        },
        todayWeek(): void {
            requireListBridge("todayWeek")?.runNavigateToWeek?.(cF.date.getCurrDateStr(cF.date.ptnDate));
        },
        leftWeek(): void {
            const bridge = requireListBridge("leftWeek");
            const anchorDate: string = String(bridge?.getCurrentAnchorDate?.() ?? "");
            if (anchorDate === "") return;
            const nextDate: string = navigateWeekDate(anchorDate, "prev");
            bridge?.runNavigateToWeek?.(nextDate);
        },
        rightWeek(): void {
            const bridge = requireListBridge("rightWeek");
            const anchorDate: string = String(bridge?.getCurrentAnchorDate?.() ?? "");
            if (anchorDate === "") return;
            const nextDate: string = navigateWeekDate(anchorDate, "next");
            bridge?.runNavigateToWeek?.(nextDate);
        },
        leftMonth(): void {
            const bridge = requireListBridge("leftMonth");
            const currentPeriod = bridge?.getCurrentPeriod?.();
            const yy: string = String(currentPeriod?.yy ?? "");
            const mnth: string = String(currentPeriod?.mnth ?? "");
            if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) return;
            if (yy === "2010" && parseInt(mnth, 10) === 1) return;
            const nextPeriod = shiftMonth(yy, mnth, "prev");
            bridge?.runYyMnth?.(nextPeriod.yy, nextPeriod.mnth);
        },
        rightMonth(): void {
            const bridge = requireListBridge("rightMonth");
            const currentPeriod = bridge?.getCurrentPeriod?.();
            const yy: string = String(currentPeriod?.yy ?? "");
            const mnth: string = String(currentPeriod?.mnth ?? "");
            if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) return;
            if (yy === "2010" && parseInt(mnth, 10) === 1) return;
            const nextPeriod = shiftMonth(yy, mnth, "next");
            bridge?.runYyMnth?.(nextPeriod.yy, nextPeriod.mnth);
        },
        pinpoint(): void {
            requireListBridge("pinpoint")?.pinpointAside?.();
        },
        turnback(): void {
            requireListBridge("turnback")?.turnbackAside?.();
        },
    },
    template: `
    <div class="journal-day-aside-yy-mnth-vue-root">
        <div class="d-flex-between gap-4">
            <div class="col">
                <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">{{ msgYy() }}</span>
                <div class="d-flex">
                    <div class="d-flex align-items-center me-2">
                        <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                           data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click"
                           :title="tooltip('bs.tooltip.journal.aside-prev-mnth')"
                           @click="leftMonth"></i>
                    </div>
                    <select name="yy" id="yy" class="form-select" aria-label="Select example" @change="onYyChange">
                        <option value="">----</option>
                        <option v-for="opt in yyOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                </div>
            </div>
            <div class="col">
                <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-2 mb-2">{{ msgMnth() }}</span>
                <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="tooltip('bs.tooltip.journal.aside-today')"
                        @click="todayMonth">
                    TODAY
                    <i class="bi bi-box-arrow-in-up-right"></i>
                </button>
                <div class="d-flex">
                    <select name="mnth" id="mnth" class="form-select" aria-label="Select example" @change="onMnthChange">
                        <option value="">--</option>
                        <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
                    </select>
                    <div class="d-flex align-items-center ms-2">
                        <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                           data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click"
                           :title="tooltip('bs.tooltip.journal.aside-next-mnth')"
                           @click="rightMonth"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="mt-0">
            <div class="d-flex-between align-items-center pe-3 mb-2">
                <div class="d-flex align-items-center flex-wrap gap-2">
                    <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6">Week</span>
                    <div class="btn btn-sm btn-light-primary py-1 px-3 pe-none fs-8">
                        <span id="journalAsideWeekRange" class="fw-semibold">----</span>
                    </div>
                </div>
                <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="tooltip('bs.tooltip.journal.aside-today')"
                        @click="todayWeek">
                    TODAY
                    <i class="bi bi-box-arrow-in-up-right"></i>
                </button>
            </div>
            <div class="journal-aside-week-nav d-flex align-items-center gap-2">
                <div class="d-flex align-items-center me-2 cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="tooltip('bs.tooltip.journal.aside-prev-week')"
                     @click="leftWeek">
                    <i class="bi bi-caret-left fs-2"></i>
                </div>
                <div id="journalAsideWeekDays" class="journal-aside-week-days flex-grow-1" aria-label="Weekly navigation"></div>
                <div class="d-flex align-items-center me-2 cursor-pointer"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     :title="tooltip('bs.tooltip.journal.aside-next-week')"
                     @click="rightWeek">
                    <i class="bi bi-caret-right fs-2"></i>
                </div>
            </div>
        </div>
        <div class="mt-0">
            <div class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">Pinpoint</div>
            <div class="d-flex align-items-center justify-content-center px-8 mb-4 justify-content-between gap-1">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="tooltip('bs.tooltip.journal.aside-pin')"
                        @click="pinpoint">
                    <i class="bi bi-bookmarks pe-0"></i>
                </button>
                <span class="mx-2">|</span>
                <span id="journal_aside_pinText" class="px-1">
                    <span id="pinnedYy" class="fs-6 text-muted text-underline-dotted">----</span>
                    <span> / </span>
                    <span id="pinnedMnth" class="fs-6 text-muted text-underline-dotted">--</span>
                    <i class="bi bi-pin-map fs-7"></i>
                </span>
                <span class="mx-2">|</span>
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="tooltip('bs.tooltip.journal.aside-turnback')"
                        @click="turnback">
                    <i class="bi bi-reply-all pe-0"></i>
                </button>
            </div>
        </div>
    </div>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.querySelector("#journal_day_aside_yy_mnth_mount") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayAsideYyMnthApp] 마운트 루트 #journal_day_aside_yy_mnth_mount 없음.");
        return;
    }

    try {
        const app = Vue.createApp(JournalDayAsideYyMnthRoot);
        app.mount(mountEl);
    } catch (e) {
        console.error("[JournalDayAsideYyMnthApp] Vue 마운트 실패:", e);
    }
});

export {};
