/**
 * journalAnnualReviewService.ts
 * 저널 결산 리뷰 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-β):
 *   - classic `static/js/view/feature/journal/annual/journal_annual_review_module.ts` 를 본 ES module 로 이전한다.
 *   - 외부 호출 시그니처 보존: `dF.JournalAnnualReview.<method>` (init / initForm / submit / regModal / mdfModal / regAjax / delAjax).
 *   - 인스턴스 필드 `tagify` 는 모듈 표면에 유지(JournalAnnualReviewRegModalApp 이 직접 set).
 *   - 적재 순서:
 *     · `_journal_annual_review_reg_modal.ftlh` 의 `journalAnnualReviewRegVueScriptDone` 가드 안에서
 *       `journalAnnualReviewCrudService` → 본 ES module → `JournalAnnualReviewRegModalApp` 순으로 적재한다.
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
     *   Vue JournalAnnualReviewRegModalApp(teleport) 가 attachRegFormControls 경로로 동일 수행한다.
     *   본 메서드는 그 브리지(`window.JournalAnnualReviewRegVueApp`) 진입만 담당한다.
     *   원본의 `dfNs.JournalAnnualReview.tagify = cF.tagify.init(...)` 인스턴스 보관은 Vue 측 attachRegFormControls 에서 동일 시점에 수행(행위 보존).
     */
    initForm: function(obj: Record<string, any> = {}): void {
        const bridge = (typeof window !== "undefined"
            ? (window as unknown as {
                JournalAnnualReviewRegVueApp?: {
                    mounted?: boolean;
                    pendingPayload?: Record<string, any> | null;
                    open?: (model: Record<string, any>) => void;
                };
            }).JournalAnnualReviewRegVueApp
            : undefined);

        if (bridge?.mounted === true && typeof bridge.open === "function") {
            bridge.open(obj);
            return;
        }
        if (bridge && bridge.mounted !== true) {
            bridge.pendingPayload = obj;
            console.log("[JournalAnnualReview.initForm] JournalAnnualReviewRegVueApp pending payload queued.");
            return;
        }
        console.error("[JournalAnnualReview.initForm] JournalAnnualReviewRegVueApp unavailable (모달 스텁 없음 또는 로드 순서 확인).");
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
     * 변경(A-4-α): JournalAnnualReviewCrudService.regModal 위임.
     */
    regModal: function({ journalAnnualId }: { journalAnnualId: string|number }): void {
        dfNs.JournalAnnualReviewCrudService?.regModal?.({ journalAnnualId });
    },

    /**
     * 등록(수정) 모달 호출
     * @param {string|number} id - 년도.
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.mdfModal 위임.
     */
    mdfModal: function(id: string|number): void {
        dfNs.JournalAnnualReviewCrudService?.mdfModal?.(id);
    },

    /**
     * 등록 (Ajax)
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.regAjax 위임.
     */
    regAjax: function(): void {
        dfNs.JournalAnnualReviewCrudService?.regAjax?.();
    },

    /**
     * 삭제 (Ajax)
     * @param {string|number} id - 글 번호.
     *
     * 변경(A-4-α): JournalAnnualReviewCrudService.delAjax 위임.
     */
    delAjax: function(id: string|number): void {
        dfNs.JournalAnnualReviewCrudService?.delAjax?.(id);
    },
};

dfNs.JournalAnnualReview = journalAnnualReviewModule;

export default journalAnnualReviewModule;
