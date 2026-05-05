/**
 * journalInterpretationService.ts
 * 저널 해석 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(I-4):
 *   - classic `static/js/view/feature/journal/interpretation/journal_interpretation_module.ts` 를 본 service 로 이전한다.
 *   - 외부 호출 시그니처 보존:
 *     · `dF.JournalInterpretation.<method>` (init / initForm / regModal / submit / regAjax /
 *        mdfModal / delAjax / resolveAjax / setLifecycleAjax / collapse /
 *        toggle / initCollapseState / copy)
 *     · 인스턴스 필드(`STORAGE_KEY` / `initialized` / `inKeywordSearchMode`) 직접 read/write 도 동일 유지.
 *
 *   변경 이력 비고:
 *     · 변경 전(I-4): dtlModal 도 표면에 포함되어 있었으나 미존재 HBS 템플릿 호출(가시 dead)이었고,
 *       I-5 에서 service 본체·thin wrapper·타입까지 정식 제거되었다.
 *   - I-3 thin wrapper 그대로 유지(상태/CRUD 위임은 `JournalInterpretationStateService` /
 *     `JournalInterpretationCrudService`). `initForm` 은 Vue 브리지 진입.
 *   - 적재 순서:
 *     · 변경(I-4): 5개 ftlh 의 classic `<script type="text/javascript">` 적재 라인을 제거하고,
 *       `_journal_interpretation_reg_modal.ftlh` 의 `journalInterpretationRegVueScriptDone` 가드 안에서
 *       본 ES module 을 *최우선* 적재한다(Service 표면 등록 → 의존 service → RegModalApp).
 *     · ES module 은 `defer` 와 동등 평가이므로 페이지의 `Url` / `Message` / `cF` 등 글로벌 등록 이후 평가된다.
 *
 * 호출 그래프 비고:
 *   - HBS onclick(`_journal_entry_context_btn_partial.hbs`), Vue 컴포넌트(JournalInterpretationItem,
 *     JournalEntryContextMenu)는 사용자 트리거 시점 호출이라 ES module defer 평가 후에 실행된다.
 *   - IIFE(부트 시점)에서 `dF.JournalInterpretation.*` 를 호출하는 외부 모듈은 없으므로
 *     entry δ-1 의 annual_known_break 같은 이슈는 발생하지 않는다.
 *
 * @author nichefish
 */

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

const journalInterpretationModule: dfModule = {
    STORAGE_KEY: "collapsedJournalInterpretationIds",

    initialized: false,
    inKeywordSearchMode: false,

    /**
     * 모듈을 초기화한다.
     */
    init: function(): void {
        if (journalInterpretationModule.initialized) return;

        dfNs.Lifecycle?.init?.();
        journalInterpretationModule.initialized = true;
        console.log("'dF.JournalInterpretation' module initialized.");
    },

    /**
     * 폼 초기화 (Vue 브리지 진입)
     * @param {Record<string, any>} obj - 폼에 바인딩할 데이터
     *
     * 변경(I-2):
     *   - 변경 전: cF.handlebars.modal + cF.validate.validateForm + cF.ui.chckboxLabel + cF.tinymce.init/setContentWhenReady 직접 호출.
     *   - 변경 후: window.JournalInterpretationRegVueApp.open(obj) 단일 진입. Vue 진입 함수가 동일 호출 순서를 attachRegFormControls 안에서 수행.
     *     모달 표시(Bootstrap show) 도 Vue 가 담당. classic 은 thin bridge 만 남긴다.
     *   - mounted=false 인 경우(스크립트 로드 전) pendingPayload 큐로 폴백.
     */
    initForm: function(obj: Record<string, any> = {}): void {
        const bridge = (window as unknown as { JournalInterpretationRegVueApp?: { mounted?: boolean; pendingPayload?: Record<string, any> | null; open?: (model: Record<string, any>) => void; }; }).JournalInterpretationRegVueApp;
        if (bridge?.mounted === true && typeof bridge.open === "function") {
            bridge.open(obj);
            return;
        }
        if (bridge && bridge.mounted !== true) {
            bridge.pendingPayload = obj;
            console.log("[JournalInterpretation.initForm] JournalInterpretationRegVueApp pending payload.");
            return;
        }
        console.error("[JournalInterpretation.initForm] JournalInterpretationRegVueApp unavailable (모달 스텁 없음 또는 로드 순서 확인).");
    },

    /**
     * 등록 모달 호출
     *
     * 변경(I-3): JournalInterpretationCrudService.regModal 위임.
     */
    regModal: function({
        journalDayId,
        refId,
        refContentType,
        stdrdDt,
        journalDateWeekDay,
    }: {
        journalDayId: string | number;
        refId: string | number;
        refContentType: string;
        stdrdDt: string;
        journalDateWeekDay: string;
    }): void {
        dfNs.JournalInterpretationCrudService?.regModal?.({
            journalDayId,
            refId,
            refContentType,
            stdrdDt,
            journalDateWeekDay,
        });
    },

    /**
     * 폼 제출
     *
     * 변경(I-3): JournalInterpretationCrudService.submit 위임.
     */
    submit: function(): void {
        dfNs.JournalInterpretationCrudService?.submit?.();
    },

    /**
     * 등록 (Ajax)
     *
     * 변경(I-3): JournalInterpretationCrudService.regAjax 위임.
     */
    regAjax: function(): void {
        dfNs.JournalInterpretationCrudService?.regAjax?.();
    },

    /**
     * (변경 I-5) 상세 모달 호출 dtlModal thin wrapper 제거됨.
     *   - 변경 전(I-3): JournalInterpretationCrudService.dtlModal 로 위임하던 thin wrapper.
     *   - 본체가 가시 dead(미존재 HBS) 였고 외부 호출자가 0 이라 본 phase 에서 정식 제거.
     */

    /**
     * 수정 모달 호출
     *
     * 변경(I-3): JournalInterpretationCrudService.mdfModal 위임.
     */
    mdfModal: function(id: string|number): void {
        dfNs.JournalInterpretationCrudService?.mdfModal?.(id);
    },

    /**
     * 삭제 (Ajax)
     *
     * 변경(I-3): JournalInterpretationCrudService.delAjax 위임.
     */
    delAjax: function(id: string|number): void {
        dfNs.JournalInterpretationCrudService?.delAjax?.(id);
    },

    /**
     * 컨텍스트 메뉴 스위치 값에 따라 해석 라이프사이클을 RESOLVED 또는 OPEN으로 설정한다.
     *
     * 변경(I-3): JournalInterpretationStateService.resolveAjax 위임.
     */
    resolveAjax: function(id: string|number, trigger?: HTMLInputElement): void {
        dfNs.JournalInterpretationStateService?.resolveAjax?.(id, trigger);
    },

    /**
     * 해석 라이프사이클을 명시적으로 설정한다.
     *
     * 변경(I-3): JournalInterpretationStateService.setLifecycleAjax 위임.
     */
    setLifecycleAjax: function(id: string|number, lifecycleKey: string): void {
        dfNs.JournalInterpretationStateService?.setLifecycleAjax?.(id, lifecycleKey);
    },

    /**
     * @param id - 글 번호.
     * @param collapsedYn - 글접기 여부.
     *
     * 변경(I-3): JournalInterpretationStateService.collapse 위임.
     */
    collapse: function(id: string|number, collapsedYn: "Y"|"N"): void {
        dfNs.JournalInterpretationStateService?.collapse?.(id, collapsedYn);
    },

    /**
     * 접기/펼치기 토글
     *
     * 변경(I-3): JournalInterpretationStateService.toggle 위임.
     */
    toggle: function(id: string|number, trigger: HTMLElement): void {
        dfNs.JournalInterpretationStateService?.toggle?.(id, trigger);
    },

    /**
     * 접힌 엔트리 초기화
     *
     * 변경(I-3): JournalInterpretationStateService.initCollapseState 위임.
     */
    initCollapseState: function(): void {
        dfNs.JournalInterpretationStateService?.initCollapseState?.();
    },

    /**
     * 복사
     *
     * 변경(I-3): JournalInterpretationCrudService.copy 위임.
     *
     * @deprecated
     */
    copy: function(id: string|number): void {
        dfNs.JournalInterpretationCrudService?.copy?.(id);
    },
};

dfNs.JournalInterpretation = journalInterpretationModule;

export default journalInterpretationModule;
