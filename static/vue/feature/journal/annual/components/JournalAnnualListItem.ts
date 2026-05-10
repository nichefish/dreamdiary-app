/**
 * JournalAnnualListItem.ts
 * 저널 결산(annual) 목록 카드 — Handlebars list_template 의 each 루프 본문 대체.
 *
 * 변경(A-5-α):
 *   - `_journal_annual_list_template.hbs` (id `journal_annual_list_template`) 안의 카드 본문 마크업을
 *     본 컴포넌트로 흡수한다.
 *   - 호출 시그니처 보존:
 *     · 카드 헤더 클릭 → `dF.JournalAnnual.detailView(yy)`
 *     · 태그 클릭 → 기존 partial 의 `module="dF.JournalDayTagService"` 결의 `select(tagId, name)` 호출과 동일.
 *   - `markdownContent` 는 서버에서 마크업 변환된 HTML 이므로 `v-html` 사용(다른 entry/interpretation 컴포넌트와 동일 패턴).
 *   - 컨텍스트 메뉴는 `JournalAnnualContextMenu` 로 위임.
 *
 * @author nichefish
 */

import JournalAnnualContextMenu from "./JournalAnnualContextMenu.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단(window/globalThis.Message 우선 결의 + key 폴백).
import { resolveMessage } from "../../../../common/messageHelper.js";

type AnnualTag = { tagId: string | number; name: string; ctgr?: string };

const JournalAnnualListItem = {
    name: "JournalAnnualListItem",
    components: { JournalAnnualContextMenu },
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
        hasTitle(): boolean {
            return cF.util.isNotEmpty(this.annual?.title);
        },
        tagList(): AnnualTag[] {
            return Array.isArray(this.annual?.tag?.list) ? this.annual.tag.list : [];
        },
        hasTags(): boolean {
            return this.tagList().length > 0;
        },
        isDreamCompleted(): boolean {
            return this.annual?.dreamComptYn === "Y";
        },
        gotoDetailView(): void {
            (window as any).dF?.JournalAnnual?.detailView?.(this.annual.yy);
        },
        /** 변경 전: HBS partial `tag_list_partial` 의 `module="dF.JournalDayTagService"` 결의 `select(...)` onclick. */
        selectTag(tag: AnnualTag): void {
            const svc = (window as any).dF?.JournalDayTagService;
            svc?.select?.(tag.tagId, String(tag.name ?? ""));
        },
    },
    template: `
    <div class="card post">
        <div class="card-body py-5">
            <div class="row journal-sumry align-items-center">
                <div class="col-2 d-flex align-items-center">
                    <i class="bi bi-calendar3 fs-6 me-2"></i>
                    <a
                        class="cursor-pointer text-underline-dotted text-dark"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="tooltip('txt.sumry', 'bs.tooltip.page.dtl')"
                        @click="gotoDetailView"
                    >
                        <template v-if="hasTitle()">
                            <span class="fs-5 me-1">{{ annual.title }}</span>
                        </template>
                        <template v-else>
                            <span class="fs-5 fw-bolder me-0">{{ annual.yy }}</span>
                            {{ t('txt.sumry-by-yy') }}
                        </template>
                        <i class="bi bi-pencil-square fs-4 ms-1"></i>
                    </a>
                </div>
                <div class="col fs-5">
                    <div class="d-flex justify-content-start">
                        <div class="text-gray-700 d-flex-center me-5">
                            <span class="fw-bold me-2">{{ t('txt.dream') }}</span>
                            <template v-if="isDreamCompleted()">
                                <span
                                    class="cursor-help"
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
                            (<span class="text-info fw-bold mx-1">{{ annual.dreamDayCnt }}</span>{{ t('txt.dd') }}
                            /
                            <span class="text-info fw-bold mx-1">{{ annual.dreamCnt }}</span>{{ t('txt.ea') }})
                        </div>
                    </div>
                    <div class="mt-2">
                        <div class="fs-6 fw-normal text-gray-800 ps-2 pt-3 text-noti" v-html="annual.markdownContent"></div>
                    </div>
                    <div class="mt-2">
                        <div v-if="hasTags()" class="ms-5 mt-3">
                            <i class="bi bi-tag"></i>
                            <span
                                v-for="tag in tagList()"
                                :key="tag.tagId + ':' + tag.name"
                                class="text-muted cursor-pointer pe-1"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                :title="t('view.tag.content-list')"
                                @click="selectTag(tag)"
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
                <div class="col-1 ms-4 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
                <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
                    <JournalAnnualContextMenu :annual="annual" />
                </div>
            </div>
        </div>
    </div>
    `,
};

export default JournalAnnualListItem;
