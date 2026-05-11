/**
 * 사이트 관리(admin_page) Vue 앱
 *
 * @author nichefish
 */
import AdminRoleTable from "./components/AdminRoleTable.js";
import adminPageDataService, { createEmptyEmbeddingStats } from "./services/adminPageDataService.js";
import createAdminPageActions from "./services/adminPageActionService.js";
import { AdminPageMeta, EmbeddingStats, EmbeddingSyncResult, RoleRow } from "./types.js";
import codeAdminUiService from "../code/services/codeAdminUiService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

type AdminPageState = {
    rows: RoleRow[];
    meta: AdminPageMeta;
    holydayYy: string;
    notionDataType: string;
    notionDataId: string;
    embeddingStats: EmbeddingStats;
    embeddingStatsLoading: boolean;
    embeddingStatsError: string;
    embeddingStatsTimer: number | null;
    embeddingSyncRunning: boolean;
    embeddingSyncMessage: string;
    embeddingSyncResult: EmbeddingSyncResult | null;
};

const state = Vue.reactive({
    rows: [],
    meta: {
        authMngrKey: "MNGR",
        authUserKey: "USER",
        authDevKey: "DEV",
        currYy: new Date().getFullYear(),
    },
    holydayYy: String(new Date().getFullYear()),
    notionDataType: "PAGE",
    notionDataId: "",
    embeddingStats: createEmptyEmbeddingStats(),
    embeddingStatsLoading: false,
    embeddingStatsError: "",
    embeddingStatsTimer: null,
    embeddingSyncRunning: false,
    embeddingSyncMessage: "",
    embeddingSyncResult: null,
}) as AdminPageState;
const i18n = createScopedI18n();

const actions = createAdminPageActions();

function t(key: string): string {
    return i18n.t(key);
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveAdminPageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

const AdminPageRoot = {
    name: "AdminPageRoot",
    components: { AdminRoleTable },
    data(): { state: AdminPageState } {
        return { state };
    },
    computed: {
        yearOptions(): number[] {
            const y = Number(this.state.meta.currYy) || new Date().getFullYear();
            return [y - 1, y, y + 1];
        },
        embeddingProgressStyle(): Record<string, string> {
            const value = Math.max(0, Math.min(100, Number(this.state.embeddingStats.completionRate) || 0));
            return { width: `${value}%` };
        },
    },
    methods: {
        t,
        formatNumber(value: number): string {
            return new Intl.NumberFormat().format(Number(value) || 0);
        },
        formatPercent(value: number): string {
            return `${(Number(value) || 0).toFixed(2)}%`;
        },
        async refreshEmbeddingStats(): Promise<void> {
            this.state.embeddingStatsLoading = true;
            this.state.embeddingStatsError = "";
            try {
                this.state.embeddingStats = await adminPageDataService.fetchEmbeddingStats();
            } catch (e) {
                this.state.embeddingStatsError = e instanceof Error ? e.message : "Embedding stats request failed";
            } finally {
                this.state.embeddingStatsLoading = false;
            }
        },
        formatEmbeddingSyncResult(result: EmbeddingSyncResult): string {
            return [
                `entries ${this.formatNumber(result.activeEntryCount)}`,
                `created ${this.formatNumber(result.created)}`,
                `requeued ${this.formatNumber(result.requeued)}`,
                `unchanged ${this.formatNumber(result.unchanged)}`,
                `skipped ${this.formatNumber(result.skipped)}`,
                `removed ${this.formatNumber(result.removed)}`,
            ].join(" · ");
        },
        async syncEmbeddingQueue(): Promise<void> {
            this.state.embeddingSyncRunning = true;
            this.state.embeddingStatsError = "";
            this.state.embeddingSyncMessage = "";
            try {
                const result = await adminPageDataService.syncEmbeddingQueue();
                this.state.embeddingSyncResult = result;
                this.state.embeddingSyncMessage = this.formatEmbeddingSyncResult(result);
                await this.refreshEmbeddingStats();
            } catch (e) {
                this.state.embeddingStatsError = e instanceof Error ? e.message : "Embedding sync request failed";
            } finally {
                this.state.embeddingSyncRunning = false;
            }
        },
        onHolydayRun(): void {
            actions.holydayAjax(this.state.holydayYy);
        },
        onNotionRun(): void {
            actions.notionAjax(this.state.notionDataType, this.state.notionDataId || "");
        },
        openCacheListModal(): void {
            actions.cacheActiveListModal();
        },
        runCacheClearAll(): void {
            actions.cacheClearAllAjax();
        },
    },
    updated(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#admin_role_table_wrap"));
    },
    mounted(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#admin_role_table_wrap"));
        this.refreshEmbeddingStats();
        this.state.embeddingStatsTimer = window.setInterval((): void => {
            this.refreshEmbeddingStats();
        }, 30000);
    },
    beforeUnmount(): void {
        if (this.state.embeddingStatsTimer != null) {
            window.clearInterval(this.state.embeddingStatsTimer);
            this.state.embeddingStatsTimer = null;
        }
    },
    template: `
    <div class="mt-5">
        <div class="row g-5 gx-xl-10 mb-5 mb-xl-10">
            <div class="col-md-6 col-lg-6 col-xl-6 col-xxl-7 mb-md-5 mb-xl-10">
                <div class="card post">
                    <div class="row mb-8">
                        <div class="col-xl-2">
                            <div class="fs-6 fw-bold col-form-label"><label>{{ t('txt.admin.site.cache.title') }}</label></div>
                        </div>
                        <div class="col-xl-3">
                            <div class="mt-1">
                                <button type="button" class="btn btn-sm btn-primary me-1"
                                        @click="openCacheListModal">
                                    {{ t('txt.admin.site.cache.btn.active-list') }}
                                </button>
                                <button type="button" class="btn btn-sm btn-primary"
                                        @click="runCacheClearAll">
                                    {{ t('txt.admin.site.cache.btn.clear-all') }}
                                </button>
                            </div>
                        </div>
                        <div class="col-xl-7 text-noti">
                            <p class="mb-0">{{ t('txt.admin.site.cache.notice.ln1') }}</p>
                            <p class="mb-0 ps-4">{{ t('txt.admin.site.cache.notice.ln2') }}</p>
                        </div>
                    </div>
                    <div class="row mb-8">
                        <div class="col-xl-2">
                            <div class="fs-6 fw-bold col-form-label">
                                <label for="holydayYy">{{ t('txt.admin.site.holyday.label') }}</label>
                            </div>
                        </div>
                        <div class="col-xl-2">
                            <select name="holydayYy" id="holydayYy" class="form-select form-select-solid"
                                    v-model="state.holydayYy">
                                <option v-for="y in yearOptions" :key="y" :value="String(y)">{{ y }}</option>
                            </select>
                        </div>
                        <div class="col-xl-2 mt-1">
                            <button type="button" class="btn btn-sm btn-primary" @click="onHolydayRun">
                                {{ t('txt.admin.site.holyday.btn') }}
                            </button>
                        </div>
                        <div class="col-xl-6 col-form-label text-noti">
                            {{ t('txt.admin.site.holyday.notice') }}
                        </div>
                    </div>
                    <div class="row mb-8">
                        <div class="col-xl-2">
                            <div class="fs-6 fw-bold col-form-label">
                                <label for="dataType">{{ t('txt.admin.site.notion.label') }}</label>
                            </div>
                        </div>
                        <div class="col-xl-2">
                            <select name="dataType" id="dataType" class="form-select form-select-solid"
                                    v-model="state.notionDataType">
                                <option value="PAGE">PAGE</option>
                                <option value="BLOCK">BLOCK</option>
                                <option value="BLOCKS">BLOCKS</option>
                                <option value="DATABASE">DATABASE</option>
                            </select>
                        </div>
                        <div class="col-xl-3">
                            <input type="text" name="dataId" id="dataId" class="form-control form-control-solid no-space"
                                   v-model="state.notionDataId" maxlength="64" />
                        </div>
                        <div class="col-xl-2 mt-1">
                            <button type="button" class="btn btn-sm btn-primary" @click="onNotionRun">
                                {{ t('txt.admin.site.notion.btn') }}
                            </button>
                        </div>
                    </div>
                    <div class="border rounded p-4 bg-light">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <div>
                                <div class="fs-6 fw-bold">AI Embedding Backfill</div>
                                <div class="text-muted fs-8">Auto refresh every 30 seconds</div>
                            </div>
                            <div class="d-flex gap-2">
                                <button type="button" class="btn btn-sm btn-light-primary"
                                        :disabled="state.embeddingStatsLoading || state.embeddingSyncRunning"
                                        @click="refreshEmbeddingStats">
                                    Refresh
                                </button>
                                <button type="button" class="btn btn-sm btn-primary"
                                        :disabled="state.embeddingStatsLoading || state.embeddingSyncRunning"
                                        @click="syncEmbeddingQueue">
                                    <span v-if="state.embeddingSyncRunning" class="spinner-border spinner-border-sm me-1"></span>
                                    Sync Entries
                                </button>
                            </div>
                        </div>
                        <div v-if="state.embeddingStatsError" class="alert alert-warning py-2 mb-3">
                            {{ state.embeddingStatsError }}
                        </div>
                        <div v-if="state.embeddingSyncMessage" class="alert alert-success py-2 mb-3">
                            {{ state.embeddingSyncMessage }}
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-6 col-md-3">
                                <div class="text-muted fs-8">Total</div>
                                <div class="fs-4 fw-bold">{{ formatNumber(state.embeddingStats.total) }}</div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="text-muted fs-8">Pending</div>
                                <div class="fs-4 fw-bold text-warning">{{ formatNumber(state.embeddingStats.pending) }}</div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="text-muted fs-8">Processing</div>
                                <div class="fs-4 fw-bold text-primary">{{ formatNumber(state.embeddingStats.processing) }}</div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="text-muted fs-8">Embedded</div>
                                <div class="fs-4 fw-bold text-success">{{ formatNumber(state.embeddingStats.embedded) }}</div>
                            </div>
                        </div>
                        <div class="d-flex flex-wrap gap-2 mb-3">
                            <span class="badge badge-light-success">Completed {{ formatNumber(state.embeddingStats.completed) }}</span>
                            <span class="badge badge-light-warning">Remaining {{ formatNumber(state.embeddingStats.remaining) }}</span>
                            <span class="badge badge-light-danger">Failed {{ formatNumber(state.embeddingStats.failed) }}</span>
                            <span class="badge badge-light">Skipped {{ formatNumber(state.embeddingStats.skipped) }}</span>
                        </div>
                        <div class="d-flex justify-content-between fs-8 text-muted mb-1">
                            <span>Completion {{ formatPercent(state.embeddingStats.completionRate) }}</span>
                            <span>Vectorized {{ formatPercent(state.embeddingStats.vectorizedRate) }}</span>
                        </div>
                        <div class="progress h-8px">
                            <div class="progress-bar bg-success" role="progressbar" :style="embeddingProgressStyle"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6 col-lg-6 col-xl-6 col-xxl-5 mb-md-5 mb-xl-10">
                <AdminRoleTable :rows="state.rows" :meta="state.meta" />
            </div>
        </div>
    </div>
    `,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveAdminPageLocale());
    state.rows = adminPageDataService.parseRoles();
    state.meta = adminPageDataService.parseMeta();
    state.holydayYy = String(state.meta.currYy);

    if (!document.getElementById("admin_page_app")) {
        console.error("[AdminPageApp] mount root #admin_page_app not found.");
        return;
    }

    actions.initCacheModule();

    const app = Vue.createApp(AdminPageRoot);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#admin_page_app");
});
