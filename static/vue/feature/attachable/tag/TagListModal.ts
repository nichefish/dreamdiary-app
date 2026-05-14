/**
 * TagListModal.ts
 * 태그 전체 목록 모달 Vue 컴포넌트.
 *
 * 변경(tag 모듈 Vue 전환, Sub-phase B):
 *   - tag_module.ts `listAjax` + HBS(`_tag_list_modal_template.hbs`) 렌더링을 Vue 컴포넌트로 대체한다.
 *   - `tag:open-list-modal` CustomEvent 수신 → AJAX → Bootstrap 모달 표시.
 *   - 태그 클릭 → `tag:open-dtl-modal` dispatch (TagDtlModal 수신).
 *   - contentType: 기존 `$("#contentType").val()` 동작 유지 — DOM에서 직접 읽음.
 *
 * @author nichefish
 */

export {};

declare const Vue: any;
declare const cF: any;
declare const Url: any;
declare const Swal: any;

type TagListLabels = {
    titleListAll: string;
    tooltipContentList: string;
};

type TagItem = {
    id: number | string;
    boardTag: string;
    cnt: number;
};

const TagListModal = {
    name: "TagListModal",
    data(): {
        tagList: TagItem[];
        loading: boolean;
        labels: TagListLabels;
    } {
        return {
            tagList: [],
            loading: false,
            labels: {} as TagListLabels,
        };
    },
    mounted(): void {
        const labelsEl = document.getElementById("tag_list_modal_labels");
        if (labelsEl) {
            try {
                this.labels = JSON.parse(labelsEl.textContent || "{}") as TagListLabels;
            } catch { /* ignore parse error */ }
        }
        window.addEventListener("tag:open-list-modal", (): void => {
            this.open();
        });
    },
    methods: {
        /**
         * 태그 전체 목록 모달 열기.
         * 변경 전: tag_module.ts `listAjax` — `$("#contentType").val()` 로 contentType 을 읽어 AJAX.
         */
        open(): void {
            this.loading = true;
            this.tagList = [];

            const el = document.getElementById("tag_list_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
            /* 변경 전: `$("#board_tag_dtl_modal").modal("hide")` */
            const dtlEl = document.getElementById("tag_dtl_modal");
            (window as any).bootstrap?.Modal.getInstance(dtlEl)?.hide();

            const contentTypeEl = document.getElementById("contentType") as HTMLInputElement | null;
            const ajaxData: Record<string, any> = { contentType: contentTypeEl?.value ?? "" };

            (cF as any).ajax.get((Url as any).TAGS, ajaxData, (res: any): void => {
                this.loading = false;
                if (!res.rslt) {
                    if ((cF as any).util.isNotEmpty(res.message)) (Swal as any).fire({ text: res.message });
                    return;
                }
                this.tagList = Array.isArray(res.rsltList) ? res.rsltList : [];
            });
        },
        /**
         * 태그 상세 모달 열기.
         * 변경 전: `onclick="Tag.dtlModal('{{id}}');"` → `tag:open-dtl-modal` CustomEvent dispatch.
         * @param {string|number} id - 태그 ID.
         */
        openTag(id: string | number): void {
            window.dispatchEvent(new CustomEvent("tag:open-dtl-modal", { detail: { id } }));
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
            <div class="card mt-0 py-4">
                <div class="mb-0">
                    <div class="d-flex align-items-center justify-content-between">
                        <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                            <span class="vertical-middle">{{ labels.titleListAll }}</span>
                        </h1>
                    </div>
                    <div class="separator separator-dashed border-gray-300 my-2"></div>
                    <div class="d-flex align-items-center mb-4 py-4">
                        <span v-for="tag in tagList" :key="tag.id"
                              class="ctgr-span ctgr-gray cursor-pointer"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              :title="labels.tooltipContentList"
                              @click="openTag(tag.id)">
                            <span>&#35;</span><span class="border-bottom text-primary fw-normal">{{ tag.boardTag }}</span>
                            <span class="fs-9 text-noti">[{{ tag.cnt }}]</span>
                        </span>
                    </div>
                </div>
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
    const mountEl = document.getElementById("tag_list_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { TagListModal },
        template: `<TagListModal />`,
    }).mount("#tag_list_modal_app");
});