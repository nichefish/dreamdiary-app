/**
 * journalEntrySearchPopupService.ts
 * 저널 entry 검색 팝업 진입 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-3-γ):
 *   - journal_entry_module.ts 의 `searchPopup` 인스턴스 메서드,
 *     `resolveSearchUrl` 모듈 헬퍼, `bindSearchPopupEnterKeys` 루트 API 를 본 서비스로 추출한다.
 *   - 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - 외부 호출자 보존:
 *     · HBS onclick (`_journal_day_keyword_search.ftlh`) → `dF.JournalEntry.get(ct).searchPopup()`
 *     · Vue mount (JournalDayCalApp/JournalDayListAppMount/JournalDayWeeklyListApp) → `dF.JournalEntry.bindSearchPopupEnterKeys()`
 *   - 본 서비스는 글로벌(`dF.JournalEntrySearchPopupService`) 로도 노출되어 classic 모듈에서 호출 가능하다.
 *
 * @author nichefish
 */

type EntryMeta = Record<string, any>;

function getMeta(contentType: string): EntryMeta | undefined {
    return ((window as any).dF?.JournalEntry?.getMeta?.(contentType)) as EntryMeta | undefined;
}

function getSearchPopupContentTypes(): string[] {
    const root = (window as any).dF?.JournalEntry;
    if (typeof root?.getSearchPopupContentTypes === "function") {
        return root.getSearchPopupContentTypes() as string[];
    }
    /* fallback: 모든 contentType 중 searchInputSelector 가 있는 것만 */
    const types: string[] = (typeof root?.getContentTypes === "function" ? root.getContentTypes() : []) as string[];
    return types.filter((ct: string): boolean => {
        const meta = getMeta(ct);
        return cF.util.isNotEmpty(meta?.searchInputSelector);
    });
}

/**
 * 검색 URL 해석.
 * 변경 전: journal_entry_module.ts 모듈 스코프의 const resolveSearchUrl(config).
 * 변경 후(E-3-γ): contentType 기반으로 meta(config) 를 lookup 한다.
 */
export function resolveSearchUrl(contentType: string): string {
    const meta = getMeta(contentType);
    if (cF.util.isNotEmpty(meta?.searchUrl) && !String(meta.searchUrl).includes("undefined")) {
        return String(meta.searchUrl);
    }
    const typeSegment: string = String(meta?.entryType ?? "DIARY").toLowerCase();
    return cF.util.bindUrl(Url.JOURNAL_EMTRY_SEARCH, { type: typeSegment });
}

/**
 * 검색 팝업 오픈.
 * 변경 전: module.searchPopup — keyword 입력값을 URL 파라미터로 전달해 cF.ui.openPopup 호출.
 */
export function searchPopup(contentType: string): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntrySearchPopupService] meta missing:", contentType);
        return;
    }

    const baseSearchUrl: string = resolveSearchUrl(contentType);
    const prefix: string = meta.cssPrefix;
    const keyword: string = (document.querySelector(`#${prefix}SearchKeyword`) as HTMLInputElement)?.value;
    const url: string = `${baseSearchUrl}?searchKeywords=${keyword}`;
    const options: string = "width=1960,height=1440,top=0,left=270";
    const popup: Window = cF.ui.openPopup(url, `${prefix} search`, options);
    if (popup) popup.focus();
}

/**
 * 검색 input(엔터키) → 검색 팝업 트리거 바인딩.
 * 변경 전: dF.JournalEntry.bindSearchPopupEnterKeys() 루트 API.
 *          getSearchPopupContentTypes() 결과를 순회하며 각 input 에 enterKey 등록.
 */
export function bindSearchPopupEnterKeys(): void {
    getSearchPopupContentTypes().forEach(function(contentType: string): void {
        const meta: EntryMeta | undefined = getMeta(contentType);
        if (cF.util.isEmpty(meta?.searchInputSelector)) return;
        cF.util.enterKey(meta.searchInputSelector, function(): void {
            searchPopup(contentType);
        });
    });
}

/**
 * 글로벌 노출. classic `journal_entry_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalEntrySearchPopupService.<method>(...)` 로 호출 가능하도록 등록한다.
 */
const journalEntrySearchPopupService = {
    resolveSearchUrl,
    searchPopup,
    bindSearchPopupEnterKeys,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalEntrySearchPopupService = journalEntrySearchPopupService;
})();

export default journalEntrySearchPopupService;
