/**
 * JournalDayMetaConfigStrip.ts
 * Handlebars `journal_day_meta_config_template`의 Vue 이전.
 */

import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

const JournalDayMetaConfigStrip = {
    name: "JournalDayMetaConfigStrip",
    props: {
        meta: { type: Object, default: null },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        openMetaModal(): void {
            const id = this.meta?.id;
            if (id == null || Number.isNaN(Number(id))) return;
            journalDayUiBridgeService.openMetaModal(id);
        },
    },
    template: `
    <div class="journal-day-meta-config-strip-vue d-flex-align-center flex-wrap gap-2">
        <template v-if="meta">
            <span class="text-muted fs-4 pe-1">
                #
                <span class="text-dark fw-bold opacity-hover">
                    <span v-if="meta.ctgr" class="fs-7 text-noti">[{{ meta.ctgr }}]</span>
                    {{ meta.metaNm }}
                </span>
            </span>
            <div class="col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 me-3 w-10px">&nbsp;</div>
            <div class="gap-1 d-flex align-items-center">
                <button
                    type="button"
                    class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="t('bs.tooltip.journal.day.meta.config-popup')"
                >
                    <i class="bi bi-gear"></i>
                </button>
                <button
                    type="button"
                    class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="t('view.meta.content-list')"
                    @click="openMetaModal"
                >
                    <i class="bi bi-bar-chart"></i>
                </button>
            </div>
        </template>
    </div>
    `,
};

export default JournalDayMetaConfigStrip;
