/**
 * JournalAnnualEntryListApp.ts
 * 저널 결산(annual) 상세 — DIARY/DREAM 엔트리 리스트 Vue 엔트리.
 *
 * 변경(A-7-γ):
 *   - 변경 전: `journalAnnualStateService.renderEntryList` 가 `cF.handlebars.compile` 로
 *     `journal_annual_entry_list_template` 를 `#journal_annual_diary_list_div` /
 *     `#journal_annual_imprtc_dream_list_div` 에 주입했다.
 *   - 변경 후: 본 ESM 이 두 컨테이너 위에 Vue 앱을 마운트한다. 동일 페이로드를
 *     `window.JournalAnnualEntryListVueApp.setList(kind, list, config)` 브리지로 받아
 *     `<JournalAnnualEntryItem>` 로 1행씩 렌더한다.
 *   - DOM/CSS/onclick 시그니처는 레거시 partial 1:1 (`JournalAnnualEntryItem` 내부 보존).
 *   - 빈 목록일 때 표시: `view.list.empty` 안내(레거시 each else 블록과 동등).
 *   - 부트 순서: `journalAnnualService.js` 이후·`JournalAnnualDetailPageBoot.js` 이전 — Ajax 콜백 전 브리지 확보.
 *
 * @author nichefish
 */

import JournalAnnualEntryItem from "./components/JournalAnnualEntryItem.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
import { resolveMessage } from "../../../common/messageHelper.js";

type EntryListKind = "DIARY" | "DREAM";

interface AnnualEntryListConfig {
    contentType: string;
    cssPrefix: string;
    contentLabel?: string;
    contentPaddingClass?: string;
    contextFirst?: boolean;
    highlightImportant?: boolean;
    showDreamStates?: boolean;
    emptyLabel?: string;
}

interface AnnualEntryListState {
    list: Record<string, any>[];
    config: AnnualEntryListConfig | null;
}

type EntryListVueBridge = {
    mounted?: boolean;
    pendingByType?: Partial<Record<EntryListKind, { list: Record<string, any>[]; config: AnnualEntryListConfig }>>;
    setList?: (kind: EntryListKind, list: Record<string, any>[], config: AnnualEntryListConfig) => void;
};

const TARGET_IDS: Record<EntryListKind, string> = {
    DIARY: "journal_annual_diary_list_div",
    DREAM: "journal_annual_imprtc_dream_list_div",
};

const stateMap: Record<EntryListKind, AnnualEntryListState> = {
    DIARY: { list: [], config: null },
    DREAM: { list: [], config: null },
};

function reinitDomDecorations(targetId: string): void {
    Vue.nextTick(function(): void {
        const target = document.getElementById(targetId);
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

function applyList(kind: EntryListKind, list: Record<string, any>[], config: AnnualEntryListConfig): void {
    const slot = stateMap[kind];
    slot.list = Array.isArray(list) ? list : [];
    slot.config = config ?? slot.config;
    reinitDomDecorations(TARGET_IDS[kind]);
}

function createRootComponent(kind: EntryListKind): Record<string, unknown> {
    const slot: AnnualEntryListState = stateMap[kind];
    return {
        name: "JournalAnnualEntryListRoot",
        components: { JournalAnnualEntryItem },
        data(): { slot: AnnualEntryListState } {
            return { slot };
        },
        computed: {
            emptyLabel(): string {
                return String(this.slot.config?.emptyLabel ?? "");
            },
        },
        methods: {
            t(key: string): string {
                return resolveMessage(key);
            },
        },
        template: `
        <template v-if="!Array.isArray(slot.list) || slot.list.length === 0">
            <div class="journal-day d-flex-center">
                {{ emptyLabel }} {{ t('view.list.empty') }}
            </div>
        </template>
        <template v-else>
            <JournalAnnualEntryItem
                v-for="entry in slot.list"
                :key="'annual-entry-' + (entry.id != null ? entry.id : entry.stdrdDt)"
                :entry="entry"
                :config="slot.config"
            />
        </template>
        `,
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
    const priorBridge = ((window as any).JournalAnnualEntryListVueApp ?? {}) as EntryListVueBridge;
    const pendingMap: Partial<Record<EntryListKind, { list: Record<string, any>[]; config: AnnualEntryListConfig }>> = {
        ...(priorBridge.pendingByType ?? {}),
    };

    (["DIARY", "DREAM"] as EntryListKind[]).forEach(function(kind: EntryListKind): void {
        const mountEl = document.getElementById(TARGET_IDS[kind]);
        if (!mountEl) {
            console.error("[JournalAnnualEntryListApp] mount root #" + TARGET_IDS[kind] + " 없음.");
            return;
        }
        const app = Vue.createApp(createRootComponent(kind));
        app.mount(mountEl);
    });

    (window as any).JournalAnnualEntryListVueApp = {
        mounted: true,
        pendingByType: null as Partial<Record<EntryListKind, { list: Record<string, any>[]; config: AnnualEntryListConfig }>> | null,
        setList: function(kind: EntryListKind, list: Record<string, any>[], config: AnnualEntryListConfig): void {
            applyList(kind, list, config);
        },
    };

    (["DIARY", "DREAM"] as EntryListKind[]).forEach(function(kind: EntryListKind): void {
        const pay = pendingMap[kind];
        if (pay != null) applyList(kind, pay.list, pay.config);
    });
});

export {};
