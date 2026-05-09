/**
 * journalDayUiBridgeService.ts
 * 저널 일자 Vue 마이그레이션용 레거시 DOM/UI 브리지 헬퍼.
 *
 * 변경 후(Phase 9): dF.JournalDayRuntimeService/ViewService/JournalEntry/JournalEntryTag 의존 완전 제거.
 * - openDetached: window.open 직접 호출
 * - buildWeeklyViewUrl/moveToWeeklyView: journalDaySearchStateService 기반 URL 빌더 인라인
 * - syncTagCloud: 일자 태그는 journalDayTagService.refreshDayTagList(Vue)만. 일기/꿈 엔트리 태그 Handlebars 재렌더 경로 제거(하드컷).
 *
 * 변경 후(A-9): 일기/꿈 엔트리 태그도 Vue 수렴(JournalDayEntryTagListApp). syncTagCloud 의 하드컷 placeholder
 *               제거하고 `dF.JournalEntryTag.get(ct).listAjax()` 진입 — Ajax 결과는 journalEntryTagService.renderList
 *               이 `JournalDayEntryTagListVueApp.setList` 브리지로 흘려보낸다(Handlebars 비경유).
 */

import journalDaySearchStateService from "./journalDaySearchStateService.js";
import journalDayTagService from "./journalDayTagService.js";
import journalDayMetaService from "./journalDayMetaService.js";

/**
 * 사이드바 일기/꿈 엔트리 태그 헤더·리스트 컨테이너(표시 토글용).
 * 변경 전: 각 항목에 대해 <code>listEntryTagAjax</code>로 Handlebars 재렌더했다.
 * 변경 후(하드컷): 리스트는 Vue 수렴 전까지 갱신하지 않으며, 보이는 경우 컨테이너에 안내 문구만 둔다.
 * 변경 후(A-9): Vue 수렴 완료(JournalDayEntryTagListApp). 안내 문구 제거하고
 *               `dF.JournalEntryTag.get(ct).listAjax()` 호출 → service.renderList → Vue 브리지.
 */
const JOURNAL_ENTRY_TAG_CLOUD_SECTIONS: Array<{
    contentType: string;
    cssPrefix: string;
    tagListTargetId: string;
}> = [
    { contentType: "JOURNAL_DIARY", cssPrefix: "diary", tagListTargetId: "journal_diary_tag_list_div" },
    { contentType: "JOURNAL_DREAM", cssPrefix: "dream", tagListTargetId: "journal_dream_tag_list_div" },
];

function syncMonthlyUrl(params: Record<string, any>): void {
    const urlObj = new URL(window.location.href);
    Object.keys(params).forEach(function(key: string): void {
        urlObj.searchParams.set(key, String(params[key]));
    });
    window.history.replaceState(null, "", urlObj.toString());
}

/**
 * 월간 URL 교체 후 사이드 정렬 아이콘 상태를 리스트 와 동기화한다.
 *
 * 변경 전: <code>#journal_aside #sortIcon</code> — #journal_aside 는 card-body 라서 #sortIcon(헤더)과 조상 관계 아님, 선택자 불일치로 DOM 갱신 누락 가능.
 * 변경 후: <code>#journal_aside_header #sortIcon</code> — journal_day_aside_state_service / journalDayListAppMount 와 동일 축.
 */
function syncMonthlySortIcon(sortValue: string): void {
    if (sortValue === "ASC") {
        $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
        return;
    }
    $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
}

/**
 * 태그 클라우드 영역(헤더·일자 태그 Vue·일기/꿈 엔트리 태그 Vue)을 동기화한다.
 *
 * 변경 전: <code>refreshDayTagList</code> 후 <code>listEntryTagAjax</code>로 일기/꿈 엔트리 태그를 Handlebars 렌더했다.
 * 변경 후(하드컷): 일자 태그만 <code>refreshDayTagList</code>로 갱신. 엔트리 태그는 재렌더하지 않고 안내 블록만 표시했다.
 * 변경 후(A-9): 엔트리 태그도 Vue 수렴(JournalDayEntryTagListApp). 안내 블록 제거하고
 *               `dF.JournalEntryTag.get(ct).listAjax()` 진입 — Ajax 응답을 service.renderList 가
 *               `JournalDayEntryTagListVueApp.setList` 브리지로 흘려보낸다(Handlebars 비경유).
 * @param {boolean} showDiaries
 * @param {boolean} showDreams
 * @param {boolean} showTagCloud
 */
function syncTagCloud(showDiaries: boolean, showDreams: boolean, showTagCloud: boolean): void {
    $("#journal_tag_header").toggle(showTagCloud);
    if (!showTagCloud) return;

    void refreshDayTagList();

    JOURNAL_ENTRY_TAG_CLOUD_SECTIONS.forEach(function(config): void {
        const headerSelector: string = `#journal_${config.cssPrefix}_tag_header`;
        const isVisible: boolean = (config.contentType === "JOURNAL_DIARY" && showDiaries)
            || (config.contentType === "JOURNAL_DREAM" && showDreams);

        $(headerSelector).toggleClass("d-none", !isVisible);
        if (!isVisible) return;

        /* 변경(A-9): listAjax → service.renderList → JournalDayEntryTagListVueApp.setList 단일 경로. */
        const tagModule = (window as any).dF?.JournalEntryTag?.get?.(config.contentType) as
            | { listAjax?: () => void }
            | undefined;
        if (typeof tagModule?.listAjax !== "function") {
            console.error(
                "[journalDayUiBridgeService.syncTagCloud] dF.JournalEntryTag.get(%s).listAjax 없음 — journalEntryTagService 적재 순서 확인.",
                config.contentType
            );
            return;
        }
        tagModule.listAjax();
    });
}

function refreshDayTagList(): void {
    void journalDayTagService.refreshDayTagList();
}

/**
 * 접기·펼치기(목록 카드 등). 레거시 <code>dF.JournalDayTag.expand</code> 대체.
 *
 * 변경 전: Vue 컴포넌트가 <code>dF.JournalDayTag.expand</code> 직호출.
 * 변경 후: <code>journalDayTagService.expandTaggedContent</code>(브리지와 동일 로직).
 *
 * 변경 전후 DOM 결과: 동일.
 * @param {HTMLElement} trigger
 */
function expandJournalDayTaggedContent(trigger: HTMLElement): void {
    journalDayTagService.expandTaggedContent(trigger);
}

function selectDayTag(tagId: string | number, name: string, ctgr?: string): void {
    journalDayTagService.selectDayTag(tagId, name, ctgr);
}

function openDayTagDetail(tagId: string | number, name: string, yy?: string): void {
    void journalDayTagService.openDayTagDetail(tagId, name, yy);
}

function selectMeta(metaId: string | number): void {
    journalDayMetaService.selectMeta(metaId);
}

function openMetaModal(metaId: string | number): void {
    void journalDayMetaService.openMetaModal(metaId);
}

function changeMetaYear(metaId: string | number, yy: string): void {
    journalDayMetaService.changeMetaYear(metaId, yy);
}

/**
 * 저널 일자를 새 탭의 일간 화면으로 연다.
 * 변경 후(Phase 9): dF.JournalDayRuntimeService.openDetatched() 제거 후 직접 호출.
 * @param {string} stdrdDt 기준 일자
 */
function openDetached(stdrdDt: string): void {
    const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt });
    window.open(url, "_blank", "noopener,noreferrer");
}

/**
 * 주간 뷰 URL을 빌드한다.
 * 변경 후(Phase 9): dF.JournalDayViewService.buildWeeklyViewUrl() 제거 후 인라인.
 * 검색 파라미터는 journalDaySearchStateService(Vue SSOT)에서 직접 읽는다.
 * @param {string} stdrdDt 주 기준 일자
 * @param {string} [targetDt] 스크롤 대상 일자
 */
function buildWeeklyViewUrl(stdrdDt: string, targetDt?: string): string {
    const currentParams: Record<string, any> = journalDaySearchStateService.getSearchParams();
    const yy: string = stdrdDt.substring(0, 4);
    const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
    const targetUrl: URL = new URL(Url.JOURNAL_DAY_WEEKLY, window.location.origin);

    targetUrl.searchParams.set("stdrdDt", stdrdDt);
    targetUrl.searchParams.set("yy", yy);
    targetUrl.searchParams.set("mnth", mnth);
    if (cF.util.isNotEmpty(targetDt)) targetUrl.searchParams.set("target", targetDt);
    if (typeof currentParams.showDiaries === "boolean") targetUrl.searchParams.set("showDiaries", String(currentParams.showDiaries));
    if (typeof currentParams.showDreams === "boolean") targetUrl.searchParams.set("showDreams", String(currentParams.showDreams));
    if (typeof currentParams.showTagCloud === "boolean") targetUrl.searchParams.set("showTagCloud", String(currentParams.showTagCloud));
    if (currentParams.showChapterCtgr === false) targetUrl.searchParams.set("showChapterCtgr", "false");
    if (cF.util.isNotEmpty(currentParams.diaryKeyword)) targetUrl.searchParams.set("diaryKeyword", currentParams.diaryKeyword);
    if (cF.util.isNotEmpty(currentParams.dreamKeyword)) targetUrl.searchParams.set("dreamKeyword", currentParams.dreamKeyword);
    if (Array.isArray(currentParams.chapterCtgrCds) && currentParams.chapterCtgrCds.length > 0) {
        targetUrl.searchParams.set("chapterCtgrCds", currentParams.chapterCtgrCds.join(","));
    }
    if (cF.util.isNotEmpty(currentParams.sort)) targetUrl.searchParams.set("sort", currentParams.sort);

    return `${targetUrl.pathname}${targetUrl.search}${targetUrl.hash}`;
}

/**
 * 주간 뷰로 화면을 전환한다.
 * 변경 후(Phase 9): dF.JournalDayViewService.moveToWeeklyView() 제거 후 인라인.
 * @param {string} stdrdDt 주 기준 일자
 */
function moveToWeeklyView(stdrdDt: string): void {
    cF.ui.blockUIReplace(buildWeeklyViewUrl(stdrdDt));
}

/**
 * 탭 전환(주간) 시 주 기준일을 SSOT에서 결정한다.
 * 변경 전: journal_day_view_service.js 의 changeView 분기와 동일한 앵커 규칙.
 * 변경 후: journalDaySearchStateService.getSearchParams()만 사용한다.
 */
function resolveStdrdDtForWeeklyTab(): string {
    const params: Record<string, any> = journalDaySearchStateService.getSearchParams();
    const anchor: string = String(params.stdrdDt ?? "").trim();
    if (cF.util.isNotEmpty(anchor)) return anchor;
    const yy: string = String(params.yy ?? cF.date.getCurrYyStr());
    const mnthRaw: string = String(params.mnth ?? cF.date.getCurrMnthStr());
    const monthNo: number = parseInt(mnthRaw, 10);
    const safeMonth: number = monthNo >= 1 && monthNo <= 12 ? monthNo : 1;
    return `${yy}-${String(safeMonth).padStart(2, "0")}-01`;
}

/**
 * 월간·캘린더·메타 목록형 뷰 URL을 SSOT 검색 파라미터와 함께 빌드한다.
 * 변경 전: journal_day_view_service.js 의 changeView + 목록 URL 빌더.
 * 변경 후: journalDayListAppMount.buildMonthlyViewUrl() 과 동일한 쿼리 규칙(경로만 인자로 분리).
 * @param {string} listPath Url.JOURNAL_DAY_MONTHLY 등 pathname 상수
 */
function buildListStyleJournalDayViewUrl(listPath: string): string {
    const params: Record<string, any> = journalDaySearchStateService.getSearchParams();
    const anchorDate: string = String(params.stdrdDt ?? "");
    const yy: string = anchorDate ? anchorDate.substring(0, 4) : String(params.yy || cF.date.getCurrYyStr());
    const mnth: string = anchorDate
        ? String(parseInt(anchorDate.substring(5, 7), 10))
        : String(params.mnth || cF.date.getCurrMnthStr());
    const targetUrl: URL = new URL(listPath, window.location.origin);
    targetUrl.searchParams.set("yy", yy);
    targetUrl.searchParams.set("mnth", mnth);
    if (cF.util.isNotEmpty(anchorDate)) targetUrl.searchParams.set("stdrdDt", anchorDate);
    if (typeof params.showDiaries === "boolean") targetUrl.searchParams.set("showDiaries", String(params.showDiaries));
    if (typeof params.showDreams === "boolean") targetUrl.searchParams.set("showDreams", String(params.showDreams));
    if (typeof params.showTagCloud === "boolean") targetUrl.searchParams.set("showTagCloud", String(params.showTagCloud));
    if (params.showChapterCtgr === false) targetUrl.searchParams.set("showChapterCtgr", "false");
    if (cF.util.isNotEmpty(params.diaryKeyword)) targetUrl.searchParams.set("diaryKeyword", params.diaryKeyword);
    if (cF.util.isNotEmpty(params.dreamKeyword)) targetUrl.searchParams.set("dreamKeyword", params.dreamKeyword);
    if (Array.isArray(params.chapterCtgrCds) && params.chapterCtgrCds.length > 0) {
        targetUrl.searchParams.set("chapterCtgrCds", params.chapterCtgrCds.join(","));
    }
    if (cF.util.isNotEmpty(params.sort)) targetUrl.searchParams.set("sort", params.sort);
    return `${targetUrl.pathname}${targetUrl.search}${targetUrl.hash}`;
}

/**
 * 저널 일자 상단 탭(월/주/캘/메타) 전환. FTL onclick 이 호출한다.
 * 변경 전: dF.JournalDayViewService.changeView (journal_day_view_service.js).
 * 변경 후: Vue 브리지가 blockUIReplace 까지 수행한다.
 * @param {string} targetPath Url.JOURNAL_DAY_* 상수
 */
function changeView(targetPath: string): void {
    const path: string = String(targetPath ?? "").trim();
    if (path.length === 0) {
        console.error("[journalDayUiBridgeService.changeView] empty targetPath");
        return;
    }
    if (path === Url.JOURNAL_DAY_WEEKLY) {
        cF.ui.blockUIReplace(buildWeeklyViewUrl(resolveStdrdDtForWeeklyTab()));
        return;
    }
    if (path === Url.JOURNAL_DAY_MONTHLY || path === Url.JOURNAL_DAY_CAL || path === Url.JOURNAL_DAY_META_VIEW) {
        cF.ui.blockUIReplace(buildListStyleJournalDayViewUrl(path));
        return;
    }
    console.error("[journalDayUiBridgeService.changeView] unsupported targetPath: %s", path);
}

function getDayTagCategoryMap(): Record<string, any> {
    return journalDayTagService.getDayTagCategoryMap();
}

function getDayMetaCategoryMap(): Record<string, any> {
    return journalDayMetaService.getDayMetaCategoryMap();
}

function initRenderedDom(rootId: string): void {
    Vue.nextTick(function(): void {
        const target = document.getElementById(rootId);
        if (!target) return;

        const bootstrapTooltip = (window as any).bootstrap?.Tooltip;
        target.querySelectorAll("[data-bs-toggle='tooltip']").forEach(function(tooltipEl: Element): void {
            if (!bootstrapTooltip) return;
            const htmlEl = tooltipEl as HTMLElement;
            const existing = bootstrapTooltip.getInstance?.(htmlEl);
            if (existing) existing.dispose();
            new bootstrapTooltip(htmlEl);
        });
        KTMenu.createInstances();
    });
}

const journalDayUiBridgeService = {
    buildListStyleJournalDayViewUrl,
    buildWeeklyViewUrl,
    changeMetaYear,
    changeView,
    expandJournalDayTaggedContent,
    getDayMetaCategoryMap,
    getDayTagCategoryMap,
    initRenderedDom,
    moveToWeeklyView,
    openDayTagDetail,
    openDetached,
    openMetaModal,
    refreshDayTagList,
    selectDayTag,
    selectMeta,
    syncMonthlyUrl,
    syncMonthlySortIcon,
    syncTagCloud,
};

export default journalDayUiBridgeService;
