/**
 * JournalDayTagPanelApp.ts
 * 저널 일자 태그 헤더/모달 Vue 렌더러 브리지.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";

type TagItem = Record<string, any>;
type TagCategoryFilter = { key: string; label: string };

type TagPanelBridge = {
    mounted?: boolean;
    setDayTagList?: (list: TagItem[]) => void;
    openTagListModal?: (list: TagItem[]) => void;
    pendingDayTagList?: TagItem[] | null;
    pendingModalTagList?: TagItem[] | null;
};

const state = Vue.reactive({
    dayTagList: [] as TagItem[],
    modalTagList: [] as TagItem[],
    hiddenCategories: new Set<string>(),
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function toTagCategoryClass(tag: TagItem): string {
    const ctgr = String(tag.ctgr ?? "").trim();
    return cF.util.isNotEmpty(ctgr) ? ctgr : "defaultCtgr";
}

function initializeTooltips(selector: string): void {
    document.querySelectorAll(selector).forEach((el: Element): void => {
        new (window as any).bootstrap.Tooltip(el as HTMLElement);
    });
}

function setDayTagList(list: TagItem[]): void {
    state.dayTagList = Array.isArray(list) ? list : [];
    Vue.nextTick(function(): void {
        initializeTooltips("#journal_day_tag_list_div [data-bs-toggle='tooltip']");
    });
}

function setModalTagList(list: TagItem[]): void {
    state.modalTagList = Array.isArray(list) ? list : [];
    state.hiddenCategories.clear();
    Vue.nextTick(function(): void {
        initializeTooltips("#journal_tag_list_div [data-bs-toggle='tooltip']");
    });
}

function openModal(): void {
    const modalEl: HTMLElement | null = document.querySelector("#journal_tag_list_modal");
    if (!modalEl) return;
    (window as any).bootstrap.Modal.getOrCreateInstance(modalEl).show();
}

const JournalDayTagPanelRootApp = {
    name: "JournalDayTagPanelRootApp",
    data(): { state: typeof state } {
        return { state };
    },
    computed: {
        modalCategories(): TagCategoryFilter[] {
            const keys: string[] = [];
            this.state.modalTagList.forEach((tag: TagItem): void => {
                const key = toTagCategoryClass(tag);
                if (!keys.includes(key)) keys.push(key);
            });
            const categoryItems: TagCategoryFilter[] = keys
                .filter((key: string): boolean => key !== "defaultCtgr")
                .map((key: string): TagCategoryFilter => ({ key, label: key }));
            return [{ key: "defaultCtgr", label: Message.get("txt.journal.tag.no-category") }, ...categoryItems];
        },
        visibleModalTagList(): TagItem[] {
            return this.state.modalTagList.filter((tag: TagItem): boolean => !this.state.hiddenCategories.has(toTagCategoryClass(tag)));
        },
    },
    methods: {
        toTagCategoryClass(tag: TagItem): string {
            return toTagCategoryClass(tag);
        },
        openDayTagDetail(tag: TagItem): void {
            journalDayUiBridgeService.selectDayTag(tag.id, String(tag.tagNm ?? ""), String(tag.ctgr ?? ""));
        },
        openDreamTagSearch(tag: TagItem): void {
            dF.JournalEntryTag.get("JOURNAL_DREAM").openSearch(tag.id);
        },
        toggleAllCategories(): void {
            if (this.state.hiddenCategories.size === 0) {
                this.modalCategories.forEach((item: TagCategoryFilter): void => this.state.hiddenCategories.add(item.key));
                return;
            }
            this.state.hiddenCategories.clear();
        },
        toggleCategory(categoryKey: string): void {
            if (this.state.hiddenCategories.has(categoryKey)) this.state.hiddenCategories.delete(categoryKey);
            else this.state.hiddenCategories.add(categoryKey);
        },
        isCategoryVisible(categoryKey: string): boolean {
            return !this.state.hiddenCategories.has(categoryKey);
        },
        categoryButtonClass(categoryKey: string): string {
            const active = this.isCategoryVisible(categoryKey);
            return `btn btn-sm text-muted ctgr ${active ? "active btn-outlined btn-light-secondary" : "btn-light"}`;
        },
    },
    template: `
    <teleport to="#journal_day_tag_list_div">
        <template v-if="state.dayTagList.length > 0">
            <span
                v-for="tag in state.dayTagList"
                :key="'day-tag-' + String(tag.id)"
                class="py-2 me-3 cursor-pointer opacity-hover"
                data-bs-toggle="tooltip"
                data-bs-placement="top"
                data-bs-dismiss="click"
                :title="Message.get('view.tag.content-list')"
                @click="openDayTagDetail(tag)"
            >
                <span :class="String(tag.tagClass || '') + ' ' + String(tag.textClass || '')">
                    <span v-if="String(tag.ctgr || '').trim().length > 0" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                    <span :class="'em_' + String(tag.tagNm || '')">{{ tag.tagNm }}</span>
                </span>
                <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">{{ tag.contentSize }}</span>
            </span>
        </template>
        <template v-else>-</template>
    </teleport>
    <teleport to="#journal_tag_ctgr_div">
        <div
            :class="categoryButtonClass('__ALL__')"
            data-ctgr="__ALL__"
            @click="toggleAllCategories"
        >
            {{ Message.get('txt.total') }} <i class="bi bi-check ctgr"></i>
        </div>
        <div
            v-for="item in modalCategories"
            :key="'ctgr-' + item.key"
            :class="categoryButtonClass(item.key) + ' ' + item.key"
            :data-ctgr="item.key"
            @click="toggleCategory(item.key)"
        >
            {{ item.label }} <i class="bi bi-check ctgr" :class="item.key"></i>
        </div>
    </teleport>
    <teleport to="#journal_tag_list_div">
        <span
            v-for="tag in visibleModalTagList"
            :key="'modal-tag-' + String(tag.id)"
            :class="'py-2 me-3 cursor-pointer opacity-hover ctgr ' + toTagCategoryClass(tag)"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            data-bs-dismiss="click"
            :title="Message.get('bs.tooltip.journal.tag.filter-by-tag')"
            @click="openDreamTagSearch(tag)"
        >
            <span :class="String(tag.tagClass || '') + ' ' + String(tag.textClass || '')">
                <span v-if="String(tag.ctgr || '').trim().length > 0" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                {{ tag.tagNm }}
            </span>
            <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">{{ tag.contentSize }}</span>
        </span>
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    const rootEl = document.querySelector("#journal_day_tag_panel_app") as HTMLElement | null;
    if (!rootEl) {
        console.error("[JournalDayTagPanelApp] Vue mount root not found.");
        return;
    }

    const queuedBridge = window.JournalDayTagPanelVueApp as TagPanelBridge | undefined;
    const app = Vue.createApp(JournalDayTagPanelRootApp);
    app.mount("#journal_day_tag_panel_app");

    window.JournalDayTagPanelVueApp = {
        mounted: true,
        setDayTagList: function(list: TagItem[]): void {
            setDayTagList(list);
        },
        openTagListModal: function(list: TagItem[]): void {
            setModalTagList(list);
            openModal();
        },
    };

    if (queuedBridge?.pendingDayTagList) window.JournalDayTagPanelVueApp.setDayTagList?.(queuedBridge.pendingDayTagList);
    if (queuedBridge?.pendingModalTagList) window.JournalDayTagPanelVueApp.openTagListModal?.(queuedBridge.pendingModalTagList);
});

export {};
