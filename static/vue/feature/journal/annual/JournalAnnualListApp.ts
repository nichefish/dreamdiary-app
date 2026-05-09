/**
 * JournalAnnualListApp.ts
 * 저널 결산(annual) 목록 페이지 Vue 엔트리.
 *
 * 변경(A-5-α):
 *   - 변경 전: `journal_annual_list.ts` 의 Page IIFE 가 `dF.JournalAnnual.init()` + `listAjax()` 진입,
 *     `cF.handlebars.template(rsltList, "journal_annual_list")` 가 `<div id="journal_annual_list_div">`
 *     에 HBS 카드 마크업을 직접 주입했다(KTMenu.createInstances + tooltip 부착).
 *   - 변경 후: 본 ES module 이 `#journal_annual_list_div` 위에 Vue 앱을 마운트한다.
 *     `journalAnnualCrudService.listAjax` 는 `cF.handlebars.template` 대신 본 앱의 `setList(rsltList)`
 *     브리지를 호출한다. Vue 가 `<JournalAnnualListItem>` 로 카드를 렌더하고, 다음 nextTick 에서
 *     `KTMenu.createInstances()` + tooltip 재초기화를 수행한다(기존과 동등).
 *   - 부트 시 `dF.JournalAnnual.init() + listAjax()` 를 직접 호출한다(레거시 page script 흡수).
 *
 * @author nichefish
 */

import JournalAnnualListItem from "./components/JournalAnnualListItem.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
import { resolveMessage } from "../../../common/messageHelper.js";

type JournalAnnualListVueBridge = {
    mounted?: boolean;
    pendingList?: Record<string, any>[] | null;
    setList?: (list: Record<string, any>[]) => void;
};

const VUE_MOUNT_ID = "journal_annual_list_div";

const state: { list: Record<string, any>[] } = { list: [] };
let setListHandler: ((list: Record<string, any>[]) => void) | null = null;

function reinitDomDecorations(): void {
    Vue.nextTick(function(): void {
        const target = document.getElementById(VUE_MOUNT_ID);
        if (!target) return;

        const bsTooltip = (window as any).bootstrap?.Tooltip;
        target.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            if (!bsTooltip) return;
            const htmlEl = el as HTMLElement;
            const existing = bsTooltip.getInstance?.(htmlEl);
            if (existing) existing.dispose();
            new bsTooltip(htmlEl);
        });
        if (typeof KTMenu !== "undefined" && typeof (KTMenu as any).createInstances === "function") {
            (KTMenu as any).createInstances();
        }
    });
}

function applyList(list: Record<string, any>[]): void {
    state.list = Array.isArray(list) ? list : [];
    reinitDomDecorations();
}

function createRootComponent(): Record<string, unknown> {
    return {
        name: "JournalAnnualListRoot",
        components: { JournalAnnualListItem },
        data(): { state: typeof state } {
            return { state };
        },
        template: `
        <template v-if="state.list.length === 0">
            <div class="journal-day d-flex-center">
                {{ messageGet('msg.rslt.empty') }}
            </div>
        </template>
        <template v-else>
            <JournalAnnualListItem
                v-for="annual in state.list"
                :key="'annual-' + (annual.id != null ? annual.id : annual.yy)"
                :annual="annual"
            />
        </template>
        `,
        methods: {
            messageGet(key: string): string {
                return resolveMessage(key);
            },
        },
    };
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    const priorBridge = ((window as any).JournalAnnualListVueApp ?? {}) as JournalAnnualListVueBridge;
    const pending = priorBridge.pendingList ?? null;

    const mountEl = document.getElementById(VUE_MOUNT_ID) as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalAnnualListApp] mount element not found:", VUE_MOUNT_ID);
        return;
    }

    state.list = [];
    setListHandler = function(list: Record<string, any>[]): void {
        applyList(list);
    };

    const app = Vue.createApp(createRootComponent());
    app.mount("#" + VUE_MOUNT_ID);

    (window as any).JournalAnnualListVueApp = {
        mounted: true,
        pendingList: null,
        setList: function(list: Record<string, any>[]): void {
            if (typeof setListHandler === "function") {
                setListHandler(list);
                return;
            }
            const b = (window as any).JournalAnnualListVueApp as JournalAnnualListVueBridge;
            b.pendingList = list;
            console.log("[JournalAnnualListApp] pending list queued.");
        },
    };

    /* 변경(A-5-α): 페이지 부트 진입 — 레거시 `journal_annual_list.ts` Page IIFE 동등 동작.
     * dF.JournalAnnual 표면은 동일 가드의 ES module 묶음(_journal_annual_reg_modal.ftlh) 에서 등록된다. */
    const ns = (window as any).dF?.JournalAnnual;
    if (ns?.init) ns.init();
    if (ns?.listAjax) ns.listAjax();

    /* 부트 이전에 큐잉된 list 가 있으면 처리한다(통상 listAjax 가 위에서 수행하지만 외부 큐잉 호환). */
    if (Array.isArray(pending)) {
        applyList(pending);
    }
});

export {};
