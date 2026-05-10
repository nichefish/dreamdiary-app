/**
 * JournalDayContextMenu.ts
 * 저널 일자(journal_day) 헤더 컨텍스트 메뉴.
 *
 * 변경 전: dF.JournalDayCrudService(레거시)를 직접 호출했다.
 * 변경 후: CRUD 액션은 journalDayCrudService(Vue 소유)를 통해 수행한다.
 */

import journalDayCrudService from "../services/journalDayCrudService.js";
import journalDayUiBridgeService from "../services/journalDayUiBridgeService.js";

const JournalDayContextMenu = {
    name: "JournalDayContextMenu",
    props: {
        day: { type: Object, required: true },
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
        hasState(stateKey: string): boolean {
            const states = this.day?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        openDetached(): void {
            journalDayUiBridgeService.openDetached(this.day.stdrdDt);
        },
        moveToWeeklyView(): void {
            journalDayUiBridgeService.moveToWeeklyView(this.day.stdrdDt);
        },
        openModifyModal(): void {
            journalDayCrudService.openMdfModal(this.day.id);
        },
        toggleCollapsed(): void {
            dF.JournalEntry.get("JOURNAL_DIARY").collapseAjax(this.day.id);
        },
        deleteDay(): void {
            journalDayCrudService.delAjax(this.day.id);
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
        <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
            <div class="menu-item px-3">
                <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('txt.journal.day') }}</div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.day', 'bs.tooltip.modal.mdf')">
                <div class="menu-link flex-stack px-3" @click="openDetached">
                    {{ t('txt.comm.open-in-new-window') }}
                    <i class="bi bi-window-stack fs-8"></i>
                </div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.comm.go-to-weekly-view')">
                <div class="menu-link flex-stack px-3" @click="moveToWeeklyView">
                    {{ t('txt.comm.go-to-weekly-view') }}
                    <i class="bi bi-calendar-week fs-8"></i>
                </div>
            </div>
            <div class="separator my-2"></div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.day', 'bs.tooltip.modal.mdf')">
                <div class="menu-link flex-stack px-3" @click="openModifyModal">
                    {{ t('txt.comm.edit') }}
                    <i class="bi bi-pencil-square fs-8"></i>
                </div>
            </div>
            <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                <a href="#" class="menu-link px-3" @click.prevent>
                    <span class="menu-title">{{ t('txt.comm.status') }}</span>
                    <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown w-175px py-4">
                    <div class="menu-item px-3">
                        <div class="menu-content px-3">
                            <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.status.imprtc')">
                                <input class="form-check-input w-30px h-20px cursor-pointer" type="checkbox" value="1" :checked="hasState('IMPRTC')">
                                <span class="form-check-label text-muted fs-7">{{ t('txt.status.imprtc') }}</span>
                            </label>
                        </div>
                    </div>
                    <div class="menu-item px-3">
                        <div class="menu-content px-3">
                            <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('txt.status.collapsed')">
                                <input class="form-check-input w-30px h-20px cursor-pointer diary-context-collapsed-check" type="checkbox" value="1" :checked="hasState('COLLAPSED')" @click="toggleCollapsed">
                                <span class="form-check-label text-muted fs-7">{{ t('txt.status.collapsed') }}</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="separator my-2"></div>
            <div class="menu-item px-3 my-1 cursor-pointer" data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="tooltip('txt.journal.day', 'bs.tooltip.del')">
                <div class="menu-link flex-stack px-3 text-danger" @click="deleteDay">
                    {{ t('txt.comm.del') }}
                    <i class="bi bi-trash text-danger p-0 fs-8"></i>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalDayContextMenu;
