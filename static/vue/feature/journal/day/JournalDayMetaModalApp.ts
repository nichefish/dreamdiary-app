/**
 * JournalDayMetaModalApp.ts
 * 저널 메타 조회 모달(`journal_day_meta`) 본문 렌더 — Handlebars 템플릿 대체.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalDayMetaModalBody, { type JournalDayMetaModalPayload } from "./components/JournalDayMetaModalBody.js";

type JournalDayMetaVueBridge = {
    mounted?: boolean;
    open?: (payload: JournalDayMetaModalPayload) => void;
    pendingPayload?: JournalDayMetaModalPayload | null;
};

const state = Vue.reactive({
    payload: null as JournalDayMetaModalPayload | null,
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

function showMetaModal(): void {
    const modalEl = document.querySelector("#journal_day_meta_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalDayMetaModalApp] Modal root #journal_day_meta_modal not found.");
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

function openMetaModal(payload: JournalDayMetaModalPayload): void {
    state.payload = payload;
    Vue.nextTick(function(): void {
        journalDayUiBridgeService.initRenderedDom("journal_day_meta_div");
        if (typeof KTMenu !== "undefined" && typeof KTMenu.createInstances === "function") {
            KTMenu.createInstances();
        }
        showMetaModal();
    });
}

const JournalDayMetaModalRootApp = {
    name: "JournalDayMetaModalRootApp",
    components: {
        JournalDayMetaModalBody,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_day_meta_div">
        <JournalDayMetaModalBody v-if="state.payload" :payload="state.payload" />
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
    const mountEl = document.querySelector("#journal_day_meta_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayMetaModalApp] Mount root #journal_day_meta_vue_app not found.");
        return;
    }

    await i18n.load(resolveJournalDayLocale());

    const priorBridge = (window.JournalDayMetaVueApp ?? {}) as JournalDayMetaVueBridge;
    const pendingPayload: JournalDayMetaModalPayload | null | undefined = priorBridge.pendingPayload;

    const app = Vue.createApp(JournalDayMetaModalRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_day_meta_vue_app");

    window.JournalDayMetaVueApp = {
        mounted: true,
        pendingPayload: null,
        open: openMetaModal,
    };

    if (pendingPayload && typeof pendingPayload === "object") {
        openMetaModal(pendingPayload);
    }
});
