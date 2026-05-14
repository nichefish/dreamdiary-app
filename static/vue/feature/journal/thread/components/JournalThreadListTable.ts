/**
 * 저널 스레드 목록 테이블 본문(행·단일 피허 행 포함).
 *
 * 변경: 목록 레이아웃·클래스는 `journal_thread_list.ftlh` SSR 마크업을 그대로 따른다(동일 목록 줄 DOM).
 *
 * @author nichefish
 */
import type { JournalThreadListLabels, JournalThreadListRow } from "../types.js";

export default {
    name: "JournalThreadListTable",
    props: {
        rows: { type: Array, required: true },
        labels: { type: Object, required: true },
    },
    computed: {
        items(): JournalThreadListRow[] {
            return this.rows as JournalThreadListRow[];
        },
        l(): JournalThreadListLabels {
            return this.labels as JournalThreadListLabels;
        },
    },
    methods: {
        detailHref(id: number): string {
            const base = (typeof Url !== "undefined" ? (Url as any).JOURNAL_THREAD_DETAIL : "") as string;
            return `${base}?id=${id}`;
        },

        emitOpenDetailModal(id: number): void {
            window.dispatchEvent(new CustomEvent("journal-thread:open-detail-modal", { detail: { id } }));
        },

        commentModal(post: JournalThreadListRow): void {
            const w = window as any;
            if (typeof w.CommentList?.modal !== "function")
                return;
            w.CommentList.modal(post.id, post.contentType);
        },

        /**
         * 태그 모달 진입.
         * 변경 전: 전역 레거시 `dF.Tag.dtlModal` 브리지 → `tag:open-dtl-modal` CustomEvent dispatch.
         */
        tagDetail(tagId: string): void {
            window.dispatchEvent(new CustomEvent("tag:open-dtl-modal", { detail: { id: tagId } }));
        },

        /**
         * 첨부파일 목록(:: 전역 `FileGroupList.modal`).
         */
        fileModal(fileGroupId: string | number): void {
            const w = window as any;
            if (typeof w.FileGroupList?.modal !== "function")
                return;
            w.FileGroupList.modal(String(fileGroupId));
        },
    },
    template: `
    <template v-if="items.length === 0">
        <tr>
            <td colspan="3" class="text-center">{{ l.emptyList }}</td>
        </tr>
    </template>
    <template v-else>
        <tr
            v-for="post in items"
            :key="'journal-thread-' + post.id"
            class="bg-hover-secondary cursor-default"
        >
            <td class="text-center hidden-table">
                {{ post.rnum }}
            </td>
            <td
                class="text-start"
                :class="{ 'pb-4': post.hasTagsLayout }"
            >
                <div class="row d-flex align-items-center justify-content-between">
                    <div class="col-xl-8 col-12 d-flex flex-column">
                        <div class="my-1">
                            <a
                                :href="detailHref(post.id)"
                                class="text-dark vertical-middle text-underline-dotted"
                                data-bs-toggle="tooltip"
                                data-bs-placement="top"
                                data-bs-dismiss="click"
                                :title="l.pageDetail"
                            >
                                <span v-if="post.categoryName" class="ctgr-span ctgr-gray">{{ post.categoryName }}</span>
                                {{ post.title }}
                            </a>
                            <span
                                v-if="post.commentCnt > 0"
                                class="mx-1 text-noti btn-active-warning fs-x-small cursor-pointer opacity-hover"
                                role="button"
                                tabindex="0"
                                data-bs-toggle="tooltip"
                                data-bs-placement="top"
                                :title="l.comment + ' 모달 호출'"
                                @click.prevent="commentModal(post)"
                                @keyup.enter.prevent="commentModal(post)"
                            >
                                [{{ post.commentCnt }}]
                            </span>
                            <div v-if="post.isNew" class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                        </div>
                        <div v-if="post.hasTagsLayout" class="ms-1">
                            <span class="me-6 fs-7">
                                <span class="pe-1 text-muted"><i class="bi bi-tags"></i></span>
                                <span
                                    v-for="tag in post.tags"
                                    :key="'tag-' + post.id + '-' + tag.tagId"
                                    class="text-muted pe-1 cursor-pointer"
                                    role="button"
                                    tabindex="0"
                                    data-bs-toggle="tooltip"
                                    data-bs-placement="top"
                                    data-bs-dismiss="click"
                                    :title="l.tagContentList"
                                    @click.prevent="tagDetail(tag.tagId)"
                                    @keyup.enter.prevent="tagDetail(tag.tagId)"
                                >
                                    <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                                    #<span class="border-bottom text-primary fw-lighter opacity-hover">{{ tag.name }}</span>
                                </span>
                            </span>
                        </div>
                    </div>
                    <div class="col-xl-4 col-12 d-flex justify-content-end align-items-center">
                        <a
                            class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow float-end"
                            href="javascript:void(0);"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="l.modalView"
                            role="button"
                            @click.prevent="emitOpenDetailModal(post.id)"
                        >
                            <i class="bi bi-stickies fs-5 text-noti opacity-hover"></i>
                        </a>
                    </div>
                </div>
            </td>
            <td class="text-center col-form-label hidden-table">
                <template v-if="post.hasFiles">
                    <a
                        class="badge badge-secondary p-2 btn-white bg-hover-white blank blink-slow"
                        href="javascript:void(0);"
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        :title="l.atchFile"
                        role="button"
                        @click.prevent="fileModal(post.fileGroupId)"
                    >
                        <i class="bi bi-file-earmark-arrow-down fs-5 text-info opacity-hover"></i>
                    </a>
                </template>
                <template v-else>-</template>
            </td>
        </tr>
    </template>
    `,
};
