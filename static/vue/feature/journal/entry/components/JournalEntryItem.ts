/**
 * JournalEntryItem.ts
 * Vue가 소유하는 저널 엔트리 셸.
 */

import JournalEntryContextMenu from "./JournalEntryContextMenu.js";
import JournalEntryContent from "./JournalEntryContent.js";
import JournalInterpretationItem from "../../interpretation/components/JournalInterpretationItem.js";

const JournalEntryItem = {
    name: "JournalEntryItem",
    components: {
        JournalEntryContextMenu,
        JournalEntryContent,
        JournalInterpretationItem,
    },
    props: {
        entry: { type: Object, required: true },
        contentType: { type: String, required: true },
    },
    computed: {
        isDream(): boolean {
            return this.contentType === "JOURNAL_DREAM";
        },
        cssPrefix(): string {
            return this.isDream ? "dream" : "diary";
        },
        idxPrefix(): string {
            return this.isDream ? "dream" : "diary";
        },
        iconIdPrefix(): string {
            return this.isDream ? "dream-toggle-icon-" : "";
        },
        contentLabel(): string {
            return this.isDream ? this.t("txt.journal.dream") : this.t("txt.journal.diary");
        },
        rightBorderClass(): string {
            return this.isDream ? "" : "ms-4";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        hasState(stateKey: string): boolean {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        isResolved(): boolean {
            return this.entry?.lifecycle?.lifecycleKey === "RESOLVED";
        },
        resolvedYn(): string {
            return this.isResolved() ? "Y" : "N";
        },
        checkedYn(stateKey: string): string {
            return this.hasState(stateKey) ? "Y" : "N";
        },
        interpretationList(): Record<string, any>[] {
            return Array.isArray(this.entry?.journalInterpretationList) ? this.entry.journalInterpretationList : [];
        },
        toggleEntry(event: MouseEvent): void {
            dF.JournalEntry.get(this.contentType).toggle(this.entry.id, event.currentTarget);
        },
        openCommentRegModal(): void {
            dF.Comment.modal.regModal(this.entry.id, this.contentType);
        },
        copyEntry(): void {
            dF.JournalEntry.get(this.contentType).copy(this.entry.id);
        },
        tooltip(labelKey: string, actionKey: string): string {
            return [this.t(labelKey), this.t(actionKey)].join(" ");
        },
    },
    template: `
    <div>
        <div
            :class="['journal-' + cssPrefix + '-item', { 'is-collapsed': hasState('COLLAPSED') }]"
            :data-id="entry.id"
            :data-stdrd-dt="entry.stdrdDt"
            :data-yy="entry.yy"
            :data-mnth="entry.mnth"
            :data-collapsed="hasState('COLLAPSED') ? 'Y' : 'N'"
            :data-lifecycle="entry.lifecycle?.lifecycleKey"
            :data-resolved="resolvedYn()"
            :data-imprtc="checkedYn('IMPRTC')"
            :data-refrnc="checkedYn('REFRNC')"
            :data-nhtmr="isDream ? checkedYn('NHTMR') : null"
            :data-halluc="isDream ? checkedYn('HALLUC') : null"
        >
            <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 ps-5 me-2 h-75" style="width:85px;">
                <div class="d-flex flex-column align-items-center">
                    <span :class="['journal-' + idxPrefix + '-idx', { 'text-success': isResolved() }]"># {{ entry.sortOrder }}</span>
                    <button
                        type="button"
                        :class="['btn btn-sm ms-2 px-3 toggle-' + cssPrefix + '-btn', { 'is-active': hasState('COLLAPSED') }]"
                        @click="toggleEntry"
                    >
                        <i
                            :id="iconIdPrefix ? iconIdPrefix + entry.id : null"
                            :class="['bi pe-0 ' + cssPrefix + '-toggle-icon', hasState('COLLAPSED') ? 'bi-arrows-expand' : 'bi-arrows-collapse']"
                        ></i>
                    </button>
                </div>
            </div>
            <div class="col">
                <div v-if="isDream" class="d-flex journal-dream-states ms-2 mb-1 gap-1">
                    <div class="d-flex-align-center title-wrap">
                        <div class="dream-nhtmr-badge ctgr-span ctgr-info w-auto" :class="{ 'd-none': !hasState('NHTMR') }">!{{ t('txt.dream.nhtmr') }}</div>
                        <div class="dream-halluc-badge ctgr-span ctgr-gray w-auto" :class="{ 'd-none': !hasState('HALLUC') }">!{{ t('txt.dream.halluc') }}</div>
                        <div v-if="entry.elseDreamYn === 'Y'" class="ctgr-span ctgr-gray w-70px d-flex-center">({{ entry.elseDreamerNm }} {{ t('txt.dream') }})</div>
                        <div v-if="entry.title" class="d-inline-block fw-bold">{{ entry.title }}</div>
                    </div>
                </div>
                <div :class="'journal-' + cssPrefix + '-content p-2'">
                    <JournalEntryContent :entry="entry" :content-type="contentType" />
                </div>
            </div>
            <div :class="['col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px', rightBorderClass]">&nbsp;</div>
            <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')" @click="openCommentRegModal">
                    <i class="bi bi-chat-left-dots p-0"></i>
                </button>
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.copy')" @click="copyEntry">
                    <i class="bi bi-copy p-0"></i>
                </button>
                <JournalEntryContextMenu
                    :entry="entry"
                    :content-type="contentType"
                    :content-label="contentLabel"
                    :css-prefix="cssPrefix"
                    :show-interpretation="true"
                    :show-related="true"
                    :show-dream-states="isDream"
                />
            </div>
        </div>
        <div v-if="interpretationList().length > 0" class="journal-item">
            <JournalInterpretationItem
                v-for="interpretation in interpretationList()"
                :key="'interpretation-' + interpretation.id"
                :interpretation="interpretation"
            />
        </div>
    </div>
    `,
};

export default JournalEntryItem;
