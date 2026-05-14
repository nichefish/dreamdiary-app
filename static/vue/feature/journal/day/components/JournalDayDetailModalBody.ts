/**
 * JournalDayDetailModalBody.ts
 * 저널 일자 상세 모달 본문(Handlebars `journal_day_dtl_modal_template`) Vue 이전.
 */

import JournalEntryContent from "../../entry/components/JournalEntryContent.js";
import JournalEntryContextMenu from "../../entry/components/JournalEntryContextMenu.js";
import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

const JournalDayDetailModalBody = {
    name: "JournalDayDetailModalBody",
    components: {
        JournalEntryContent,
        JournalEntryContextMenu,
    },
    props: {
        model: { type: Object, required: true },
    },
    computed: {
        day(): Record<string, any> {
            return this.model && typeof this.model === "object" ? this.model : {};
        },
        journalChapterList(): Record<string, any>[] {
            const list = this.day.journalChapterList;
            return Array.isArray(list) ? list : [];
        },
        showDiarySection(): boolean {
            return this.journalChapterList.length > 0;
        },
        journalDreamList(): Record<string, any>[] {
            const list = this.day.journalDreamList;
            return Array.isArray(list) ? list : [];
        },
        journalElseDreamList(): Record<string, any>[] {
            const list = this.day.journalElseDreamList;
            return Array.isArray(list) ? list : [];
        },
        tagList(): Record<string, any>[] {
            const list = this.day?.tag?.list;
            return Array.isArray(list) ? list : [];
        },
        diaryContentLabel(): string {
            return this.t("txt.journal.diary");
        },
        dreamContentLabel(): string {
            return this.t("txt.journal.dream");
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        tooltip(labelKey: string, actionKey: string): string {
            return [this.t(labelKey), this.t(actionKey)].filter((value: string): boolean => value.length > 0).join(" ");
        },
        hasState(entry: Record<string, any>, targetState: string): boolean {
            const states = entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === targetState);
        },
        selectDayTag(tag: Record<string, any>): void {
            journalDayUiBridgeService.selectDayTag(tag.tagId, String(tag.name ?? ""));
        },
        openCommentReg(entry: Record<string, any>, contentType: string): void {
            window.dispatchEvent(new CustomEvent("comment:open-reg-modal", {
                detail: { refId: entry.id, refContentType: contentType },
            }));
        },
        copyEntry(entry: Record<string, any>, contentType: string): void {
            dF.JournalEntry.get(contentType).copy(entry.id);
        },
        chapterEntries(chapter: Record<string, any>): Record<string, any>[] {
            const list = chapter?.journalEntryList;
            return Array.isArray(list) ? list : [];
        },
        dreamBodyClass(entry: Record<string, any>): string {
            return this.hasState(entry, "IMPRTC") ? "journal-dream-content bg-secondary" : "journal-dream-content";
        },
        /** Handlebars `journalElseDreamList` 행: 항상 `journal-dream-content bg-secondary`. */
        elseDreamOuterClass(): string {
            return "journal-dream-content bg-secondary";
        },
    },
    template: `
    <div class="journal-day-dtl-vue-root">
        <input type="hidden" name="id" :value="day.id">

        <div class="row row-cols-lg-2 mb-3 d-flex flex-column">
            <div class="d-flex align-items-center mt-1 gap-1">
                <i class="bi bi-calendar3 fs-6 me-1"></i>
                {{ day.stdrdDt }}
                <span class="fs-8 text-gray-600">({{ day.journalDateWeekDay }})</span>
                <span class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
                <span class="fs-7 ms-4 text-muted" v-html="day.weather"></span>
            </div>
            <div class="mt-3" v-if="tagList.length > 0">
                <span
                    v-for="tag in tagList"
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

        <div class="row p-5">
            <template v-if="showDiarySection">
                <div class="d-flex align-items-center mt-2">
                    <span class="text-gray-700 fs-6 ps-1 ps-md-5 fw-bolder">
                        {{ t('txt.diary') }}
                        <i class="bi bi-book fs-4 pe-1"></i>
                    </span>
                </div>
                <template v-for="chapter in journalChapterList" :key="'dtl-ch-' + chapter.id">
                    <template v-for="entry in chapterEntries(chapter)" :key="'dtl-de-' + entry.id">
                        <div class="journal-diary-item" :data-id="entry.id">
                            <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 border-end ps-10 me-4 h-75" style="width:85px;">
                                <div># {{ entry.sortOrder }}</div>
                            </div>
                            <div class="col journal-diary-content">
                                <JournalEntryContent
                                    :entry="entry"
                                    content-type="JOURNAL_DIARY"
                                    collapse-class="collapse-4"
                                />
                            </div>
                            <div class="col-1 d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
                            <div class="col-1 py-3 d-none d-md-flex-between w-50px gap-1">
                                <button
                                    type="button"
                                    class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                                    data-bs-toggle="tooltip"
                                    data-bs-placement="top"
                                    data-bs-dismiss="click"
                                    :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')"
                                    @click="openCommentReg(entry, 'JOURNAL_DIARY')"
                                >
                                    <i class="bi bi-chat-left-dots p-0"></i>
                                </button>
                                <button
                                    type="button"
                                    class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                                    data-bs-toggle="tooltip"
                                    data-bs-placement="top"
                                    data-bs-dismiss="click"
                                    :title="t('bs.tooltip.copy')"
                                    @click="copyEntry(entry, 'JOURNAL_DIARY')"
                                >
                                    <i class="bi bi-copy p-0"></i>
                                </button>
                                <JournalEntryContextMenu
                                    :entry="entry"
                                    content-type="JOURNAL_DIARY"
                                    :content-label="diaryContentLabel"
                                    css-prefix="diary"
                                    :show-interpretation="true"
                                    :show-related="true"
                                    :show-dream-states="false"
                                />
                            </div>
                        </div>
                    </template>
                </template>
            </template>

            <template v-for="entry in journalDreamList" :key="'dtl-dr-' + entry.id">
                <div class="journal-item">
                    <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 border-end ps-10 me-4 h-75" style="width:85px;">
                        <div># {{ entry.sortOrder }}</div>
                    </div>
                    <div class="col py-3" :class="dreamBodyClass(entry)">
                        <JournalEntryContent
                            :entry="entry"
                            content-type="JOURNAL_DREAM"
                            collapse-class="collapse-4"
                        />
                    </div>
                    <div class="col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
                    <div class="col-1 py-3 d-none d-md-flex-between w-75px gap-1">
                        <button
                            type="button"
                            class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')"
                            @click="openCommentReg(entry, 'JOURNAL_DREAM')"
                        >
                            <i class="bi bi-chat-left-dots p-0"></i>
                        </button>
                        <button
                            type="button"
                            class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="t('bs.tooltip.copy')"
                            @click="copyEntry(entry, 'JOURNAL_DREAM')"
                        >
                            <i class="bi bi-copy p-0"></i>
                        </button>
                        <JournalEntryContextMenu
                            :entry="entry"
                            content-type="JOURNAL_DREAM"
                            :content-label="dreamContentLabel"
                            css-prefix="dream"
                            :show-interpretation="true"
                            :show-related="true"
                            :show-dream-states="true"
                        />
                    </div>
                </div>
            </template>

            <template v-for="entry in journalElseDreamList" :key="'dtl-ed-' + entry.id">
                <div class="journal-item">
                    <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 border-end ps-10 me-4 h-75" style="width:85px;">
                        <div>
                            -
                        </div>
                    </div>
                    <div class="col py-3" :class="elseDreamOuterClass()">
                        <JournalEntryContent
                            :entry="entry"
                            content-type="JOURNAL_DIARY"
                            collapse-class="collapse-4"
                        />
                    </div>
                    <div class="col-1 py-3 d-none d-md-flex-between w-50px gap-1">
                        <button
                            type="button"
                            class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')"
                            @click="openCommentReg(entry, 'JOURNAL_DREAM')"
                        >
                            <i class="bi bi-chat-left-dots p-0"></i>
                        </button>
                    </div>
                    <div class="col-1 ms-4 pe-0 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
                    <div class="col-1 py-3 d-none d-md-flex-between w-50px gap-1">
                        <button
                            type="button"
                            class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="t('bs.tooltip.copy')"
                            @click="copyEntry(entry, 'JOURNAL_DREAM')"
                        >
                            <i class="bi bi-copy p-0"></i>
                        </button>
                        <JournalEntryContextMenu
                            :entry="entry"
                            content-type="JOURNAL_DREAM"
                            :content-label="dreamContentLabel"
                            css-prefix="dream"
                            :show-interpretation="true"
                            :show-related="true"
                            :show-dream-states="true"
                        />
                    </div>
                </div>
            </template>
        </div>
    </div>
    `,
};

export default JournalDayDetailModalBody;
