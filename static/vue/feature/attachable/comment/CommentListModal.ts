/**
 * CommentListModal.ts
 * 댓글 목록 모달 Vue 컴포넌트
 *
 * 변경(D): _comment_list_modal.ftlh 의 CommentList 인라인 스크립트와 Handlebars 템플릿을
 *          Vue 컴포넌트로 전환.
 *          - CustomEvent('comment:open-list-modal') 로 모달 진입.
 *
 * @author nichefish
 */
import commentDataService from "./services/commentDataService.js";
import type { CommentItem } from "./types.js";

declare const Vue: any;

const CommentListModal = {
    name: "CommentListModal",
    data(): { comments: CommentItem[]; loading: boolean; actvtyCtgrCd: string } {
        return {
            comments: [],
            loading: false,
            actvtyCtgrCd: "",
        };
    },
    mounted(): void {
        // actvtyCtgrCd JSON 주입값 읽기
        const dataEl = document.getElementById("comment_list_modal_data");
        if (dataEl) {
            try {
                const parsed: { actvtyCtgrCd?: string } = JSON.parse(dataEl.textContent || "{}");
                this.actvtyCtgrCd = parsed.actvtyCtgrCd || "";
            } catch { /* ignore parse error */ }
        }

        window.addEventListener("comment:open-list-modal", (e: Event): void => {
            const { refId, refContentType } =
                (e as CustomEvent<{ refId: string | number; refContentType: string }>).detail;
            void this.open(refId, refContentType);
        });
    },
    methods: {
        /**
         * 댓글 목록 모달 열기
         * @param {string|number} refId - 참조 게시물 번호.
         * @param {string} refContentType - 참조 콘텐츠 타입.
         */
        async open(refId: string | number, refContentType: string): Promise<void> {
            this.loading = true;
            this.comments = [];
            const el = document.getElementById("comment_list_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
            try {
                this.comments = await commentDataService.getList(
                    refId, refContentType, this.actvtyCtgrCd || undefined
                );
            } finally {
                this.loading = false;
            }
        },
    },
    template: `
    <div>
        <div v-if="loading" class="d-flex justify-content-center my-5">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
        <table v-else class="table align-middle table-row-dashed fs-small gy-3 table-fixed hoverTable mb-3">
            <tbody>
                <tr v-for="comment in comments" :key="comment.id">
                    <td class="col-lg-2 col-3 text-start wb-keepall ps-6">
                        <div class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                            <div v-if="comment.createdByInfo && comment.createdByInfo.profileImageUrl"
                                 class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img :src="comment.createdByInfo.profileImageUrl" class="img-thumbnail p-0 w-100" />
                            </div>
                            {{ comment.createdByNm }}
                        </div>
                        <div class="text-muted fs-7 lh-1">
                            {{ comment.createdAt }}
                        </div>
                    </td>
                    <td class="col-lg-10 col-9 text-start fs-small d-flex flex-column border-bottom-0">
                        <div class="div-textarea-smp">{{ comment.content }}</div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    `,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    const mountEl = document.getElementById("comment_list_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { CommentListModal },
        template: `<CommentListModal />`,
    }).mount("#comment_list_modal_app");
});