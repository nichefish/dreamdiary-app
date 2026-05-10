/**
 * JournalDayMetaHeaderList.ts
 * Handlebars `journal_day_meta_list_template`과 `meta_list_partial`의 Vue 이전.
 */

import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

const JournalDayMetaHeaderList = {
    name: "JournalDayMetaHeaderList",
    props: {
        items: { type: Array, default: (): unknown[] => [] },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        selectMeta(item: Record<string, any>): void {
            const id = item?.id;
            if (id == null || Number.isNaN(Number(id))) return;
            journalDayUiBridgeService.selectMeta(id);
        },
    },
    template: `
    <div class="journal-day-meta-header-list-vue">
        <template v-if="items.length > 0">
            <span
                v-for="item in items"
                :key="'meta-h-' + item.id"
                class="text-muted cursor-pointer pe-1"
                data-bs-toggle="tooltip"
                data-bs-placement="top"
                data-bs-dismiss="click"
                :title="t('view.meta.content-list')"
                @click="selectMeta(item)"
            >
                #
                <span class="border-bottom text-primary fw-lighter opacity-hover">
                    <span v-if="item.ctgr" class="fs-7 text-noti">[{{ item.ctgr }}]</span>
                    {{ item.name }}
                </span>
            </span>
        </template>
        <template v-else>-</template>
    </div>
    `,
};

export default JournalDayMetaHeaderList;
