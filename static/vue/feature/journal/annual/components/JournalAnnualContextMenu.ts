/**
 * JournalAnnualContextMenu.ts
 * 저널 결산(annual) 목록 카드의 컨텍스트 메뉴 — Handlebars partial 대체.
 *
 * 변경(A-5-α):
 *   - `_partial/_journal_annual_context_btn_partial.hbs` (id `journal_annual_context_btn_partial`) 의
 *     마크업/이벤트(상세 보기 / 등록(수정) 모달)를 본 컴포넌트로 흡수한다.
 *   - 호출 시그니처 보존: `dF.JournalAnnual.dtlView(yy)` / `dF.JournalAnnual.mdfModal(yy)`.
 *   - 메시지 키는 기존 partial 의 `<@spring.message ... />` 출력값을 보존하기 위해 `Message.get(...)` 으로 옮긴다.
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단(window/globalThis.Message 우선 결의 + key 폴백).
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

const JournalAnnualContextMenu = {
    name: "JournalAnnualContextMenu",
    props: {
        annual: { type: Object, required: true },
    },
    methods: {
        t(key: string): string {
            return resolveMessage(key);
        },
        tooltip(labelKey: string, actionKey: string): string {
            const label = this.t(labelKey);
            const action = this.t(actionKey);
            return [label, action].filter((value: string): boolean => value.length > 0).join(" ");
        },
        /** 상세 보기 (새 창 이동 — `dF.JournalAnnual.dtlView(yy)` 가 location.href 처리). */
        openDetached(): void {
            (window as any).dF?.JournalAnnual?.dtlView?.(this.annual.yy);
        },
        /** 등록(수정) 모달 호출. */
        openModifyModal(): void {
            (window as any).dF?.JournalAnnual?.mdfModal?.(this.annual.yy);
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
        <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true" style="">
            <div class="menu-item px-3">
                <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('txt.journal.annual') }}</div>
            </div>
            <div
                class="menu-item px-3 my-1 cursor-pointer"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="tooltip('txt.journal.annual', 'txt.comm.open-in-new-window')"
            >
                <div class="menu-link flex-stack px-3" @click="openDetached">
                    {{ t('txt.sumry') }} {{ t('txt.comm.open-in-new-window') }}
                    <i class="bi bi-window-stack fs-8"></i>
                </div>
            </div>
            <div
                class="menu-item px-3 my-1 cursor-pointer"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                :title="tooltip('txt.journal.annual', 'bs.tooltip.modal.mdf')"
            >
                <div class="menu-link flex-stack px-3" @click="openModifyModal">
                    {{ t('txt.sumry') }} {{ t('txt.comm.edit') }}
                    <i class="bi bi-pencil-square fs-8"></i>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalAnnualContextMenu;
