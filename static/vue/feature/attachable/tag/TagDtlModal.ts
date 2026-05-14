/**
 * TagDtlModal.ts
 * 태그 상세 모달 Vue 컴포넌트.
 *
 * 변경(tag 모듈 Vue 전환, Sub-phase B):
 *   - tag_module.ts `dtlModal` + HBS(`_tag_dtl_modal_template.hbs`, `_tag_dtl_post_list_template.hbs`)
 *     렌더링을 Vue 컴포넌트로 대체한다.
 *   - `tag:open-dtl-modal` CustomEvent 수신 → AJAX → Bootstrap 모달 표시.
 *   - "전체 태그 보기" 클릭 → `tag:open-list-modal` dispatch (TagListModal 수신).
 *   - 글 행 클릭 → `window.Page?.dtl(id)` (기존 `Page.dtl('{{id}}')` onclick 동작 유지).
 *
 * @author nichefish
 */

export {};

declare const Vue: any;
declare const cF: any;
declare const Url: any;
declare const Swal: any;

type TagDtlLabels = {
    postCountPrefix: string;
    postCountSuffix: string;
    tooltipOpenAll: string;
    btnListAll: string;
    thTitle: string;
    thAuthor: string;
    thCreatedAt: string;
    thHit: string;
    tooltipMovePost: string;
};

type PostItem = {
    id: number | string;
    hasCtgrNm: boolean;
    ctgrNm: string;
    title: string;
    hasComment: boolean;
    commentCnt: number;
    createdByNm: string;
    createdAt: string;
    hitCnt: number;
};

const TagDtlModal = {
    name: "TagDtlModal",
    data(): {
        tagName: string;
        size: number;
        postList: PostItem[];
        loading: boolean;
        labels: TagDtlLabels;
    } {
        return {
            tagName: "",
            size: 0,
            postList: [],
            loading: false,
            labels: {} as TagDtlLabels,
        };
    },
    mounted(): void {
        const labelsEl = document.getElementById("tag_dtl_modal_labels");
        if (labelsEl) {
            try {
                this.labels = JSON.parse(labelsEl.textContent || "{}") as TagDtlLabels;
            } catch { /* ignore parse error */ }
        }
        window.addEventListener("tag:open-dtl-modal", (e: Event): void => {
            const { id } = (e as CustomEvent<{ id: string | number }>).detail;
            this.open(id);
        });
    },
    methods: {
        /**
         * 태그 상세 모달 열기.
         * @param {string|number} id - 태그 ID.
         */
        open(id: string | number): void {
            if (isNaN(Number(id))) return;
            this.loading = true;
            this.tagName = "";
            this.size = 0;
            this.postList = [];

            const el = document.getElementById("tag_dtl_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
            /* 변경 전: `$("#tag_list_modal").modal("hide")` */
            const listEl = document.getElementById("tag_list_modal");
            (window as any).bootstrap?.Modal.getInstance(listEl)?.hide();

            const url: string = (cF as any).util.bindUrl((Url as any).TAG, { id });
            (cF as any).ajax.get(url, {}, (res: any): void => {
                this.loading = false;
                if (!res.rslt) {
                    if ((cF as any).util.isNotEmpty(res.message)) (Swal as any).fire({ text: res.message });
                    return;
                }
                this.tagName = res.rsltObj?.name ?? "";
                this.size = res.rsltObj?.size ?? 0;
                this.postList = Array.isArray(res.rsltList) ? res.rsltList : [];
            });
        },
        /**
         * 전체 태그 목록 모달 열기.
         * 변경 전: `onclick="Tag.listAjax();"` → `tag:open-list-modal` CustomEvent dispatch.
         */
        openAll(): void {
            window.dispatchEvent(new CustomEvent("tag:open-list-modal"));
        },
        /**
         * 태그 글 상세 이동.
         * 변경 전: `onclick="Page.dtl('{{id}}');"` 동작 유지.
         * @param {string|number} id - 글 ID.
         */
        openPost(id: string | number): void {
            (window as any).Page?.dtl(id);
        },
    },
    template: `
    <div>
        <div v-if="loading" class="d-flex justify-content-center my-5">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        </div>
        <template v-else>
            <div class="d-flex align-items-center justify-content-between">
                <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                    <span class="vertical-middle">
                        "<u class="border-bottom">{{ tagName }}</u>" : {{ labels.postCountPrefix }} <u>{{ size }}</u>{{ labels.postCountSuffix }}
                    </span>
                </h1>
                <h5 class="modal-title">
                    <div class="btn btn-sm bg-secondary btn-light-primary"
                         @click="openAll"
                         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                         :title="labels.tooltipOpenAll">
                        <span class="border-bottom">{{ labels.btnListAll }}</span>
                    </div>
                </h5>
            </div>
            <div class="separator separator-dashed border-gray-300 my-2"></div>
            <div v-if="postList.length > 0" class="fs-4 mx-4 px-4 fw-normal text-gray-800">
                <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed hoverTable mb-3">
                    <thead>
                        <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                            <th class="col-lg-6 col-6 text-center wb-keepall">{{ labels.thTitle }}</th>
                            <th class="col-2 text-center wb-keepall">{{ labels.thAuthor }}</th>
                            <th class="col-lg-2 text-center wb-keepall hidden-table">{{ labels.thCreatedAt }}</th>
                            <th class="text-center wb-keepall w-15 hidden-table">{{ labels.thHit }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="post in postList" :key="post.id"
                            class="bg-hover-secondary cursor-pointer"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                            :title="labels.tooltipMovePost"
                            @click="openPost(post.id)">
                            <td class="text-start px-2">
                                <span class="vertical-middle">
                                    <span v-if="post.hasCtgrNm" class="ctgr-span ctgr-gray">{{ post.ctgrNm }}</span>
                                    <span class="text-primary text-active-primary">{{ post.title }}</span>
                                </span>
                                <span v-if="post.hasComment" class="mx-1 text-noti fs-x-small">
                                    [{{ post.commentCnt }}]
                                </span>
                            </td>
                            <td class="text-center col-form-label">{{ post.createdByNm }}</td>
                            <td class="text-center hidden-table">{{ post.createdAt }}</td>
                            <td class="text-center hidden-table">{{ post.hitCnt }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </template>
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
    const mountEl = document.getElementById("tag_dtl_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { TagDtlModal },
        template: `<TagDtlModal />`,
    }).mount("#tag_dtl_modal_app");
});