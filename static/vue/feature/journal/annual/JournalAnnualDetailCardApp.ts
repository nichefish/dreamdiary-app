/**
 * JournalAnnualDetailCardApp.ts
 * 저널 결산 상세 카드 영역 Vue 엔트리 (`#journal_annual_detail_div`).
 *
 * 변경(A-7-β):
 *   - 변경 전: `journalAnnualCrudService.detailAjax` 가 `cF.handlebars.template(rsltObj, "journal_annual_detail")` 로
 *     `_journal_annual_detail_template.hbs` + 리뷰 partial 3종을 컴파일해 주입했다.
 *   - 변경 후: 동일 데이터를 `window.JournalAnnualDetailVueApp.setModel(rsltObj)` 브리지로 Vue 가 렌더한다.
 *     마크업·class·인라인 동작(`dF.JournalAnnualReview.registModal` 등)은 기존 HBS 와 동등(UI 변경 0).
 *   - 부트 순서: 본 ES module 은 `journalAnnualService.js`(dF.JournalAnnual 표면) 이후,
 *     `JournalAnnualDetailPageBoot.js` 이전에 적재되어 Ajax 완료 전 브리지가 존재하도록 한다.
 *
 * @author nichefish
 */

import JournalAnnualReviewRow from "./components/JournalAnnualReviewRow.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
import { resolveMessage } from "../../../common/messageHelper.js";

type DetailVueBridge = {
    mounted?: boolean;
    pendingModel?: Record<string, any> | null;
    setModel?: (obj: Record<string, any>) => void;
};

/** Vue 반응 대상 단일 원천 — Root data() 가 동일 참조를 노출한다. */
const pageState: { model: Record<string, any> | null } = { model: null };

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

/**
 * KTMenu / Bootstrap Tooltip 재초기화 — StateService 의 entry/tag 렌더와 동등한 후처리.
 */
function reinitDomDecorations(): void {
    Vue.nextTick(function(): void {
        const target = document.getElementById("journal_annual_detail_div");
        if (!target) return;

        const bsTooltip = (window as any).bootstrap?.Tooltip;
        target.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(el: Element): void {
            if (!bsTooltip) return;
            const htmlEl = el as HTMLElement;
            const existing = bsTooltip.getInstance?.(htmlEl);
            if (existing) existing.dispose();
            new bsTooltip(htmlEl);
        });
        if (typeof KTMenu !== "undefined" && typeof (KTMenu as any).createInstances === "function") {
            (KTMenu as any).createInstances();
        }
    });
}

const JournalAnnualDetailRoot = {
    name: "JournalAnnualDetailRoot",
    components: { JournalAnnualReviewRow },
    data(): { pageState: typeof pageState } {
        return { pageState };
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
        hasTitle(): boolean {
            const m = this.pageState.model;
            return m != null && cF.util.isNotEmpty(m.title);
        },
        dreamCompleted(): boolean {
            return this.pageState.model?.dreamComptYn === "Y";
        },
        tagList(): Array<{ tagId?: string | number; name?: string; ctgr?: string }> {
            const m = this.pageState.model;
            return Array.isArray(m?.tag?.list) ? m.tag.list : [];
        },
        hasTags(): boolean {
            return this.tagList().length > 0;
        },
        reviewList(): Record<string, any>[] {
            const m = this.pageState.model;
            return Array.isArray(m?.journalAnnualReviewList) ? m.journalAnnualReviewList : [];
        },
        /** 변경 전: `tag_list_partial` + `module="dF.JournalDayTagService"` (상단 SUMMARY 태그). */
        selectDayTag(tag: { tagId?: string | number; name?: string; ctgr?: string }): void {
            (window as any).dF?.JournalDayTagService?.select?.(tag.tagId, String(tag.name ?? ""));
        },
        /** 변경 전: 리뷰 등록 버튼 onclick `dF.JournalAnnualReview.registModal({ journalAnnualId: {{id}} })`. */
        openReviewRegistModal(): void {
            const journalAnnualId = this.pageState.model?.id;
            (window as any).dF?.JournalAnnualReview?.registModal?.({ journalAnnualId });
        },
    },
    template: `
    <template v-if="pageState.model">
        <div class="card-header py-5">
            <div class="col-2 d-flex align-items-center">
                <i class="bi bi-calendar3 fs-5 me-2"></i>
                <template v-if="hasTitle()">
                    <span class="fs-4 me-1">{{ pageState.model.title }}</span>
                </template>
                <template v-else>
                    <span class="fs-4 fw-bolder me-1">{{ pageState.model.yy }}</span>
                    <span class="fs-5 me-1">{{ t('txt.sumry-by-yy') }}</span>
                </template>
            </div>
            <div class="d-flex justify-content-start">
                <div class="d-flex-center text-gray-700 fs-6 me-5">
                    <span class="fw-bold me-2">{{ t('txt.dream') }}</span>
                    <template v-if="dreamCompleted()">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              :title="t('bs.tooltip.journal.annual.dream-completed')"
                        >
                            <i class="bi bi-moon-stars-fill fs-4 me-2 text-success"></i>
                            <i class="bi bi-check text-success" style="margin-left:-0.8rem"></i>
                        </span>
                    </template>
                    <template v-else>
                        <span><i class="bi bi-moon-stars fs-4 me-2"></i></span>
                    </template>
                    (<span class="text-info fw-bold mx-1">{{ pageState.model.dreamDayCnt }}</span>{{ t('txt.dd') }}
                    /
                    <span class="text-info fw-bold mx-1">{{ pageState.model.dreamCnt }}</span>{{ t('txt.ea') }})
                </div>
            </div>
        </div>
        <div class="card-body py-5">
            <div class="journal-sumry-item">
                <div class="ms-3 fs-6">SUMMARY</div>
                <div class="fs-6 fw-normal text-gray-800 ps-2 pt-2 text-noti" v-html="pageState.model.markdownContent"></div>
                <div class="mt-2">
                    <div v-if="hasTags()" class="ms-5 mt-3">
                        <i class="bi bi-tag"></i>
                        <span
                            v-for="tag in tagList()"
                            :key="String(tag.tagId) + ':' + String(tag.name)"
                            class="text-muted cursor-pointer pe-1"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
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
            <div class="journal-sumry-review-container mt-8">
                <div class="d-flex-align-center ms-3 fs-6 gap-5">
                    <span>REVIEWS</span>
                    <button type="button" class="btn btn-sm btn-light-primary btn-outlined ps-2 pe-3 py-1 cursor-pointer"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                            :title="tooltip('txt.journal.diary', 'bs.tooltip.modal.reg')"
                            @click="openReviewRegistModal"
                    >
                        <i class="bi bi-plus fs-4 pe-0"></i>
                        {{ t('txt.journal.annual.review.reg') }}
                    </button>
                </div>
                <JournalAnnualReviewRow
                    v-for="rev in reviewList()"
                    :key="'rev-' + String(rev.id)"
                    :item="rev"
                />
            </div>
        </div>
    </template>
    `,
};

let setModelHandler: ((obj: Record<string, any>) => void) | null = null;

runWhenDomReady(function(): void {
    const mountEl = document.getElementById("journal_annual_detail_div") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalAnnualDetailCardApp] mount root #journal_annual_detail_div 없음.");
        return;
    }

    const priorBridge = ((window as any).JournalAnnualDetailVueApp ?? {}) as DetailVueBridge;
    const pending = priorBridge.pendingModel ?? null;

    pageState.model = null;

    setModelHandler = function(obj: Record<string, any>): void {
        pageState.model = obj;
        reinitDomDecorations();
    };

    const app = Vue.createApp(JournalAnnualDetailRoot);
    app.mount(mountEl);

    (window as any).JournalAnnualDetailVueApp = {
        mounted: true,
        pendingModel: null,
        setModel: function(obj: Record<string, any>): void {
            if (typeof setModelHandler === "function") {
                setModelHandler(obj);
                return;
            }
            const b = (window as any).JournalAnnualDetailVueApp as DetailVueBridge;
            b.pendingModel = obj;
            console.log("[JournalAnnualDetailCardApp] pending model queued.");
        },
    };

    if (pending != null) {
        setModelHandler(pending);
    }
});

export {};
