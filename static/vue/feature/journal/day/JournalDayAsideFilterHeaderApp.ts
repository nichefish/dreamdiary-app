/**
 * JournalDayAsideFilterHeaderApp.ts
 * 저널 일자 사이드바 — 필터 카드 헤더(제목·정렬 토글) Vue 렌더.
 *
 * 변경 전: _journal_day_aside_base.ftlh 의 #journal_aside_header 정적 마크업 + onclick.
 * 변경 후: 동일 id/클래스·아이콘 id(#sortIcon) 유지 — 정렬 클릭은 JournalDay*App.sortAside 브리지 직접 호출.
 *         사용자 인터랙션은 Vue @click 으로만 위임 동작 재현(외부 레이아웃·토글 없음).
 */

// 변경(D): 글로벌 `Message` 결의를 `resolveMessage` 헬퍼로 위임 — typeof guard 분기 통일.
import { resolveMessage } from "../../../common/messageHelper.js";

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function asideMsg(key: string): string {
    return resolveMessage(key);
}
/** @keepInSync static/js/view/feature/journal/day/journalDayListBridge.ts */
const journalDayResolveListBridge = (): JournalDayListAppBridge | undefined =>
    window.JournalDayMonthlyApp ?? window.JournalDayWeeklyApp ?? window.JournalDayDailyApp;

function initAsideFilterHeaderTooltips(root: HTMLElement): void {
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
                console.warn("[JournalDayAsideFilterHeaderApp] tooltip init 실패:", e);
            }
        });
    });
}

const JournalDayAsideFilterHeaderRoot = {
    name: "JournalDayAsideFilterHeaderRoot",
    mounted(): void {
        const root = this.$el as HTMLElement;
        if (root) initAsideFilterHeaderTooltips(root);
        window.JournalDayAsideFilterHeaderVueApp = { mounted: true };
    },
    methods: {
        msgFilterTitle(): string {
            return asideMsg("txt.journal.day.filter");
        },
        tooltipSort(): string {
            return asideMsg("bs.tooltip.journal.aside-sort");
        },
        sortAside(): void {
            const bridge = journalDayResolveListBridge();
            if (bridge?.mounted === true && typeof bridge.sortAside === "function") {
                bridge.sortAside();
                return;
            }
            console.error("[JournalDayAsideFilterHeaderApp] sortAside 호출 불가 — JournalDay*App.sortAside 브리지 미등록");
        },
    },
    template: `
    <div id="journal_aside_header" class="card-header min-h-auto mb-5">
        <h3 class="card-title text-gray-900 fw-bold fs-3">
            <i class="bi bi-filter fs-2 me-1"></i> {{ msgFilterTitle() }}
        </h3>
        <div class="card-toolbar">
            <a href="javascript:void(0);" class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
               :title="tooltipSort()"
               @click.prevent="sortAside">
                <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
            </a>
        </div>
    </div>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.querySelector("#journal_day_aside_filter_header_mount") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayAsideFilterHeaderApp] 마운트 루트 #journal_day_aside_filter_header_mount 없음.");
        return;
    }

    try {
        const app = Vue.createApp(JournalDayAsideFilterHeaderRoot);
        app.mount(mountEl);
    } catch (e) {
        console.error("[JournalDayAsideFilterHeaderApp] Vue 마운트 실패:", e);
    }
});

export {};
