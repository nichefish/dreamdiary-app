/**
 * JournalInterpretationItem.ts
 * Vue가 소유하는 저널 해석 항목.
 */

import journalDayUiBridgeService from "../../day/services/journalDayUiBridgeService.js";

const LIFECYCLE_OPTIONS = [
    { key: "OPEN", labelKey: "txt.lifecycle.open", activeClass: "text-gray-800" },
    { key: "PENDING", labelKey: "txt.lifecycle.pending", activeClass: "text-primary" },
    { key: "RESOLVED", labelKey: "txt.status.resolved", activeClass: "text-success" },
];

const JournalInterpretationItem = {
    name: "JournalInterpretationItem",
    props: { interpretation: { type: Object, required: true } },
    computed: {
        lifecycleOptions(): Record<string, string>[] { return LIFECYCLE_OPTIONS; },
        lifecycleKey(): string { return String(this.interpretation?.lifecycle?.lifecycleKey ?? ""); },
        commentList(): Record<string, any>[] { return Array.isArray(this.interpretation?.comment?.list) ? this.interpretation.comment.list : []; },
        hasHistory(): boolean { return Boolean(this.interpretation?.history?.historyTriggeredAt); },
    },
    methods: {
        t(key: string): string { return this.$t ? this.$t(key) : key; },
        hasState(stateKey: string): boolean {
            const states = this.interpretation?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        lifecycleLabelClass(option: Record<string, string>): string { return this.lifecycleKey === option.key ? option.activeClass : "text-muted"; },
        resolvedYn(): string { return this.lifecycleKey === "RESOLVED" ? "Y" : "N"; },
        toggleInterpretation(event: MouseEvent): void { dF.JournalInterpretation.toggle(this.interpretation.id, event.currentTarget as HTMLElement); },
        expandContent(event: MouseEvent): void {
            if (!(event.currentTarget instanceof HTMLElement)) return;
            /* 변경 전: <code>dF.JournalDayTag.expand</code>. 변경 후: UI 브리지 → 태그 서비스 단일 로직. */
            journalDayUiBridgeService.expandJournalDayTaggedContent(event.currentTarget);
        },
        openCommentRegistModal(): void { dF.Comment.modal.regModal(this.interpretation.id, "JOURNAL_INTERPRETATION"); },
        openCommentModifyModal(comment: Record<string, any>): void { dF.Comment.modal.mdfModal(comment.id); },
        deleteComment(comment: Record<string, any>): void { dF.Comment.modal.delAjax(comment.id); },
        copyInterpretation(): void { dF.JournalInterpretation.copy(this.interpretation.id); },
        openModifyModal(): void { dF.JournalInterpretation.modifyModal(this.interpretation.id); },
        openHistoryModal(): void { if (this.hasHistory) dF.History.modal.open("JOURNAL_INTERPRETATION", this.interpretation.id); },
        setLifecycle(lifecycleKey: string): void { dF.JournalInterpretation.setLifecycleAjax(this.interpretation.id, lifecycleKey); },
        toggleCollapsedState(): void { dF.JournalInterpretation.collapse(this.interpretation.id, this.hasState("COLLAPSED") ? "N" : "Y"); },
        deleteInterpretation(): void { dF.JournalInterpretation.deleteAjax(this.interpretation.id); },
        tooltip(labelKey: string, actionKey: string): string { return [this.t(labelKey), this.t(actionKey)].join(" "); },
    },
    template: `
    <div class="journal-interpretation-item ps-7" :data-id="interpretation.id">
        <div class="col-1 py-3 d-none d-md-flex-between border-2 border-gray-300 ps-5 me-2 h-75" style="width:85px;">
            <div class="d-flex flex-column align-items-center">
                <span :class="{ 'text-success': lifecycleKey === 'RESOLVED' }"># {{ interpretation.sortOrder }}</span>
                <button type="button" class="btn btn-sm btn-secondary ms-2 px-3 toggle-interpretation-btn" @click="toggleInterpretation">
                    <i class="bi pe-0 interpretation-toggle-icon" :class="hasState('COLLAPSED') ? 'bi-arrows-expand' : 'bi-arrows-collapse'"></i>
                </button>
            </div>
        </div>
        <div class="col journal-interpretation-content" :class="{ 'bg-secondary': hasState('IMPRTC') }" :data-id="interpretation.id" :data-lifecycle="lifecycleKey" :data-resolved="resolvedYn()">
            <div class="d-flex-align-center title-wrap"><div v-if="interpretation.title" class="d-inline-block fw-bold">{{ interpretation.title }}</div></div>
            <div class="journal-content" :class="{ collapsed: hasState('COLLAPSED') }" v-html="interpretation.markdownContent"></div>
            <button type="button" class="btn btn-xxs btn-active-light-info badge-light-primary btn-outlined expand-btn" @click="expandContent"></button>
            <div v-for="(comment, index) in commentList" :key="'interpretation-comment-' + comment.id" :class="['row d-flex-align-center', { 'mt-2': index === 0, 'mb-1': index === commentList.length - 1 }]">
                <div class="col d-flex-align-center position-relative ms-4"><div class="li text-comment" v-html="comment.markdownContent"></div></div>
                <div class="col-1">
                    <button type="button" class="btn btn-sm btn-light-primary btn-outlined py-1 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.comment', 'bs.tooltip.modal.modify')" @click="openCommentModifyModal(comment)"><i class="bi bi-pencil-square p-0"></i></button>
                    <button type="button" class="btn btn-sm btn-light-danger btn-outlined py-1 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.comment', 'bs.tooltip.del')" @click="deleteComment(comment)"><i class="bi bi-trash p-0"></i></button>
                </div>
            </div>
        </div>
        <div class="col-1 ms-4 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')" @click="openCommentRegistModal"><i class="bi bi-chat-left-dots p-0"></i></button>
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.copy')" @click="copyInterpretation"><i class="bi bi-copy p-0"></i></button>
            <div class="me-0 d-flex align-items-center">
                <button class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary" data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.context.menu.show')"><i class="ki-solid ki-dots-horizontal fs-2x"></i></button>
                <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
                    <div class="menu-item px-3"><div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t("txt.journal.interpretation") }}</div></div>
                    <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.interpretation', 'bs.tooltip.modal.modify')"><div class="menu-link flex-stack px-3" @click="openModifyModal">{{ t("txt.comm.edit") }}<i class="bi bi-pencil-square fs-8"></i></div></div>
                    <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="hasHistory ? t('txt.history') : t('txt.history.empty')"><div class="menu-link flex-stack px-3" :class="{ 'disabled text-muted': !hasHistory }" @click="openHistoryModal">{{ t("txt.history") }}<i class="bi bi-clock-history fs-8"></i></div></div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                        <a href="#" class="menu-link px-3" @click.prevent><span class="menu-title">{{ t("txt.lifecycle") }}</span><span class="menu-arrow"></span></a>
                        <div class="menu-sub menu-sub-dropdown w-175px py-4">
                            <div v-for="option in lifecycleOptions" :key="'interpretation-lifecycle-' + option.key" class="menu-item px-3">
                                <div class="menu-content px-3">
                                    <label class="form-check form-check-custom form-check-solid cursor-pointer">
                                        <input class="form-check-input w-18px h-18px cursor-pointer interpretation-context-lifecycle-check" :class="{ 'interpretation-context-resolved-check': option.key === 'RESOLVED' }" type="radio" :name="'interpretation-lifecycle-' + interpretation.id" :value="option.key" :checked="lifecycleKey === option.key" @click="setLifecycle(option.key)">
                                        <span class="form-check-label fs-7" :class="lifecycleLabelClass(option)">{{ t(option.labelKey) }}</span>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3"><div class="menu-content px-3"><label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.status.collapsed')"><input class="form-check-input w-30px h-20px cursor-pointer interpretation-context-collapsed-check" type="checkbox" value="1" :checked="hasState('COLLAPSED')" @click="toggleCollapsedState"><span class="form-check-label text-muted fs-7">{{ t("txt.status.collapsed") }}</span></label></div></div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.interpretation', 'bs.tooltip.del')"><div class="menu-link flex-stack px-3 text-danger" @click="deleteInterpretation">{{ t("txt.comm.del") }}<i class="bi bi-trash text-danger p-0 fs-8"></i></div></div>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalInterpretationItem;
