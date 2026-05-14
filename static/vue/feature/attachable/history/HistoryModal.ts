/**
 * HistoryModal.ts
 * 공통 변경이력 모달 Vue 컴포넌트
 *
 * 변경(D): _history_modal.ftlh 의 history_modal_module 인라인 스크립트와
 *          Handlebars 템플릿을 Vue 컴포넌트로 전환.
 *          - CustomEvent('history:open-modal') → 이력 목록 조회 후 모달 표시.
 *          - restore / remove / clear 액션 성공 시 location.reload().
 *
 * @author nichefish
 */

export {};

declare const Vue: any;
declare const cF: any;
declare const Url: any;
declare const Swal: any;
declare const Message: any;

type HistoryItem = {
    id: number | string;
    historyType: string;
    fromHistoryId?: number | string;
    createdAt: string;
    createdByNm: string;
    previewContent: string;
    markdownContent?: string;
};

type HistoryModalLabels = {
    triggeredAt: string;
    typeRestore: string;
    typeChange: string;
    regDt: string;
    createdBy: string;
    fromNo: string;
    restore: string;
    del: string;
    clear: string;
    summary: string;
    detail: string;
    empty: string;
    tooltipRestore: string;
    tooltipDel: string;
    tooltipClear: string;
};

const HistoryModal = {
    name: "HistoryModal",
    data(): {
        contentType: string;
        postId: string | number;
        historyTriggeredAt: string;
        historyList: HistoryItem[];
        loading: boolean;
        labels: HistoryModalLabels;
    } {
        return {
            contentType: "",
            postId: "",
            historyTriggeredAt: "",
            historyList: [],
            loading: false,
            labels: {} as HistoryModalLabels,
        };
    },
    mounted(): void {
        // 레이블 JSON 주입값 읽기
        const labelsEl = document.getElementById("history_modal_labels");
        if (labelsEl) {
            try {
                this.labels = JSON.parse(labelsEl.textContent || "{}") as HistoryModalLabels;
            } catch { /* ignore parse error */ }
        }

        window.addEventListener("history:open-modal", (e: Event): void => {
            const { contentType, id } =
                (e as CustomEvent<{ contentType: string; id: string | number }>).detail;
            void this.open(contentType, id);
        });
    },
    methods: {
        /**
         * 이력 모달 열기 — 서버에서 이력 목록 조회 후 모달 표시
         * @param {string} contentType - 콘텐츠 타입.
         * @param {string|number} id - 게시물 번호.
         */
        async open(contentType: string, id: string | number): Promise<void> {
            if (!contentType || isNaN(Number(id))) return;
            this.loading = true;
            this.historyList = [];
            const el = document.getElementById("attachable_history_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
            const url: string = (cF as any).util.bindUrl((Url as any).HISTORIES, { contentType, id });
            (cF as any).ajax.get(url, null, (res: any): void => {
                this.loading = false;
                if (!res.rslt) {
                    if ((cF as any).util.isNotEmpty(res.message)) (Swal as any).fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj ?? {};
                this.contentType = contentType;
                this.postId = rsltObj.id ?? Number(id);
                this.historyTriggeredAt = rsltObj.historyTriggeredAt ?? rsltObj.history?.historyTriggeredAt ?? "";
                this.historyList = Array.isArray(rsltObj.historyList) ? rsltObj.historyList : [];
            });
        },
        /**
         * 이력 복원
         * @param {string|number} historyId - 복원할 이력 번호.
         */
        restore(historyId: string | number): void {
            (Swal as any).fire({
                text: (Message as any).get("view.cnfm.restore"),
                showCancelButton: true,
            }).then((result: { value: boolean }): void => {
                if (!result.value) return;
                const url: string = (cF as any).util.bindUrl((Url as any).HISTORY_RESTORE, {
                    contentType: this.contentType, id: this.postId, historyId,
                });
                (cF as any).$ajax.post(url, null, (res: any): void => {
                    (Swal as any).fire({ text: res.message }).then((): void => {
                        if (res.rslt) location.reload();
                    });
                }, "block");
            });
        },
        /**
         * 이력 단건 삭제
         * @param {string|number} historyId - 삭제할 이력 번호.
         */
        remove(historyId: string | number): void {
            (Swal as any).fire({
                text: (Message as any).get("view.cnfm.del"),
                showCancelButton: true,
            }).then((result: { value: boolean }): void => {
                if (!result.value) return;
                const url: string = (cF as any).util.bindUrl((Url as any).HISTORY, {
                    contentType: this.contentType, id: this.postId, historyId,
                });
                (cF as any).$ajax.delete(url, null, (res: any): void => {
                    (Swal as any).fire({ text: res.message }).then((): void => {
                        if (res.rslt) location.reload();
                    });
                }, "block");
            });
        },
        /**
         * 이력 전체 삭제
         */
        clear(): void {
            (Swal as any).fire({
                text: (Message as any).get("view.cnfm.del"),
                showCancelButton: true,
            }).then((result: { value: boolean }): void => {
                if (!result.value) return;
                const url: string = (cF as any).util.bindUrl((Url as any).HISTORY_CLEAR, {
                    contentType: this.contentType, id: this.postId,
                });
                (cF as any).$ajax.delete(url, null, (res: any): void => {
                    (Swal as any).fire({ text: res.message }).then((): void => {
                        if (res.rslt) location.reload();
                    });
                }, "block");
            });
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
            <div class="d-flex justify-content-between align-items-center mb-5">
                <div class="fs-7 text-muted">
                    {{ labels.triggeredAt }}: <span class="text-gray-700">{{ historyTriggeredAt || '-' }}</span>
                </div>
                <button v-if="historyList.length > 0"
                        type="button"
                        class="btn btn-sm btn-light-danger"
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        :title="labels.tooltipClear"
                        @click="clear()">
                    <i class="bi bi-trash3"></i>
                    <span>{{ labels.clear }}</span>
                </button>
            </div>

            <div class="d-flex flex-column gap-4">
                <template v-if="historyList.length > 0">
                    <div v-for="item in historyList" :key="item.id" class="card card-bordered shadow-sm">
                        <div class="card-body py-4 px-5">
                            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                                <div class="flex-grow-1">
                                    <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                                        <span v-if="item.historyType === 'RESTORE'"
                                              class="badge badge-light-warning text-warning">{{ labels.typeRestore }}</span>
                                        <span v-else class="badge badge-light-primary text-primary">{{ labels.typeChange }}</span>
                                        <span class="badge badge-light-dark text-dark"># {{ item.id }}</span>
                                        <span v-if="item.fromHistoryId" class="fs-8 text-muted">{{ labels.fromNo }} # {{ item.fromHistoryId }}</span>
                                    </div>
                                    <div class="d-flex flex-wrap gap-4 fs-7">
                                        <div>
                                            <span class="text-muted">{{ labels.regDt }}:</span>
                                            <span class="fw-semibold text-dark ms-1">{{ item.createdAt || '-' }}</span>
                                        </div>
                                        <div>
                                            <span class="text-muted">{{ labels.createdBy }}:</span>
                                            <span class="fw-semibold text-dark ms-1">{{ item.createdByNm || '-' }}</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="d-flex gap-2">
                                    <button type="button"
                                            class="btn btn-sm btn-light-primary"
                                            data-bs-toggle="tooltip"
                                            data-bs-placement="top"
                                            :title="labels.tooltipRestore"
                                            @click="restore(item.id)">
                                        <i class="bi bi-arrow-counterclockwise"></i>
                                        <span>{{ labels.restore }}</span>
                                    </button>
                                    <button type="button"
                                            class="btn btn-sm btn-light-danger"
                                            data-bs-toggle="tooltip"
                                            data-bs-placement="top"
                                            :title="labels.tooltipDel"
                                            @click="remove(item.id)">
                                        <i class="bi bi-trash"></i>
                                        <span>{{ labels.del }}</span>
                                    </button>
                                </div>
                            </div>
                            <div class="journal-history-preview fs-7 text-gray-700">
                                <div class="fw-semibold text-muted mb-2">{{ labels.summary }}</div>
                                {{ item.previewContent || '-' }}
                            </div>
                            <div class="mt-3">
                                <button type="button"
                                        class="btn btn-sm btn-light"
                                        data-bs-toggle="collapse"
                                        :data-bs-target="'#attachable_history_detail_' + item.id"
                                        aria-expanded="false"
                                        :aria-controls="'attachable_history_detail_' + item.id">
                                    <i class="bi bi-layout-text-window"></i>
                                    <span>{{ labels.detail }}</span>
                                </button>
                            </div>
                            <div :id="'attachable_history_detail_' + item.id" class="collapse mt-3">
                                <div class="border rounded bg-light px-4 py-4 fs-7 text-gray-800 journal-history-detail">
                                    <div class="fw-semibold text-muted mb-3">{{ labels.detail }}</div>
                                    <span v-if="item.markdownContent" v-html="item.markdownContent"></span>
                                    <span v-else>-</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
                <div v-else class="d-flex-center min-h-150px text-muted">
                    {{ labels.empty }}
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
    const mountEl = document.getElementById("history_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { HistoryModal },
        template: `<HistoryModal />`,
    }).mount("#history_modal_app");
});