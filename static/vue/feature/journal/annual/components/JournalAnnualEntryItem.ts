/**
 * JournalAnnualEntryItem.ts
 * 저널 결산(annual) 상세 — 엔트리 리스트 1행 컴포넌트.
 *
 * 변경(A-7-γ):
 *   - 변경 전: `_journal_annual_entry_list_template.hbs` (id `journal_annual_entry_list`) 의 each 본문이
 *     `journal_day_stdrd_dt_partial` + `journal_day_context_btn_partial` + `journal_entry_content_partial`
 *     + `comment_reg_btn_partial` + `journal_entry_copy_btn_partial` + `journal_entry_context_btn_partial` 로
 *     1행을 합성했다(`cF.handlebars.compile` 경유).
 *   - 변경 후: 본 컴포넌트가 동일 마크업을 1:1 재현한다. 좌열 일자 셀(stdrdDt partial)은 인라인,
 *     일자 컨텍스트 메뉴는 `JournalDayContextMenu` Vue 재사용(어댑터 day = { id: entry.journalDayId, stdrdDt, state }).
 *     중간 본문은 `JournalEntryContent`, 우열 컨텍스트 메뉴는 `JournalEntryContextMenu` 재사용.
 *
 * 보존 규칙:
 *   - DOM 구조·CSS 클래스·on-click/모달 진입 시그니처는 레거시 partial 1:1.
 *   - `contextFirst` 분기(DREAM 만 day_context_btn 이 stdrdDt 보다 먼저)는 그대로 유지한다.
 *   - `highlightImportant` && IMPRTC 상태 시 외곽 content wrapper 에 `bg-secondary` 부여(레거시와 동일).
 *   - 본문 안쪽 `journal-content` 클래스 처리는 `entry.contentClass`(buildViewModel 결과) 가 있으면 그대로 사용.
 *
 * @author nichefish
 */

import JournalEntryContent from "../../entry/components/JournalEntryContent.js";
import JournalEntryContextMenu from "../../entry/components/JournalEntryContextMenu.js";
import JournalDayContextMenu from "../../day/components/JournalDayContextMenu.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
import { resolveMessage } from "../../../../common/messageHelper.js";

interface AnnualEntryRowConfig {
    contentType: string;     // "JOURNAL_DIARY" | "JOURNAL_DREAM"
    cssPrefix: string;       // "diary" | "dream"
    contentLabel: string;    // 컨텍스트 메뉴 헤더 라벨
    contentPaddingClass?: string;  // "p-2" / "p-3"
    contextFirst?: boolean;  // true 면 day_context_btn 을 stdrdDt 보다 먼저 배치(DREAM)
    highlightImportant?: boolean;  // 본문 외곽에 IMPRTC 강조(`bg-secondary`)
    showDreamStates?: boolean;     // DREAM 한정 NHTMR/HALLUC 토글 노출
}

const JournalAnnualEntryItem = {
    name: "JournalAnnualEntryItem",
    components: {
        JournalEntryContent,
        JournalEntryContextMenu,
        JournalDayContextMenu,
    },
    props: {
        entry: { type: Object, required: true },
        config: { type: Object, required: true },
    },
    computed: {
        cssPrefix(): string {
            return String((this.config as AnnualEntryRowConfig).cssPrefix ?? "");
        },
        contentType(): string {
            return String((this.config as AnnualEntryRowConfig).contentType ?? "");
        },
        contentLabel(): string {
            return String((this.config as AnnualEntryRowConfig).contentLabel ?? "");
        },
        contentPaddingClass(): string {
            return String((this.config as AnnualEntryRowConfig).contentPaddingClass ?? "");
        },
        contextFirst(): boolean {
            return Boolean((this.config as AnnualEntryRowConfig).contextFirst);
        },
        highlightImportant(): boolean {
            return Boolean((this.config as AnnualEntryRowConfig).highlightImportant);
        },
        showDreamStates(): boolean {
            return Boolean((this.config as AnnualEntryRowConfig).showDreamStates);
        },
        /**
         * 변경 전: `journal_day_context_btn_partial this id=this.journalDayId` — `this` 는 entry view-model 이지만
         *   `id` 만 `journalDayId` 로 override 했다. 외곽 day 컨텍스트 메뉴는 entry.state 를 day.state 로 그대로 사용한다.
         * 변경 후: 동일 의미의 어댑터 객체를 합성해 `JournalDayContextMenu` 에 전달.
         */
        adaptedDay(): Record<string, any> {
            return {
                id: this.entry?.journalDayId,
                stdrdDt: this.entry?.stdrdDt,
                state: this.entry?.state,
            };
        },
        contentWrapperClass(): (string | Record<string, boolean>)[] {
            const classes: (string | Record<string, boolean>)[] = ["col"];
            if (this.contentPaddingClass.length > 0) classes.push(this.contentPaddingClass);
            classes.push(`journal-${this.cssPrefix}-content`);
            if (this.highlightImportant && this.hasImprtc()) classes.push("bg-secondary");
            return classes;
        },
        isHolyday(): boolean {
            return Boolean(this.entry?.isHolyday);
        },
        precisionLabel(): string {
            return String(this.entry?.journalDatePrecision ?? "");
        },
        showPrecisionBadge(): boolean {
            const p: string = this.precisionLabel;
            return p === "APPROXIMATE" || p === "UNKNOWN";
        },
        hasHolydayNm(): boolean {
            return cF.util.isNotEmpty(this.entry?.holydayNm);
        },
    },
    methods: {
        t(key: string): string {
            return resolveMessage(key);
        },
        tooltip(labelKey: string, actionKey: string): string {
            const label: string = this.t(labelKey);
            const action: string = this.t(actionKey);
            return [label, action].filter((value: string): boolean => value.length > 0).join(" ");
        },
        hasImprtc(): boolean {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === "IMPRTC");
        },
        /**
         * 변경 전: `comment_reg_btn_partial` onclick="dF.Comment.modal.regModal({{id}}, '{{contentType}}');".
         */
        openCommentRegModal(): void {
            const ns: any = (window as any).dF?.Comment?.modal;
            if (!ns?.regModal) {
                console.error("[JournalAnnualEntryItem] dF.Comment.modal.regModal 미등록.");
                return;
            }
            ns.regModal(this.entry.id, this.contentType);
        },
        /**
         * 변경 전: `journal_entry_copy_btn_partial` onclick="{{module}}.copy({{id}});" — module 은 contentType 분기로 결정된 entry module.
         * 변경 후: 동일 의미로 `dF.JournalEntry.get(contentType).copy(entry.id)` 직접 호출.
         */
        copyEntry(): void {
            const ns: any = (window as any).dF?.JournalEntry?.get?.(this.contentType);
            if (!ns?.copy) {
                console.error("[JournalAnnualEntryItem] dF.JournalEntry.get(%s).copy 미등록.", this.contentType);
                return;
            }
            ns.copy(this.entry.id);
        },
    },
    template: `
    <div :class="'journal-' + cssPrefix + '-item'" :data-id="entry.id">
        <div class="col-12 col-md-1 d-flex flex-wrap align-items-center fs-5 fw-bold">
            <template v-if="contextFirst">
                <JournalDayContextMenu :day="adaptedDay" />
                <div :class="{ 'text-danger': isHolyday }" style="column-gap: .25rem">
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': isHolyday }"></i>
                    {{ entry.stdrdDt }}
                    <span class="fs-8" :class="isHolyday ? 'text-danger' : 'text-gray-600'">({{ entry.journalDateWeekDay }})</span>
                    <span v-if="showPrecisionBadge" class="badge badge-light-primary ms-2">{{ precisionLabel }}</span>
                    <span class="fs-7 ms-4 text-muted" v-html="entry.weather"></span>
                    <div v-if="hasHolydayNm" class="w-100 ps-5 fs-6 fw-normal text-truncate">{{ entry.holydayNm }}</div>
                </div>
            </template>
            <template v-else>
                <div :class="{ 'text-danger': isHolyday }" style="column-gap: .25rem">
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': isHolyday }"></i>
                    {{ entry.stdrdDt }}
                    <span class="fs-8" :class="isHolyday ? 'text-danger' : 'text-gray-600'">({{ entry.journalDateWeekDay }})</span>
                    <span v-if="showPrecisionBadge" class="badge badge-light-primary ms-2">{{ precisionLabel }}</span>
                    <span class="fs-7 ms-4 text-muted" v-html="entry.weather"></span>
                    <div v-if="hasHolydayNm" class="w-100 ps-5 fs-6 fw-normal text-truncate">{{ entry.holydayNm }}</div>
                </div>
                <JournalDayContextMenu :day="adaptedDay" />
            </template>
        </div>
        <div :class="contentWrapperClass" :data-id="entry.id">
            <JournalEntryContent :entry="entry" :content-type="contentType" />
        </div>
        <div class="col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 py-3 d-none d-md-flex-between w-75px gap-1">
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')"
                    @click="openCommentRegModal">
                <i class="bi bi-chat-left-dots p-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    :title="t('bs.tooltip.copy')"
                    @click="copyEntry">
                <i class="bi bi-copy p-0"></i>
            </button>
            <JournalEntryContextMenu
                :entry="entry"
                :content-type="contentType"
                :content-label="contentLabel"
                :css-prefix="cssPrefix"
                :show-interpretation="true"
                :show-related="true"
                :show-day-open="false"
                :show-dream-states="showDreamStates"
            />
        </div>
    </div>
    `,
};

export default JournalAnnualEntryItem;
