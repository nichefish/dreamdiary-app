import JournalEntryContent from "./JournalEntryContent.js";
import JournalEntryContextMenu from "./JournalEntryContextMenu.js";
import JournalInterpretationItem from "../../interpretation/components/JournalInterpretationItem.js";

const JournalEntrySearchItem = {
    name: "JournalEntrySearchItem",
    components: {
        JournalEntryContent,
        JournalEntryContextMenu,
        JournalInterpretationItem,
    },
    props: {
        entry: { type: Object, required: true },
        contentType: { type: String, required: true },
        contentLabel: { type: String, required: true },
        cssPrefix: { type: String, required: true },
        iconIdPrefix: { type: String, required: true },
        showDreamStates: { type: Boolean, default: false },
        highlightImportant: { type: Boolean, default: false },
        rightBorderClass: { type: String, default: "" },
    },
    methods: {
        hasState(stateKey: string): boolean {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        isResolved(): boolean {
            return this.entry?.lifecycle?.lifecycleKey === "RESOLVED";
        },
        checkedYn(stateKey: string): string {
            return this.hasState(stateKey) ? "Y" : "N";
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
        interpretationList(): Record<string, any>[] {
            return Array.isArray(this.entry?.journalInterpretationList) ? this.entry.journalInterpretationList : [];
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
            :data-resolved="isResolved() ? 'Y' : 'N'"
            :data-imprtc="checkedYn('IMPRTC')"
            :data-refrnc="checkedYn('REFRNC')"
            :data-nhtmr="showDreamStates ? checkedYn('NHTMR') : null"
            :data-halluc="showDreamStates ? checkedYn('HALLUC') : null"
        >
            <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 ps-5 me-2 h-75" style="width:85px;">
                <div class="d-flex flex-column align-items-center">
                    <span :class="['journal-' + cssPrefix + '-idx', { 'text-success': isResolved() }]"># {{ entry.sortOrder }}</span>
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
                <div class="d-flex flex-wrap align-items-center fs-5 fw-bold mb-2 ps-1">
                    <span>{{ entry.stdrdDt }} ({{ entry.journalDateWeekDay }})</span>
                </div>
                <div :class="'journal-' + cssPrefix + '-content p-2 ' + (highlightImportant && hasState('IMPRTC') ? 'bg-secondary' : '')">
                    <JournalEntryContent :entry="entry" :content-type="contentType" />
                </div>
            </div>
            <div :class="['col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px', rightBorderClass]">&nbsp;</div>
            <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" @click="openCommentRegModal">
                    <i class="bi bi-chat-left-dots p-0"></i>
                </button>
                <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" @click="copyEntry">
                    <i class="bi bi-copy p-0"></i>
                </button>
                <JournalEntryContextMenu
                    :entry="entry"
                    :content-type="contentType"
                    :content-label="contentLabel"
                    :css-prefix="cssPrefix"
                    :show-day-open="true"
                    :show-interpretation="true"
                    :show-related="true"
                    :show-dream-states="showDreamStates"
                />
            </div>
        </div>
        <div class="journal-entry-search-related journal-item" :data-entry-id="entry.id">
            <JournalInterpretationItem
                v-for="interpretation in interpretationList()"
                :key="'interpretation-' + interpretation.id"
                :interpretation="interpretation"
            />
        </div>
    </div>
    `,
};

export default JournalEntrySearchItem;
