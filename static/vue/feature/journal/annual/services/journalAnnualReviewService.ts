/**
 * journalAnnualReviewService.ts
 * 저널 결산 리뷰 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-β):
 *   - classic `static/js/view/feature/journal/annual/journal_annual_review_module.ts` 를 본 ES module 로 이전한다.
 *   - 외부 호출 시그니처 보존: `dF.JournalAnnualReview.<method>` (init / initForm / submit / registModal / modifyModal / registAjax / deleteAjax).
 *   - 인스턴스 필드 `tagify` 는 모듈 표면에 유지(JournalAnnualReviewRegistModalApp 이 직접 set).
 *   - 적재 순서:
 *     · `_journal_annual_review_regist_modal.ftlh` 의 `journalAnnualReviewRegistVueScriptDone` 가드 안에서
 *       `journalAnnualReviewCrudService` → 본 ES module → `JournalAnnualReviewRegistModalApp` 순으로 적재한다.
 *
 * @author nichefish
 */

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

const journalAnnualReviewModule: dfModule = {
    initialized: false,
    tagify: null,

    /**
     * 모듈을 초기화한다.
     */
    init: function(): void {
        if (journalAnnualReviewModule.initialized) return;

        journalAnnualReviewModule.initialized = true;
        console.log("'dF.JournalAnnualReview' module initialized.");
    },

    /**
     * 폼 초기화
     * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
     *
     * 변경(A-3): cF.handlebars.modal + jQuery validate + tagify(이중 호출 보존) + tinymce 부착은
     *   Vue JournalAnnualReviewRegistModalApp(teleport) 가 attachRegistFormControls 경로로 동일 수행한다.
     *   본 메서드는 그 브리지(`window.JournalAnnualReviewRegistVueApp`) 진입만 담당한다.
     *   원본의 `dfNs.JournalAnnualReview.tagify = cF.tagify.init(...)` 인스턴스 보관은 Vue 측 attachRegistFormControls 에서 동일 시점에 수행(행위 보존).
     */
    initForm: function(obj: Record<string, any> = {}): void {
        const bridge = (typeof window !== "undefined"
            ? (window as unknown as {
                JournalAnnualReviewRegistVueApp?: {
                    mounted?: boolean;
                    pendingPayload?: Record<string, any> | null;
                    open?: (model: Record<string, any>) => void;
                };
            }).JournalAnnualReviewRegistVueApp
            : undefined);

        if (bridge?.mounted === true && typeof bridge.open === "function") {
            bridge.open(obj);
            return;
        }
        if (bridge && bridge.mounted !== true) {
            bridge.pendingPayload = obj;
            console.log("[JournalAnnualReview.initForm] JournalAnnualReviewRegistVueApp pending payload queued.");
            return;
        }
        console.error("[JournalAnnualReview.initForm] JournalAnnualReviewRegistVueApp unavailable (모달 스텁 없음 또는 로드 순서 확인).");
    },

    /**
     * 폼 제출
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.submit 위임.
     */
    submit: function(): void {
        dfNs.JournalAnnualReviewCrudService?.submit?.();
    },

    /**
     * 등록(수정) 모달 호출
     * @param {string|number} journalAnnualId - 저널 결산 번호
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.registModal 위임.
     */
    registModal: function({ journalAnnualId }: { journalAnnualId: string|number }): void {
        dfNs.JournalAnnualReviewCrudService?.registModal?.({ journalAnnualId });
    },

    /**
     * 등록(수정) 모달 호출
     * @param {string|number} id - 년도.
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.modifyModal 위임.
     */
    modifyModal: function(id: string|number): void {
        dfNs.JournalAnnualReviewCrudService?.modifyModal?.(id);
    },

    /**
     * 등록 (Ajax)
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.registAjax 위임.
     */
    registAjax: function(): void {
        dfNs.JournalAnnualReviewCrudService?.registAjax?.();
    },

    /**
     * 삭제 (Ajax)
     * @param {string|number} id - 글 번호.
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.deleteAjax 위임.
     */
    deleteAjax: function(id: string|number): void {
        dfNs.JournalAnnualReviewCrudService?.deleteAjax?.(id);
    },
};

dfNs.JournalAnnualReview = journalAnnualReviewModule;

export default journalAnnualReviewModule;
