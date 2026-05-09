/**
 * JournalAnnualReviewContextMenu.ts
 * 저널 결산 리뷰 행 컨텍스트 메뉴 — `_journal_annual_review_context_btn_partial.hbs` 대체.
 *
 * 변경(A-7-β):
 *   - 변경 전: Handlebars partial `journal_annual_review_context_btn_partial` 의 KTMenu 마크업 +
 *     `dF.JournalAnnualReview.mdfModal(id)` / `delAjax(id)` onclick.
 *   - 변경 후: 동일 class/data-kt-* 속성·레이아웃 보존(UI 변경 0).
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

const JournalAnnualReviewContextMenu = {
    name: "JournalAnnualReviewContextMenu",
    props: {
        reviewId: { type: [Number, String], required: true },
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
        openMdf(): void {
            (window as any).dF?.JournalAnnualReview?.mdfModal?.(this.reviewId);
        },
        openDel(): void {
            (window as any).dF?.JournalAnnualReview?.delAjax?.(this.reviewId);
        },
    },
    template: `
    <div class="me-0 d-flex align-items-center">
        <button class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
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
                <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('txt.journal.annual.review') }}</div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                 :title="tooltip('txt.journal.annual.review', 'bs.tooltip.modal.mdf')"
            >
                <div class="menu-link flex-stack px-3" @click="openMdf">
                    {{ t('txt.comm.edit') }}
                    <i class="bi bi-pencil-square fs-8"></i>
                </div>
            </div>
            <div class="separator my-2"></div>
            <div class="menu-item px-3 my-1 cursor-pointer"
                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                 :title="tooltip('txt.journal.annual.review', 'bs.tooltip.del')"
            >
                <div class="menu-link flex-stack px-3 text-danger" @click="openDel">
                    {{ t('txt.comm.del') }}
                    <i class="bi bi-trash text-danger p-0 fs-8"></i>
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalAnnualReviewContextMenu;
