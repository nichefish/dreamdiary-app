/**
 * JournalEntryContextMenu.ts
 * Vue가 소유하는 저널 엔트리 컨텍스트 메뉴.
 */

const LIFECYCLE_OPTIONS = [
    { key: "OPEN", labelKey: "txt.lifecycle.open", activeClass: "text-gray-800" },
    { key: "PENDING", labelKey: "txt.lifecycle.pending", activeClass: "text-primary" },
    { key: "RESOLVED", labelKey: "txt.status.resolved", activeClass: "text-success" },
];

const STATUS_OPTIONS = [
    { key: "IMPRTC", labelKey: "txt.status.imprtc", activeClass: "text-danger", inputClass: "imprtc" },
    { key: "REFRNC", labelKey: "txt.status.refrnc", activeClass: "text-warning", inputClass: "refrnc" },
];

const DREAM_STATUS_OPTIONS = [
    { key: "NHTMR", labelKey: "txt.dream.nhtmr", activeClass: "text-info", inputClass: "nhtmr" },
    { key: "HALLUC", labelKey: "txt.dream.halluc", activeClass: "text-gray-700", inputClass: "halluc" },
];

const JournalEntryContextMenu = {
    name: "JournalEntryContextMenu",
    props: {
        entry: { type: Object, required: true },
        contentType: { type: String, required: true },
        contentLabel: { type: String, required: true },
        cssPrefix: { type: String, required: true },
        showInterpretation: { type: Boolean, default: true },
        showRelated: { type: Boolean, default: true },
        showDayOpen: { type: Boolean, default: false },
        showDreamStates: { type: Boolean, default: false },
    },
    computed: {
        lifecycleOptions(): Record<string, string>[] {
            return LIFECYCLE_OPTIONS;
        },
        statusOptions(): Record<string, string>[] {
            return STATUS_OPTIONS;
        },
        dreamStatusOptions(): Record<string, string>[] {
            return DREAM_STATUS_OPTIONS;
        },
        lifecycleKey(): string {
            return String(this.entry?.lifecycle?.lifecycleKey ?? "");
        },
        hasHistory(): boolean {
            return Boolean(this.entry?.history?.historyTriggeredAt);
        },
        canAddRelated(): boolean {
            return this.showRelated && this.entry?.elseDreamYn !== "Y";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        module(): any {
            return dF.JournalEntry.get(this.contentType);
        },
        hasState(stateKey: string): boolean {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        tooltip(labelKey: string, actionKey: string): string {
            return [this.t(labelKey), this.t(actionKey)].join(" ");
        },
        contentTooltip(actionKey: string): string {
            return [this.contentLabel, this.t(actionKey)].join(" ");
        },
        statusLabelClass(option: Record<string, string>): string {
            return this.hasState(option.key) ? option.activeClass : "text-muted";
        },
        lifecycleLabelClass(option: Record<string, string>): string {
            return this.lifecycleKey === option.key ? option.activeClass : "text-muted";
        },
        openDayDetached(): void {
            /* 변경 후(Phase 9): dF.JournalDayRuntimeService.openDetatched() 제거 후 직접 호출. */
            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt: this.entry.stdrdDt });
            window.open(url, "_blank", "noopener,noreferrer");
        },
        openInterpretationRegModal(): void {
            dF.JournalInterpretation.regModal({
                journalDayId: this.entry.journalDayId,
                refId: this.entry.id,
                refContentType: this.contentType,
                stdrdDt: this.entry.stdrdDt,
                journalDateWeekDay: this.entry.journalDateWeekDay,
            });
        },
        openMdfModal(): void {
            this.module().mdfModal(this.entry.id, this.entry.stdrdDt);
        },
        openHistoryModal(): void {
            if (!this.hasHistory) return;
            window.dispatchEvent(new CustomEvent("history:open-modal", {
                detail: { contentType: this.contentType, id: this.entry.id },
            }));
        },
        openRelatedAddModal(): void {
            dF.RelatedContent.openAddModalBySource(this.contentType, this.entry.id);
        },
        setLifecycle(lifecycleKey: string): void {
            this.module().setLifecycleAjax(this.entry.id, lifecycleKey);
        },
        toggleStatus(stateKey: string): void {
            const module = this.module();
            if (stateKey === "IMPRTC") module.imprtcAjax(this.entry.id);
            if (stateKey === "REFRNC") module.refrncAjax(this.entry.id);
            if (stateKey === "NHTMR") module.nhtmrAjax(this.entry.id);
            if (stateKey === "HALLUC") module.hallucAjax(this.entry.id);
            if (stateKey === "COLLAPSED") module.collapseAjax(this.entry.id);
        },
        deleteEntry(): void {
            this.module().delAjax(this.entry.id);
        },
    },
    template: `
    <div class="me-0 d-flex align-items-center">
        <button
            class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            data-bs-dismiss="click"
            :title="t('bs.tooltip.context.menu.show')"
        >
            <i class="ki-solid ki-dots-horizontal fs-2x"></i>
        </button>
        <div
            class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
            data-kt-menu="true"
        >
            <div class="menu-item px-3">
                <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ contentLabel }}</div>
            </div>
            <template v-if="showDayOpen">
                <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.day', 'bs.tooltip.modal.mdf')">
                    <div class="menu-link flex-stack px-3" @click="openDayDetached">
                        {{ t('txt.comm.open-in-new-window') }}
                        <i class="bi bi-window-stack fs-8"></i>
                    </div>
                </div>
                <div class="separator my-2"></div>
            </template>
            <template v-if="showInterpretation">
                <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.interpretation', 'bs.tooltip.modal.reg')">
                    <div class="menu-link flex-stack px-3" @click="openInterpretationRegModal">
                        {{ t('txt.journal.interpretation.reg') }}
                        <i class="bi bi-layers-half fs-8"></i>
                    </div>
                </div>
                <div class="separator my-2"></div>
            </template>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="contentTooltip('bs.tooltip.modal.mdf')">
                <div class="menu-link flex-stack px-3" @click="openMdfModal">
                    {{ t('txt.comm.edit') }}
                    <i class="bi bi-pencil-square fs-8"></i>
                </div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="hasHistory ? t('txt.history') : t('txt.history.empty')">
                <div class="menu-link flex-stack px-3" :class="{ 'disabled text-muted': !hasHistory }" @click="openHistoryModal">
                    {{ t('txt.history') }}
                    <i class="bi bi-clock-history fs-8"></i>
                </div>
            </div>
            <div v-if="canAddRelated" class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.related-content.add')">
                <div class="menu-link flex-stack px-3" @click="openRelatedAddModal">
                    {{ t('txt.related-content.add') }}
                    <i class="bi bi-link-45deg fs-8"></i>
                </div>
            </div>
            <div class="separator my-2"></div>
            <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                <a href="#" class="menu-link px-3" @click.prevent>
                    <span class="menu-title">{{ t('txt.lifecycle') }}</span>
                    <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown w-175px py-4">
                    <div v-for="option in lifecycleOptions" :key="'entry-lifecycle-' + option.key" class="menu-item px-3">
                        <div class="menu-content px-3">
                            <label class="form-check form-check-custom form-check-solid cursor-pointer">
                                <input class="form-check-input w-18px h-18px cursor-pointer" :class="[cssPrefix + '-context-lifecycle-check', { [cssPrefix + '-context-resolved-check']: option.key === 'RESOLVED' }]" type="radio" :name="cssPrefix + '-lifecycle-' + entry.id" :value="option.key" :checked="lifecycleKey === option.key" @click="setLifecycle(option.key)">
                                <span class="form-check-label fs-7" :class="lifecycleLabelClass(option)">{{ t(option.labelKey) }}</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                <a href="#" class="menu-link px-3" @click.prevent>
                    <span class="menu-title">{{ t('txt.comm.status') }}</span>
                    <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown w-175px py-4">
                    <div v-for="option in statusOptions" :key="'entry-status-' + option.key" class="menu-item px-3">
                        <div class="menu-content px-3">
                            <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t(option.labelKey)">
                                <input class="form-check-input w-30px h-20px cursor-pointer" :class="cssPrefix + '-context-' + option.inputClass + '-check'" type="checkbox" value="1" :checked="hasState(option.key)" @click="toggleStatus(option.key)">
                                <span class="form-check-label fs-7" :class="statusLabelClass(option)">{{ t(option.labelKey) }}</span>
                            </label>
                        </div>
                    </div>
                    <template v-if="showDreamStates">
                        <div v-for="option in dreamStatusOptions" :key="'dream-status-' + option.key" class="menu-item px-3">
                            <div class="menu-content px-3">
                                <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t(option.labelKey)">
                                    <input class="form-check-input w-30px h-20px cursor-pointer" :class="'dream-context-' + option.inputClass + '-check'" type="checkbox" value="1" :checked="hasState(option.key)" @click="toggleStatus(option.key)">
                                    <span class="form-check-label fs-7" :class="statusLabelClass(option)">{{ t(option.labelKey) }}</span>
                                </label>
                            </div>
                        </div>
                    </template>
                    <div class="menu-item px-3">
                        <div class="menu-content px-3">
                            <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.status.collapsed')">
                                <input class="form-check-input w-30px h-20px cursor-pointer" :class="cssPrefix + '-context-collapsed-check'" type="checkbox" value="1" :checked="hasState('COLLAPSED')" @click="toggleStatus('COLLAPSED')">
                                <span class="form-check-label fs-7" :class="hasState('COLLAPSED') ? 'text-gray-700' : 'text-muted'">{{ t('txt.status.collapsed') }}</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="separator my-2"></div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="contentTooltip('bs.tooltip.del')">
                <div class="menu-link flex-stack px-3 text-danger" @click="deleteEntry">
                    {{ t('txt.comm.del') }}
                    <i class="bi bi-trash text-danger p-0 fs-8"></i>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalEntryContextMenu;
