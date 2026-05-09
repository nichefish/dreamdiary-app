/**
 * JournalDayMetaModalBody.ts
 * 저널 메타 조회 모달 본문(Handlebars `journal_day_meta_modal_template`) Vue 이전.
 */

import JournalDayMetaModalStdrdDt from "./JournalDayMetaModalStdrdDt.js";
import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

export type JournalDayMetaModalPayload = {
    metaId: string | number;
    yy: string;
    yearOptions: Array<{ value: string | number; label: string; selected?: boolean }>;
    list: Record<string, any>[];
};

const JournalDayMetaModalBody = {
    name: "JournalDayMetaModalBody",
    components: {
        JournalDayMetaModalStdrdDt,
    },
    props: {
        payload: { type: Object, required: true },
    },
    computed: {
        listRows(): Record<string, any>[] {
            return Array.isArray(this.payload?.list) ? this.payload.list : [];
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        onYyChange(event: Event): void {
            const target = event.target as HTMLSelectElement | null;
            if (!target) return;
            journalDayUiBridgeService.changeMetaYear(this.payload.metaId, target.value);
        },
        showMonthHeader(day: Record<string, any>, index: number): boolean {
            const list: Record<string, any>[] = this.listRows;
            if (index === 0) return true;
            const prev = list[index - 1];
            return String(day?.mnth ?? "") !== String(prev?.mnth ?? "");
        },
        matchingMetaRows(day: Record<string, any>): Record<string, any>[] {
            const targetId: string = String(this.payload?.metaId ?? "");
            const metaList: Record<string, any>[] = Array.isArray(day?.meta?.list) ? day.meta.list : [];
            return metaList.filter((row: Record<string, any>): boolean => String(row?.metaId ?? "") === targetId);
        },
        tagList(day: Record<string, any>): Record<string, any>[] {
            return Array.isArray(day?.tag?.list) ? day.tag.list : [];
        },
        hasTagList(day: Record<string, any>): boolean {
            return this.tagList(day).length > 0;
        },
        selectDayTag(tag: Record<string, any>): void {
            journalDayUiBridgeService.selectDayTag(tag.tagId, String(tag.name ?? ""));
        },
    },
    template: `
    <div class="journal-day-meta-modal-vue-root">
        <div class="d-flex flex-column mb-4">
            <div class="d-flex justify-content-start align-items-center gap-3 mb-4">
                <label for="journal_day_meta_yy" class="form-label mb-0 fw-bold">{{ t('txt.yy') }}</label>
                <select
                    id="journal_day_meta_yy"
                    class="form-select form-select-sm w-auto"
                    :value="String(payload.yy ?? '')"
                    @change="onYyChange"
                >
                    <option
                        v-for="opt in (payload.yearOptions || [])"
                        :key="'yy-' + opt.value"
                        :value="String(opt.value)"
                    >{{ opt.label }}</option>
                </select>
            </div>

            <template v-for="(day, index) in listRows" :key="'meta-day-' + index + '-' + (day.stdrdDt || '')">
                <div v-if="showMonthHeader(day, index)" class="d-flex-center mt-6 mb-4 fs-5 text-dark">
                    {{ day.yy }}{{ t('txt.date.suffix.after-year-number') }}{{ day.mnth }}{{ t('txt.date.suffix.after-month-number') }}
                </div>

                <div class="d-flex align-items-center gap-2">
                    <JournalDayMetaModalStdrdDt :day="day" />
                    <template v-for="(metaRow, mIdx) in matchingMetaRows(day)" :key="'mm-' + index + '-' + mIdx + '-' + metaRow.metaId">
                        <div>
                            <span v-if="metaRow.ctgr" class="text-noti pe-1">[{{ metaRow.ctgr }}]</span>
                            {{ metaRow.metaNm }}
                            <template v-if="metaRow.label">
                                - <span>{{ metaRow.label }}</span>
                            </template>
                            <span class="text-dialog">: {{ metaRow.value }}{{ metaRow.unit }}</span>
                        </div>
                        <div class="separator separator-dashed my-2"></div>
                        <button
                            type="button"
                            class="btn btn-sm btn-secondary btn-active-primary"
                            data-journal-day-action="open-detatched"
                            :data-journal-day-stdrd-dt="day.stdrdDt"
                        >
                            {{ t('txt.comm.open-in-new-window') }}
                            <i class="bi bi-window-stack fs-7 ms-1 pe-0"></i>
                        </button>
                    </template>
                </div>

                <div v-if="hasTagList(day)" class="mb-4 d-flex-align-center">
                    <div class="ms-5 mt-3">
                        <i class="bi bi-tag"></i>
                        <span
                            v-for="tag in tagList(day)"
                            :key="String(tag.tagId) + ':' + String(tag.name)"
                            class="text-muted cursor-pointer pe-1"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="t('view.tag.content-list')"
                            @click="selectDayTag(tag)"
                        >
                            #
                            <span class="border-bottom text-primary fw-lighter opacity-hover">
                                <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                                {{ tag.name }}
                            </span>
                        </span>
                    </div>
                </div>
            </template>
        </div>
    </div>
    `,
};

export default JournalDayMetaModalBody;
