/**
 * 사이트 관리(admin_page) Vue 앱
 *
 * @author nichefish
 */
import AdminRoleTable from "./components/AdminRoleTable.js";
import adminPageDataService from "./services/adminPageDataService.js";
import createAdminPageActions from "./services/adminPageActionService.js";
import { AdminPageMeta, RoleRow } from "./types.js";
import codeAdminUiService from "../code/services/codeAdminUiService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";

type AdminPageState = {
    rows: RoleRow[];
    meta: AdminPageMeta;
    holydayYy: string;
    notionDataType: string;
    notionDataId: string;
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
    },
    methods: {
        t,
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
