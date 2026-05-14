/**
 * RelatedContentAddModal.ts
 * 관련 글 추가 모달 Vue 컴포넌트
 *
 * 변경(D): related_content_module.ts 의 openAddModalWithSource / searchTargetsInPopup /
 *          selectTargetByKey / buildAddModalHtml / Swal 팝업 기반 UI 를
 *          Bootstrap Modal + Vue 컴포넌트로 전환.
 *          - CustomEvent('related-content:open-add-modal') { contentType, id } → open().
 *          - 저장 성공 시 CustomEvent('related-content:refresh') dispatch.
 *
 * @author nichefish
 */
import * as relatedContentService from "./relatedContentService.js";
import type { RelatedSource, RelatedTargetItem, RelatedContentPayload } from "./relatedContentService.js";

declare const Vue: any;

/** 콘텐츠 타입 → 한글 레이블 */
const CONTENT_TYPE_LABEL_MAP: Record<string, string> = {
    JOURNAL_DIARY: "일기",
    JOURNAL_DREAM: "꿈",
};

const RelatedContentAddModal = {
    name: "RelatedContentAddModal",
    data(): {
        srcContentType: string;
        srcId: number;
        relationType: string;
        targetContentType: string;
        keyword: string;
        searchResults: RelatedTargetItem[];
        selectedTargetId: number;
        selectedTarget: RelatedTargetItem | null;
        reason: string;
        searching: boolean;
        searchAttempted: boolean;
        validationMsg: string;
        searchMap: Record<string, RelatedTargetItem>;
    } {
        return {
            srcContentType: "",
            srcId: 0,
            relationType: "REFERENCE",
            targetContentType: "JOURNAL_DREAM",
            keyword: "",
            searchResults: [],
            selectedTargetId: 0,
            selectedTarget: null,
            reason: "",
            searching: false,
            searchAttempted: false,
            validationMsg: "",
            searchMap: {},
        };
    },
    computed: {
        srcLabel(): string {
            return CONTENT_TYPE_LABEL_MAP[this.srcContentType] ?? this.srcContentType;
        },
    },
    mounted(): void {
        window.addEventListener("related-content:open-add-modal", (e: Event): void => {
            const { contentType, id } =
                (e as CustomEvent<{ contentType: string; id: number }>).detail;
            this.open(contentType, Number(id));
        });
    },
    methods: {
        /**
         * 모달 열기
         * @param {string} contentType - 출처 콘텐츠 타입.
         * @param {number} id - 출처 게시물 번호.
         */
        open(contentType: string, id: number): void {
            this.srcContentType = contentType;
            this.srcId = id;
            this.relationType = "REFERENCE";
            this.targetContentType = contentType === "JOURNAL_DIARY" ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
            this.keyword = "";
            this.searchResults = [];
            this.selectedTargetId = 0;
            this.selectedTarget = null;
            this.reason = "";
            this.searching = false;
            this.searchAttempted = false;
            this.validationMsg = "";
            this.searchMap = {};
            const el = document.getElementById("related_content_add_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).show();
        },
        /** 대상 유형 변경 시 검색 상태 초기화 */
        onTargetTypeChange(): void {
            this.searchResults = [];
            this.selectedTargetId = 0;
            this.selectedTarget = null;
            this.searchAttempted = false;
            this.searchMap = {};
        },
        /** 검색 실행 */
        search(): void {
            this.validationMsg = "";
            if (!this.keyword.trim()) {
                this.searchResults = [];
                this.searchAttempted = false;
                return;
            }
            this.searching = true;
            this.searchAttempted = true;
            relatedContentService.searchTargets(
                this.targetContentType,
                this.keyword,
                (list: RelatedTargetItem[]): void => {
                    this.searching = false;
                    this.searchMap = {};
                    list.forEach((item: RelatedTargetItem): void => {
                        this.searchMap[this.targetKey(item)] = item;
                    });
                    this.searchResults = list;
                }
            );
        },
        /**
         * 검색 결과 항목 선택
         * @param {string} key - 항목 키(contentType:id).
         */
        selectTarget(key: string): void {
            const item: RelatedTargetItem | undefined = this.searchMap[key];
            if (!item) return;
            this.selectedTargetId = item.id;
            this.selectedTarget = item;
        },
        /**
         * 항목 키 생성
         * @param {RelatedTargetItem} item - 검색 결과 항목.
         */
        targetKey(item: RelatedTargetItem): string {
            return `${item.contentType}:${item.id}`;
        },
        /**
         * 콘텐츠 타입 한글 레이블
         * @param {string} contentType - 콘텐츠 타입.
         */
        contentTypeLabel(contentType: string): string {
            return CONTENT_TYPE_LABEL_MAP[contentType] ?? contentType;
        },
        /**
         * 본문 미리보기 텍스트 생성 (HTML 제거 + 120자 절단)
         * @param {string} value - 원본 텍스트.
         */
        toPreviewText(value: string): string {
            const text: string = String(value ?? "")
                .replace(/<[^>]+>/g, " ")
                .replace(/\s+/g, " ")
                .trim();
            return text.length <= 120 ? text : text.substring(0, 120) + "...";
        },
        /** 저장 실행 — 유효성 검사 후 AJAX */
        save(): void {
            this.validationMsg = "";
            if (!this.targetContentType) {
                this.validationMsg = "대상 글 유형을 선택해 주세요."; return;
            }
            if (!this.selectedTargetId) {
                this.validationMsg = "검색 결과에서 연결할 글을 선택해 주세요."; return;
            }
            if (this.targetContentType === this.srcContentType && this.selectedTargetId === this.srcId) {
                this.validationMsg = "현재 글 자신과는 연결할 수 없습니다."; return;
            }
            if (!this.relationType) {
                this.validationMsg = "관련 유형을 선택해 주세요."; return;
            }
            const source: RelatedSource = { id: this.srcId, contentType: this.srcContentType };
            const payload: RelatedContentPayload = {
                srcId: this.srcId,
                srcContentType: this.srcContentType,
                targetId: this.selectedTargetId,
                targetContentType: this.targetContentType,
                relationType: this.relationType,
                reason: this.reason,
            };
            relatedContentService.saveAjax(source, payload, (): void => {
                this._hideModal();
                window.dispatchEvent(new CustomEvent("related-content:refresh"));
            });
        },
        /** Bootstrap 모달 닫기 */
        _hideModal(): void {
            const el = document.getElementById("related_content_add_modal");
            (window as any).bootstrap?.Modal.getOrCreateInstance(el).hide();
        },
        /**
         * 키워드 입력 keydown 핸들러 — Enter 시 검색
         * @param {KeyboardEvent} e - 키보드 이벤트.
         */
        onKeydown(e: KeyboardEvent): void {
            if (e.key === "Enter") {
                e.preventDefault();
                this.search();
            }
        },
    },
    template: `
    <div id="related_content_add_modal" class="modal fade" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">관련 글 추가</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body px-5 py-6">
                    <div class="rounded bg-light-primary text-primary px-4 py-3 fs-7 mb-4">
                        현재 글: {{ srcLabel }} #{{ srcId }}
                    </div>
                    <div class="row g-3 mb-4">
                        <div class="col-md-4">
                            <label class="form-label fw-semibold text-gray-700">관련 유형</label>
                            <select v-model="relationType" class="form-select form-select-solid">
                                <option value="REFERENCE">참조</option>
                                <option value="EXTENSION">확장</option>
                                <option value="PARALLEL">병렬</option>
                                <option value="CAUSE">원인</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-semibold text-gray-700">대상 글 유형</label>
                            <select v-model="targetContentType" class="form-select form-select-solid" @change="onTargetTypeChange">
                                <option value="JOURNAL_DIARY">일기</option>
                                <option value="JOURNAL_DREAM">꿈</option>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end">
                            <button type="button" class="btn btn-light-account w-100" @click="search">검색</button>
                        </div>
                    </div>
                    <div class="mb-4">
                        <label class="form-label fw-semibold text-gray-700">검색 키워드</label>
                        <input
                            type="text"
                            v-model="keyword"
                            class="form-control form-control-solid"
                            maxlength="100"
                            placeholder="제목이나 내용 키워드를 입력해 주세요."
                            @keydown="onKeydown"
                        />
                        <div class="text-muted fs-8 mt-2">검색 결과를 클릭하면 연결 대상이 선택됩니다.</div>
                    </div>
                    <div v-if="selectedTarget"
                         class="rounded border border-primary bg-light-primary px-4 py-3 text-start mb-4">
                        <div class="fw-semibold text-primary mb-1">{{ selectedTarget.title || '#' + selectedTarget.id }}</div>
                        <div class="text-muted fs-7">
                            {{ contentTypeLabel(selectedTarget.contentType) }} #{{ selectedTarget.id }}
                            <span v-if="selectedTarget.stdrdDt"> | {{ selectedTarget.stdrdDt }}</span>
                        </div>
                    </div>
                    <div v-else class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4">
                        아직 선택한 글이 없습니다.
                    </div>
                    <div v-if="validationMsg" class="rounded border border-dashed border-warning px-4 py-3 text-warning fs-7 mb-4">
                        {{ validationMsg }}
                    </div>
                    <div v-if="searching" class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4">
                        <span class="spinner-border spinner-border-sm me-2"></span>검색 중입니다.
                    </div>
                    <template v-else-if="searchAttempted">
                        <div v-if="searchResults.length === 0" class="rounded border border-dashed border-gray-300 px-4 py-3 text-muted fs-7 mb-4">
                            검색 결과가 없습니다.
                        </div>
                        <div v-for="item in searchResults" :key="targetKey(item)" class="mb-3">
                            <button
                                type="button"
                                :class="['btn w-100 text-start rounded border px-4 py-3', selectedTargetId === item.id ? 'border-primary bg-light-primary' : 'border-gray-300']"
                                @click="selectTarget(targetKey(item))"
                            >
                                <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap mb-2">
                                    <span class="fw-semibold text-gray-900">{{ item.title || '#' + item.id }}</span>
                                    <span class="text-muted fs-8">#{{ item.id }}<span v-if="item.stdrdDt"> | {{ item.stdrdDt }}</span></span>
                                </div>
                                <div class="text-muted fs-7">{{ toPreviewText(item.content) || '미리보기가 없습니다.' }}</div>
                            </button>
                        </div>
                    </template>
                    <div>
                        <label class="form-label fw-semibold text-gray-700">메모</label>
                        <textarea
                            v-model="reason"
                            class="form-control form-control-solid"
                            rows="3"
                            maxlength="255"
                            placeholder="왜 연결하는지 간단히 적어 둘 수 있습니다."
                        ></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-primary" @click="save">저장</button>
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
    const mountEl = document.getElementById("related_content_add_modal_app");
    if (!mountEl) return;

    Vue.createApp({
        components: { RelatedContentAddModal },
        template: `<RelatedContentAddModal />`,
    }).mount("#related_content_add_modal_app");
});