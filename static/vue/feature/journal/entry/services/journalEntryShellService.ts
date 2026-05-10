/**
 * journalEntryShellService.ts
 * 저널 entry 화면 수명주기·등록 폼 브리지·챕터 목록 해석 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-4-δ-2):
 *   - `journalEntryService.ts` 인스턴스 전용 로직(`init`, `refresh`, `initForm`,
 *     `resolveChapterList`) 을 본 서비스로 추출한다.
 *   - `journalEntryCrudService.openRegModalWithDayContext` 의 챕터 목록 해석·등록 폼 진입도
 *     동일 구현을 재사용한다.
 *     변경 전: `module.resolveChapterList(res.rsltObj)` / `module.initForm({...})`.
 *     변경 후: `resolveJournalEntryChapterList(meta, res.rsltObj)` /
 *     `openJournalEntryRegForm(meta, {...})` — `dF.JournalEntry.get(ct)` 부재 시점에도
 *     동작 가능(annual classic IIFE 등록 순서 이슈 완화).
 *   - 외부 호출 표면(`dF.JournalEntry.get(ct).init|refresh|initForm|resolveChapterList(...)`) 은
 *     journalEntryService 가 동일 시그니처 thin wrapper 로 유지한다.
 *
 * @author nichefish
 */

type EntryConfig = Record<string, any>;
type EntryModule = Record<string, any>;

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

/**
 * @keepInSync static/vue/feature/journal/day/journalDayListBridge.ts
 */
function journalDayResolveListBridge(): JournalDayListAppBridge | undefined {
    const w = window as any;
    return w.JournalDayMonthlyApp ?? w.JournalDayWeeklyApp ?? w.JournalDayDailyApp;
}

/**
 * 저널 일자 Ajax 응답에서 챕터 배열을 해석·필터한다.
 * 변경 전: journalEntryService 인스턴스 메서드 `resolveChapterList`.
 *
 * @param config DIARY/DREAM entry 메타(`chapterType` 분기)
 * @param day 서버 응답 일자 객체(`chapterList` 또는 `journalChapterList`)
 */
export function resolveJournalEntryChapterList(config: EntryConfig, day: Record<string, any> = {}): Record<string, any>[] {
    const chapterList: Record<string, any>[] = Array.isArray(day?.chapterList)
        ? day.chapterList
        : (Array.isArray(day?.journalChapterList) ? day.journalChapterList : []);
    if (config.chapterType == null) {
        return chapterList.filter((chapter: Record<string, any>): boolean => chapter?.chapterType !== "DREAM");
    }
    return chapterList.filter((chapter: Record<string, any>): boolean => chapter?.chapterType === config.chapterType);
}

/**
 * 등록/수정 모달 폼 브리지 — Vue JournalEntryRegModalApp teleport 진입.
 * 변경 전: journalEntryService 인스턴스 메서드 `initForm`.
 *
 * @param config entry 메타(`contentType` 필요)
 * @param obj 모달 초기 payload
 */
export function openJournalEntryRegForm(config: EntryConfig, obj: Record<string, any> = {}): void {
    /**
     * 변경(E-2): cF.handlebars.modal 제거 — 헤더/본문은 Vue JournalEntryRegModalApp 가 teleport 렌더한다.
     * 검증·tinymce·tagify 부착은 bridge.open → attachRegFormControls 경로로 동일 수행.
     */
    const bridge = (typeof window !== "undefined"
        ? (window as unknown as {
            JournalEntryRegVueApp?: {
                mounted?: boolean;
                pendingPayloads?: Record<string, Record<string, any> | null | undefined>;
                open?: (contentType: string, model: Record<string, any>) => void;
            };
        }).JournalEntryRegVueApp
        : undefined);

    if (bridge?.mounted === true && typeof bridge.open === "function") {
        bridge.open(config.contentType, obj);
        return;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingPayloads = bridge.pendingPayloads ?? {};
        bridge.pendingPayloads[config.contentType] = obj;
        console.log("[JournalEntry.initForm] JournalEntryRegVueApp pending payload:", config.contentType);
        return;
    }
    console.error("[JournalEntry.initForm] JournalEntryRegVueApp unavailable (모달 스텁 없음 또는 로드 순서 확인).");
}

/**
 * 태그·라이프사이클 초기화 및 viewType 설정.
 * 변경 전: journalEntryService 인스턴스 메서드 `init`.
 */
export async function runJournalEntryInit(
    module: EntryModule,
    config: EntryConfig,
    viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH",
): Promise<void> {
    if (module.initPromise) return module.initPromise;

    module.initPromise = (async () => {
        if (config.useTag) await dfNs.JournalEntryTag.get(config.contentType).init();
        dfNs.Lifecycle?.init?.();
        module.viewType = viewType;
        module.initialized = true;
        console.log(`'dF.${config.moduleName}' module initialized.`);
    })();

    return module.initPromise;
}

/**
 * 목록/달력/검색 화면 갱신 분기.
 * 변경 전: journalEntryService 인스턴스 메서드 `refresh`.
 */
export function runJournalEntryRefresh(module: EntryModule, config: EntryConfig): void {
    switch (module.viewType) {
        case "LIST":
            /* 변경(Phase 14): dF.JournalDayViewService.yyMnthListAjax() 제거 → Vue 브리지 직접 호출. */
            journalDayResolveListBridge()?.applySearchParamsAndReload?.({}, "MONTHLY");
            if (config.useTag) dfNs.JournalEntryTag.get(config.contentType).listAjax();
            break;
        case "CAL":
            if (typeof Page.refreshEventList === "function") {
                Page.refreshEventList();
            } else if (typeof dfNs.JournalDayMetaService?.listMetaHeaders === "function") {
                dfNs.JournalDayMetaService.listMetaHeaders();
            }
            if (config.useTag) dfNs.JournalEntryTag.get(config.contentType).listAjax();
            break;
        case "DAILY":
        case "WEEKLY":
            /* 변경(Phase 16): dF.JournalDayRuntimeService.refresh() 제거.
             * DAILY/WEEKLY 화면에서는 JournalDayCalVueApp이 마운트되지 않으므로 Vue 브리지 직접 호출. */
            journalDayResolveListBridge()?.refresh?.();
            ModalHistory.reset();
            break;
        case "SEARCH":
            dfNs.JournalEntrySearch?.get?.(config.contentType)?.search?.();
            break;
        default:
            /* 변경(Phase 16): 도달 불가 dead code — viewType은 LIST/CAL/DAILY/WEEKLY/SEARCH 중 하나. */
            console.warn("[journalEntryShellService] Unhandled viewType:", module.viewType);
            break;
    }
    cF.ui.unblockUI();
}

const journalEntryShellService = {
    resolveJournalEntryChapterList,
    openJournalEntryRegForm,
    runJournalEntryInit,
    runJournalEntryRefresh,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalEntryShellService = journalEntryShellService;
})();

export default journalEntryShellService;
