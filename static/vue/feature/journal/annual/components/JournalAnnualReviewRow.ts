/**
 * JournalAnnualReviewRow.ts
 * 저널 결산 리뷰 한 행 — `_journal_annual_review_item_partial.hbs` + `_journal_annual_review_content_partial.hbs` 흡수.
 *
 * 변경(A-7-β):
 *   - 변경 전: Handlebars `journal_annual_review_item_partial` 가 content / comment·copy 버튼 / context 메뉴 partial 을 조립.
 *   - 변경 후: 동일 DOM 구조·class·data 속성 보존(UI 변경 0).
 *   - `contentStateClass` Handlebars 헬퍼(`handlebars.ts`) 동작을 Vue 메서드로 복제한다.
 *
 * @author nichefish
 */

import JournalAnnualReviewContextMenu from "./JournalAnnualReviewContextMenu.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임 — 글로벌 결의 race 차단.
import { resolveMessage } from "../../../../common/messageHelper.js";

type TagItem = { tagId?: string | number; tagNm?: string; ctgr?: string };
type CommentItem = { id?: string | number; markdownContent?: string };

/**
 * Handlebars `contentStateClass` 헬퍼와 동등 (`static/js/common/helper/handlebars.ts`).
 */
function contentStateClassFromState(state: { list?: Array<{ stateKey?: string }> } | null | undefined): string {
    if (!state?.list || !Array.isArray(state.list)) return "";
    return state.list
        .map((s: { stateKey?: string }) => (s?.stateKey || "").toLowerCase())
        .filter((v: string) => v.length > 0)
        .join(" ");
}

const JournalAnnualReviewRow = {
    name: "JournalAnnualReviewRow",
    components: { JournalAnnualReviewContextMenu },
    props: {
        item: { type: Object, required: true },
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
        reviewContentOuterClass(): string {
            const row = this.item as Record<string, any>;
            if (cF.util.isNotEmpty(row.contentClass)) return String(row.contentClass);
            const sc = contentStateClassFromState(row.state);
            return sc ? `journal-content ${sc}` : "journal-content";
        },
        tagList(): TagItem[] {
            const row = this.item as Record<string, any>;
            return Array.isArray(row.tag?.list) ? row.tag.list : [];
        },
        hasTags(): boolean {
            return this.tagList().length > 0;
        },
        commentList(): CommentItem[] {
            const row = this.item as Record<string, any>;
            return Array.isArray(row.comment?.list) ? row.comment.list : [];
        },
        hasComments(): boolean {
            return this.commentList().length > 0;
        },
        commentRowClass(index: number, total: number): string {
            const parts = ["row", "d-flex-align-center"];
            if (index === 0) parts.push("mt-2");
            if (index === total - 1) parts.push("mb-1");
            return parts.join(" ");
        },
        /** 변경 전: `tag_list_partial` + module `dF.JournalEntryTag.get('JOURNAL_DIARY')`. */
        selectDiaryTag(tag: TagItem): void {
            const mod = (window as any).dF?.JournalEntryTag?.get?.("JOURNAL_DIARY");
            mod?.select?.(tag.tagId, String(tag.tagNm ?? ""));
        },
        /** 변경 전: `comment_reg_btn_partial` onclick. */
        openCommentReg(): void {
            const id = (this.item as Record<string, any>).id;
            (window as any).dF?.Comment?.modal?.regModal?.(id, "JOURNAL_ANNUAL_REVIEW");
        },
        /** 변경 전: `journal_entry_copy_btn_partial` + module JournalEntry.get('JOURNAL_DIARY'). */
        copyReview(): void {
            const id = (this.item as Record<string, any>).id;
            (window as any).dF?.JournalEntry?.get?.("JOURNAL_DIARY")?.copy?.(id);
        },
        commentMdf(commentId: string | number | undefined): void {
            if (commentId == null) return;
            (window as any).dF?.Comment?.modal?.mdfModal?.(commentId);
        },
        commentDel(commentId: string | number | undefined): void {
            if (commentId == null) return;
            (window as any).dF?.Comment?.modal?.delAjax?.(commentId);
        },
    },
    template: `
    <div class="journal-sumry-review-item ms-7 me-15 ps-3" :data-id="item.id">
        <div class="col">
            <div class="journal-sumry-review-content p-2 text-noti">
                <div :class="reviewContentOuterClass()" v-html="item.markdownContent"></div>
                <div v-if="hasTags()" class="tags ms-2 mt-3">
                    <i class="bi bi-tag"></i>
                    <span
                        v-for="tag in tagList()"
                        :key="String(tag.tagId) + ':' + String(tag.tagNm)"
                        class="text-muted cursor-pointer pe-1"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                        :title="t('view.tag.content-list')"
                        @click="selectDiaryTag(tag)"
                    >
                        #
                        <span class="border-bottom text-primary fw-lighter opacity-hover">
                            <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                            {{ tag.tagNm }}
                        </span>
                    </span>
                </div>
                <template v-if="hasComments()">
                    <div
                        v-for="(c, idx) in commentList()"
                        :key="'c-' + String(c.id)"
                        :class="commentRowClass(idx, commentList().length)"
                    >
                        <div class="col d-flex-align-center position-relative ms-4">
                            <div class="li text-comment" v-html="c.markdownContent"></div>
                        </div>
                        <div class="col-1">
                            <button type="button" class="btn btn-sm btn-light-primary btn-outlined py-1 px-2 cursor-pointer"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                    :title="tooltip('txt.comment', 'bs.tooltip.modal.mdf')"
                                    @click="commentMdf(c.id)"
                            >
                                <i class="bi bi-pencil-square p-0"></i>
                            </button>
                            <button type="button" class="btn btn-sm btn-light-danger btn-outlined py-1 px-2 cursor-pointer"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                    :title="tooltip('txt.comment', 'bs.tooltip.del')"
                                    @click="commentDel(c.id)"
                            >
                                <i class="bi bi-trash p-0"></i>
                            </button>
                        </div>
                    </div>
                </template>
            </div>
        </div>
        <div class="col-1 ms-4 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 py-3 d-none d-md-flex-between w-75px ps-2 gap-1">
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    :title="tooltip('txt.comment', 'bs.tooltip.modal.reg')"
                    @click="openCommentReg"
            >
                <i class="bi bi-chat-left-dots p-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-light-primary btn-outlined m-1 py-0 px-2 cursor-pointer"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                    :title="t('bs.tooltip.copy')"
                    @click="copyReview"
            >
                <i class="bi bi-copy p-0"></i>
            </button>
            <JournalAnnualReviewContextMenu :review-id="item.id" />
        </div>
    </div>
    `,
};

export default JournalAnnualReviewRow;
