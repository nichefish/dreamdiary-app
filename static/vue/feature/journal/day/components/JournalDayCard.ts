/**
 * JournalDayCard.ts
 * 레거시 자식 partial을 포함하는 저널 일자 Vue 카드.
 */

import JournalChapterItem from "../../chapter/components/JournalChapterItem.js";
import JournalEntryItem from "../../entry/components/JournalEntryItem.js";
import JournalDayContextMenu from "./JournalDayContextMenu.js";
import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

const JournalDayCard = {
    name: "JournalDayCard",
    components: {
        JournalChapterItem,
        JournalEntryItem,
        JournalDayContextMenu,
    },
    props: {
        day: { type: Object, required: true },
        showDiaries: { type: Boolean, default: true },
        showDreams: { type: Boolean, default: true },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        tooltip(labelKey: string, actionKey: string): string {
            const label = this.t(labelKey);
            const action = this.t(actionKey);
            return [label, action].filter((value: string): boolean => value.length > 0).join(" ");
        },
        /**
         * 챕터 등록 모달 진입.
         * 변경 전: dF.JournalChapter.regModal({...}) — 내부에서 cF.handlebars.modal 로 본문 렌더 후 모달 표시.
         * 변경 후(Phase B): window.JournalChapterRegVueApp.open(initialModel) 큐로 단일 진입.
         */
        openChapterRegModal(): void {
            const initialModel: Record<string, any> = {
                journalDayId: this.day.id,
                stdrdDt: this.day.stdrdDt,
                journalDateWeekDay: this.day.journalDateWeekDay,
                chapterType: "DIARY",
            };
            const bridge = window.JournalChapterRegVueApp;
            if (!bridge) {
                console.error("[JournalDayCard] window.JournalChapterRegVueApp not available.");
                return;
            }
            if (bridge.mounted === true && typeof bridge.open === "function") {
                bridge.open(initialModel);
            } else {
                bridge.pendingPayload = initialModel;
            }
        },
        openDreamRegModal(): void {
            dF.JournalEntry.get("JOURNAL_DREAM").regModal({
                journalDayId: this.day.id,
                stdrdDt: this.day.stdrdDt,
                journalDateWeekDay: this.day.journalDateWeekDay,
            });
        },
        selectDayTag(tag: Record<string, any>): void {
            journalDayUiBridgeService.selectDayTag(tag.tagId, String(tag.name ?? ""));
        },
        hiddenChapterCtgrList(): Record<string, any>[] {
            return Array.isArray(this.day?.hiddenChapterCtgrList) ? this.day.hiddenChapterCtgrList : [];
        },
        journalChapterList(): Record<string, any>[] {
            return Array.isArray(this.day?.journalChapterList) ? this.day.journalChapterList : [];
        },
        journalDreamList(): Record<string, any>[] {
            return Array.isArray(this.day?.journalDreamList) ? this.day.journalDreamList : [];
        },
        journalElseDreamList(): Record<string, any>[] {
            return Array.isArray(this.day?.journalElseDreamList) ? this.day.journalElseDreamList : [];
        },
        tagList(): Record<string, any>[] {
            return Array.isArray(this.day?.tag?.list) ? this.day.tag.list : [];
        },
        metaList(): Record<string, any>[] {
            return Array.isArray(this.day?.meta?.list) ? this.day.meta.list : [];
        },
        hasVisibleTags(): boolean {
            return this.tagList().length > 0;
        },
        hasMeta(): boolean {
            return this.metaList().length > 0;
        },
        hasDream(): boolean {
            return this.day?.hasDream === true
                || this.journalDreamList().length + this.journalElseDreamList().length > 0;
        },
        escapeHtml(value: unknown): string {
            return String(value ?? "")
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
        },
        metaTooltipHtml(): string {
            return this.metaList().map((meta: Record<string, any>): string => {
                const metaId = this.escapeHtml(meta.metaId);
                const category = cF.util.isNotEmpty(meta.ctgr)
                    ? `<span class='text-noti pe-1'>[${this.escapeHtml(meta.ctgr)}]</span>`
                    : "";
                const value = `${this.escapeHtml(meta.value)}${this.escapeHtml(meta.unit)}`;
                return `<div id='meta-id-${metaId}' class='cursor-pointer btn btn-sm btn-bg-light btn-active-color-primary meta-item' data-meta-id='${metaId}'>`
                    + `${category} ${this.escapeHtml(meta.name)}: <span class='text-dialog'>${value}</span>`
                    + `</div>`;
            }).join("");
        },
    },
    template: `
    <div class="journal-day" :id="'journal-day-' + day.stdrdDt" :data-stdrd-dt="day.stdrdDt">
        <div class="journal-day-header" :data-date="day.stdrdDt">
            <div class="col-12 col-md-1 d-flex flex-wrap align-items-center fs-5 fw-bold">
                <div :class="{ 'text-danger': day.isHolyday }" style="column-gap: .25rem">
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': day.isHolyday }"></i>
                    {{ day.stdrdDt }}
                    <span class="fs-8" :class="day.isHolyday ? 'text-danger' : 'text-gray-600'">({{ day.journalDateWeekDay }})</span>
                    <span v-if="day.journalDatePrecision === 'APPROXIMATE'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
                    <span v-if="day.journalDatePrecision === 'UNKNOWN'" class="badge badge-light-primary ms-2">{{ day.journalDatePrecision }}</span>
                    <span class="fs-7 ms-4 text-muted" v-html="day.weather"></span>
                    <div v-if="day.holydayNm" class="w-100 ps-5 fs-6 fw-normal text-truncate">{{ day.holydayNm }}</div>
                </div>
            </div>
            <div class="col-3 d-none d-md-flex align-items-center gap-2">
                <button
                    v-if="showDiaries"
                    type="button"
                    class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="tooltip('txt.journal.chapter', 'bs.tooltip.modal.reg')"
                    @click="openChapterRegModal"
                >
                    <i class="bi bi-list-columns-reverse fs-4 pe-1"></i>
                    {{ t('txt.journal.chapter.reg') }}
                </button>
                <button
                    v-if="showDreams"
                    type="button"
                    class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="tooltip('txt.journal.dream', 'bs.tooltip.modal.reg')"
                    @click="openDreamRegModal"
                >
                    <i class="bi bi-moon-stars fs-4 pe-1"></i>
                    {{ t('txt.journal.dream.reg') }}
                </button>
                <JournalDayContextMenu :day="day" />
                <button
                    v-if="hasMeta()"
                    class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    data-bs-html="true"
                    data-bs-custom-class="meta-tooltip"
                    data-bs-sanitize="false"
                    :title="metaTooltipHtml()"
                >
                    <i class="bi bi-bar-chart"></i>
                </button>
            </div>
        </div>
        <div class="row">
            <div class="col-1 d-none d-md-flex"></div>
            <div class="col">
                <div v-if="hasVisibleTags()" class="ms-5 mt-3">
                    <i class="bi bi-tag"></i>
                    <span
                        v-for="tag in tagList()"
                        :key="tag.tagId + ':' + tag.name"
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
        </div>
        <div class="journal-day-content row p-5">
            <template v-if="showDiaries">
                <div v-if="hiddenChapterCtgrList().length > 0" class="d-flex align-items-center mb-3">
                    <div class="d-flex flex-wrap align-items-center gap-2 ps-1 ps-md-5">
                        <span class="badge badge-light-warning text-warning fw-semibold">CHAPTER FILTER</span>
                        <span class="text-muted fs-7">{{ t('txt.journal.day.list.hidden-chapter-ctgr') }}</span>
                        <span
                            v-for="ctgr in hiddenChapterCtgrList()"
                            :key="ctgr.categoryCode"
                            class="badge badge-light-secondary text-muted"
                        >{{ ctgr.categoryName }} {{ ctgr.categoryCode }}</span>
                    </div>
                </div>
                <JournalChapterItem
                    v-for="chapter in journalChapterList()"
                    :key="'chapter-' + chapter.id"
                    :chapter="chapter"
                />
            </template>
            <template v-if="showDreams">
                <JournalEntryItem
                    v-for="dream in journalDreamList()"
                    :key="'dream-' + dream.id"
                    :entry="dream"
                    content-type="JOURNAL_DREAM"
                />
                <JournalEntryItem
                    v-for="dream in journalElseDreamList()"
                    :key="'else-dream-' + dream.id"
                    :entry="dream"
                    content-type="JOURNAL_DREAM"
                />
            </template>
            <template v-else-if="hasDream()">
                <div class="d-flex align-items-center mt-2">
                    <div class="col ps-1 ps-md-5">
                        <span class="badge badge-light-secondary text-muted fw-normal">
                            <i class="bi bi-moon-stars me-1"></i>
                            {{ t('txt.journal.day.list.dreams-hidden') }}
                        </span>
                    </div>
                </div>
            </template>
        </div>
    </div>
    `,
};

export default JournalDayCard;
