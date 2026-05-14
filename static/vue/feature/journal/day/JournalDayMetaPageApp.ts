/**
 * JournalDayMetaPageApp.ts
 * 메타 뷰 헤더 목록(`journal_day_meta_list`) + 설정 스트립(`journal_day_meta_config`) Vue 이전.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import journalDaySearchStateService from "./services/journalDaySearchStateService.js";
import journalDayMetaService from "./services/journalDayMetaService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalDayMetaHeaderList from "./components/JournalDayMetaHeaderList.js";
import JournalDayMetaConfigStrip from "./components/JournalDayMetaConfigStrip.js";
import journalTodoCrudService from "../todo/services/journalTodoCrudService.js";

type JournalDayMetaPageVueBridge = {
    mounted?: boolean;
    setMetaList?: (list: Record<string, any>[]) => void;
    setSelectedConfig?: (obj: Record<string, any> | null) => void;
    pendingList?: Record<string, any>[] | null;
    pendingConfig?: Record<string, any> | null;
};

const state = Vue.reactive({
    items: [] as Record<string, any>[],
    selected: null as Record<string, any> | null,
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

function refreshMetaPageTooltips(): void {
    Vue.nextTick(function(): void {
        journalDayUiBridgeService.initRenderedDom("journal_day_meta_list_div");
        journalDayUiBridgeService.initRenderedDom("journal_day_meta_config_div");
    });
}

function setMetaList(list: Record<string, any>[]): void {
    state.items = Array.isArray(list) ? list : [];
    refreshMetaPageTooltips();
}

function setSelectedConfig(obj: Record<string, any> | null): void {
    state.selected = obj && typeof obj === "object" ? obj : null;
    refreshMetaPageTooltips();
}

function initializeMetaPage(): void {
    /* 변경(Phase 17): bootstrap_service 제거. 변경(통합): Tag/Meta/delegation → bootstrapDfJournalDayShell. */
    dF.JournalDayRuntimeService.bootstrapDfJournalDayShell();
    journalDaySearchStateService.initFromUrl();
    journalDaySearchStateService.syncSearchFilterDomFromParams();
    void dF.JournalEntry.initAll("CAL");
    /* 변경(T-2-β): dF.JournalTodo.init() → journalTodoCrudService.yyMnthListAjax() 단일 진입.
     * 기존 init 의 initialized 플래그는 페이지 진입 시점 1회 호출 보장으로 자연 소멸. */
    journalTodoCrudService.yyMnthListAjax();
    window.addEventListener("comment:modal-refresh", function(): void {
        journalDayMetaService.listMetaHeaders();
    });
    dF.State.init();

    dF.JournalDayRuntimeService.initJournalDayAsideShell();
    journalDayMetaService.listMetaHeaders();
}

const JournalDayMetaPageRootApp = {
    name: "JournalDayMetaPageRootApp",
    components: {
        JournalDayMetaHeaderList,
        JournalDayMetaConfigStrip,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_day_meta_list_div">
        <JournalDayMetaHeaderList :items="state.items" />
    </teleport>
    <teleport to="#journal_day_meta_config_div">
        <JournalDayMetaConfigStrip :meta="state.selected" />
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
    const mountEl = document.querySelector("#journal_day_meta_page_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayMetaPageApp] Mount root #journal_day_meta_page_vue_app not found.");
        return;
    }

    await i18n.load(resolveJournalDayLocale());

    const prior = (window.JournalDayMetaPageVueApp ?? {}) as JournalDayMetaPageVueBridge;

    const app = Vue.createApp(JournalDayMetaPageRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_day_meta_page_vue_app");

    window.JournalDayMetaPageVueApp = {
        mounted: true,
        pendingList: null,
        pendingConfig: null,
        setMetaList,
        setSelectedConfig,
    };

    if (Array.isArray(prior.pendingList)) {
        setMetaList(prior.pendingList);
    }
    if (prior.pendingConfig != null && typeof prior.pendingConfig === "object") {
        setSelectedConfig(prior.pendingConfig as Record<string, any>);
    }
    initializeMetaPage();
});
