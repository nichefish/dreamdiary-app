/**
 * journalDayListAppMount.ts
 * 저널 일자 목록 Vue 마운트 공유 구현(SSOT).
 *
 * 변경 전: 단일 파일 JournalDayListApp 이 DOM 의 data-view-type 만으로 월/주/일을 분기.
 * 변경 후(기존): 진입 모듈이 <code>mountJournalDayListApp(타입)</code> 한 함수로 호출·단일 전역 <code>JournalDayVueApp</code>.
 * 변경 후(현재): 진입 모듈이 <code>mountJournalDayMonthlyListApp</code> 등 세 API로 호출하고,
 * 전역 브리지는 <code>JournalDayMonthlyApp</code> / <code>JournalDayWeeklyApp</code> / <code>JournalDayDailyApp</code> 로 분리한다.
 * 레거시 호출자는 <code>getJournalDayListBridge()</code> 로 현재 페이지의 활성 브리지를 조회한다.
 */

import journalDayDataService from "./services/journalDayDataService.js";
import journalDayCrudService from "./services/journalDayCrudService.js";
import journalDayMetaService from "./services/journalDayMetaService.js";
import journalDayTagService from "./services/journalDayTagService.js";
import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import journalDayWeeklyService, { JournalDayWeeklyState } from "./services/journalDayWeeklyService.js";
import journalDaySearchStateService, { CHAPTER_CTGR_ALL, CHAPTER_CTGR_NONE } from "./services/journalDaySearchStateService.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalDayList from "./components/JournalDayList.js";
import journalTodoCrudService from "../todo/services/journalTodoCrudService.js";

type JournalDayVueStub = {
    pendingLoad?: {
        type: "monthly" | "weekly" | "daily" | "refresh" | "reload";
        scope?: JournalDayReloadScope;
        patch?: Record<string, any>;
        stdrdDt?: string;
        targetDt?: string;
    } | null;
};

type JournalDayReloadScope = "CURRENT" | "MONTHLY" | "WEEKLY" | "DAILY";
export type JournalDayRuntimeViewType = "MONTHLY" | "WEEKLY" | "DAILY";

/** 월·주·일 목록 페이지 각각에 심기는 전역 브리지 키. */
export type JournalDayListBridgeGlobalKey =
    | "JournalDayMonthlyApp"
    | "JournalDayWeeklyApp"
    | "JournalDayDailyApp";

let runtimeViewType: JournalDayRuntimeViewType = "MONTHLY";

/**
 * FTL 선행 스텁 등에서 들어온 pendingLoad 를 읽는다.
 * @param {JournalDayListBridgeGlobalKey} bridgeKey
 */
function readJournalDayListBridgeStub(bridgeKey: JournalDayListBridgeGlobalKey): JournalDayVueStub | undefined {
    const w = window as unknown as Record<string, unknown>;
    return w[bridgeKey] as JournalDayVueStub | undefined;
}

/**
 * 마운트 완료 후 해당 페이지 전용 브리지 객체를 전역에 심는다.
 * @param {JournalDayListBridgeGlobalKey} bridgeKey
 * @param {JournalDayListAppBridge} api
 */
function assignJournalDayListBridge(bridgeKey: JournalDayListBridgeGlobalKey, api: JournalDayListAppBridge): void {
    const w = window as unknown as Record<string, unknown>;
    w[bridgeKey] = api;
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveViewType(root: HTMLElement): string {
    return root.dataset.viewType ?? "monthly";
}

function normalizeViewType(viewType: string): JournalDayRuntimeViewType {
    const normalizedViewType: string = String(viewType ?? "").trim().toUpperCase();
    if (normalizedViewType === "WEEKLY") return "WEEKLY";
    if (normalizedViewType === "DAILY") return "DAILY";
    return "MONTHLY";
}

const state = Vue.reactive({
    model: null as Record<string, any> | null,
    weekly: null as JournalDayWeeklyState | null,
});
const i18n = createScopedI18n();

function t(key: string): string {
    return i18n.t(key);
}

function resolveJournalDayLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const locale = w?.Model?.locale;
    if (locale) return locale;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

/**
 * 검색 파라미터를 Vue reactive SSOT에서 읽는다.
 */
function getSharedSearchParams(): Record<string, any> {
    return journalDaySearchStateService.getSearchParams();
}

function getCurrentSort(): string {
    const currentSort: string = String(getSharedSearchParams().sort ?? "").trim();
    if (cF.util.isNotEmpty(currentSort)) return currentSort;
    const storedSort: string = String(localStorage.getItem("journal_day_sort") ?? "").trim();
    if (cF.util.isNotEmpty(storedSort)) return storedSort;
    return "DESC";
}

function getCurrentPeriod(): { yy: string; mnth: string } {
    const searchParams: Record<string, any> = getSharedSearchParams();
    const yy: string = String(searchParams.yy ?? "").trim();
    const mnth: string = String(searchParams.mnth ?? "").trim();
    return { yy, mnth };
}

function getCurrentAnchorDate(): string {
    const searchParams: Record<string, any> = getSharedSearchParams();
    if (cF.util.isNotEmpty(searchParams?.stdrdDt)) return searchParams.stdrdDt;

    const yy: string = String(searchParams?.yy ?? cF.date.getCurrYyStr());
    const mnth: string = String(searchParams?.mnth ?? cF.date.getCurrMnthStr());
    const currYy: string = cF.date.getCurrYyStr();
    const currMnth: string = cF.date.getCurrMnthStr();
    if (yy === currYy && String(parseInt(mnth, 10)) === String(parseInt(currMnth, 10))) {
        return cF.date.getCurrDateStr(cF.date.ptnDate);
    }

    const parsedMonthNo: number = parseInt(mnth, 10);
    const monthNo: number = (parsedMonthNo >= 1 && parsedMonthNo <= 12) ? parsedMonthNo : 1;
    return `${yy}-${String(monthNo).padStart(2, "0")}-01`;
}

function buildAnchorDateForMonth(yy: string, mnth: string, fallbackDay?: number): string {
    const baseAnchor: string = getCurrentAnchorDate();
    const baseDay: number = parseInt(baseAnchor?.substring(8, 10) ?? String(fallbackDay ?? 1), 10);
    const parsedMonthNo: number = parseInt(mnth, 10);
    const monthNo: number = (parsedMonthNo >= 1 && parsedMonthNo <= 12) ? parsedMonthNo : 1;
    const lastDay: number = new Date(Number(yy), monthNo, 0).getDate();
    const safeDay: string = String(Math.min(baseDay, lastDay)).padStart(2, "0");
    return `${yy}-${String(monthNo).padStart(2, "0")}-${safeDay}`;
}

/**
 * 사이드바 주간 네비(범위 라벨·요일 칸)를 기준일/주간 목록에 맞춘다.
 *
 * 변경 전: <code>dF.JournalDayAside.syncWeekNavigator</code>로 <code>journal_day_aside_module</code>을 경유했다(한 단계 래퍼만 호출).
 * 변경 후: <code>JournalDayAsideWeekNavigatorVueApp.syncWeekNavigator</code> 브리지로 직접 동기화한다.
 * @param {string} [stdrdDt]
 * @param {Record<string, any>[]|undefined} [weeklyList]
 */
function syncAsideWeekNavigator(stdrdDt?: string, weeklyList?: Record<string, any>[]): void {
    const bridge = window.JournalDayAsideWeekNavigatorVueApp;
    if (bridge?.mounted === true && typeof bridge.syncWeekNavigator === "function") {
        bridge.syncWeekNavigator(stdrdDt, weeklyList);
        return;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingSyncRequest = { stdrdDt, weeklyList };
        return;
    }
    console.error("[journalDayListAppMount] JournalDayAsideWeekNavigatorVueApp.syncWeekNavigator unavailable.");
}

function syncSortIcon(sort: string): void {
    if (sort === "DESC") {
        $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
        return;
    }
    $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
}

function initAsideYyMnth(): void {
    const currentPeriod = getCurrentPeriod();
    const sort: string = getCurrentSort();

    const yearElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
    if (yearElement) yearElement.value = currentPeriod.yy === "" ? cF.date.getCurrYyStr() : currentPeriod.yy;

    const monthElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
    if (monthElement) monthElement.value = currentPeriod.mnth;

    const sortElement = document.querySelector("#journal_aside #sort") as HTMLInputElement | null;
    if (sort !== "" && sortElement != null) sortElement.value = sort;

    /* 변경 후(Phase 10): dF.JournalDayPageStateService 제거 → Vue 주간 네비 브리지로 직접 동기화. */
    syncAsideWeekNavigator();
}

function pinpointAside(): void {
    const { yy, mnth } = getCurrentPeriod();
    localStorage.setItem("journal_pinned_yy", yy);
    localStorage.setItem("journal_pinned_mnth", mnth);
    $("#journal_aside_pinText #pinnedYy").text(yy);
    $("#journal_aside_pinText #pinnedMnth").text(mnth);
}

function turnbackAside(): void {
    const pinnedYy: string = localStorage.getItem("journal_pinned_yy") ?? "";
    const pinnedMnth: string = localStorage.getItem("journal_pinned_mnth") ?? "";
    if (cF.util.isEmpty(pinnedYy) || cF.util.isEmpty(pinnedMnth)) return;

    const nextStdrdDt: string = buildAnchorDateForMonth(pinnedYy, pinnedMnth, 1);
    const nextSearchParams: Record<string, any> = journalDaySearchStateService.patchSearchParams({
        yy: pinnedYy,
        mnth: pinnedMnth,
        stdrdDt: nextStdrdDt,
    });
    applySearchParamsAndReload({
        yy: pinnedYy,
        mnth: pinnedMnth,
        stdrdDt: nextStdrdDt,
        sort: nextSearchParams.sort,
    }, "CURRENT");
}

function sortAside(toBe?: string): void {
    const asIs: string = cF.util.getInputValue("#journal_aside #sort");
    const nextSort: string = toBe ?? ((asIs !== "ASC") ? "ASC" : "DESC");

    localStorage.setItem("journal_day_sort", nextSort);
    $("#journal_aside #sort").val(nextSort);
    syncSortIcon(nextSort);
    applySearchParamsAndReload({ sort: nextSort }, "CURRENT");
}

function syncAsideYyMnthInputs(yy: string, mnth: string): void {
    const yearElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
    const monthElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
    if (yearElement) yearElement.value = yy;
    if (monthElement) monthElement.value = mnth;
}

function clearAsideSearchResetButtons(): void {
    $("#journal_aside #journal_diary_reset_btn").remove();
    $("#journal_aside #journal_dream_reset_btn").remove();
}

function clearDreamKeywordMode(): void {
    dF.JournalEntry.get("JOURNAL_DREAM").inKeywordSearchMode = false;
}

/**
 * WEEKLY → MONTHLY 전환 시 검색 파라미터를 포함한 월간 화면 URL을 빌드한다.
 * 변경 후(Phase 10): dF.JournalDayViewService.buildViewUrl() 제거 후 인라인.
 * 변경 후(P3): journalDayUiBridgeService.buildListStyleJournalDayViewUrl 로 단일 경로 수렴.
 * 검색 파라미터는 journalDaySearchStateService(Vue SSOT)에서 직접 읽는다.
 */
function buildMonthlyViewUrl(): string {
    return journalDayUiBridgeService.buildListStyleJournalDayViewUrl(Url.JOURNAL_DAY_MONTHLY);
}

function runYyMnth(yy: string, mnth: string | number, sort?: string): void {
    const mnthStr: string = String(mnth);
    syncAsideYyMnthInputs(yy, mnthStr);
    clearAsideSearchResetButtons();

    const patch: Record<string, any> = {
        yy,
        mnth: mnthStr,
        stdrdDt: buildAnchorDateForMonth(yy, mnthStr),
    };
    if (sort) patch.sort = sort;

    const nextSearchParams: Record<string, any> = journalDaySearchStateService.patchSearchParams(patch);
    const nextStdrdDt: string = nextSearchParams.stdrdDt;
    syncAsideWeekNavigator(nextStdrdDt);

    if (runtimeViewType === "WEEKLY") {
        clearDreamKeywordMode();
        /* 변경 후(Phase 10): dF.JournalDayViewService.buildViewUrl() 제거 후 인라인 빌더 사용. */
        cF.ui.blockUIReplace(buildMonthlyViewUrl());
        Layout.toPageTop();
        return;
    }

    const vuePatch: Record<string, any> = { yy, mnth: mnthStr, stdrdDt: nextStdrdDt };
    if (sort) vuePatch.sort = sort;
    applySearchParamsAndReload(vuePatch, "MONTHLY");
    clearDreamKeywordMode();
    Layout.toPageTop();
}

function runNavigateToWeek(stdrdDt: string): void {
    if (cF.util.isEmpty(stdrdDt)) return;

    if (runtimeViewType === "WEEKLY") {
        runSetAnchorDateForCurrentView(stdrdDt, false);
        return;
    }

    const yy: string = stdrdDt.substring(0, 4);
    const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
    syncAsideYyMnthInputs(yy, mnth);
    journalDaySearchStateService.patchSearchParams({ yy, mnth, stdrdDt, target: "" });
    syncAsideWeekNavigator(stdrdDt);
    applySearchParamsAndReload({ yy, mnth, stdrdDt, target: "" }, "WEEKLY");
}

function runSetAnchorDateForCurrentView(stdrdDt: string, useTarget?: boolean): void {
    if (cF.util.isEmpty(stdrdDt)) return;

    const yy: string = stdrdDt.substring(0, 4);
    const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
    syncAsideYyMnthInputs(yy, mnth);
    journalDaySearchStateService.patchSearchParams({ yy, mnth, stdrdDt, target: useTarget ? stdrdDt : "" });
    syncAsideWeekNavigator(stdrdDt);

    if (runtimeViewType === "WEEKLY") {
        const patch: Record<string, any> = { yy, mnth, stdrdDt };
        patch.target = useTarget ? stdrdDt : "";
        applySearchParamsAndReload(patch, "WEEKLY");
        return;
    }

    applySearchParamsAndReload({
        yy,
        mnth,
        stdrdDt,
        target: "",
        sort: getCurrentSort(),
    }, "MONTHLY");
}

function render(model: Record<string, any>): void {
    state.model = model;
    journalDayUiBridgeService.initRenderedDom("journal_day_list_div");
}

function showAjaxFailure(res: AjaxResponse): void {
    if (cF.util.isNotEmpty(res?.message)) Swal.fire({ text: res.message });
}

function loadMonthly(): void {
    const params: Record<string, any> = getSharedSearchParams();
    journalDayUiBridgeService.syncMonthlyUrl(params);

    journalDayDataService.listMonthly(params)
        .then(function(rsltList: Record<string, any>[]): void {
            const filteredList = [...rsltList];
            const sortStr = String($("#journal_aside #sort").val() ?? params.sort ?? "DESC");
            journalDayUiBridgeService.syncMonthlySortIcon(sortStr);
            if (sortStr !== "ASC" && cF.util.isNotEmpty(filteredList)) filteredList.reverse();

            cF.ui.closeModal();
            render({
                list: filteredList,
                showDiaries: params.showDiaries,
                showDreams: params.showDreams,
            });
            journalDayUiBridgeService.syncTagCloud(params.showDiaries, params.showDreams, params.showTagCloud);
        })
        .catch(showAjaxFailure);
}

function loadWeekly(stdrdDt: string, targetDt?: string): void {
    const resolvedTargetDt = cF.util.isNotEmpty(targetDt) ? targetDt : undefined;
    const weeklyState = journalDayWeeklyService.resolveWeeklyState(stdrdDt, resolvedTargetDt);
    state.weekly = weeklyState;
    journalDayWeeklyService.syncAsidePeriodState(weeklyState);
    window.history.replaceState(null, "", journalDayUiBridgeService.buildWeeklyViewUrl(stdrdDt, resolvedTargetDt));

    const searchParams: Record<string, any> = getSharedSearchParams();
    const showDiaries = searchParams.showDiaries !== false;
    const showDreams = searchParams.showDreams !== false;

    journalDayDataService.listWeekly({
        weekStartDt: weeklyState.weekStartDt,
        stdrdDt: weeklyState.stdrdDt,
        showDiaries,
        showDreams,
        diaryKeyword: searchParams.diaryKeyword ?? "",
        dreamKeyword: searchParams.dreamKeyword ?? "",
        chapterCtgrCds: searchParams.chapterCtgrCds ?? [],
    })
        .then(function(rsltList: Record<string, any>[]): void {
            const weeklyList = journalDayWeeklyService.normalizeWeekDays(rsltList, searchParams.sort ?? "DESC");
            cF.ui.closeModal();
            render({
                list: weeklyList,
                showDiaries,
                showDreams,
            });
            syncAsideWeekNavigator(stdrdDt, weeklyList);
            journalDayUiBridgeService.syncTagCloud(showDiaries, showDreams, searchParams.showTagCloud !== false);
            journalDayWeeklyService.scrollToTarget(weeklyState.targetDt);
        })
        .catch(showAjaxFailure);
}

function loadDaily(stdrdDt: string): void {
    const resolvedStdrdDt: string = String(stdrdDt ?? "").trim() || cF.date.getCurrDateStr(cF.date.ptnDate);
    journalDaySearchStateService.patchSearchParams({ stdrdDt: resolvedStdrdDt });

    const targetUrl: string = cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt: resolvedStdrdDt });
    window.history.replaceState(null, "", targetUrl);
    const stdrdDtInput = document.querySelector("#stdrdDt") as HTMLInputElement | null;
    if (stdrdDtInput) stdrdDtInput.value = resolvedStdrdDt;

    journalDayDataService.listDaily(resolvedStdrdDt)
        .then(function(rsltList: Record<string, any>[]): void {
            cF.ui.closeModal();
            render({
                list: rsltList,
                showDiaries: true,
                showDreams: true,
            });
        })
        .catch(showAjaxFailure);
}

function refresh(): void {
    const currentStdrdDt: string = String(getSharedSearchParams().stdrdDt ?? "").trim();
    if (runtimeViewType === "DAILY") {
        loadDaily(currentStdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate));
        return;
    }
    if (runtimeViewType === "WEEKLY") {
        loadWeekly(currentStdrdDt || state.weekly?.stdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate));
        return;
    }
    loadMonthly();
}

/**
 * 검색 파라미터를 Vue reactive SSOT에 patch한다.
 */
function updateSearchParams(patch: Record<string, any>): void {
    journalDaySearchStateService.patchSearchParams(patch);
}

function reloadByScope(scope: JournalDayReloadScope = "CURRENT"): void {
    const searchParams = getSharedSearchParams();
    const nextStdrdDt = String(searchParams.stdrdDt ?? "").trim();
    if (scope === "MONTHLY") {
        loadMonthly();
        return;
    }
    if (scope === "WEEKLY") {
        loadWeekly(
            nextStdrdDt || state.weekly?.stdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate),
            String(searchParams.target ?? "")
        );
        return;
    }
    if (scope === "DAILY") {
        loadDaily(nextStdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate));
        return;
    }
    if (runtimeViewType === "DAILY") {
        loadDaily(nextStdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate));
        return;
    }
    if (runtimeViewType === "WEEKLY") {
        loadWeekly(
            nextStdrdDt || state.weekly?.stdrdDt || cF.date.getCurrDateStr(cF.date.ptnDate),
            String(searchParams.target ?? "")
        );
        return;
    }
    loadMonthly();
}

function applySearchParamsAndReload(patch: Record<string, any>, scope: JournalDayReloadScope = "CURRENT"): void {
    updateSearchParams(patch);
    reloadByScope(scope);
}

/**
 * 월간 리스트 전용 선행 부트(Tag/Entry/Todo/Comment/State 등).
 *
 * 변경 전: 이 함수 안에서 <code>dF.JournalDayAside.init()</code>까지 호출했다.
 * 변경 후: Aside 초기화는 목록 Vue 브리지 확정 뒤 <code>initJournalDayAsideShell</code>에서만 호출한다(사이드 이벤트가 Vue 메서드를 참조하므로).
 */
function initializeMonthlyPage(): void {
    /* 변경(3순위, 2026-05-09): bootstrapDfJournalDayShell 은 runWhenDomReady 동기 구간에서 이미 호출됨. */
    void dF.JournalEntry.initAll("LIST");
    window.addEventListener("comment:modal-refresh", function(): void {
        applySearchParamsAndReload({}, "MONTHLY");
    });
    dF.State.init();

    dF.JournalEntry.bindSearchPopupEnterKeys();
}

/**
 * 일기/꿈/태그 클라우드 표시 여부를 토글한다.
 * Vue 검색 상태를 갱신하고 현재 화면을 다시 조회한다.
 */
function toggleParam(): void {
    const showDiaries: boolean = $("#toggleDiaries").is(":checked");
    const showDreams: boolean = $("#toggleDreams").is(":checked");
    const showTagCloud: boolean = $("#toggleTagCloud").is(":checked");
    updateSearchParams({ showDiaries, showDreams, showTagCloud });
    journalDaySearchStateService.syncChapterCtgrState(showDiaries);
    journalDaySearchStateService.syncKeywordFilterState();
    const url: URL = new URL(window.location.href);
    url.searchParams.set("showDiaries", String(showDiaries));
    url.searchParams.set("showDreams", String(showDreams));
    url.searchParams.set("showTagCloud", String(showTagCloud));
    window.history.replaceState(null, "", url.toString());
    reloadByScope("CURRENT");
}

/**
 * 일기/꿈 키워드 필터를 적용한다.
 * Vue 검색 상태를 갱신하고 현재 화면을 다시 조회한다.
 */
function applyKeywordFilters(): void {
    const params: Record<string, any> = getSharedSearchParams();
    const showDiaries: boolean = params.showDiaries === true;
    const showDreams: boolean = params.showDreams === true;
    const diaryKeyword: string = showDiaries ? String($("#diaryFilterKeyword").val() ?? "").trim() : "";
    const dreamKeyword: string = showDreams ? String($("#dreamFilterKeyword").val() ?? "").trim() : "";
    applySearchParamsAndReload({ diaryKeyword, dreamKeyword }, "CURRENT");
}

/**
 * 챕터 카테고리 필터 사용 여부를 토글한다.
 * Vue 검색 상태를 갱신하고 현재 화면을 다시 조회한다.
 */
function toggleChapterCtgr(): void {
    const params: Record<string, any> = getSharedSearchParams();
    if (!params.showDiaries) {
        journalDaySearchStateService.syncChapterCtgrState(false);
        return;
    }
    const enabled: boolean = $("#toggleChapterCtgr").is(":checked");
    const selectElmt = $("#chapterCtgrFilter");
    if (!enabled) {
        selectElmt.prop("disabled", true);
        selectElmt.val([]);
        const url: URL = new URL(window.location.href);
        url.searchParams.set("showChapterCtgr", "false");
        url.searchParams.delete("chapterCtgrCds");
        window.history.replaceState(null, "", url.toString());
        applySearchParamsAndReload({ chapterCtgrCds: [], showChapterCtgr: false }, "CURRENT");
        return;
    }
    selectElmt.prop("disabled", false);
    let selectedCtgrCds: string[] = params.chapterCtgrCds ?? [];
    if (selectedCtgrCds.length === 0) selectedCtgrCds = journalDaySearchStateService.getSelectableChapterCtgrCds();
    const normalized: string[] = journalDaySearchStateService.syncChapterCtgrSelectUi(selectedCtgrCds);
    const urlOn: URL = new URL(window.location.href);
    urlOn.searchParams.delete("showChapterCtgr");
    window.history.replaceState(null, "", urlOn.toString());
    applySearchParamsAndReload({ chapterCtgrCds: normalized, showChapterCtgr: true }, "CURRENT");
}

/**
 * 챕터 카테고리 필터 변경값을 적용한다.
 * Vue 검색 상태를 갱신하고 현재 화면을 다시 조회한다.
 */
function changeChapterCtgr(): void {
    const params: Record<string, any> = getSharedSearchParams();
    if (!params.showDiaries) {
        journalDaySearchStateService.syncChapterCtgrState(false);
        return;
    }
    const enabled: boolean = $("#toggleChapterCtgr").is(":checked");
    const selectElmt = $("#chapterCtgrFilter");
    if (!enabled) {
        selectElmt.val([]);
        applySearchParamsAndReload({ chapterCtgrCds: [] }, "CURRENT");
        return;
    }
    const rawSelected: string[] = (selectElmt.val() as string[] | null) ?? [];
    const selectedCtgrCds: string[] = journalDaySearchStateService.normalizeChapterCtgrCds(rawSelected, [CHAPTER_CTGR_NONE]);
    journalDaySearchStateService.syncChapterCtgrSelectUi(selectedCtgrCds);
    applySearchParamsAndReload({ chapterCtgrCds: selectedCtgrCds }, "CURRENT");
}

/**
 * 챕터 카테고리 "전체" 옵션의 mousedown을 처리한다.
 * 기본 선택 동작 전에 sentinel 선택 상태를 직접 동기화한다.
 */
function handleChapterCtgrMouseDown(event: MouseEvent): boolean {
    const target = event.target as HTMLElement | null;
    if (!(target instanceof HTMLOptionElement) || target.value !== CHAPTER_CTGR_ALL) return true;
    event.preventDefault();
    const nextCtgrCds: string[] = journalDaySearchStateService.isAllChapterCtgrSelected()
        ? [CHAPTER_CTGR_NONE]
        : journalDaySearchStateService.getSelectableChapterCtgrCds();
    journalDaySearchStateService.syncChapterCtgrSelectUi(nextCtgrCds);
    applySearchParamsAndReload({ chapterCtgrCds: nextCtgrCds }, "CURRENT");
    return false;
}

const JournalDayRootApp = {
    name: "JournalDayRootApp",
    components: {
        JournalDayList,
    },
    data(): { state: typeof state; viewType: string } {
        /* 변경: runtimeViewType 은 mountJournalDayListAppForView 진입 시 설정된다. */
        return {
            state,
            viewType: runtimeViewType === "MONTHLY" ? "monthly" : runtimeViewType === "WEEKLY" ? "weekly" : "daily",
        };
    },
    template: `
    <teleport to="#journal_day_list_div">
        <JournalDayList v-if="state.model" :model="state.model" />
    </teleport>
    `,
};

/**
 * 저널 일자 목록 Vue 앱을 마운트한다(내부 구현). 진입은 월·주·일 전용 export 만 사용한다.
 * @param {JournalDayRuntimeViewType} entryViewType 진입 모듈이 단일 진실로 넘기는 뷰 타입.
 * @param {JournalDayListBridgeGlobalKey} bridgeKey 이 페이지에 심을 전역 브리지 키.
 */
async function mountJournalDayListAppForView(
    entryViewType: JournalDayRuntimeViewType,
    bridgeKey: JournalDayListBridgeGlobalKey
): Promise<void> {
    const root = document.getElementById("journal_day_app") as HTMLElement | null;
    if (!root) {
        console.error("[journalDayListAppMount] Vue mount root #journal_day_app not found.");
        return;
    }

    const domResolved: JournalDayRuntimeViewType = normalizeViewType(resolveViewType(root));
    runtimeViewType = normalizeViewType(entryViewType);
    if (domResolved !== runtimeViewType) {
        console.warn(
            "[journalDayListAppMount] #journal_day_app data-view-type 과 진입 모듈 불일치:",
            domResolved,
            runtimeViewType
        );
    }
    const viewType: string = runtimeViewType === "MONTHLY" ? "monthly" : runtimeViewType === "WEEKLY" ? "weekly" : "daily";
    const queuedBridge: JournalDayVueStub | undefined = readJournalDayListBridgeStub(bridgeKey);
    journalDaySearchStateService.initFromUrl();
    journalDaySearchStateService.syncSearchFilterDomFromParams();
    /* 변경(3순위, 2026-05-09): Tag/Meta/레거시 위임을 ListApp 부트 동기 구간(첫 await 전)에서 한 번만 호출한다.
     * 변경 전: 월간은 initializeMonthlyPage, 주간·일간은 각 Page.init에서 bootstrapDfJournalDayShell을 따로 호출했다.
     * 변경 후: monthly/weekly/daily 공통으로 이 경로만 사용해 DOMContentLoaded 이전 순서 불일치를 없앤다. */
    dF.JournalDayRuntimeService.bootstrapDfJournalDayShell();
    void journalDayTagService.getCtgrMap();
    void journalDayMetaService.getCtgrMap();
    await i18n.load(resolveJournalDayLocale());
    const app = Vue.createApp(JournalDayRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_day_app");
    const listBridge: JournalDayListAppBridge = {
        viewType,
        dataService: journalDayDataService,
        mounted: true,
        refresh,
        applySearchParamsAndReload,
        /**
         * 남은 브리지 호출자를 위해 Vue 검색 파라미터 조회를 노출한다.
         */
        getSearchParams: function(): Record<string, any> {
            return journalDaySearchStateService.getSearchParams();
        },
        /**
         * 남은 브리지 호출자를 위해 Vue 검색 파라미터 patch를 노출한다.
         */
        patchSearchParams: function(patch: Record<string, any>): Record<string, any> {
            return journalDaySearchStateService.patchSearchParams(patch);
        },
        getFilterSnapshot: function(): Record<string, any> {
            return journalDaySearchStateService.getSearchParams();
        },
        getCurrentSort,
        getCurrentPeriod,
        getCurrentAnchorDate,
        buildAnchorDateForMonth,
        initAsideYyMnth,
        pinpointAside,
        turnbackAside,
        sortAside,
        runYyMnth,
        runNavigateToWeek,
        runSetAnchorDateForCurrentView,
        syncAsideWeekNavigator,
        regModal: journalDayCrudService.openRegModal,
        mdfModal: journalDayCrudService.openMdfModal,
        dtlModal: journalDayCrudService.openDtlModal,
        delAjax: journalDayCrudService.delAjax,
        /**
         * Vue 소유 필터 액션.
         */
        toggleParam,
        /**
         * Vue 소유 키워드 필터 액션.
         */
        applyKeywordFilters,
        /**
         * Vue 소유 챕터 카테고리 토글 액션.
         */
        toggleChapterCtgr,
        /**
         * Vue 소유 챕터 카테고리 변경 액션.
         */
        changeChapterCtgr,
        /**
         * Vue 소유 챕터 카테고리 전체 선택 액션.
         */
        handleChapterCtgrMouseDown,
        /**
         * 레거시 data-journal-day-action 위임에서 호출한다.
         */
        moveToWeeklyView: journalDayUiBridgeService.moveToWeeklyView,
        openDetached: journalDayUiBridgeService.openDetached,
        /**
         * 변경(Phase 15): aside_module.navigateToWeekDay() 브리지.
         * dF.JournalDayViewService.buildWeeklyViewUrl() 제거 후 Vue 소유 빌더로 이전.
         */
        navigateToWeekDay: (stdrdDt: string): void => {
            cF.ui.blockUIReplace(journalDayUiBridgeService.buildWeeklyViewUrl(stdrdDt, stdrdDt));
        },
        /**
         * 사이드바 <code>dF.JournalDayAside</code> DOM 이벤트·주간 네비 초기화.
         *
         * 변경 전: <code>initializeMonthlyPage</code> 안에서 <code>dF.JournalDayAside.init()</code> 호출.
         * 변경 후: 해당 페이지 전역 브리지(<code>JournalDayMonthlyApp</code> 등) 확정 뒤 호출해, Aside.init 내부의 Vue 참조가 유효하도록 한다.
         */
        initJournalDayAsideShell: function(): void {
            dF.JournalDayRuntimeService.initJournalDayAsideShell();
        },
    };
    assignJournalDayListBridge(bridgeKey, listBridge);
    if (runtimeViewType === "MONTHLY") initializeMonthlyPage();
    listBridge.initJournalDayAsideShell?.();
    /* Aside 스텁·목록 초기화: 월/주/일 목록 페이지 공통(SSOT 저널 day aside 포함). 변경 전에는 월간 initializeMonthlyPage 안에서만 init 호출되어 주간·일간에서는 TODO 카드 목록 미로딩이었음. */
    /* 변경(T-2-β): dF.JournalTodo.init() → journalTodoCrudService.yyMnthListAjax() 단일 진입.
     * 기존 init 의 initialized 플래그는 페이지 진입 시점 1회 호출 보장으로 자연 소멸. */
    journalTodoCrudService.yyMnthListAjax();
    if (queuedBridge?.pendingLoad?.type === "monthly") loadMonthly();
    if (queuedBridge?.pendingLoad?.type === "weekly") {
        const queuedStdrdDt: string = queuedBridge.pendingLoad.stdrdDt
            ?? String(getSharedSearchParams().stdrdDt ?? "")
            ?? cF.date.getCurrDateStr(cF.date.ptnDate);
        loadWeekly(queuedStdrdDt, queuedBridge.pendingLoad.targetDt);
    }
    if (queuedBridge?.pendingLoad?.type === "daily") {
        const queuedStdrdDt: string = queuedBridge.pendingLoad.stdrdDt
            ?? String(getSharedSearchParams().stdrdDt ?? "")
            ?? cF.date.getCurrDateStr(cF.date.ptnDate);
        loadDaily(queuedStdrdDt);
    }
    if (queuedBridge?.pendingLoad?.type === "refresh") refresh();
    if (queuedBridge?.pendingLoad?.type === "reload") {
        applySearchParamsAndReload(queuedBridge.pendingLoad.patch ?? {}, queuedBridge.pendingLoad.scope ?? "CURRENT");
    }
    if (runtimeViewType === "MONTHLY" && queuedBridge?.pendingLoad == null) loadMonthly();
    window.dispatchEvent(new CustomEvent("journal-day:vue-mounted", { detail: { viewType } }));
}

/** 저널 일자 월간 목록 페이지 전용 마운트. */
export async function mountJournalDayMonthlyListApp(): Promise<void> {
    return mountJournalDayListAppForView("MONTHLY", "JournalDayMonthlyApp");
}

/** 저널 일자 주간 목록 페이지 전용 마운트. */
export async function mountJournalDayWeeklyListApp(): Promise<void> {
    return mountJournalDayListAppForView("WEEKLY", "JournalDayWeeklyApp");
}

/** 저널 일자 일간 목록 페이지 전용 마운트. */
export async function mountJournalDayDailyListApp(): Promise<void> {
    return mountJournalDayListAppForView("DAILY", "JournalDayDailyApp");
}
