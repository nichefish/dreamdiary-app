/**
 * CommentPageArea.ts
 * 댓글 페이지 영역 Vue 컴포넌트
 *
 * 변경(D): comment_page_module.ts(dF.Comment.page) + _comment_page_area.ftlh 서버렌더 + Handlebars 등록 폼을 Vue 컴포넌트로 전환.
 *          - 등록/수정/삭제 성공 시 cF.ui.blockUIReload() (전체 페이지 리로드) → 댓글 목록 반응형 갱신으로 개선.
 *          - 인라인 수정 폼: __INDEX__ 패턴 hidden div + jQuery DOM 조작 → v-if 기반 상태 토글로 대체.
 *
 * @author nichefish
 */
import type { CommentItem, CommentPageContext, CommentCurrentUser } from "./types.js";
import commentDataService   from "./services/commentDataService.js";
import commentActionService from "./services/commentActionService.js";

declare const Vue: any;
declare const Swal: any;

const CommentPageArea = {
    name: "CommentPageArea",

    data() {
        return {
            comments:       [] as CommentItem[],
            refId:          "" as string | number,
            refContentType: "" as string,
            actvtyCtgrCd:   "" as string,
            currentUser:    { nickname: "", profileImageUrl: "" } as CommentCurrentUser,
            /** 등록 폼 textarea v-model */
            regContent:     "" as string,
            /** 현재 인라인 수정 중인 댓글 id (null이면 수정 모드 없음) */
            editingId:      null as string | number | null,
            /** 수정 폼 textarea v-model */
            editContent:    "" as string,
        };
    },

    computed: {
        /** 댓글 수 (헤더 표시용) */
        commentCount(): number {
            return ((this as any).comments as CommentItem[]).length;
        },
    },

    methods: {
        /**
         * 댓글 목록 갱신 (등록·수정·삭제 성공 후 반응형 갱신)
         * 변경(D): 기존 cF.ui.blockUIReload() 전체 리로드 → AJAX 목록 재조회로 개선.
         */
        async refreshComments(): Promise<void> {
            try {
                const list: CommentItem[] = await commentDataService.getList(
                    (this as any).refId,
                    (this as any).refContentType,
                    (this as any).actvtyCtgrCd || undefined
                );
                (this as any).comments = list;
            } catch (msg) {
                if (msg) (Swal as any).fire({ text: msg });
            }
        },

        /** 댓글 등록 */
        submitReg(): void {
            const content: string = ((this as any).regContent as string).trim();
            if (!content) {
                (Swal as any).fire({ text: "댓글 내용을 입력해주세요." });
                return;
            }
            const formEl = (this as any).$refs.regForm as HTMLFormElement;
            commentActionService.reg(formEl, (): void => {
                (this as any).regContent = "";
                (this as any).refreshComments();
            });
        },

        /**
         * 인라인 수정 폼 열기
         * 변경(D): 기존 jQuery DOM 조작(mdfForm) → editingId 상태 토글로 대체.
         * @param {CommentItem} comment - 수정할 댓글.
         */
        openMdf(comment: CommentItem): void {
            (this as any).editingId  = comment.id;
            (this as any).editContent = comment.content;
        },

        /** 인라인 수정 폼 닫기 */
        closeMdf(): void {
            (this as any).editingId  = null;
            (this as any).editContent = "";
        },

        /**
         * 댓글 수정
         * @param {string|number} id - 수정할 댓글 번호.
         */
        submitMdf(id: string | number): void {
            const content: string = ((this as any).editContent as string).trim();
            if (!content) {
                (Swal as any).fire({ text: "댓글 내용을 입력해주세요." });
                return;
            }
            const ajaxData: Record<string, unknown> = {
                id:             String(id),
                content,
                refId:          String((this as any).refId),
                refContentType: (this as any).refContentType,
                actvtyCtgrCd:   (this as any).actvtyCtgrCd,
            };
            commentActionService.mdf(id, ajaxData, (): void => {
                (this as any).closeMdf();
                (this as any).refreshComments();
            });
        },

        /**
         * 댓글 삭제
         * @param {string|number} id - 삭제할 댓글 번호.
         */
        delComment(id: string | number): void {
            const extraData: Record<string, unknown> = { actvtyCtgrCd: (this as any).actvtyCtgrCd };
            commentActionService.del(id, extraData, (): void => {
                (this as any).refreshComments();
            });
        },
    },

    mounted(): void {
        const el = document.getElementById("comment_page_area_data");
        if (!el) return;
        try {
            const ctx: CommentPageContext = JSON.parse(el.textContent || "{}");
            (this as any).comments       = ctx.comments       || [];
            (this as any).refId          = ctx.refId          || "";
            (this as any).refContentType = ctx.refContentType || "";
            (this as any).actvtyCtgrCd   = ctx.actvtyCtgrCd   || "";
            (this as any).currentUser    = ctx.currentUser    || { nickname: "", profileImageUrl: "" };
        } catch { /* ignore parse error */ }
    },

    template: `
    <div>
        <a id="answers" data-kt-scroll-offset="{default: 100, lg: 125}"></a>
        <h2 class="fw-bolder text-gray-900 my-8 mx-5">댓글({{ commentCount }})</h2>

        <!-- begin::댓글 목록 -->
        <div v-for="comment in comments" :key="comment.id" class="border rounded p-2 p-lg-6 mx-8 mb-3">
            <div class="mb-0">
                <div class="d-flex flex-stack flex-wrap mb-5">
                    <!-- begin::작성자 정보 -->
                    <div class="d-flex align-items-center py-1">
                        <div class="d-flex flex-column align-items-start justify-content-center">
                            <span class="text-gray-800 fs-6 fw-bold lh-1 mb-2">
                                <div v-if="comment.createdByInfo && comment.createdByInfo.profileImageUrl"
                                     class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                    <img :src="comment.createdByInfo.profileImageUrl" class="img-thumbnail p-0 w-100" />
                                </div>
                                {{ comment.createdByNm }}
                            </span>
                            <span class="text-muted fs-7 fw-bold lh-1">
                                {{ comment.createdAt }}<template v-if="comment.updatedAt"> / {{ comment.updatedAt }}</template>
                            </span>
                        </div>
                    </div>
                    <!-- begin::수정/삭제 버튼 (본인 댓글만) -->
                    <div v-if="comment.isCreatedBy" class="d-flex align-items-center py-1">
                        <template v-if="editingId !== comment.id">
                            <a href="javascript:void(0);"
                               class="btn btn-sm btn-flex btn-light-primary btn-icon mx-1"
                               @click="openMdf(comment)"
                               data-bs-toggle="tooltip" title="댓글 수정 폼을 호출합니다." data-bs-dismiss="click">
                                <span class="svg-icon svg-icon-7"><i class="fas fa-solid fa-pen"></i></span>
                            </a>
                            <a href="javascript:void(0);"
                               class="btn btn-sm btn-flex btn-light-danger btn-icon"
                               @click="delComment(comment.id)"
                               data-bs-toggle="tooltip" title="댓글을 삭제합니다." data-bs-dismiss="click">
                                <span class="svg-icon svg-icon-7"><i class="fas fa-trash-alt"></i></span>
                            </a>
                        </template>
                        <template v-else>
                            <button type="button" class="btn btn-sm btn-primary me-2 mb-2"
                                    @click="submitMdf(comment.id)"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                    title="댓글을 수정합니다.">
                                <i class="bi bi-pencil-square"></i> 저장
                            </button>
                            <button type="button" class="btn btn-sm btn-secondary me-2"
                                    @click="closeMdf"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                    title="댓글 수정 영역을 닫습니다.">
                                닫기
                            </button>
                        </template>
                    </div>
                </div>
                <!-- begin::댓글 내용 (보기 or 수정 폼) -->
                <div class="row">
                    <div class="col fs-5 fw-normal text-gray-800">
                        <template v-if="editingId !== comment.id">
                            <div class="fs-6 col-form-label">
                                <div class="div-textarea-smp">{{ comment.content }}</div>
                            </div>
                        </template>
                        <template v-else>
                            <div class="fs-6 fw-bold col-form-label p-0">
                                <textarea v-model="editContent"
                                          class="form-control w-100" rows="3" maxlength="10000"></textarea>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>

        <div class="separator separator-dashed my-6 mt-8"></div>

        <!-- begin::등록 폼 -->
        <div class="row ms-5">
            <div class="row">
                <div class="col-xl-1">
                    <div class="fs-6 fw-bold col-form-label vertical-center">
                        <div v-if="currentUser.profileImageUrl"
                             class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                            <img :src="currentUser.profileImageUrl" class="img-thumbnail p-0 w-100" />
                        </div>
                        {{ currentUser.nickname }}
                    </div>
                </div>
                <div class="row col-xl-11">
                    <div class="col">
                        <form ref="regForm" id="commentPageRegForm" enctype="multipart/form-data">
                            <input type="hidden" name="id" value="">
                            <input type="hidden" name="refId" :value="refId">
                            <input type="hidden" name="refContentType" :value="refContentType">
                            <input type="hidden" name="actvtyCtgrCd" :value="actvtyCtgrCd">
                            <textarea v-model="regContent" name="content" id="commentCn"
                                      class="form-control w-100" maxlength="1000"></textarea>
                        </form>
                    </div>
                    <div class="col-1 col-form-label w-125px">
                        <button type="button" class="btn btn-primary btn-sm"
                                @click="submitReg"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                title="댓글을 저장합니다.">
                            저장
                        </button>
                    </div>
                </div>
            </div>
        </div>
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
    const mountEl = document.getElementById("comment_page_area_app");
    if (!mountEl) return;

    (Vue as any).createApp({
        components: { CommentPageArea },
        template: `<CommentPageArea />`,
    }).mount("#comment_page_area_app");
});