/**
 * journalDaySearchStateService.ts
 * 저널 일자 검색 파라미터 Vue SSOT.
 *
 * URL과 필터 DOM에서 초기 검색 파라미터를 구성하고 Vue reactive 상태로 소유한다.
 * Vue 화면은 이 상태를 기준으로 목록을 조회하고 필터 UI를 동기화한다.
 * 남은 레거시 호출자는 <code>getJournalDayListBridge()</code> / <code>journalDayResolveListBridge()</code>를 통해 이 상태에 접근한다.
 * aside의 레거시 DOM 조작은 Vue 마이그레이션이 끝날 때까지 필요한 부분만 남긴다.
 */

/** 챕터 카테고리 전체 선택 sentinel 값. */
export const CHAPTER_CTGR_ALL: string = "__ALL__";
/** 챕터 카테고리 미선택 sentinel 값. */
export const CHAPTER_CTGR_NONE: string = "__NONE__";

const searchParams: Record<string, any> = Vue.reactive({
    viewType: "list",
    yy: "",
    mnth: "",
    stdrdDt: "",
    showDiaries: true,
    showDreams: true,
    showTagCloud: true,
    showChapterCtgr: true,
    chapterCtgrCds: [] as string[],
    diaryKeyword: "",
    dreamKeyword: "",
    sort: "DESC",
    target: "",
});

let initialized: boolean = false;

/**
 * URL 검색 파라미터의 챕터 카테고리 값을 배열로 파싱한다.
 * @param {string} rawValue
 */
export function parseChapterCtgrCds(rawValue: string): string[] {
    if (!rawValue || rawValue.trim() === "") return [];
    return rawValue
        .split(",")
        .map((v: string): string => v.trim())
        .filter((v: string): boolean => v.length > 0);
}

/**
 * URL 검색 파라미터와 localStorage에서 searchParams를 초기화한다.
 * 여러 번 호출해도 같은 초기 상태를 유지한다.
 */
export function initFromUrl(): void {
    if (initialized) return;
    initialized = true;

    const yy: string = cF.util.getUrlParam("yy") ?? "9999";
    const mnth: string = cF.util.getUrlParam("mnth") ?? "99";
    const showDiaries: boolean = cF.util.getUrlParam("showDiaries") !== "false";
    const showDreams: boolean = cF.util.getUrlParam("showDreams") !== "false";
    const showTagCloud: boolean = cF.util.getUrlParam("showTagCloud") !== "false";
    const showChapterCtgr: boolean = cF.util.getUrlParam("showChapterCtgr") !== "false";
    const rawEntryCtgr: string = cF.util.getUrlParam("chapterCtgrCds") ?? cF.util.getUrlParam("chapterCtgrCd") ?? "";
    const chapterCtgrCds: string[] = parseChapterCtgrCds(rawEntryCtgr);
    const diaryKeyword: string = cF.util.getUrlParam("diaryKeyword") ?? "";
    const dreamKeyword: string = cF.util.getUrlParam("dreamKeyword") ?? "";
    const stdrdDt: string = cF.util.getUrlParam("stdrdDt") ?? "";
    const target: string = cF.util.getUrlParam("target") ?? "";
    const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("journal_day_sort") ?? "DESC";

    Object.assign(searchParams, {
        viewType: "list",
        yy,
        mnth,
        stdrdDt,
        showDiaries,
        showDreams,
        showTagCloud,
        showChapterCtgr,
        chapterCtgrCds,
        diaryKeyword,
        dreamKeyword,
        sort,
        target,
    });
}

function syncSearchFilterDomFromParams(): void {
    if (!initialized) initFromUrl();

    $("#toggleDiaries").prop("checked", searchParams.showDiaries === true);
    $("#toggleDreams").prop("checked", searchParams.showDreams === true);
    $("#toggleTagCloud").prop("checked", searchParams.showTagCloud === true);
    $("#toggleChapterCtgr").prop("checked", searchParams.showChapterCtgr === true);
    $("#diaryFilterKeyword").val(searchParams.diaryKeyword ?? "");
    $("#dreamFilterKeyword").val(searchParams.dreamKeyword ?? "");

    const chapterCtgrCds: string[] = Array.isArray(searchParams.chapterCtgrCds)
        ? searchParams.chapterCtgrCds
        : [];
    if (searchParams.showChapterCtgr === true && chapterCtgrCds.length === 0) {
        syncChapterCtgrSelectUi(getSelectableChapterCtgrCds());
    } else {
        syncChapterCtgrSelectUi(chapterCtgrCds);
    }
    syncChapterCtgrState(searchParams.showDiaries === true);
    syncKeywordFilterState();
}

/**
 * 검색 파라미터 스냅샷을 반환한다. 초기화 전이면 URL에서 먼저 초기화한다.
 */
export function getSearchParams(): Record<string, any> {
    if (!initialized) initFromUrl();
    return searchParams;
}

/**
 * 검색 파라미터를 patch로 갱신하고 같은 객체를 반환한다.
 * @param {Record<string, any>} patch
 */
export function patchSearchParams(patch: Record<string, any>): Record<string, any> {
    Object.keys(patch ?? {}).forEach((key: string): void => {
        searchParams[key] = patch[key];
    });
    return searchParams;
}

/**
 * #chapterCtgrFilter select에서 선택 가능한 챕터 카테고리 코드 목록을 반환한다.
 */
export function getSelectableChapterCtgrCds(): string[] {
    return ($("#chapterCtgrFilter").find("option").map(function(): string {
        return String($(this).val() ?? "").trim();
    }).get() as string[]).filter((v: string): boolean =>
        v.length > 0 && v !== CHAPTER_CTGR_ALL && v !== CHAPTER_CTGR_NONE
    );
}

/**
 * 선택값을 챕터 카테고리 코드 목록으로 정규화한다. sentinel 값과 전체/미선택 상태를 함께 처리한다.
 * @param {string[]} selectedCtgrCds
 * @param {string[]} emptyFallback
 */
export function normalizeChapterCtgrCds(selectedCtgrCds: string[] = [], emptyFallback: string[] = []): string[] {
    const selectableCtgrCds: string[] = getSelectableChapterCtgrCds();
    const uniqueSelected: string[] = Array.from(new Set(
        (selectedCtgrCds ?? [])
            .map((v: string): string => String(v ?? "").trim())
            .filter((v: string): boolean => v.length > 0)
    ));
    const realSelected: string[] = uniqueSelected.filter((v: string): boolean => selectableCtgrCds.includes(v));

    if (realSelected.length === selectableCtgrCds.length && selectableCtgrCds.length > 0) return selectableCtgrCds;
    if (uniqueSelected.includes(CHAPTER_CTGR_ALL) && realSelected.length === 0) return selectableCtgrCds;
    if (uniqueSelected.includes(CHAPTER_CTGR_NONE) && realSelected.length === 0) return [CHAPTER_CTGR_NONE];
    if (realSelected.length === 0) return [...emptyFallback];
    return realSelected;
}

/**
 * #chapterCtgrFilter select UI를 정규화된 선택값에 맞추고 적용된 코드 목록을 반환한다.
 * @param {string[]} selectedCtgrCds
 */
export function syncChapterCtgrSelectUi(selectedCtgrCds: string[] = []): string[] {
    const selectableCtgrCds: string[] = getSelectableChapterCtgrCds();
    const normalizedCtgrCds: string[] = normalizeChapterCtgrCds(selectedCtgrCds);
    const isAllSelected: boolean = selectableCtgrCds.length > 0
        && normalizedCtgrCds.length === selectableCtgrCds.length
        && selectableCtgrCds.every((c: string): boolean => normalizedCtgrCds.includes(c));
    const uiSelected: string[] = isAllSelected
        ? [CHAPTER_CTGR_ALL, ...selectableCtgrCds]
        : normalizedCtgrCds.filter((v: string): boolean => v !== CHAPTER_CTGR_NONE);
    $("#chapterCtgrFilter").val(uiSelected);
    return normalizedCtgrCds;
}

/**
 * 현재 선택값이 모든 선택 가능한 챕터 카테고리를 포함하는지 확인한다.
 * @param {string[]} [selectedCtgrCds]
 */
export function isAllChapterCtgrSelected(selectedCtgrCds?: string[]): boolean {
    const selectableCtgrCds: string[] = getSelectableChapterCtgrCds();
    const normalizedCtgrCds: string[] = normalizeChapterCtgrCds(
        selectedCtgrCds ?? (searchParams.chapterCtgrCds as string[]) ?? []
    );
    return selectableCtgrCds.length > 0
        && normalizedCtgrCds.length === selectableCtgrCds.length
        && selectableCtgrCds.every((c: string): boolean => normalizedCtgrCds.includes(c));
}

/**
 * showDiaries 값에 따라 챕터 카테고리 필터 DOM 상태를 동기화한다.
 * @param {boolean} [showDiaries]
 */
export function syncChapterCtgrState(showDiaries?: boolean): void {
    const showDiaryFilter: boolean = (showDiaries ?? searchParams.showDiaries) === true;
    const toggleElmt = $("#toggleChapterCtgr");
    const selectElmt = $("#chapterCtgrFilter");
    const sectionElmt = $("#chapterCtgrFilterSection");

    if (!showDiaryFilter) {
        if (sectionElmt.length) sectionElmt.addClass("d-none");
        toggleElmt.prop("checked", false);
        toggleElmt.prop("disabled", true);
        selectElmt.prop("disabled", true);
        selectElmt.val([]);
        patchSearchParams({ chapterCtgrCds: [] });
        return;
    }

    if (sectionElmt.length) sectionElmt.removeClass("d-none");
    toggleElmt.prop("disabled", false);
    const enabled: boolean = toggleElmt.is(":checked");
    selectElmt.prop("disabled", !enabled);

    if (!enabled) {
        selectElmt.val([]);
        patchSearchParams({ chapterCtgrCds: [] });
        return;
    }

    let selectedCtgrCds: string[] = (searchParams.chapterCtgrCds as string[]) ?? [];
    if (selectedCtgrCds.length === 0) selectedCtgrCds = getSelectableChapterCtgrCds();
    const normalizedCtgrCds: string[] = syncChapterCtgrSelectUi(selectedCtgrCds);
    patchSearchParams({ chapterCtgrCds: normalizedCtgrCds });
}

/**
 * showDiaries/showDreams 값에 따라 키워드 필터 DOM 상태를 동기화한다.
 */
export function syncKeywordFilterState(): void {
    const showDiaries: boolean = searchParams.showDiaries === true;
    const showDreams: boolean = searchParams.showDreams === true;
    const diaryElmt = $("#diaryFilterKeyword");
    const dreamElmt = $("#dreamFilterKeyword");
    diaryElmt.prop("disabled", !showDiaries);
    dreamElmt.prop("disabled", !showDreams);
    if (!showDiaries) {
        diaryElmt.val("");
        patchSearchParams({ diaryKeyword: "" });
    }
    if (!showDreams) {
        dreamElmt.val("");
        patchSearchParams({ dreamKeyword: "" });
    }
}

const journalDaySearchStateService = {
    CHAPTER_CTGR_ALL,
    CHAPTER_CTGR_NONE,
    initFromUrl,
    syncSearchFilterDomFromParams,
    getSearchParams,
    patchSearchParams,
    parseChapterCtgrCds,
    getSelectableChapterCtgrCds,
    normalizeChapterCtgrCds,
    syncChapterCtgrSelectUi,
    isAllChapterCtgrSelected,
    syncChapterCtgrState,
    syncKeywordFilterState,
};

export default journalDaySearchStateService;
