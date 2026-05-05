/**
 * JournalDayTagDetailModalApp.ts
 * 저널 일자 태그 상세 모달 Vue 렌더러 브리지.
 */

import JournalDayList from "./components/JournalDayList.js";
import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";

type TagDetailYearOption = {
    value: string;
    label: string;
    selected?: boolean;
};

type TagDetailPayload = {
    tagId: string | number;
    tagNm: string;
    yy: string;
    yearOptions: TagDetailYearOption[];
    list: Record<string, any>[];
    weekMode?: boolean;
};

type TagDetailBridge = {
    mounted?: boolean;
    open?: (payload: TagDetailPayload) => void;
    pendingPayload?: TagDetailPayload | null;
};

const state = Vue.reactive({
    tagId: "" as string | number,
    tagNm: "",
    yy: "",
    yearOptions: [] as TagDetailYearOption[],
    list: [] as Record<string, any>[],
    weekMode: false,
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveNormalizedYy(payload: TagDetailPayload): string {
    const selected = payload.yearOptions.find((item: TagDetailYearOption): boolean => item.selected === true);
    if (selected) return String(selected.value);
    if (payload.yy) return String(payload.yy);
    return String(payload.yearOptions[0]?.value ?? "");
}

function applyPayload(payload: TagDetailPayload): void {
    state.tagId = payload.tagId;
    state.tagNm = String(payload.tagNm ?? "");
    state.yearOptions = Array.isArray(payload.yearOptions) ? payload.yearOptions : [];
    state.yy = resolveNormalizedYy(payload);
    state.list = Array.isArray(payload.list) ? payload.list : [];
    state.weekMode = payload.weekMode === true;
}

function showModal(): void {
    const modalEl = document.querySelector("#journal_day_tag_dtl_modal") as HTMLElement | null;
    if (!modalEl) return;
    (window as any).bootstrap.Modal.getOrCreateInstance(modalEl).show();
}

const JournalDayTagDetailRootApp = {
    name: "JournalDayTagDetailRootApp",
    components: {
        JournalDayList,
    },
    data(): { state: typeof state } {
        return { state };
    },
    methods: {
        changeYear(evt: Event): void {
            const target = evt.target as HTMLSelectElement | null;
            if (!target) return;
            const yy = target.value;
            journalDayUiBridgeService.openDayTagDetail(state.tagId, state.tagNm, yy);
        },
    },
    template: `
    <teleport to="#journal_day_tag_dtl_modal .header_tag_nm">
        <span>{{ state.tagNm }}</span>
    </teleport>
    <teleport to="#journal_day_tag_dtl_modal .header_tag_cnt">
        <span>{{ state.list.length }}</span>
    </teleport>
    <teleport to="#journal_day_tag_yy">
        <option
            v-for="option in state.yearOptions"
            :key="String(option.value)"
            :value="String(option.value)"
            :selected="String(option.value) === String(state.yy)"
        >{{ option.label }}</option>
    </teleport>
    <teleport to="#journal_day_tag_dtl_list">
        <JournalDayList :model="{ list: state.list, showDiaries: true, showDreams: true }" />
    </teleport>
    `,
    mounted(): void {
        const yearSelectEl = document.querySelector("#journal_day_tag_yy") as HTMLSelectElement | null;
        yearSelectEl?.addEventListener("change", this.changeYear);
    },
};

runWhenDomReady(function(): void {
    const appRoot = document.querySelector("#journal_day_tag_dtl_app") as HTMLElement | null;
    if (!appRoot) {
        console.error("[JournalDayTagDetailModalApp] Vue mount root not found.");
        return;
    }

    const queuedBridge = window.JournalDayTagDetailVueApp as TagDetailBridge | undefined;
    const app = Vue.createApp(JournalDayTagDetailRootApp);
    app.mount("#journal_day_tag_dtl_app");

    window.JournalDayTagDetailVueApp = {
        mounted: true,
        open: function(payload: TagDetailPayload): void {
            applyPayload(payload);
            showModal();
            KTMenu.createInstances();
        },
    };

    if (queuedBridge?.pendingPayload) {
        window.JournalDayTagDetailVueApp.open?.(queuedBridge.pendingPayload);
    }
});
