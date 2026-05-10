/**
 * journalAnnualService.ts
 * 저널 결산 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-β):
 *   - classic `static/js/view/feature/journal/annual/journal_annual_module.ts` 를 본 ES module 로 이전한다.
 *   - 외부 호출 시그니처 보존: `dF.JournalAnnual.<method>` (init / initForm / listAjax / detailView / … / registAjax).
 *   - thin 위임은 `JournalAnnualCrudService` / `JournalAnnualStateService` 유지(A-4-α).
 *   - 적재 순서:
 *     · `_journal_annual_regist_modal.ftlh` 의 `journalAnnualRegistVueScriptDone` 가드 안에서
 *       `journalAnnualCrudService` → `journalAnnualStateService` → 본 ES module → `JournalAnnualRegistModalApp`
 *       순으로 적재한다(list 페이지는 결산 등록 모달 include 가 단일 진입점).
 *     · `journal_annual_detail.ftlh` 는 결산 등록 모달을 포함하지 않으므로 페이지 하단에서 동일 의존 순으로 적재한다.
 *   - ES module 은 `defer` 와 동등 평가이므로 페이지의 `Url` / `Message` / `cF` 등 글로벌 등록 이후 평가된다.
 *
 * 호출 그래프 비고:
 *   - 부트 시점에 `dF.JournalEntry` 를 참조하지 않는다(lazy entry/tag config 는 `journalAnnualStateService` 보존).
 *
 * @author nichefish
 */

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

const journalAnnualModule: dfModule = {
    initialized: false,

    /**
     * 모듈을 초기화한다.
     */
    init: function(): void {
        if (journalAnnualModule.initialized) return;

        journalAnnualModule.initialized = true;
        console.log("'dF.JournalAnnual' module initialized.");
    },

    /**
     * 폼 초기화
     * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
     *
     * 변경(A-3): cF.handlebars.modal + jQuery validate + tagify + tinymce 부착은
     *   Vue JournalAnnualRegistModalApp(teleport) 가 attachRegistFormControls 경로로 동일 수행한다.
     *   본 메서드는 그 브리지(`window.JournalAnnualRegistVueApp`) 진입만 담당한다.
     */
    initForm: function(obj: Record<string, any> = {}): void {
        const bridge = (typeof window !== "undefined"
            ? (window as unknown as {
                JournalAnnualRegistVueApp?: {
                    mounted?: boolean;
                    pendingPayload?: Record<string, any> | null;
                    open?: (model: Record<string, any>) => void;
                };
            }).JournalAnnualRegistVueApp
            : undefined);

        if (bridge?.mounted === true && typeof bridge.open === "function") {
            bridge.open(obj);
            return;
        }
        if (bridge && bridge.mounted !== true) {
            bridge.pendingPayload = obj;
            console.log("[JournalAnnual.initForm] JournalAnnualRegistVueApp pending payload queued.");
            return;
        }
        console.error("[JournalAnnual.initForm] JournalAnnualRegistVueApp unavailable (모달 스텁 없음 또는 로드 순서 확인).");
    },

    /**
     * 목록 갱신 (Ajax)
     *
     * 변경(A-4-α): JournalAnnualCrudService.listAjax 위임.
     */
    listAjax: function(): void {
        dfNs.JournalAnnualCrudService?.listAjax?.();
    },

    /**
     * 상세 화면으로 이동 (년도로 조회)
     * @param {string|number} yy - 조회할 년도.
     *
     * 변경(A-4-α): JournalAnnualCrudService.detailView 위임.
     */
    detailView: function(yy: string|number): void {
        dfNs.JournalAnnualCrudService?.detailView?.(yy);
    },

    /**
     * 섹션 전환 이동 (년도로 조회)
     * @param {"DIARY"|"DREAM"} section - 조회 섹션
     *
     * 변경(A-4-α): JournalAnnualCrudService.detailViewWithSection 위임.
     */
    detailViewWithSection: function(section: "DIARY"|"DREAM"): void {
        dfNs.JournalAnnualCrudService?.detailViewWithSection?.(section);
    },

    /**
     * 상세 조회 (Ajax) (년도로 조회)
     * @param {string|number} yy - 조회할 년도.
     *
     * 변경(A-4-α): JournalAnnualCrudService.detailAjax 위임.
     */
    detailAjax: function(yy: string|number): void {
        dfNs.JournalAnnualCrudService?.detailAjax?.(yy);
    },

    /**
     * URL 파라미터로부터 파라미터 객체 초기화
     *
     * 변경(A-4-α): JournalAnnualStateService.toggleParam 위임.
     */
    toggleParam: function(): void {
        dfNs.JournalAnnualStateService?.toggleParam?.();
    },

    /**
     * 중요 일기 목록 조회 (Ajax) (년도로 조회)
     * @param {string|number} yy - 조회할 년도.
     *
     * 변경(A-4-α): JournalAnnualStateService.getAnnualDiaryListAjax 위임.
     */
    getAnnualDiaryListAjax: function(yy: string|number): void {
        dfNs.JournalAnnualStateService?.getAnnualDiaryListAjax?.(yy);
    },

    /**
     * 엔트리 리스트 렌더
     * @param {Record<string, any>[]} list - 렌더할 데이터 리스트.
     * @param {"DIARY"|"DREAM"} type - 엔트리 타입.
     *
     * 변경(A-4-α): JournalAnnualStateService.renderEntryList 위임.
     */
    renderEntryList: function(list: Record<string, any>[] = [], type: "DIARY"|"DREAM"): void {
        dfNs.JournalAnnualStateService?.renderEntryList?.(list, type);
    },

    /**
     * 중요 꿈 목록 조회 (Ajax) (년도로 조회)
     * @param {string|number} yy - 조회할 년도.
     *
     * 변경(A-4-α): JournalAnnualStateService.getAnnualDreamListAjax 위임.
     */
    getAnnualDreamListAjax: function(yy: string|number): void {
        dfNs.JournalAnnualStateService?.getAnnualDreamListAjax?.(yy);
    },

    /**
     * 태그 목록 조회 (Ajax) (년도로 조회)
     * @param {string|number} yy - 조회할 년도.
     * @param {"DAY"|"DIARY"|"DREAM"} type - 조회 타입
     *
     * 변경(A-4-α): JournalAnnualStateService.getTagListAjax 위임.
     */
    getTagListAjax: function(yy: string|number, type: "DAY"|"DIARY"|"DREAM"): void {
        dfNs.JournalAnnualStateService?.getTagListAjax?.(yy, type);
    },

    /**
     * 태그 리스트 렌더
     * @param {Record<string, any>[]} list - 렌더할 데이터 리스트.
     * @param {"DAY"|"DIARY"|"DREAM"} type - 조회 타입
     *
     * 변경(A-4-α): JournalAnnualStateService.renderTagList 위임.
     */
    renderTagList: function(list: Record<string, any>[] = [], type: "DAY"|"DIARY"|"DREAM"): void {
        dfNs.JournalAnnualStateService?.renderTagList?.(list, type);
    },

    /**
     * 목록 화면으로 이동
     *
     * 변경(A-4-α): JournalAnnualCrudService.list 위임.
     */
    list: function(): void {
        dfNs.JournalAnnualCrudService?.list?.();
    },

    /**
     * 특정 년도 결산 생성 (Ajax)
     * @param {string|number} yy - 결산을 생성할 년도.
     *
     * 변경(A-4-α): JournalAnnualCrudService.makeYyAnnualAjax 위임.
     */
    makeYyAnnualAjax: function(yy: string|number): void {
        dfNs.JournalAnnualCrudService?.makeYyAnnualAjax?.(yy);
    },

    /**
     * 전체 년도 결산 갱신 (Ajax)
     *
     * 변경(A-4-α): JournalAnnualCrudService.makeTotalAnnualAjax 위임.
     */
    makeTotalAnnualAjax: function(): void {
        dfNs.JournalAnnualCrudService?.makeTotalAnnualAjax?.();
    },

    /**
     * 꿈 기록 완료 처리 (Ajax)
     * @param {string|number} id - 글 번호.
     *
     * 변경(A-4-α): JournalAnnualCrudService.comptAjax 위임.
     */
    comptAjax: function(id: string|number): void {
        dfNs.JournalAnnualCrudService?.comptAjax?.(id);
    },

    /**
     * 폼 제출
     *
     * 변경(A-4-α): JournalAnnualCrudService.submit 위임.
     */
    submit: function(): void {
        dfNs.JournalAnnualCrudService?.submit?.();
    },

    /**
     * 등록(수정) 모달 호출
     * @param {string|number} yy - 년도.
     *
     * 변경(A-4-α): JournalAnnualCrudService.modifyModal 위임.
     */
    modifyModal: function(yy: string|number): void {
        dfNs.JournalAnnualCrudService?.modifyModal?.(yy);
    },

    /**
     * 등록 (Ajax)
     *
     * 변경(A-4-α): JournalAnnualCrudService.registAjax 위임.
     */
    registAjax: function(): void {
        dfNs.JournalAnnualCrudService?.registAjax?.();
    },
};

dfNs.JournalAnnual = journalAnnualModule;

export default journalAnnualModule;
