/**
 * JournalDayEntryTagListApp.ts
 * 저널 일자(monthly/weekly/daily/cal/meta) + 엔트리 검색 페이지의 일기/꿈 태그 헤더 Vue 엔트리.
 *
 * 변경(A-9):
 *   - 변경 전: `journalEntryTagService.renderList` 가 `cF.handlebars.compile(..., "journal_entry_tag_list")`
 *     로 `#journal_diary_tag_list_div` / `#journal_dream_tag_list_div` 안에 sized 태그 마크업을 주입했다.
 *     이후 phase 에서 `journalDayUiBridgeService.syncTagCloud` 가 `listEntryTagAjax` 진입을 끊고
 *     컨테이너에 빨간 안내 박스(하드컷 placeholder)만 그렸다 — 사용자에게 화면이 비어 보이는 상태.
 *   - 변경 후: 본 ESM 이 두 컨테이너 위에 Vue 앱을 마운트한다. 동일 페이로드를
 *     `window.JournalDayEntryTagListVueApp.setList(kind, list, config)` 브리지로 받아
 *     `_tag_list_sized_partial.hbs` 와 동등한 sized 태그 행을 렌더한다(UI/DOM/onclick 1:1).
 *   - 부트 순서: `journalEntryTagService` 적재 이후·페이지 부트(monthly/weekly/daily/cal/meta/entry_search)
 *     이전에 적재해 Ajax 콜백 전 브리지가 존재하도록 한다.
 *
 * 보존 규칙:
 *   - 마크업: `<span class="py-2 me-3 cursor-pointer opacity-hover" ... onclick="{{module}}.select({id},'{tagNm}','{ctgr}')">`
 *     의 외곽 클래스, 내부 `tagClass`/`textClass`/`em_` prefix, `contentSize` 까지 1:1 보존.
 *   - module 문자열 결의: `dF.JournalEntryTag.get('JOURNAL_DIARY')` 등 표현식을 `new Function("return (" + expr + ");")`
 *     로 결의(레거시 onclick 의 동적 module 와 동일).
 *   - 외곽 row(컨테이너 div, hide/openAll 버튼) 는 `_journal_day_tag_header.ftlh` 가 그대로 보유 — 본 컴포넌트는
 *     컨테이너 div **안쪽** sized 태그 v-for 부분만 채운다(결산의 EntryTagListApp 과 다른 점: 결산은 외곽 row 까지 합성).
 *
 * @author nichefish
 */

type EntryTagKind = "DIARY" | "DREAM";

interface EntryTagRowConfig {
    /** 레거시 onclick 의 module 문자열(예: `dF.JournalEntryTag.get('JOURNAL_DIARY')`). */
    module?: string;
}

interface EntryTagRowState {
    list: Record<string, any>[];
    config: EntryTagRowConfig;
}

type EntryTagListVueBridge = {
    mounted?: boolean;
    pendingByType?: Partial<Record<EntryTagKind, { list: Record<string, any>[]; config: EntryTagRowConfig }>>;
    setList?: (kind: EntryTagKind, list: Record<string, any>[], config: EntryTagRowConfig) => void;
};

const TARGET_IDS: Record<EntryTagKind, string> = {
    DIARY: "journal_diary_tag_list_div",
    DREAM: "journal_dream_tag_list_div",
};

/**
 * 변경(F-1): 반응 저장소를 `Vue.reactive(...)` 로 감싼다.
 * 변경 전(A-9): plain 객체로 stateMap 을 두고 closure 로 캡처해 `data()` 에서 `slot` 직접 노출했다.
 *               이 경우 외부 `applyList` 가 raw 참조에 mutation 하면 Vue 의 reactivity proxy 를 거치지 않아
 *               의존성 트래킹/재렌더가 트리거되지 않는다 — 결과적으로 ajax 응답이 와도 `<template v-else>-</template>`
 *               만 영구 표시되어 일기/꿈 태그 클라우드가 화면에 안 뜨는 결함이 있었다.
 * 변경 후: `JournalAnnualEntryTagListApp` 의 `tagRowStore = Vue.reactive(...)` 패턴과 통일한다.
 *          외부 mutation(`stateMap[kind].list = ...`) 도 Vue 의 reactive setter 를 거쳐 의존성 추적 트리거.
 */
const stateMap: Record<EntryTagKind, EntryTagRowState> = Vue.reactive({
    DIARY: { list: [], config: {} } as EntryTagRowState,
    DREAM: { list: [], config: {} } as EntryTagRowState,
}) as Record<EntryTagKind, EntryTagRowState>;

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

/**
 * 변경 전: Handlebars `tag_list_sized_partial` 의 `onclick="{{module}}.select({{id}}, '{{tagNm}}', '{{ctgr}}');"` 와 동등.
 * module 문자열은 entry meta 의 `tagModuleExpr` (예: `dF.JournalEntryTag.get('JOURNAL_DIARY')`).
 */
function resolveTagModuleForSelect(moduleExpr: string): { select?: (id: any, tagNm?: string, ctgr?: string) => void } | undefined {
    if (cF.util.isEmpty(moduleExpr)) return undefined;
    try {
        const runner = new Function("return (" + moduleExpr + ");");
        return runner.call(window);
    } catch (e) {
        console.error("[JournalDayEntryTagListApp] tag module resolve 실패:", moduleExpr, e);
        return undefined;
    }
}

function reinitContainerTooltips(targetId: string): void {
    Vue.nextTick(function(): void {
        const root = document.getElementById(targetId);
        if (!root) return;
        const bsTooltip = (window as any).bootstrap?.Tooltip;
        if (!bsTooltip) return;
        root.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            const htmlEl = el as HTMLElement;
            const existing = bsTooltip.getInstance?.(htmlEl);
            if (existing) existing.dispose();
            new bsTooltip(htmlEl);
        });
    });
}

function applyList(kind: EntryTagKind, list: Record<string, any>[], config: EntryTagRowConfig): void {
    const slot = stateMap[kind];
    slot.list = Array.isArray(list) ? list : [];
    slot.config = config ?? slot.config;
    reinitContainerTooltips(TARGET_IDS[kind]);
}

/**
 * 변경 전: 매 렌더에서 `t('view.tag.content-list')` 메서드를 호출해 `Message.get(key)` 평가.
 *          이 경로는 ESM 스코프의 `Message` 식별자 결의에 의존했고, 일부 환경에서 undefined 로 결의되어
 *          `Cannot read properties of undefined (reading 'get')` 가 first render 시 터졌다.
 * 변경 후(A-9 hotfix): `window.Message` 를 직접 결의하고, 결의 결과를 `data()` 에서 한 번만 캐시해 재호출/재평가 비용 0.
 *                       `Message`/`Message.get` 미정의 환경에서는 빈 문자열로 폴백(타이틀만 비워질 뿐 렌더는 안전).
 */
function resolveTooltipTitle(): string {
    const w = window as any;
    const messageNs = w.Message;
    if (messageNs && typeof messageNs.get === "function") {
        return String(messageNs.get("view.tag.content-list") ?? "");
    }
    console.warn("[JournalDayEntryTagListApp] window.Message.get 결의 실패 — 빈 title 로 폴백.");
    return "";
}

/**
 * 변경(F-1): annual 패턴(`JournalAnnualEntryTagListApp.createTagRowRoot`)과 통일.
 * 변경 전(A-9): closure 로 `const slot = stateMap[kind]` 캡처 후 `data()` 에서 그대로 노출.
 *                stateMap 이 plain 객체이던 시기에는 reactive 가 아예 동작하지 않았고(F-1 핵심 결함),
 *                Vue.reactive 화 후에도 closure 캡처는 reactive proxy 를 잡긴 하지만 의존성 추적 시점이
 *                불투명해 의도가 코드에서 잘 드러나지 않는다.
 * 변경 후: `data()` 에는 `kind` 만 두고 `computed.slot` 으로 stateMap 을 직접 참조 — annual 과 동일 패턴.
 *          `slot.list` / `slot.config` 의 변화 추적이 명시적이고 컴포넌트 인스턴스 단위로 격리된다.
 */
function createRootComponent(kind: EntryTagKind): Record<string, unknown> {
    return {
        name: "JournalDayEntryTagListRoot",
        data(): { kind: EntryTagKind; tooltipTitle: string } {
            return { kind, tooltipTitle: resolveTooltipTitle() };
        },
        computed: {
            slot(): EntryTagRowState {
                return stateMap[this.kind as EntryTagKind];
            },
            hasList(): boolean {
                return Array.isArray(this.slot.list) && this.slot.list.length > 0;
            },
            moduleExpr(): string {
                return String(this.slot.config?.module ?? "");
            },
        },
        methods: {
            /** 변경 전: `tag_list_sized_partial` onclick 의 module 결의 select. */
            selectSizedTag(tag: Record<string, any>): void {
                const mod = resolveTagModuleForSelect(this.moduleExpr);
                mod?.select?.(tag.id, tag.tagNm, tag.ctgr);
            },
        },
        template: `
        <template v-if="hasList">
            <span
                v-for="tag in slot.list"
                :key="'entry-sized-tg-' + String(tag.id) + '-' + String(tag.tagNm)"
                class="py-2 me-3 cursor-pointer opacity-hover"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="tooltipTitle"
                @click="selectSizedTag(tag)"
            >
                <span :class="[tag.tagClass, tag.textClass]">
                    <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                    <span :class="'em_' + tag.tagNm">{{ tag.tagNm }}</span>
                </span>
                <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">{{ tag.contentSize }}</span>
            </span>
        </template>
        <template v-else>-</template>
        `,
    };
}

runWhenDomReady(function(): void {
    const priorBridge = ((window as any).JournalDayEntryTagListVueApp ?? {}) as EntryTagListVueBridge;
    const pendingMap: Partial<Record<EntryTagKind, { list: Record<string, any>[]; config: EntryTagRowConfig }>> = {
        ...(priorBridge.pendingByType ?? {}),
    };

    (["DIARY", "DREAM"] as EntryTagKind[]).forEach(function(kind: EntryTagKind): void {
        const mountEl = document.getElementById(TARGET_IDS[kind]);
        if (!mountEl) {
            /* 본 페이지에서 해당 컨테이너가 없는 경우(설계상 없을 수 있음): 조용히 skip — 결산/엔트리 검색 등 페이지마다 다르다. */
            return;
        }
        const app = Vue.createApp(createRootComponent(kind));
        app.mount(mountEl);
    });

    (window as any).JournalDayEntryTagListVueApp = {
        mounted: true,
        pendingByType: null as Partial<Record<EntryTagKind, { list: Record<string, any>[]; config: EntryTagRowConfig }>> | null,
        setList: function(kind: EntryTagKind, list: Record<string, any>[], config: EntryTagRowConfig): void {
            applyList(kind, list, config ?? {});
        },
    };

    (["DIARY", "DREAM"] as EntryTagKind[]).forEach(function(kind: EntryTagKind): void {
        const pay = pendingMap[kind];
        if (pay != null) applyList(kind, pay.list, pay.config ?? {});
    });
});

export {};
