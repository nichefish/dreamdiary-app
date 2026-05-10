/**
 * JournalAnnualEntryTagListApp.ts
 * 저널 결산 상세 — DAY/DIARY/DREAM 태그 헤더 행 Vue 엔트리.
 *
 * 변경(A-7-δ):
 *   - 변경 전: `journalAnnualStateService.renderTagList` 가 `cF.handlebars.compile` 로
 *     `journal_annual_entry_tag_list_template` + `tag_list_sized_partial` 을 `#journal_annual_*_tag_list_div` 에 주입.
 *   - 변경 후: 동일 페이로드를 `window.JournalAnnualEntryTagListVueApp.applyTagRow(kind, payload)` 브리지로 반영한다.
 *     단일 행 마크업은 `_journal_annual_entry_tag_list_template.hbs`·`tag_list_sized_partial.hbs` 와 동등(UI 변경 0).
 *   - 부트 순서: `journalAnnualService.js` 이후·`JournalAnnualDetailPageBoot.js` 이전에 적재하여 Ajax 콜백 전 브리지 확보.
 *
 * 태그 클릭: partial 과 동일하게 `module` 문자열 식(`dF.JournalDayTagService` 등)을 런타임 해석해 `select(id, name, ctgr)` 호출.
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../common/messageHelper.js";

type TagRowKind = "DAY" | "DIARY" | "DREAM";

interface TagRowModel {
    list: Record<string, any>[];
    label: string;
    module: string;
    tagListDivId: string;
}

type EntryTagListVueBridge = {
    mounted?: boolean;
    pendingByType?: Partial<Record<TagRowKind, Record<string, any>>>;
    applyTagRow?: (kind: TagRowKind, payload: Record<string, any>) => void;
};

const TARGET_IDS: Record<TagRowKind, string> = {
    DAY: "journal_annual_day_tag_list_div",
    DIARY: "journal_annual_diary_tag_list_div",
    DREAM: "journal_annual_dream_tag_list_div",
};

/** 반응 저장소 — 세 행이 동일 객체를 참조한다. */
const tagRowStore = Vue.reactive({
    DAY: { list: [], label: "", module: "", tagListDivId: "" } as TagRowModel,
    DIARY: { list: [], label: "", module: "", tagListDivId: "" } as TagRowModel,
    DREAM: { list: [], label: "", module: "", tagListDivId: "" } as TagRowModel,
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

/**
 * 변경 전: Handlebars `tag_list_sized_partial` 의 `onclick="{{module}}.select({{id}}, '{{name}}', '{{ctgr}}');"` 에 대응.
 * module 문자열은 서버·메타에서 오는 표현식(`dF.JournalDayTagService`, `dF.JournalEntryTag.get('JOURNAL_DIARY')` 등)이다.
 */
function resolveTagModuleForSelect(moduleExpr: string): { select?: (id: any, name?: string, ctgr?: string) => void } | undefined {
    if (cF.util.isEmpty(moduleExpr)) return undefined;
    try {
        const runner = new Function("return (" + moduleExpr + ");");
        return runner.call(window);
    } catch (e) {
        console.error("[JournalAnnualEntryTagListApp] tag module resolve 실패:", moduleExpr, e);
        return undefined;
    }
}

function reinitTagHeaderTooltips(): void {
    Vue.nextTick(function(): void {
        const bsTooltip = (window as any).bootstrap?.Tooltip;
        if (!bsTooltip) return;
        (["DAY", "DIARY", "DREAM"] as TagRowKind[]).forEach(function(k: TagRowKind): void {
            const root = document.getElementById(TARGET_IDS[k]);
            if (!root) return;
            root.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
                const htmlEl = el as HTMLElement;
                const existing = bsTooltip.getInstance?.(htmlEl);
                if (existing) existing.dispose();
                new bsTooltip(htmlEl);
            });
        });
    });
}

function createTagRowRoot(kind: TagRowKind): Record<string, unknown> {
    return {
        name: "JournalAnnualTagRowRoot",
        data(): { kind: TagRowKind } {
            return { kind };
        },
        computed: {
            row(): TagRowModel {
                return tagRowStore[this.kind as TagRowKind];
            },
            hasList(): boolean {
                return Array.isArray(this.row.list) && this.row.list.length > 0;
            },
        },
        methods: {
            t(key: string): string {
                return resolveMessage(key);
            },
            /** 변경 전: `onclick="dF.Tag.hideSingleTag('#{{tagListDivId}}');"` */
            hideSingleTag(): void {
                (window as any).dF?.Tag?.hideSingleTag?.("#" + this.row.tagListDivId);
            },
            /** 변경 전: `onclick="dF.JournalTag.listAllAjax();"` */
            listAllAjax(): void {
                (window as any).dF?.JournalTag?.listAllAjax?.();
            },
            /** 변경 전: `tag_list_sized_partial` 의 module 결의 select. */
            selectSizedTag(tag: Record<string, any>): void {
                const mod = resolveTagModuleForSelect(this.row.module);
                mod?.select?.(tag.id, tag.name, tag.ctgr);
            },
        },
        template: `
        <div class="row d-flex flex-nowrap align-items-center w-100 mb-4 ms-4">
            <div class="col-1 d-none d-md-flex ms-4 me-6 text-center fs-6">
                <b>{{ row.label }} : </b>
            </div>
            <div class="col">
                <template v-if="hasList">
                    <span
                        v-for="tag in row.list"
                        :key="'tg-' + String(tag.id) + '-' + String(tag.name)"
                        class="py-2 me-3 cursor-pointer opacity-hover"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="t('view.tag.content-list')"
                        @click="selectSizedTag(tag)"
                    >
                        <span :class="[tag.tagClass, tag.textClass]">
                            <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                            <span :class="'em_' + tag.name">{{ tag.name }}</span>
                        </span>
                        <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">{{ tag.contentSize }}</span>
                    </span>
                </template>
                <template v-else>-</template>
            </div>
            <div class="col-1 d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
            <div class="col-1 d-none d-md-flex ms-4 me-20 text-center fs-6 w-auto gap-3">
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="t('bs.tooltip.journal.annual.tag.hide-single')"
                        @click="hideSingleTag"
                >
                    <i class="bi bi-tag pe-0"></i>
                </button>
                <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="t('bs.tooltip.journal.annual.tag.open-all')"
                        @click="listAllAjax"
                >
                    <i class="bi bi-tag pe-0"></i>
                </button>
            </div>
        </div>
        `,
    };
}

function applyTagRow(kind: TagRowKind, payload: Record<string, any>): void {
    const list: Record<string, any>[] = Array.isArray(payload.list) ? payload.list : [];
    tagRowStore[kind].list = list;
    if (typeof payload.label === "string") tagRowStore[kind].label = payload.label;
    if (typeof payload.module === "string") tagRowStore[kind].module = payload.module;
    if (typeof payload.tagListDivId === "string") tagRowStore[kind].tagListDivId = payload.tagListDivId;
    reinitTagHeaderTooltips();
}

runWhenDomReady(function(): void {
    const priorBridge = ((window as any).JournalAnnualEntryTagListVueApp ?? {}) as EntryTagListVueBridge;
    const pendingMap: Partial<Record<TagRowKind, Record<string, any>>> = {
        ...(priorBridge.pendingByType ?? {}),
    };

    (["DAY", "DIARY", "DREAM"] as TagRowKind[]).forEach(function(kind: TagRowKind): void {
        const mountEl = document.getElementById(TARGET_IDS[kind]);
        if (!mountEl) {
            console.error("[JournalAnnualEntryTagListApp] mount root #" + TARGET_IDS[kind] + " 없음.");
            return;
        }
        const app = Vue.createApp(createTagRowRoot(kind));
        app.mount(mountEl);
    });

    (window as any).JournalAnnualEntryTagListVueApp = {
        mounted: true,
        pendingByType: null as Partial<Record<TagRowKind, Record<string, any>>> | null,
        applyTagRow: function(kind: TagRowKind, payload: Record<string, any>): void {
            applyTagRow(kind, payload);
        },
    };

    (["DAY", "DIARY", "DREAM"] as TagRowKind[]).forEach(function(k: TagRowKind): void {
        const pay = pendingMap[k];
        if (pay != null) applyTagRow(k, pay);
    });
});

export {};
