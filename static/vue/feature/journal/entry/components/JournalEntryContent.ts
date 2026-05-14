/**
 * JournalEntryContent.ts
 * Vue가 소유하는 저널 엔트리 본문, 태그, 관련 글, 댓글 영역.
 */

import journalDayUiBridgeService from "../../day/services/journalDayUiBridgeService.js";
import commentActionService from "../../../attachable/comment/services/commentActionService.js";
import * as relatedContentService from "../../../attachable/related/relatedContentService.js";

const JournalEntryContent = {
    name: "JournalEntryContent",
    props: {
        entry: { type: Object, required: true },
        contentType: { type: String, required: true },
        /**
         * Handlebars `journal_entry_content_partial`의 `collapse` 인자와 동일한 역할.
         * 비어 있으면 `entry.collapse`를 사용한다(기존 목록 카드 동작 유지).
         */
        collapseClass: { type: String, default: "" },
    },
    computed: {
        contentClasses(): string[] {
            const customClass = String(this.entry?.contentClass ?? "").trim();
            if (customClass.length > 0) return [customClass];

            const classes = ["journal-content", ...this.contentStateClasses()];
            const resolvedCollapse: string = String(this.collapseClass || (this.entry?.collapse ?? "")).trim();
            if (resolvedCollapse.length > 0 && this.hasState("COLLAPSED")) classes.push(resolvedCollapse);
            return classes;
        },
        tagList(): Record<string, any>[] {
            return Array.isArray(this.entry?.tag?.list) ? this.entry.tag.list : [];
        },
        relatedContentList(): Record<string, any>[] {
            return Array.isArray(this.entry?.relatedContentList) ? this.entry.relatedContentList : [];
        },
        commentList(): Record<string, any>[] {
            return Array.isArray(this.entry?.comment?.list) ? this.entry.comment.list : [];
        },
        hasRelatedAnchor(): boolean {
            return Boolean(this.entry?.id) && this.entry?.elseDreamYn !== "Y";
        },
    },
    methods: {
        t(key: string): string {
            return this.$t ? this.$t(key) : key;
        },
        hasState(stateKey: string): boolean {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return false;
            return states.some((state: Record<string, any>): boolean => state?.stateKey === stateKey);
        },
        contentStateClasses(): string[] {
            const states = this.entry?.state?.list;
            if (!Array.isArray(states)) return [];

            return states
                .map((state: Record<string, any>): string => String(state?.stateKey ?? "").trim().toLowerCase())
                .filter((stateKey: string): boolean => stateKey.length > 0);
        },
        expandTags(event: MouseEvent): void {
            if (!(event.currentTarget instanceof HTMLElement)) return;
            /*
             * 변경 전: <code>dF.JournalDayTag.expand</code>.
             * 변경 후: <code>journalDayTagService.expandTaggedContent</code> 브리지 경로(journalDayUiBridgeService).
             */
            journalDayUiBridgeService.expandJournalDayTaggedContent(event.currentTarget);
        },
        selectTag(tag: Record<string, any>): void {
            dF.JournalEntryTag.get(this.contentType).select(tag.tagId, tag.name);
        },
        openRelatedTarget(relatedContent: Record<string, any>): void {
            relatedContentService.openTarget(relatedContent.targetContentType, Number(relatedContent.targetPostNo));
        },
        deleteRelatedContent(relatedContent: Record<string, any>): void {
            relatedContentService.deleteRelated(Number(relatedContent.id), (): void => {
                window.dispatchEvent(new CustomEvent("related-content:refresh"));
            });
        },
        openCommentMdfModal(comment: Record<string, any>): void {
            window.dispatchEvent(new CustomEvent("comment:open-mdf-modal", {
                detail: { id: comment.id },
            }));
        },
        deleteComment(comment: Record<string, any>): void {
            commentActionService.del(comment.id, {}, (): void => {
                window.dispatchEvent(new CustomEvent("comment:modal-refresh"));
            });
        },
        tooltip(labelKey: string, actionKey: string): string {
            return [this.t(labelKey), this.t(actionKey)].join(" ");
        },
    },
    template: `
    <div>
        <div :class="contentClasses" v-html="entry.markdownContent"></div>
        <button
            type="button"
            class="btn btn-xxs btn-active-light-info badge-light-primary btn-outlined expand-btn"
            @click="expandTags"
        ></button>

        <div v-if="tagList.length > 0" class="tags ms-2 mt-3">
            <i class="bi bi-tag"></i>
            <span
                v-for="tag in tagList"
                :key="'entry-tag-' + tag.tagId"
                class="text-muted cursor-pointer pe-1"
                data-bs-toggle="tooltip"
                data-bs-placement="top"
                data-bs-dismiss="click"
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

        <template v-if="hasRelatedAnchor">
            <div
                class="related-content-anchor"
                :data-id="entry.id"
                :data-post-no="entry.id"
                :data-content-type="contentType"
            ></div>
            <div
                v-if="relatedContentList.length > 0"
                class="related-content-box mt-4 pt-4 border-top border-gray-200"
                :data-id="entry.id"
                :data-post-no="entry.id"
                :data-content-type="contentType"
            >
                <div class="d-flex align-items-center gap-2 mb-3 text-gray-700">
                    <i class="bi bi-link-45deg"></i>
                    <span class="fw-semibold">{{ t('txt.related-content') }}</span>
                </div>
                <div class="related-content-list">
                    <div
                        v-for="relatedContent in relatedContentList"
                        :key="'related-content-' + relatedContent.id"
                        class="related-content-item rounded border border-gray-300 bg-light px-4 py-3 mb-3"
                        :data-related-content-id="relatedContent.id"
                    >
                        <div class="d-flex align-items-start justify-content-between gap-3 flex-wrap">
                            <div class="flex-grow-1">
                                <div class="text-muted fs-8 mb-2">
                                    {{ relatedContent.targetContentType }} <span>#</span>{{ relatedContent.targetPostNo }}
                                </div>
                                <div class="fw-semibold text-gray-900 fs-6">{{ relatedContent.targetTitle }}</div>
                                <div v-if="relatedContent.reason" class="text-muted fs-7 mt-2">{{ relatedContent.reason }}</div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <button
                                    type="button"
                                    class="btn btn-xxs btn-light-primary btn-outlined"
                                    @click="openRelatedTarget(relatedContent)"
                                >{{ t('txt.comm.open') }}</button>
                                <button
                                    type="button"
                                    class="btn btn-xxs btn-light-danger btn-outlined"
                                    @click="deleteRelatedContent(relatedContent)"
                                >{{ t('txt.comm.del') }}</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </template>

        <div
            v-for="(comment, index) in commentList"
            :key="'entry-comment-' + comment.id"
            :class="['row d-flex-align-center', { 'mt-2': index === 0, 'mb-1': index === commentList.length - 1 }]"
        >
            <div class="col d-flex-align-center position-relative ms-4">
                <div class="li text-comment" v-html="comment.markdownContent"></div>
            </div>
            <div class="col-1">
                <button
                    type="button"
                    class="btn btn-sm btn-light-primary btn-outlined py-1 px-2 cursor-pointer"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="tooltip('txt.comment', 'bs.tooltip.modal.mdf')"
                    @click="openCommentMdfModal(comment)"
                >
                    <i class="bi bi-pencil-square p-0"></i>
                </button>
                <button
                    type="button"
                    class="btn btn-sm btn-light-danger btn-outlined py-1 px-2 cursor-pointer"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-dismiss="click"
                    :title="tooltip('txt.comment', 'bs.tooltip.del')"
                    @click="deleteComment(comment)"
                >
                    <i class="bi bi-trash p-0"></i>
                </button>
            </div>
        </div>
    </div>
    `,
};

export default JournalEntryContent;
