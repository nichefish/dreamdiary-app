/**
 * journalInterpretationStateService.ts
 * 저널 해석 상태/라이프사이클/접힘 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(I-3):
 *   - journal_interpretation_module.ts 의 상태 계열 메서드(resolveAjax/setLifecycleAjax/collapse/toggle/initCollapseState)
 *     를 본 서비스로 추출한다. 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - STORAGE_KEY 문자열은 `dF.JournalInterpretation.STORAGE_KEY` 를 읽는다(모듈이 부트 시 부여한 값과 단일 축).
 *
 * @author nichefish
 */

import * as lifecycleService from "../../../attachable/lifecycle/lifecycleService.js";

function getDfNs(): Record<string, any> {
    return (window as any).dF ?? {};
}

function getStorageKey(): string {
    const k = getDfNs()?.JournalInterpretation?.STORAGE_KEY;
    return typeof k === "string" ? k : "collapsedJournalInterpretationIds";
}


/**
 * 컨텍스트 메뉴 스위치 값에 따라 해석 라이프사이클을 RESOLVED 또는 OPEN으로 설정한다.
 *
 * 완료 진행 상태는 라이프사이클 endpoint가 소유한다. 이 메소드는 보이는 항목을 즉시 갱신하고,
 * 완료 상태일 때 글접기 표시를 적용한다.
 *
 * 변경 전: journal_interpretation_module.resolveAjax — this.setLifecycleAjax 호출.
 * 변경 후(I-3): setLifecycleAjax 직접 호출(strict ES module 에서 this 바인딩 제거).
 *
 * @param id interpretation ID
 * @param trigger 원하는 완료 여부를 나타내는 checkbox
 */
export function resolveAjax(id: string|number, trigger?: HTMLInputElement): void {
    if (isNaN(Number(id))) return;

    const lifecycleKey: string = trigger?.checked ? "RESOLVED" : "OPEN";
    setLifecycleAjax(id, lifecycleKey);
}

/**
 * 해석 라이프사이클을 명시적으로 설정한다.
 *
 * 변경 전: journal_interpretation_module.setLifecycleAjax.
 */
export function setLifecycleAjax(id: string|number, lifecycleKey: string): void {
    if (isNaN(Number(id))) return;

    const content = document.querySelector(`.journal-interpretation-content[data-id='${id}']`) as HTMLElement;
    const item = content?.closest(".journal-interpretation-item") as HTMLElement;

    const cacheContext = lifecycleService.resolveJournalCacheContext(content);
    const payload = { id, contentType: "JOURNAL_INTERPRETATION", lifecycleKey, cacheContext };
    lifecycleService.setAjax(payload, function(_res: AjaxResponse): void {
        if (!content) return;

        content.dataset.lifecycle = lifecycleKey;
        content.dataset.resolved = lifecycleKey === "RESOLVED" ? "Y" : "N";

        const idx: HTMLElement = item?.querySelector(".col-1 span") as HTMLElement;
        idx?.classList.toggle("text-success", lifecycleKey === "RESOLVED");

        const resolvedChk: HTMLInputElement = item?.querySelector(".interpretation-context-resolved-check") as HTMLInputElement;
        if (resolvedChk) resolvedChk.checked = lifecycleKey === "RESOLVED";

        const lifecycleChecks: NodeListOf<HTMLInputElement> = item?.querySelectorAll(".interpretation-context-lifecycle-check") as NodeListOf<HTMLInputElement>;
        lifecycleChecks?.forEach(function(chk: HTMLInputElement): void {
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
        const journalContent: HTMLElement = content.querySelector(".journal-content") as HTMLElement;
        journalContent?.classList.add("collapsed");
    });
}

/**
 * @param id - 글 번호.
 * @param collapsedYn - 글접기 여부.
 *
 * 변경 전: journal_interpretation_module.collapse.
 */
export function collapse(id: string|number, collapsedYn: "Y"|"N"): void {
    if (isNaN(Number(id))) return;

    const url: string = Url.JOURNAL_INTERPRETATION_SET_COLLAPSE_AJAX;
    const ajaxData: Record<string, any> = { id, collapsedYn };
    cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) return;

        // 찾아서 해당 그것만 collapse 추가 제거.
        const item: HTMLElement = document.querySelector(`.journal-interpretation-content[data-id='${id}']`);
        if (!item) return console.log("item not found.");

        const content: HTMLElement = item.querySelector(".journal-content");
        if (!content) return console.log("content not found.");

        if (collapsedYn === "Y") {
            content.classList.add("collapsed");
        } else {
            content.classList.remove("collapsed");
        }
    }, "block");
}

/**
 * 접기/펼치기 토글
 *
 * 변경 전: journal_interpretation_module.toggle.
 */
export function toggle(id: string|number, trigger: HTMLElement): void {
    if (isNaN(Number(id))) return;

    const item: HTMLElement = trigger.closest(`.journal-interpretation-item[data-id='${id}']`);
    if (!item) return console.log("item not found.");

    const content: HTMLElement = item.querySelector(".journal-interpretation-content .journal-content");
    if (!content) return console.log("content not found.");

    const icon: HTMLElement = document.querySelector(`#interpretation-toggle-icon-${id}`);
    if (!icon) console.log("icon not found.");
    const storageKey = getStorageKey();
    const collapsedIds = new Set(JSON.parse(localStorage.getItem(storageKey) || "[]"));

    const isCollapsed: boolean = content.classList.contains("collapsed");
    if (isCollapsed) {
        content.classList.remove("collapsed");
        icon?.classList.replace("bi-chevron-down", "bi-chevron-up");
        collapsedIds.delete(id);
    } else {
        content.classList.add("collapsed");
        icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
        collapsedIds.add(id);
    }

    localStorage.setItem(storageKey, JSON.stringify(Array.from(collapsedIds)));
}

/**
 * 접힌 엔트리 초기화
 *
 * 변경 전: journal_interpretation_module.initCollapseState.
 */
export function initCollapseState(): void {
    const storageKey = getStorageKey();
    const collapsedIds = new Set(JSON.parse(localStorage.getItem(storageKey) || "[]"));
    document.querySelectorAll(".journal-interpretation-item .journal-interpretation-content").forEach((item: HTMLElement): void => {
        const id: string = item.dataset.id;
        const content: HTMLElement = item.querySelector(".journal-content");
        const icon: HTMLElement = document.querySelector(`#interpretation-toggle-icon-${id}`);
        if (!icon) console.log("icon not found.");
        if (id && collapsedIds.has(id)) {
            content?.classList.add("collapsed");
            icon?.classList.replace("bi-chevron-up", "bi-chevron-down");
        }
    });
}

/**
 * 글로벌 노출. classic `journal_interpretation_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalInterpretationStateService.<method>(...)` 로 호출한다.
 */
const journalInterpretationStateService = {
    resolveAjax,
    setLifecycleAjax,
    collapse,
    toggle,
    initCollapseState,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalInterpretationStateService = journalInterpretationStateService;
})();

export default journalInterpretationStateService;
