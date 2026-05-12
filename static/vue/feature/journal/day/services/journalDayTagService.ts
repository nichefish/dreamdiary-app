/**
 * journalDayTagService.ts
 * Vue 소유 저널 일자 태그 조율 서비스.
 */

import { getJournalDayListBridge } from "../journalDayListBridge.js";
// 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
import { resolveMessage } from "../../../../common/messageHelper.js";

type YearOption = {
    value: string | number;
    label: string | number;
    selected?: boolean;
};

let dayTagCategoryMap: Record<string, any> = {};

function showAjaxFailure(res: AjaxResponse): void {
    if (cF.util.isNotEmpty(res?.message)) Swal.fire({ text: res.message });
}

/**
 * 태그 Vue 브리지 실패 시 DOM에 경고를 그린다(빈 화면으로 위장하지 않음).
 * 변경 전: 브리지 없음만 <code>false</code>로 반환해 호출측이 침묵할 수 있었다.
 * 변경 후: 대상 컨테이너에 경고 블록을 넣는다. 선택자 무효 시에만 여기서 <code>console.error</code>를 남긴다.
 * @param {string} targetSelector querySelector용 선택자
 * @param {string} context 로그·화면에 넣을 맥락 문자열(고정 분기에서만 전달)
 */
function paintJournalDayTagBridgeFailureAlert(targetSelector: string, context: string): void {
    const el: HTMLElement | null = document.querySelector(targetSelector);
    if (el == null) {
        console.error("[JournalDayTag] 경고 배너 대상 DOM 없음: selector=%s context=%s", targetSelector, context);
        return;
    }
    el.innerHTML = "";
    const wrap: HTMLDivElement = document.createElement("div");
    wrap.className = "alert alert-danger py-2 px-2 mb-0 small";
    wrap.setAttribute("role", "alert");
    wrap.style.cssText = "color:#842029;background:#f8d7da;border:1px solid #f5c2c7;font-size:12px;";
    const strong: HTMLElement = document.createElement("strong");
    strong.textContent = "JournalDay ";
    wrap.appendChild(strong);
    wrap.appendChild(document.createTextNode("태그: Vue 브리지 없음(하드컷) — "));
    const code: HTMLElement = document.createElement("code");
    code.textContent = context;
    wrap.appendChild(code);
    wrap.appendChild(document.createTextNode(". 콘솔을 확인하세요."));
    el.appendChild(wrap);
}

/**
 * 태그 상세 모달용 브리지 없음: 리스트 영역에 경고 후 모달을 연다(비정상 가시화).
 * @param {string} context 맥락 문자열
 */
function reportJournalDayTagDetailBridgeMissing(context: string): void {
    console.error("[JournalDayTag] JournalDay Vue 브리지 없음(하드컷): %s", context);
    paintJournalDayTagBridgeFailureAlert("#journal_day_tag_dtl_list", context);
    const modalEl: HTMLElement | null = document.querySelector("#journal_day_tag_dtl_modal");
    const ModalCtor: { getOrCreateInstance?: (el: HTMLElement) => { show: () => void } } | undefined = (window as any).bootstrap?.Modal;
    if (modalEl != null && typeof ModalCtor?.getOrCreateInstance === "function") {
        ModalCtor.getOrCreateInstance(modalEl).show();
    }
}

/**
 * Vue 태그 패널에 일자 태그 목록을 넘긴다.
 *
 * 변경 전: <code>dF.JournalDayTag</code>와 이 파일에서 이중 구현.
 * 변경 후: 이 함수가 단일 구현이며 <code>dF.JournalDayTagService</code>를 통해 호출된다.
 * @param {Record<string, any>[]} list
 * @returns {boolean}
 */
function setTagListByVueBridge(list: Record<string, any>[]): boolean {
    const bridge = window.JournalDayTagPanelVueApp;
    const safeList: Record<string, any>[] = Array.isArray(list) ? list : [];
    if (bridge?.mounted && typeof bridge.setDayTagList === "function") {
        bridge.setDayTagList(safeList);
        return true;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingDayTagList = safeList;
        return true;
    }
    console.error("[JournalDayTag] setTagListByVueBridge: JournalDayTagPanelVueApp 없음(하드컷).");
    paintJournalDayTagBridgeFailureAlert("#journal_day_tag_list_div", "JournalDayTagPanelVueApp#setDayTagList");
    return false;
}

/**
 * Vue 태그 패널에서 태그 목록 모달을 연다.
 *
 * 변경 전: <code>journal_day_tag_module.ts</code> 단일 구현.
 * 변경 후: <code>journalDayTagService</code> 소유·<code>dF.JournalDayTagService</code> 단일 진입.
 * @param {Record<string, any>[]} list
 * @returns {boolean}
 */
function openTagListModalByVueBridge(list: Record<string, any>[]): boolean {
    const bridge = window.JournalDayTagPanelVueApp;
    const safeList: Record<string, any>[] = Array.isArray(list) ? list : [];
    if (bridge?.mounted && typeof bridge.openTagListModal === "function") {
        bridge.openTagListModal(safeList);
        return true;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingModalTagList = safeList;
        return true;
    }
    console.error("[JournalDayTag] openTagListModalByVueBridge: JournalDayTagPanelVueApp 없음(하드컷).");
    paintJournalDayTagBridgeFailureAlert("#journal_day_tag_list_div", "JournalDayTagPanelVueApp#openTagListModal");
    return false;
}

/**
 * Vue 태그 프로필 모달을 연다.
 *
 * 변경 전: <code>dF.JournalDayTag.openProfileModalByVueBridge</code> 위임.
 * 변경 후: 이 함수가 단일 구현이며 <code>dF.JournalDayTagService</code>를 통해 호출된다.
 * @param {Record<string, any>} payload
 * @returns {boolean}
 */
function openProfileModalByVueBridge(payload: Record<string, any>): boolean {
    const bridge = window.JournalDayTagProfileVueApp;
    if (bridge?.mounted && typeof bridge.open === "function") {
        const opened: boolean = bridge.open(payload);
        if (!opened) {
            console.error("[JournalDayTag] openProfileModalByVueBridge: bridge.open 결과 false(DOM/폼 미준비).");
            if (typeof Swal !== "undefined") {
                Swal.fire({ icon: "error", text: "태그 프로필 모달 적용 실패. 콘솔을 확인하세요." });
            }
        }
        return opened;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingPayload = payload;
        return true;
    }
    console.error("[JournalDayTag] openProfileModalByVueBridge: JournalDayTagProfileVueApp 없음(하드컷).");
    if (typeof Swal !== "undefined") {
        Swal.fire({
            icon: "error",
            text: "태그 프로필 Vue 브리지 없음(하드컷). 콘솔을 확인하세요.",
        });
    }
    return false;
}

/**
 * Vue 태그 상세 모달을 연다.
 *
 * 변경 전: <code>dF.JournalDayTag.openTagDetailByVueBridge</code> 위임.
 * 변경 후: 이 함수가 단일 구현이며 <code>dF.JournalDayTagService</code>를 통해 호출된다.
 * @param payload
 * @returns {boolean}
 */
function openTagDetailByVueBridge(payload: {
    tagId: string | number;
    name: string;
    yy: string;
    yearOptions: Array<{ value: string; label: string; selected?: boolean }>;
    list: Record<string, any>[];
    weekMode?: boolean;
}): boolean {
    const bridge = window.JournalDayTagDetailVueApp;
    if (bridge?.mounted && typeof bridge.open === "function") {
        bridge.open(payload);
        return true;
    }
    if (bridge && bridge.mounted !== true) {
        bridge.pendingPayload = payload;
        return true;
    }
    reportJournalDayTagDetailBridgeMissing("JournalDayTagDetailVueApp#open");
    return false;
}

/**
 * 본문 앞 줄의 <code>.journal-content</code>에 <code>expanded</code> 클래스를 토글한다.
 *
 * 변경 전: <code>journal_day_tag_module.expand</code> 단일 보유·Handlebars/onclick 참조.
 * 변경 후: 본 함수가 단일 구현이며 레거시 모듈·Vue 브리지 양쪽이 호출한다. jQuery <code>$</code>와 동작 정합.
 *
 * 변경 전후 DOM/클래스 결과: 동일(<code>$(trigger).prev(".journal-content").toggleClass("expanded")</code>).
 *
 * @param {HTMLElement} trigger
 */
function expandTaggedContent(trigger: HTMLElement): void {
    if (typeof $ === "undefined") {
        console.error("[journalDayTagService.expandTaggedContent] jQuery($) 미로드; 확장 생략.");
        return;
    }
    ($(trigger).prev(".journal-content") as any).toggleClass("expanded");
}

/**
 * 태그 카테고리 맵 캐시를 맞춘다.
 *
 * 변경 전: <code>getDayTagCategoryMap</code> 동기 폴백으로 <code>dF.JournalDayTag.ctgrMap</code>을 읽었다.
 * 변경 후: 본 객체(<code>dayTagCategoryMap</code>)만 SSOT이다. 레거시 Ajax는 브리지·pending 경로로 이 함수를 호출한다.
 *
 * 변경 전후 동작 차이(Promise <code>getCtgrMap</code> 성공 시): 결과 맵 적재 방식은 동일하고, 레거시 콜백과의 단일 근거만 정렬한다.
 *
 * @param {Record<string, any>} map
 */
function hydrateDayTagCategoryMap(map: Record<string, any>): void {
    if (map == null || typeof map !== "object" || Array.isArray(map)) {
        dayTagCategoryMap = {};
        return;
    }
    dayTagCategoryMap = { ...map };
}

/**
 * 연도 문자열 결정(태그 상세 등).
 *
 * 변경 전: Vue SSOT 다음에 URL 쿼리 <code>yy</code> 폴백.
 * 변경 후: <code>journal_day_tag_module.ts</code>의 <code>getSelectedYy</code>와 동일 — URL 제거, Vue 없으면 <code>console.warn</code> 후 현재 연도.
 * @param {string|number} [yy]
 * @returns {string}
 */
function getSelectedYy(yy?: string | number): string {
    if (yy != null && cF.util.isNotEmpty(String(yy))) return String(yy);

    const currentSearchYy: string = getJournalDayListBridge()?.getSearchParams?.()?.yy;
    if (cF.util.isNotEmpty(currentSearchYy)) return currentSearchYy;

    console.warn(
        "[journalDayTagService.getSelectedYy] Vue SSOT yy 없음; URL yy 폴백 제거됨. 현재 연도 문자열로 진행한다."
    );
    return cF.date.getCurrYyStr();
}

function getCurrentWeekStartDt(): string {
    const currentWeekStartDt: string = getJournalDayListBridge()?.getSearchParams?.()?.weekStartDt;
    if (cF.util.isNotEmpty(currentWeekStartDt)) return currentWeekStartDt;

    const stdrdDt: string = getJournalDayListBridge()?.getSearchParams?.()?.stdrdDt
        ?? cF.date.getCurrDateStr(cF.date.ptnDate);
    return cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
}

function normalizeSelectedYy(selectedYy: string, yyList: (string | number)[]): string {
    if (yyList.length === 0) return selectedYy;

    const matchedYy = yyList.find((yy: string | number): boolean => String(yy) === String(selectedYy));
    if (matchedYy != null) return String(matchedYy);

    return String(yyList[0]);
}

function getYearOptions(selectedYy: string, yyList: (string | number)[]): YearOption[] {
    return yyList.map((yy: string | number): YearOption => ({
        value: yy,
        label: yy,
        selected: String(yy) === String(selectedYy),
    }));
}

function getContentTypeLabel(contentType: string): string {
    switch (contentType) {
        case "JOURNAL_DAY": return "\uC77C\uC790";
        case "JOURNAL_DIARY": return "\uC77C\uAE30";
        case "JOURNAL_DREAM": return "\uAFC8";
        case "JOURNAL_INTERPRETATION": return "\uD574\uC11D";
        default: return contentType;
    }
}

function getCtgrMap(): Promise<Record<string, any>> {
    return new Promise((resolve): void => {
        cF.ajax.get(Url.JOURNAL_DAY_TAG_CTGR_MAP, {}, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve({});
                return;
            }
            hydrateDayTagCategoryMap((res.rsltMap as Record<string, any>) ?? {});
            resolve(dayTagCategoryMap);
        });
    });
}

function getYyList(tagId: string | number): Promise<any[]> {
    return new Promise((resolve): void => {
        const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_TAG_YYS, { tagId });
        cF.ajax.get(url, null, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve([]);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        });
    });
}

function listTags(params: Record<string, any>): Promise<Record<string, any>[]> {
    return new Promise((resolve): void => {
        cF.ajax.get(Url.JOURNAL_DAY_TAGS, params, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve([]);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        });
    });
}

/**
 * yy=9999, mnth=99 전역 일자 태그 목록으로 태그 리스트 모달을 연다.
 *
 * 변경 전: <code>journal_day_tag_module.listAllAjax</code>에서 레거시 데이터 서비스·jQuery 직결.
 * 변경 후: 저널 태그 Ajax 로직 단일 근거(레거시 <code>dF.JournalDayTag.listAllAjax</code> 는 브리지만 호출).
 *
 * 변경 전후: 모달 목록 내용·<code>#journal_tag_dtl_modal</code> 닫기 동작 동일(jQuery 존재 시).
 */
async function listAllTagsOpenModalAsync(): Promise<void> {
    const list = await listTags({ yy: 9999, mnth: 99 });
    openTagListModalByVueBridge(list);
    if (typeof $ !== "undefined") {
        $("#journal_tag_dtl_modal").modal("hide");
        return;
    }
    console.warn("[journalDayTagService.listAllTagsOpenModalAsync] jQuery 없음 — #journal_tag_dtl_modal 을 숨길 수 없음.");
}

function getTagDetail(tagId: string | number, params: Record<string, any>): Promise<Record<string, any>[]> {
    return new Promise((resolve): void => {
        const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_TAG, { tagId });
        cF.ajax.get(url, params, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve([]);
                return;
            }
            resolve(Array.isArray(res.rsltList) ? res.rsltList : []);
        });
    });
}

function getTagProfile(tagId: string | number, contentType: string): Promise<Record<string, any>> {
    return new Promise((resolve): void => {
        const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId });
        cF.ajax.get(url, { contentType }, function(res: AjaxResponse): void {
            if (!res.rslt) {
                showAjaxFailure(res);
                resolve({});
                return;
            }
            resolve((res.rsltObj as Record<string, any>) ?? {});
        });
    });
}

/**
 * 태그 프로필 폼 직렬화.
 *
 * 변경 전: <code>dF.JournalDayTagProfileService.getProfileFormData</code> (<code>journal_day_tag_profile_service.ts</code> → P7 <code>journalDayTagProfileShell.ts</code>).
 * 변경 후: 본 모듈 단일 소유. 반환 형식·폼 선택자 동일(<code>#tagProfileForm</code> <code>serializeArray</code>).
 * @returns {Record<string, any>}
 */
function getTagProfileFormData(): Record<string, any> {
    const data: Record<string, any> = {};
    if (typeof $ === "undefined") {
        console.error("[journalDayTagService.getTagProfileFormData] jQuery($) 미로드;");
        return data;
    }
    $("#tagProfileForm").serializeArray().forEach(function(item: JQuery.NameValuePair): void {
        data[item.name] = item.value;
    });
    return data;
}

/**
 * 태그 프로필 저장(Ajax).
 *
 * 변경 전: <code>dF.JournalDayTagProfileService.submitProfile</code>.
 * 변경 후: <code>dF.JournalDayTagService.submitProfile</code>. 입력 검증·Swal 확인·POST URL·성공 시 <code>#tag_profile_modal</code> 숨김·<code>reload</code> 동일.
 */
function submitTagProfile(): void {
    const ajaxData: Record<string, any> = getTagProfileFormData();
    if (isNaN(Number(ajaxData.tagId)) || cF.util.isEmpty(ajaxData.contentType)) return;

    Swal.fire({
        text: resolveMessage(cF.util.isEmpty(ajaxData.id) ? "view.cnfm.reg" : "view.cnfm.mdf"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId: ajaxData.tagId });
        cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): boolean {
            Swal.fire({ text: res.message }).then(function(): void {
                if (!res.rslt) return;
                if (typeof $ !== "undefined") {
                    $("#tag_profile_modal").modal("hide");
                } else {
                    console.warn("[journalDayTagService.submitTagProfile] jQuery 없음 — #tag_profile_modal 숨김 생략.");
                }
                window.location.reload();
            });
            return res.rslt;
        });
    });
}

/**
 * 태그 프로필 삭제(Ajax).
 *
 * 변경 전: <code>dF.JournalDayTagProfileService.deleteProfileAjax</code>.
 * 변경 후: <code>dF.JournalDayTagService.deleteProfileAjax</code>. <code>cF.util.getInputValue</code>로 tagId·contentType 조회하는 방식 동일.
 */
function deleteTagProfileAjax(): void {
    const tagId: string = cF.util.getInputValue("#tagProfileForm [name='tagId']");
    const contentType: string = cF.util.getInputValue("#tagProfileForm [name='contentType']");
    if (isNaN(Number(tagId)) || cF.util.isEmpty(contentType)) return;

    Swal.fire({
        text: resolveMessage("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { tagId });
        cF.$ajax.delete(url, { contentType }, function(res: AjaxResponse): boolean {
            Swal.fire({ text: res.message }).then(function(): void {
                if (!res.rslt) return;
                if (typeof $ !== "undefined") {
                    $("#tag_profile_modal").modal("hide");
                } else {
                    console.warn("[journalDayTagService.deleteTagProfileAjax] jQuery 없음 — #tag_profile_modal 숨김 생략.");
                }
                window.location.reload();
            });
            return res.rslt;
        });
    });
}

/**
 * 일자 태그 목록 Ajax 파라미터(주간/월간).
 *
 * 변경 전: 월간에서 URL <code>yy</code>/<code>mnth</code> 및 <code>"9999"</code>/<code>"99"</code> 기본값 폴백.
 * 변경 후: <code>journal_day_tag_module.ts</code>의 <code>listAjax</code> 월간 분기와 동일 — Vue SSOT만 사용, 비면 <code>null</code> + <code>console.error</code>.
 * @returns {Record<string, any>|null}
 */
function resolveTagListParams(): Record<string, any> | null {
    if (getJournalDayListBridge()?.viewType === "weekly") {
        const weekStartDt: string = getCurrentWeekStartDt();
        if (cF.util.isEmpty(weekStartDt)) return null;
        return { weekStartDt };
    }

    const yy: string = String(getJournalDayListBridge()?.getSearchParams?.()?.yy ?? "").trim();
    const mnth: string = String(getJournalDayListBridge()?.getSearchParams?.()?.mnth ?? "").trim();
    if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) {
        console.error(
            "[journalDayTagService.resolveTagListParams] 월간 모드에서 yy/mnth가 Vue SSOT에 없음; URL·하드코드 폴백 제거. refreshDayTagList 생략."
        );
        return null;
    }
    return { yy, mnth };
}

async function refreshDayTagListAsync(): Promise<void> {
    const params = resolveTagListParams();
    if (!params) return;
    const list = await listTags(params);
    setTagListByVueBridge(list);
}

async function openProfileModal(tagId: string | number, contentType: string, name: string, ctgr: string = ""): Promise<void> {
    if (isNaN(Number(tagId)) || cF.util.isEmpty(contentType)) return;

    const profileObj = await getTagProfile(tagId, contentType);
    const viewModel: Record<string, any> = {
        ...(profileObj ?? {}),
        tagId: profileObj?.tagId ?? Number(tagId),
        tagCategoryId: profileObj?.tagCategoryId,
        contentType: profileObj?.contentType ?? contentType,
        name,
        ctgr: profileObj?.ctgr ?? ctgr ?? "",
        contentTypeLabel: getContentTypeLabel(contentType),
    };
    openProfileModalByVueBridge(viewModel);
}

function selectDayTag(tagId: string | number, name: string, ctgr: string = ""): void {
    /**
     * 변경 전: <code>dF.JournalDayTagContextMenu.openContextMenu</code> 직접 참조.
     * 변경 후: <code>dF.JournalDayTagContextMenu</code> 단일 경로 참조. 스크립트 미포함 시 상세만 연다.
     */
    const menu = dF.JournalDayTagContextMenu;
    if (menu?.openContextMenu) {
        menu.openContextMenu({
            tagId,
            name,
            ctgr,
            contentType: "JOURNAL_DAY",
            onSearch: function(): void {
                void openDayTagDetail(tagId, name);
            },
            onConfigure: function(): void {
                void openProfileModal(tagId, "JOURNAL_DAY", name, ctgr);
            },
        });
        return;
    }
    console.warn(
        "[journalDayTagService.selectDayTag] dF.JournalDayTagContextMenu 없음; 컨텍스트 메뉴 대신 태그 상세만 연다."
    );
    void openDayTagDetail(tagId, name);
}

async function openDayTagDetail(tagId: string | number, name: string, yy?: string | number): Promise<void> {
    if (typeof event !== "undefined" && event) event.stopPropagation();
    if (isNaN(Number(tagId))) return;

    ModalHistory.reset();

    const args: any[] = Array.from(arguments);
    if (getJournalDayListBridge()?.viewType === "weekly") {
        const weekStartDt: string = getCurrentWeekStartDt();
        const list = await getTagDetail(tagId, { weekStartDt });
        const openedByVue: boolean = openTagDetailByVueBridge({
            tagId,
            name,
            yy: weekStartDt,
            yearOptions: [{ value: weekStartDt, label: `Week ${weekStartDt}`, selected: true }],
            list,
            weekMode: true,
        });
        if (openedByVue) ModalHistory.push(journalDayTagService, "openDayTagDetail", args);
        return;
    }

    const preferredYy: string = getSelectedYy(yy);
    const yyList = await getYyList(tagId);
    const selectedYy: string = normalizeSelectedYy(preferredYy, yyList);
    const list = await getTagDetail(tagId, { yy: selectedYy });
    const openedByVue: boolean = openTagDetailByVueBridge({
        tagId,
        name,
        yy: selectedYy,
        yearOptions: getYearOptions(selectedYy, yyList).map((option: YearOption) => ({
            value: String(option.value),
            label: String(option.label),
            selected: option.selected,
        })),
        list,
        weekMode: false,
    });
    if (openedByVue) ModalHistory.push(journalDayTagService, "openDayTagDetail", args);
}

/**
 * Tagify 등에 넘기는 카테고리 맵(동기).
 *
 * 변경 전: 캐시가 비어 있으면 <code>dF.JournalDayTag.ctgrMap</code> 폴백.
 * 변경 후: <code>dayTagCategoryMap</code>만 반환. 레거시 Ajax는 <code>hydrateDayTagCategoryMap</code>으로 동기화한다.
 * @returns {Record<string, any>}
 */
function getDayTagCategoryMap(): Record<string, any> {
    return dayTagCategoryMap;
}

const journalDayTagService = {
    expandTaggedContent,
    getCtgrMap,
    getDayTagCategoryMap,
    hydrateDayTagCategoryMap,
    listAllTagsOpenModal: listAllTagsOpenModalAsync,
    openProfileModalByVueBridge,
    openTagDetailByVueBridge,
    openTagListModalByVueBridge,
    openDayTagDetail,
    openProfileModal,
    submitProfile: submitTagProfile,
    deleteProfileAjax: deleteTagProfileAjax,
    scheduleOpenDayTagDetail: openDayTagDetail,
    scheduleOpenProfileModal: openProfileModal,
    tagCtgrSyncAjax(): void {
        // 기존 dF.JournalDayTag.tagCtgrSyncAjax와 동일하게 현재 no-op 유지.
    },
    refreshDayTagList: refreshDayTagListAsync,
    setTagListByVueBridge,
    select: selectDayTag,
    selectDayTag,
};

dF.JournalDayTagService = journalDayTagService;
const pendingTagCtgrMap: Record<string, any> | undefined = window.__journalDayTagCategoryMapPendingHydrate;
if (pendingTagCtgrMap != null && typeof pendingTagCtgrMap === "object" && !Array.isArray(pendingTagCtgrMap)) {
    hydrateDayTagCategoryMap(pendingTagCtgrMap);
    delete window.__journalDayTagCategoryMapPendingHydrate;
}

export default journalDayTagService;
