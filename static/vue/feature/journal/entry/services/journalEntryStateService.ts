/**
 * journalEntryStateService.ts
 * 저널 entry 상태/라이프사이클/접힘/뷰모델 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-3-β):
 *   - journal_entry_module.ts 의 상태 메서드(`toggleStateAjax`, `collapseAjax`, `imprtcAjax`,
 *     `refrncAjax`, `nhtmrAjax`, `hallucAjax`, `setLifecycleAjax`, `resolveAjax`,
 *     `toggle`, `initCollapseState`, `buildViewModel`) 와 `hasState` 헬퍼를 본 서비스로 추출한다.
 *   - 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - 외부 호출자(HBS onclick / Vue 컴포넌트 / journalAnnualService(`dF.JournalAnnual`) / JournalEntrySearchApp)는
 *     기존 진입점(`dF.JournalEntry.get(ct).<method>(...)`)을 그대로 사용한다.
 *   - 서비스는 stateless. `dF.JournalEntry.getMeta(ct)` 로 meta(config) 조회.
 *   - 본 서비스는 글로벌(`dF.JournalEntryStateService`) 로도 노출되어 classic 모듈에서 호출 가능하다.
 *
 * @author nichefish
 */

import * as lifecycleService from "../../../attachable/lifecycle/lifecycleService.js";

type EntryMeta = Record<string, any>;
type EntryModule = Record<string, any>;
type StateOnOff = (res: AjaxResponse, item: HTMLElement) => void;

function getMeta(contentType: string): EntryMeta | undefined {
    return ((window as any).dF?.JournalEntry?.getMeta?.(contentType)) as EntryMeta | undefined;
}

function getModule(contentType: string): EntryModule | undefined {
    return ((window as any).dF?.JournalEntry?.get?.(contentType)) as EntryModule | undefined;
}

function getStateNs(): Record<string, any> | undefined {
    return ((window as any).dF?.State) as Record<string, any> | undefined;
}


/**
 * entry 가 특정 state(stateKey) 를 가지고 있는지 검사.
 * 변경 전: journal_entry_module.ts 모듈 스코프 const.
 */
export function hasState(entry: Record<string, any>, targetState: string): boolean {
    return Array.isArray(entry?.state?.list)
        && entry.state.list.some((state: any): boolean => state?.stateKey === targetState);
}

/**
 * 상태 토글 Ajax 공통 진입.
 * 변경 전: module.toggleStateAjax — dF.State.toggleAjax 호출 후 DOM 갱신 + onOffFunc.
 */
export function toggleStateAjax(
    contentType: string,
    id: string|number,
    stateKey: string,
    onOffFunc: StateOnOff,
): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    const stateNs = getStateNs();
    if (!meta || !stateNs) {
        console.error("[journalEntryStateService] meta/State namespace missing:", contentType);
        return;
    }

    const item = document.querySelector(`.${meta.itemClass}[data-id='${id}']`) as HTMLElement;
    const cacheContext = stateNs.resolveJournalCacheContext?.(item);
    const payload = { id, contentType, stateKey, cacheContext };
    stateNs.toggleAjax(payload, function(res: AjaxResponse): void {
        if (!item) return;
        const lowerStateKey: string = stateKey.toLowerCase();
        item.dataset[lowerStateKey] = res.rsltSts === "ON" ? "Y" : "N";
        const icon: HTMLElement = item.querySelector(`.icon-${lowerStateKey}`);
        icon?.classList.toggle("d-none", res.rsltSts !== "ON");
        const chk: HTMLInputElement = item.querySelector(`.${meta.cssPrefix}-context-${lowerStateKey}-check`);
        if (chk) chk.checked = res.rsltSts === "ON";
        onOffFunc(res, item);
    });
}

/**
 * 글접기(COLLAPSED) 상태 토글.
 * 변경 전: module.collapseAjax — toggleStateAjax 위임.
 */
export function collapseAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) return;

    const onOffFunc: StateOnOff = function(res: AjaxResponse, item: HTMLElement): void {
        const content: HTMLDivElement = item.querySelector(`div.${meta.contentClass} .journal-content`);
        if (!content) return console.warn("content not found.");

        content.classList.toggle("collapsed", res.rsltSts === "ON");
        item.classList.toggle("is-collapsed", res.rsltSts === "ON");
    };
    toggleStateAjax(contentType, id, "COLLAPSED", onOffFunc);
}

/**
 * 중요(IMPRTC) 상태 토글.
 */
export function imprtcAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) return;

    const onOffFunc: StateOnOff = function(res: AjaxResponse, item: HTMLElement): void {
        const wrapper: HTMLDivElement = item.querySelector(`div.${meta.contentClass}`);
        const content: HTMLDivElement = item.querySelector(`div.${meta.contentClass} .journal-content`);
        if (!content) return console.warn("content not found.");

        wrapper?.classList.remove("bg-secondary");
        content.classList.toggle("imprtc", res.rsltSts === "ON");
    };
    toggleStateAjax(contentType, id, "IMPRTC", onOffFunc);
}

/**
 * 참고(REFRNC) 상태 토글.
 */
export function refrncAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) return;

    const onOffFunc: StateOnOff = function(res: AjaxResponse, item: HTMLElement): void {
        const wrapper: HTMLDivElement = item.querySelector(`div.${meta.contentClass}`);
        const content: HTMLDivElement = item.querySelector(`div.${meta.contentClass} .journal-content`);
        if (!content) return console.warn("content not found.");

        wrapper?.classList.remove("bg-secondary");
        content.classList.toggle("refrnc", res.rsltSts === "ON");
    };
    toggleStateAjax(contentType, id, "REFRNC", onOffFunc);
}

/**
 * 악몽(NHTMR) 상태 토글 — DREAM 전용.
 */
export function nhtmrAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const onOffFunc: StateOnOff = function(res: AjaxResponse, item: HTMLElement): void {
        item.querySelector(".dream-nhtmr-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
    };
    toggleStateAjax(contentType, id, "NHTMR", onOffFunc);
}

/**
 * 환각/현시(HALLUC) 상태 토글 — DREAM 전용.
 */
export function hallucAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const onOffFunc: StateOnOff = function(res: AjaxResponse, item: HTMLElement): void {
        item.querySelector(".dream-halluc-badge")?.classList.toggle("d-none", res.rsltSts !== "ON");
    };
    toggleStateAjax(contentType, id, "HALLUC", onOffFunc);
}

/**
 * 라이프사이클(OPEN/PENDING/RESOLVED) 명시 설정.
 * 변경 전: module.setLifecycleAjax — dF.Lifecycle.setAjax 호출 후 DOM 일괄 갱신.
 *
 * RESOLVED 시점에는 글접기(collapsed) 까지 동기화한다(서버 영속화 파생 동작 화면 반영).
 *
 * @param contentType 저널 entry 컨텐츠 타입
 * @param id 저널 entry ID
 * @param lifecycleKey 설정할 라이프사이클 키
 */
export function setLifecycleAjax(contentType: string, id: string|number, lifecycleKey: string): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryStateService] meta missing:", contentType);
        return;
    }

    const item = document.querySelector(`.${meta.itemClass}[data-id='${id}']`) as HTMLElement;
    const cacheContext = lifecycleService.resolveJournalCacheContext(item);
    const payload = { id, contentType, lifecycleKey, cacheContext };
    lifecycleService.setAjax(payload, function(_res: AjaxResponse): void {
        if (!item) return;

        item.dataset.lifecycle = lifecycleKey;
        item.dataset.resolved = lifecycleKey === "RESOLVED" ? "Y" : "N";

        const idx: HTMLElement = item.querySelector(`.journal-${meta.cssPrefix}-idx`);
        idx?.classList.toggle("text-success", lifecycleKey === "RESOLVED");

        const resolvedChk: HTMLInputElement = item.querySelector(`.${meta.cssPrefix}-context-resolved-check`);
        if (resolvedChk) resolvedChk.checked = lifecycleKey === "RESOLVED";

        const lifecycleChecks: NodeListOf<HTMLInputElement> = item.querySelectorAll(`.${meta.cssPrefix}-context-lifecycle-check`);
        lifecycleChecks.forEach(function(chk: HTMLInputElement): void {
            chk.checked = chk.value === lifecycleKey;
            const label: HTMLElement = chk.closest("label")?.querySelector(".form-check-label") as HTMLElement;
            label?.classList.toggle("text-primary", chk.value === "PENDING" && chk.checked);
            label?.classList.toggle("text-success", chk.value === "RESOLVED" && chk.checked);
            label?.classList.toggle("text-muted", !chk.checked);
        });

        const resolvedLabel: HTMLElement = resolvedChk?.closest("label")?.querySelector(".form-check-label") as HTMLElement;
        resolvedLabel?.classList.toggle("text-success", lifecycleKey === "RESOLVED");
        resolvedLabel?.classList.toggle("text-muted", lifecycleKey !== "RESOLVED");

        if (lifecycleKey !== "RESOLVED") return;

        const content: HTMLDivElement = item.querySelector(`div.${meta.contentClass} .journal-content`);
        if (!content) console.warn("content not found.");
        content?.classList.add("collapsed");
        item.dataset.collapsed = "Y";
        item.classList.add("is-collapsed");

        const collapsedChk: HTMLInputElement = item.querySelector(`.${meta.cssPrefix}-context-collapsed-check`);
        if (collapsedChk) collapsedChk.checked = true;
        const icon: HTMLElement = item.querySelector(".icon-collapsed");
        icon?.classList.toggle("d-none", false);
    });
}

/**
 * 컨텍스트 메뉴 스위치 값에 따라 라이프사이클을 RESOLVED 또는 OPEN 으로 설정한다.
 * 변경 전: module.resolveAjax — setLifecycleAjax 위임.
 *
 * @param contentType 저널 entry 컨텐츠 타입
 * @param id 저널 entry ID
 * @param trigger 원하는 완료 여부를 나타내는 checkbox
 */
export function resolveAjax(contentType: string, id: string|number, trigger?: HTMLInputElement): void {
    if (isNaN(Number(id))) return;
    const lifecycleKey: string = trigger?.checked ? "RESOLVED" : "OPEN";
    setLifecycleAjax(contentType, id, lifecycleKey);
}

/**
 * 글접기 토글 (클라이언트 DOM/localStorage 만, 서버 상태 저장 없음).
 * 변경 전: module.toggle — DOM 클래스/아이콘/localStorage 갱신.
 */
export function toggle(contentType: string, id: string|number, trigger: HTMLElement): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) return;

    const item: HTMLElement = trigger.closest(`.${meta.itemClass}[data-id='${id}']`);
    if (!item) return console.log("item not found.");

    const content: HTMLElement = item.querySelector(`.${meta.contentClass} .journal-content`);
    if (!content) return console.log("content not found.");

    const icon: HTMLElement = meta.toggleIconSelector.startsWith("#")
        ? document.querySelector(`${meta.toggleIconSelector}${id}`)
        : item.querySelector(meta.toggleIconSelector);
    if (!icon) console.log("icon not found.");

    const collapsedIds = new Set(JSON.parse(localStorage.getItem(meta.storageKey) || "[]"));
    const isCollapsed: boolean = content.classList.contains("collapsed");
    if (isCollapsed) {
        content.classList.remove("collapsed");
        item.classList.remove("is-collapsed");
        icon?.classList.replace("bi-arrows-expand", "bi-arrows-collapse");
        collapsedIds.delete(String(id));
        collapsedIds.delete(id);
    } else {
        content.classList.add("collapsed");
        item.classList.add("is-collapsed");
        icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
        collapsedIds.add(String(id));
    }

    if (meta.plugin?.persistToggleToStorage) {
        localStorage.setItem(meta.storageKey, JSON.stringify(Array.from(collapsedIds)));
    }
}

/**
 * 페이지 진입 시 글접기 상태 복원 (localStorage 기준).
 * 변경 전: module.initCollapseState — 모든 항목 순회 후 클래스 적용.
 */
export function initCollapseState(contentType: string): void {
    const meta = getMeta(contentType);
    if (!meta) return;

    const collapsedIds = new Set(JSON.parse(localStorage.getItem(meta.storageKey) || "[]"));
    document.querySelectorAll(`.${meta.itemClass} .${meta.contentClass}`).forEach((item: HTMLElement): void => {
        const id: string = item.dataset.id;
        const content: HTMLElement = item.querySelector(".journal-content");
        const icon: HTMLElement = meta.toggleIconSelector.startsWith("#")
            ? document.querySelector(`${meta.toggleIconSelector}${id}`)
            : item.querySelector(meta.toggleIconSelector);
        if (!icon) console.log("icon not found.");
        if (id && collapsedIds.has(id)) {
            content?.classList.add("collapsed");
            item.closest(`.${meta.itemClass}`)?.classList.add("is-collapsed");
            icon?.classList.replace("bi-arrows-collapse", "bi-arrows-expand");
        }
    });
}

/**
 * Vue/HBS 렌더용 view-model 합성 (entry + profile + contentClass).
 * 변경 전: module.buildViewModel — 모듈 인스턴스의 PROFILE 사용.
 *
 * profile 은 모듈 인스턴스의 `PROFILE` 객체에서 조회한다.
 * 변경 후(E-3-β): module.PROFILE 노출은 모듈 표면에 그대로 두고, 서비스는 lookup 만 한다.
 */
export function buildViewModel(
    contentType: string,
    entry: Record<string, any>,
    profileName: string,
): Record<string, any> {
    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) {
        throw new Error(`[journalEntryStateService] meta/module missing: ${contentType}`);
    }

    const selectedProfile: any = module.PROFILE?.[profileName];
    if (!selectedProfile) throw new Error(`Unknown render profile: ${profileName}`);

    return {
        ...entry,
        view: selectedProfile,
        contentClass: [
            "journal-content",
            selectedProfile.collapsed && hasState(entry, "COLLAPSED")
                ? meta.plugin.collapseClass
                : null,
            hasState(entry, "IMPRTC") ? "imprtc" : null,
            hasState(entry, "REFRNC") ? "refrnc" : null,
        ].filter(Boolean).join(" "),
    };
}

/**
 * 글로벌 노출. classic `journal_entry_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalEntryStateService.<method>(...)` 로 호출 가능하도록 등록한다.
 */
const journalEntryStateService = {
    hasState,
    toggleStateAjax,
    collapseAjax,
    imprtcAjax,
    refrncAjax,
    nhtmrAjax,
    hallucAjax,
    setLifecycleAjax,
    resolveAjax,
    toggle,
    initCollapseState,
    buildViewModel,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalEntryStateService = journalEntryStateService;
})();

export default journalEntryStateService;
