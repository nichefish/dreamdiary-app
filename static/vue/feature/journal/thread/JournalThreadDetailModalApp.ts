/**
 * JournalThreadDetailModalApp.ts
 * Vue detail modal for journal thread list quick view.
 */

export {};

type JournalThreadDetailBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    pendingPayload?: Record<string, any> | null;
};

const state = Vue.reactive({
    model: null as Record<string, any> | null,
});

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function showModal(): void {
    const modalEl = document.querySelector("#journal_thread_detail_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalThreadDetailModalApp] Modal root #journal_thread_detail_modal not found.");
        return;
    }
    (window as any).bootstrap?.Modal.getOrCreateInstance(modalEl).show();
}

function openDetail(model: Record<string, any>): void {
    state.model = model;
    showModal();
}

const Root = {
    name: "JournalThreadDetailModalRoot",
    data(): { state: typeof state } {
        return { state };
    },
    computed: {
        post(): Record<string, any> {
            return this.state.model || {};
        },
        tagList(): Record<string, any>[] {
            const list = this.post?.tag?.list;
            return Array.isArray(list) ? list : [];
        },
        managerName(): string {
            return this.post?.managt?.mnfNm || this.post?.managtrNm || this.post?.createdByNm || "";
        },
        managedAt(): string {
            return this.post?.managt?.managtDt || this.post?.managtDt || this.post?.createdDt || "";
        },
    },
    methods: {
        tagDetail(tagId: string): void {
            const fn = (window as any).dF?.Tag?.dtlModal;
            if (typeof fn === "function") fn(tagId);
        },
    },
    template: `
    <teleport to="#journal_thread_detail_div">
        <div v-if="state.model" class="journal-thread-dtl-vue-root">
            <div class="mb-0">
                <div class="d-flex align-items-center flex-wrap gap-2 mb-2">
                    <span v-if="post.categoryName" class="ctgr-span ctgr-gray">{{ post.categoryName }}</span>
                    <span class="fs-3 fw-bolder text-gray-900">{{ post.title }}</span>
                </div>
                <div class="d-flex align-items-center flex-wrap gap-3 text-muted fs-7">
                    <span v-if="managerName"><i class="bi bi-person pe-1"></i>{{ managerName }}</span>
                    <span v-if="managedAt"><i class="bi bi-clock pe-1"></i>{{ managedAt }}</span>
                </div>
            </div>

            <div class="separator separator-dashed border-gray-300 my-8"></div>

            <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px" v-html="post.markdownContent || post.content || ''"></div>

            <div v-if="tagList.length > 0" class="mt-4">
                <span
                    v-for="tag in tagList"
                    :key="'thread-dtl-tag-' + String(tag.tagId)"
                    class="text-muted pe-1 cursor-pointer"
                    role="button"
                    tabindex="0"
                    title="tag"
                    @click.prevent="tagDetail(tag.tagId)"
                    @keyup.enter.prevent="tagDetail(tag.tagId)"
                >
                    <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                    #<span class="border-bottom text-primary fw-lighter opacity-hover">{{ tag.name }}</span>
                </span>
            </div>
        </div>
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.getElementById("journal_thread_detail_vue_app");
    if (!mountEl) {
        console.error("[JournalThreadDetailModalApp] Mount root #journal_thread_detail_vue_app not found.");
        return;
    }

    const globalWindow = window as any;
    const priorBridge = (globalWindow.JournalThreadDetailVueApp || {}) as JournalThreadDetailBridge;
    const pendingPayload = priorBridge.pendingPayload;

    Vue.createApp(Root).mount("#journal_thread_detail_vue_app");

    globalWindow.JournalThreadDetailVueApp = {
        mounted: true,
        pendingPayload: null,
        open: openDetail,
    };

    if (pendingPayload && typeof pendingPayload === "object") {
        openDetail(pendingPayload);
    }
});
