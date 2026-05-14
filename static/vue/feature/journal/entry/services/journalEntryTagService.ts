/**
 * journalEntryTagService.ts
 * 저널 entry 태그 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-4-β):
 *   - journal_entry_tag_module.ts 의 인스턴스 메서드(`init`, `getCtgrMap`, `getNmList`,
 *     `renderList`, `getCurrentWeekStartDt`, `listAjax`, `listAllAjax`,
 *     `tagGroupListAllAjax`, `openSearch`, `select`) 와 `resolveSearchUrl` 모듈 헬퍼를
 *     본 서비스로 추출한다.
 * 변경(E-4-γ):
 *   - classic `journal_entry_tag_module.ts` 파일 제거. 인스턴스 상태(`ctgrMap`, `list`,
 *     `initialized`, `initPromise`) 는 본 파일의 내부 맵(`tagModuleInstances`) 에 보관하고,
 *     `dF.JournalEntryTag.get(ct)` 는 동일 계약으로 여기서 등록한다(순환 참조 제거).
 *   - 외부 호출자 보존:
 *     · HBS onclick (`_journal_day_tag_header.ftlh`, `_journal_tag_group_list_template.hbs`,
 *       `_journal_entry_content_partial.hbs`) 및 저널 결산 리뷰 태그 영역(Vue `JournalAnnualReviewRow`, 변경 A-7-β)
 *       → `dF.JournalEntryTag.get(ct).<method>(...)`
 *     · Vue 컴포넌트 (JournalEntryContent / JournalChapterItem / JournalDayTagPanelApp /
 *       JournalEntrySearchApp / JournalEntryRegModalApp) → 동일 진입점
 *     · journal_entry_module.ts → `init` / `listAjax` 호출
 *   - `resolveSearchUrl` 은 `dF.JournalEntrySearchPopupService.resolveSearchUrl` 을 우선
 *     재사용해 entry 검색 URL 해석 단일 진실 원천을 유지한다.
 *   - 본 서비스는 글로벌(`dF.JournalEntryTagService`) 로도 노출되어 classic 모듈에서
 *     호출 가능하다.
 *
 * @author nichefish
 */

import * as tagService from "../../../attachable/tag/tagService.js";

type EntryMeta = Record<string, any>;
type TagModule = Record<string, any>;

/** DIARY/DREAM 태그 인스턴스 상태 보관소 — 변경(E-4-γ): classic journal_entry_tag_module.ts 제거 후 내장. */
const tagModuleInstances: Record<string, TagModule> = {};

/**
 * @keepInSync static/vue/feature/journal/day/journalDayListBridge.ts
 * journal_entry_tag_module.ts 의 동일 헬퍼와 의미 동일 — list bridge(JournalDayMonthly/Weekly/Daily) 우선순위로 해석.
 */
function journalDayResolveListBridge(): JournalDayListAppBridge | undefined {
    const w = window as any;
    return w.JournalDayMonthlyApp ?? w.JournalDayWeeklyApp ?? w.JournalDayDailyApp;
}

function getMeta(contentType: string): EntryMeta | undefined {
    return ((window as any).dF?.JournalEntry?.getMeta?.(contentType)) as EntryMeta | undefined;
}

function resolveTagModuleInstance(contentType: string): TagModule | undefined {
    return tagModuleInstances[contentType];
}

/**
 * 변경(E-4-γ): Ajax/store 대상 모듈 객체 조회 — 항상 내부 `tagModuleInstances` 만 사용한다.
 */
function getTagModule(contentType: string): TagModule | undefined {
    return resolveTagModuleInstance(contentType);
}

function buildTagModuleFacades(): void {
    const attach = (contentType: string): void => {
        const module: TagModule = {
            contentType,
            initialized: false,
            initPromise: null,
            ctgrMap: new Map(),
            list: [],

            init: async function(): Promise<void> {
                return init(contentType);
            },

            getCtgrMap: async function(): Promise<void> {
                return getCtgrMap(contentType);
            },

            getNmList: async function(): Promise<void> {
                return getNmList(contentType);
            },

            renderList: function(list: Record<string, any>[] = []): void {
                renderList(contentType, list);
            },

            getCurrentWeekStartDt: function(): string {
                return getCurrentWeekStartDt();
            },

            listAjax: function(): void {
                listAjax(contentType);
            },

            listAllAjax: function(): void {
                listAllAjax(contentType);
            },

            tagGroupListAllAjax: function(): void {
                tagGroupListAllAjax(contentType);
            },

            openSearch: function(tagId: string|number, name?: string): void {
                openSearch(contentType, tagId, name);
            },

            select: function(tagId: string|number, name?: string, ctgr: string = ""): void {
                select(contentType, tagId, name, ctgr);
            },
        };

        if (contentType === "JOURNAL_DREAM") {
            module.dreamTagGroupListAllAjax = module.tagGroupListAllAjax;
        }

        tagModuleInstances[contentType] = module;
    };

    attach("JOURNAL_DIARY");
    attach("JOURNAL_DREAM");
}

/**
 * 검색 URL 해석.
 * 변경 전(E-4-β): journal_entry_tag_module.ts 모듈 스코프의 const resolveSearchUrl(config).
 * 변경 후: entry 검색 URL 해석은 dF.JournalEntrySearchPopupService.resolveSearchUrl 단일 경로로 위임한다.
 *          서비스 로드 순서가 어긋나거나 미적재인 경우에 대비해 동일한 폴백 로직을 보존한다.
 */
function resolveSearchUrl(contentType: string): string {
    const popupService = (window as any).dF?.JournalEntrySearchPopupService;
    if (typeof popupService?.resolveSearchUrl === "function") {
        return popupService.resolveSearchUrl(contentType);
    }
    /* fallback: 동일한 의미를 그대로 재현(서비스 미적재 환경 보호용). */
    const meta = getMeta(contentType);
    if (cF.util.isNotEmpty(meta?.searchUrl) && !String(meta.searchUrl).includes("undefined")) {
        return String(meta.searchUrl);
    }
    const typeSegment: string = String(meta?.entryType ?? "DIARY").toLowerCase();
    return cF.util.bindUrl(Url.JOURNAL_EMTRY_SEARCH, { type: typeSegment });
}

/**
 * 태그 카테고리 맵 조회.
 * 변경 전: module.getCtgrMap — Ajax 응답 rsltMap 을 module.ctgrMap 에 저장.
 */
export async function getCtgrMap(contentType: string): Promise<void> {
    const meta = getMeta(contentType);
    const module = getTagModule(contentType);
    if (!meta || !module) {
        console.error("[journalEntryTagService] meta/module missing:", contentType);
        return;
    }
    return cF.ajax.get(meta.tagCtgrMapUrl, { type: meta.entryType }, function(res: AjaxResponse): void {
        if (res.rsltMap) module.ctgrMap = res.rsltMap;
    });
}

/**
 * 태그 명/리스트 조회.
 * 변경 전: module.getNmList — Ajax 응답 rsltList 를 module.list 에 저장.
 */
export async function getNmList(contentType: string): Promise<void> {
    const meta = getMeta(contentType);
    const module = getTagModule(contentType);
    if (!meta || !module) {
        console.error("[journalEntryTagService] meta/module missing:", contentType);
        return;
    }
    return cF.ajax.get(meta.tagsUrl, { type: meta.entryType }, function(res: AjaxResponse): void {
        if (res.rsltList) module.list = res.rsltList;
    });
}

/**
 * 태그 모듈 초기화. ctgrMap + nmList 순차 로드.
 * 변경 전: module.init — initPromise 가드 후 getCtgrMap / getNmList 순차 await.
 *
 * 변경 후(E-4-β): 가드/플래그(`initPromise`, `initialized`) 는 모듈 인스턴스에 그대로 두고
 * 서비스가 read/write 한다(외부에서 `dF.JournalEntryTag.get(ct).initialized` 직접 조회 가능성 보존).
 */
export async function init(contentType: string): Promise<void> {
    const module = getTagModule(contentType);
    if (!module) {
        console.error("[journalEntryTagService] module missing:", contentType);
        return;
    }
    if (module.initPromise) return module.initPromise;

    module.initPromise = (async () => {
        await getCtgrMap(contentType);
        await getNmList(contentType);
        module.initialized = true;
        console.log(`'dF.JournalEntryTag[${contentType}]' module initialized.`);
    })();
    return module.initPromise;
}

/**
 * 태그 리스트 렌더 — `JournalDayEntryTagListVueApp` 브리지 경로로 수렴.
 *
 * 변경 전: module.renderList — `cF.handlebars.compile({ list, module: meta.tagModuleExpr }, "journal_entry_tag_list")`
 *          결과 HTML 을 `meta.tagListTargetId` 컨테이너에 innerHTML 주입 + bootstrap tooltip 부착.
 *          이후 phase 에서 `journalDayUiBridgeService.syncTagCloud` 가 listEntryTagAjax 진입을 끊고
 *          컨테이너에 빨간 안내 박스(하드컷 placeholder)만 그렸다 — 본 함수는 호출되지 않은 dead 경로였다.
 * 변경 후(A-9): Handlebars 컴파일 제거. `JournalDayEntryTagListVueApp.setList(kind, list, { module })` 로
 *               동일 페이로드를 Vue 앱에 넘기고, Vue 가 sized 태그 v-for 를 렌더한다.
 *               브리지 미마운트 시 `pendingByType` 에 적재해 마운트 후 흡수되도록 한다.
 *               렌더 결과 DOM/onclick 시그니처는 `_tag_list_sized_partial.hbs` 와 1:1 동등.
 */
export function renderList(contentType: string, list: Record<string, any>[] = []): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryTagService] meta missing:", contentType);
        return;
    }
    /* 변경(A-9): contentType("JOURNAL_DIARY"|"JOURNAL_DREAM") → Vue 브리지 kind("DIARY"|"DREAM") 매핑. */
    const kind: "DIARY" | "DREAM" | null
        = contentType === "JOURNAL_DIARY" ? "DIARY"
        : contentType === "JOURNAL_DREAM" ? "DREAM"
        : null;
    if (kind == null) {
        console.error("[journalEntryTagService.renderList] 알 수 없는 contentType:", contentType);
        return;
    }
    const safeList: Record<string, any>[] = Array.isArray(list) ? list : [];
    const config: { module: string } = { module: String(meta.tagModuleExpr ?? "") };

    const bridge = (window as any).JournalDayEntryTagListVueApp as {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DIARY" | "DREAM", { list: Record<string, any>[]; config: { module: string } }>> | null;
        setList?: (kind: "DIARY" | "DREAM", list: Record<string, any>[], config: { module: string }) => void;
    } | undefined;

    if (bridge?.mounted === true && typeof bridge.setList === "function") {
        bridge.setList(kind, safeList, config);
        return;
    }
    if (bridge) {
        bridge.pendingByType = bridge.pendingByType ?? {};
        bridge.pendingByType[kind] = { list: safeList, config };
        console.log("[journalEntryTagService.renderList] JournalDayEntryTagListVueApp pending:", kind);
        return;
    }
    console.error(
        "[journalEntryTagService.renderList] JournalDayEntryTagListVueApp 브리지 없음 — JournalDayEntryTagListApp 적재 순서 확인."
    );
}

/**
 * 현재 주의 시작일 해석. weekly viewType 이거나 stdrdDt 가 있을 때 사용.
 * 변경 전: module.getCurrentWeekStartDt — list bridge searchParams 우선, 없으면 stdrdDt 기반 계산.
 */
export function getCurrentWeekStartDt(): string {
    const searchParams: Record<string, any> = journalDayResolveListBridge()?.getSearchParams?.() ?? {};
    const currentWeekStartDt: string = searchParams.weekStartDt;
    if (cF.util.isNotEmpty(currentWeekStartDt)) return currentWeekStartDt;

    const stdrdDt: string = searchParams.stdrdDt
        ?? cF.date.getCurrDateStr(cF.date.ptnDate);
    return cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
}

/**
 * 태그 리스트 Ajax. viewType(weekly/monthly) 별 파라미터 분기 후 renderList.
 * 변경 전: module.listAjax — Phase 13 에서 Vue SSOT 직접 조회로 전환.
 */
export function listAjax(contentType: string): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryTagService] meta missing:", contentType);
        return;
    }
    const ajaxData: Record<string, any> = {};
    ajaxData.type = meta.entryType;
    /* 변경(Phase 13): dF.JournalDayPageStateService.getViewType() 제거 → Vue SSOT 직접 조회. */
    if (journalDayResolveListBridge()?.viewType === "weekly") {
        const weekStartDt: string = getCurrentWeekStartDt();
        if (cF.util.isEmpty(weekStartDt)) return;
        ajaxData.weekStartDt = weekStartDt;
    } else {
        const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "9999";
        if (cF.util.isEmpty(yy)) return;
        const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "99";
        if (cF.util.isEmpty(mnth)) return;
        ajaxData.yy = yy;
        ajaxData.mnth = mnth;
    }

    cF.ajax.get(meta.tagsUrl, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        renderList(contentType, res.rsltList ?? []);
    });
}

/**
 * 전체 태그 모달용 리스트 Ajax (handlebars template/modal).
 * 변경 전: module.listAllAjax — yy=9999, mnth=99 로 전체 조회 후 carousel 모달 띄움.
 */
export function listAllAjax(contentType: string): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryTagService] meta missing:", contentType);
        return;
    }
    const ajaxData: Record<string, any> = { yy: 9999, mnth: 99, type: meta.entryType };
    cF.ajax.get(meta.tagsUrl, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const ctgrSet: Set<string> = new Set();
        res.rsltList.forEach((item: Record<string, string>): void => {
            if (item.ctgr) ctgrSet.add(item.ctgr);
        });
        cF.handlebars.template(ctgrSet, "journal_tag_ctgr");
        cF.handlebars.modal(res.rsltList, "journal_tag_list");
    });
}

/**
 * 태그 그룹 리스트 모달 Ajax (카테고리별 group 후 append).
 * 변경 전: module.tagGroupListAllAjax — dF.Tag.groupTagsByCategory 로 그룹핑 후 append.
 */
export function tagGroupListAllAjax(contentType: string): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryTagService] meta missing:", contentType);
        return;
    }
    const ajaxData: Record<string, any> = { yy: 9999, mnth: 99, type: meta.entryType };
    cF.ajax.get(meta.tagsUrl, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const groupedList = tagService.groupTagsByCategory(res.rsltList);
        for (const ctgr in groupedList) {
            if (!Object.prototype.hasOwnProperty.call(groupedList, ctgr)) continue;
            cF.handlebars.append({ ctgr, tagList: groupedList[ctgr] }, "journal_tag_list");
        }
        $("#journal_tag_list_modal").modal("show");
    });
}

/**
 * 태그 클릭 → 검색 진입.
 * 변경 전: module.openSearch — JournalEntrySearch 활성 시 select 위임,
 *          그 외에는 search URL + tagIds 파라미터로 popup open.
 */
export function openSearch(contentType: string, tagId: string|number, name?: string): void {
    const meta = getMeta(contentType);
    const module = getTagModule(contentType);
    if (!meta || !module) {
        console.error("[journalEntryTagService] meta/module missing:", contentType);
        return;
    }

    if ((window as any).journalEntrySearchContentType === contentType) {
        const resolvedName: string = name
            ?? module.list?.find?.((tag: any): boolean => Number(tag.id) === Number(tagId))?.name
            ?? String(tagId);
        (window as any).dF?.JournalEntrySearch?.get?.(contentType)?.select?.(tagId, resolvedName);
        return;
    }

    const baseSearchUrl: string = resolveSearchUrl(contentType);
    let url: string = `${baseSearchUrl}?tagIds=${tagId}`;
    /* 변경(Phase 13): dF.JournalDayPageStateService.getViewType() 제거 → Vue SSOT 직접 조회. */
    if (journalDayResolveListBridge()?.viewType === "weekly") {
        const weekStartDt: string = getCurrentWeekStartDt();
        if (cF.util.isNotEmpty(weekStartDt)) url += `&weekStartDt=${encodeURIComponent(weekStartDt)}`;
    }

    const options: string = "width=1960,height=1440,top=0,left=270";
    const popup: Window = cF.ui.openPopup(url, meta.popupName, options);
    if (popup) popup.focus();
}

/**
 * 태그 컨텍스트 메뉴 진입.
 * 변경 전: module.select — dF.JournalDayTagContextMenu.openContextMenu 우선,
 *          없으면 module.openSearch 폴백.
 *
 * 컨텍스트 메뉴 콜백:
 *   · onSearch: 본 서비스의 openSearch 호출.
 *   · onConfigure: dF.JournalDayTagService.openProfileModal 단일 진입점 위임.
 */
export function select(
    contentType: string,
    tagId: string|number,
    name?: string,
    ctgr: string = ""
): void {
    const dfNs = (window as any).dF;
    const menu = dfNs?.JournalDayTagContextMenu;
    if (menu?.openContextMenu) {
        menu.openContextMenu({
            tagId,
            name: name ?? "",
            ctgr,
            contentType,
            onSearch: function(): void {
                openSearch(contentType, tagId, name);
            },
            onConfigure: function(): void {
                const openProfile = dfNs?.JournalDayTagService?.openProfileModal;
                if (typeof openProfile === "function") {
                    openProfile(tagId, contentType, name ?? "", ctgr);
                    return;
                }
                console.error(
                    "[journalEntryTagService.select.onConfigure] dF.JournalDayTagService.openProfileModal 없음; journalDayTagService 로드 순서 확인."
                );
            },
        });
        return;
    }
    openSearch(contentType, tagId, name);
}

/**
 * 글로벌 노출. classic `journal_entry_tag_module.ts` 제거(E-4-γ) 이전 thin wrapper 가
 * `(window as any).dF.JournalEntryTagService.<method>(...)` 로 호출 가능하도록 등록한다.
 */
const journalEntryTagService = {
    init,
    getCtgrMap,
    getNmList,
    renderList,
    getCurrentWeekStartDt,
    listAjax,
    listAllAjax,
    tagGroupListAllAjax,
    openSearch,
    select,
};

buildTagModuleFacades();

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalEntryTagService = journalEntryTagService;
    w.dF.JournalEntryTag = {
        initialized: true,
        init: function(): void {},
        get: function(contentType: string): TagModule {
            return tagModuleInstances[contentType];
        },
    };
})();

export default journalEntryTagService;
