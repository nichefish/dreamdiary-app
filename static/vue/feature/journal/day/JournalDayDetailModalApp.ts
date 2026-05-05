/**
 * JournalDayDetailModalApp.ts
 * 저널 일자 상세 모달(`journal_day_dtl`) 본문 렌더 — Handlebars 템플릿 대체.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalDayDetailModalBody from "./components/JournalDayDetailModalBody.js";

type JournalDayDetailVueBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    pendingPayload?: Record<string, any> | null;
};

const state = Vue.reactive({
    model: null as Record<string, any> | null,
});

const i18n = createScopedI18n();

function t(key: string): string {
    return i18n.t(key);
}

function resolveJournalDayLocale(): string {
    const win = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const locale = win?.Model?.locale;
    if (locale) return locale;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

function showModal(): void {
    const modalEl = document.querySelector("#journal_day_dtl_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalDayDetailModalApp] Modal root #journal_day_dtl_modal not found.");
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

function openDetail(model: Record<string, any>): void {
    state.model = model;
    Vue.nextTick(function(): void {
        journalDayUiBridgeService.initRenderedDom("journal_day_dtl_div");
        if (typeof KTMenu !== "undefined" && typeof KTMenu.createInstances === "function") {
            KTMenu.createInstances();
        }
    });
    showModal();
}

const JournalDayDetailRootApp = {
    name: "JournalDayDetailRootApp",
    components: {
        JournalDayDetailModalBody,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_day_dtl_div">
        <JournalDayDetailModalBody v-if="state.model" :model="state.model" />
    </teleport>
    `,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(async function(): Promise<void> {
    const mountEl = document.querySelector("#journal_day_dtl_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayDetailModalApp] Mount root #journal_day_dtl_vue_app not found.");
        return;
    }

    await i18n.load(resolveJournalDayLocale());

    const priorBridge = (window.JournalDayDetailVueApp ?? {}) as JournalDayDetailVueBridge;
    const pendingPayload: Record<string, any> | null | undefined = priorBridge.pendingPayload;

    const app = Vue.createApp(JournalDayDetailRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_day_dtl_vue_app");

    window.JournalDayDetailVueApp = {
        mounted: true,
        pendingPayload: null,
        open: openDetail,
    };

    if (pendingPayload && typeof pendingPayload === "object") {
        openDetail(pendingPayload);
    }
});
