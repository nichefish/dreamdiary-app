/**
 * journalAnnualStateService.ts
 * 저널 결산 Ajax 목록/태그/렌더 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-α):
 *   - journal_annual_module.ts 의 Ajax 목록/태그 렌더 계열 메서드(toggleParam/
 *     getAnnualDiaryListAjax/getAnnualDreamListAjax/getTagListAjax/renderEntryList/renderTagList)
 *     와 lazy entry/tag list config 캐시(buildEntryAndTagListConfigs)·내부 helper(renderToTarget)
 *     를 본 service 로 추출한다. 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 *   - 변경 이력(A-1) 보존: dF.JournalEntry 의 ES module 적재 후에야 entry/tag list config 가
 *     완성된다(annual_known_break). lazy 진입은 그대로 — entryListConfigsCache/tagListConfigsCache
 *     은 본 service 의 모듈 스코프에 둔다.
 *
 * 변경(A-7-γ):
 *   - `renderEntryList` 의 Handlebars 컴파일 경로 제거 — `JournalAnnualEntryListVueApp.setList(kind, list, config)`
 *     브리지로 Vue 반영. 내부 helper `renderToTarget` 도 함께 삭제(본 서비스에서 Handlebars 직호출 0 건).
 *
 * 변경(D):
 *   - `tagListConfigsCache` 빌드 시점의 `Message.get(...)` 직호출을 `resolveMessage` 헬퍼로 통일.
 *     함수 호출 시점이라 module top-level 보다는 race 위험이 낮으나, 글로벌 `Message` 결의 일관성을 위해 통일.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

function getDfNs(): Record<string, any> {
    return (window as any).dF ?? {};
}

let entryListConfigsCache: Record<string, Record<string, any>> | null = null;
let tagListConfigsCache: Record<string, Record<string, any>> | null = null;

/**
 * DIARY/DREAM entry 리스트·태그 리스트용 설정을 한 번만 구성한다.
 * 변경(A-1) 보존: `JournalEntry` 접근을 호출 시점으로 지연 — defer ES module 등록 이후에만 호출된다.
 */
function buildEntryAndTagListConfigs(): void {
    if (entryListConfigsCache != null && tagListConfigsCache != null) return;

    const dfNs = getDfNs();
    const je = dfNs.JournalEntry as { getMeta?: (ct: string) => Record<string, any> } | undefined;
    if (je == null || typeof je.getMeta !== "function") {
        console.error("[journalAnnualStateService] dF.JournalEntry 미등록 — entry ES module 적재 후에 재시도되거나 페이지 스크립트 순서를 확인한다.");
        return;
    }

    const createEntryListConfig = function(contentType: string, targetId: string, overrides: Record<string, any> = {}): Record<string, any> {
        const meta: Record<string, any> = je.getMeta!(contentType);
        return {
            targetId,
            contentType,
            module: meta.moduleExpr,
            tagModule: meta.tagModuleExpr,
            contentLabel: meta.contentLabel,
            emptyLabel: meta.emptyLabel,
            cssPrefix: meta.cssPrefix,
            iconIdPrefix: meta.iconIdPrefix,
            highlightImportant: meta.highlightImportant,
            showDreamStates: meta.hasDreamStates,
            ...overrides,
        };
    };

    entryListConfigsCache = {
        DIARY: createEntryListConfig("JOURNAL_DIARY", "journal_annual_diary_list_div", {
            contentPaddingClass: "p-2",
            collapse: "collapse-4",
        }),
        DREAM: createEntryListConfig("JOURNAL_DREAM", "journal_annual_imprtc_dream_list_div", {
            contentPaddingClass: "p-3",
            contextFirst: true,
        }),
    };

    tagListConfigsCache = {
        DAY: {
            targetId: "journal_annual_day_tag_list_div",
            label: resolveMessage("txt.day.tag"),
            /* 변경 후: 결산 목록 렌더는 <code>dF.JournalDayTagService</code> 문자열을 사용한다. */
            module: "dF.JournalDayTagService",
            tagListDivId: "journal_day_tag_list_div",
        },
        DIARY: {
            targetId: "journal_annual_diary_tag_list_div",
            label: resolveMessage("txt.diary.tag"),
            module: je.getMeta("JOURNAL_DIARY").tagModuleExpr,
            tagListDivId: je.getMeta("JOURNAL_DIARY").tagListTargetId,
        },
        DREAM: {
            targetId: "journal_annual_dream_tag_list_div",
            label: resolveMessage("txt.dream.tag"),
            module: je.getMeta("JOURNAL_DREAM").tagModuleExpr,
            tagListDivId: je.getMeta("JOURNAL_DREAM").tagListTargetId,
        },
    };
}

function getEntryListConfigs(): Record<string, Record<string, any>> {
    buildEntryAndTagListConfigs();
    return entryListConfigsCache ?? {};
}

function getTagListConfigs(): Record<string, Record<string, any>> {
    buildEntryAndTagListConfigs();
    return tagListConfigsCache ?? {};
}

/* 변경(A-7-γ): `renderToTarget` 제거 — 본 서비스의 마지막 `cF.handlebars.compile` 사용처였던
 * `renderEntryList` 가 `JournalAnnualEntryListVueApp.setList` 브리지로 수렴했다. tag header 도 A-7-δ 에서
 * 동일 패턴으로 전환 완료. 본 서비스에서 Handlebars 직호출은 0건이다. */

/**
 * URL 파라미터로부터 파라미터 객체 초기화 → 섹션별 재조회
 * 변경 전: journal_annual_module.toggleParam.
 */
export function toggleParam(): void {
    const showImprtc: boolean = $("#toggleImprtc").is(":checked");
    const showRefrnc: boolean = $("#toggleRefrnc").is(":checked");

    // URL 동기화
    const url = new URL(window.location.href);
    url.searchParams.set("showImprtc", String(showImprtc));
    url.searchParams.set("showRefrnc", String(showRefrnc));
    window.history.replaceState(null, "", url.toString());

    // 재조회
    const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
    const section: string = cF.util.getUrlParam("section");
    switch (section) {
        case "DIARY":
            getAnnualDiaryListAjax(yy);
            break;
        case "DREAM":
            getAnnualDreamListAjax(yy);
            break;
    }
}

/**
 * 중요 일기 목록 조회 (Ajax) (년도로 조회)
 * 변경 전: journal_annual_module.getAnnualDiaryListAjax.
 */
export function getAnnualDiaryListAjax(yy: string|number): void {
    const showImprtc: boolean = $("#toggleImprtc").is(":checked");
    const showRefrnc: boolean = $("#toggleRefrnc").is(":checked");

    // 둘 다 false → 조회 의미 없음
    if (!showImprtc && !showRefrnc) {
        renderEntryList([], "DIARY");
        return;
    }

    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_DIARIES, { yy });
    const ajaxData: Record<string, any> = {
        showImprtc,
        showRefrnc
    };
    cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }

        /**
         * 변경(E-4-α) 보존: dfNs.JournalEntry.get("JOURNAL_DIARY").buildViewModel(...) 진입 → 서비스 직접 호출.
         * 의도: journal_entry_module 의 thin wrapper 경유 의존을 끊어, E-4-γ 모듈 절제 시 annual 측 호출 그래프가 영향받지 않게 한다.
         * 동작은 동일하다(thin wrapper 도 동일 service 를 호출했음).
         */
        const dfNs = getDfNs();
        const viewModels: any[] = res.rsltList.map((diary: any): void =>
            dfNs.JournalEntryStateService.buildViewModel("JOURNAL_DIARY", diary, "ANNUAL")
        );
        renderEntryList(viewModels, "DIARY");
        document.querySelectorAll(".journal-content.collapsed").forEach(el => el.classList.remove("collapsed"));
        KTMenu.createInstances();
    });
}

/**
 * 중요 꿈 목록 조회 (Ajax) (년도로 조회)
 * 변경 전: journal_annual_module.getAnnualDreamListAjax.
 */
export function getAnnualDreamListAjax(yy: string|number): void {
    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_DREAMS, { yy });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }

        /**
         * 변경(E-4-α) 보존: dfNs.JournalEntry.get("JOURNAL_DREAM").buildViewModel(...) 진입 → 서비스 직접 호출.
         */
        const dfNs = getDfNs();
        const viewModels: any[] = res.rsltList.map((dream: any): void =>
            dfNs.JournalEntryStateService.buildViewModel("JOURNAL_DREAM", dream, "ANNUAL")
        );
        renderEntryList(viewModels, "DREAM");
        document.querySelectorAll(".journal-content.collapsed").forEach(el => el.classList.remove("collapsed"));
        KTMenu.createInstances();
    });
}

/**
 * 엔트리 리스트 렌더
 * 변경 전: journal_annual_module.renderEntryList.
 * 변경(A-7-γ): `cF.handlebars.compile` / `renderToTarget` 제거 —
 *   `JournalAnnualEntryListVueApp.setList(kind, list, config)` 브리지로 Vue 반영.
 *   브리지가 아직 마운트되지 않았으면 `pendingByType` 에 큐잉(`setList` 호출 시점 vs `DOMContentLoaded` 경합 보정).
 */
export function renderEntryList(list: Record<string, any>[] = [], type: "DIARY"|"DREAM"): void {
    const config: Record<string, any> | undefined = getEntryListConfigs()[type];
    if (config == null) {
        console.error("[journalAnnualStateService] entry list config 없음(type=", type, ") — JournalEntry 준비 여부 확인.");
        return;
    }
    const safeList: Record<string, any>[] = Array.isArray(list) ? list : [];
    const bridge = (window as any).JournalAnnualEntryListVueApp as {
        mounted?: boolean;
        setList?: (kind: "DIARY"|"DREAM", list: Record<string, any>[], config: Record<string, any>) => void;
        pendingByType?: Partial<Record<"DIARY"|"DREAM", { list: Record<string, any>[]; config: Record<string, any> }>>;
    } | undefined;
    if (bridge?.mounted === true && typeof bridge.setList === "function") {
        bridge.setList(type, safeList, config);
        return;
    }
    if (bridge) {
        bridge.pendingByType = bridge.pendingByType ?? {};
        bridge.pendingByType[type] = { list: safeList, config };
        console.log("[journalAnnualStateService.renderEntryList] JournalAnnualEntryListVueApp pending:", type);
        return;
    }
    console.error("[journalAnnualStateService.renderEntryList] JournalAnnualEntryListVueApp unavailable — 적재 순서 확인.");
}

/**
 * 태그 목록 조회 (Ajax) (년도로 조회)
 * 변경 전: journal_annual_module.getTagListAjax.
 */
export function getTagListAjax(yy: string|number, type: "DAY"|"DIARY"|"DREAM"): void {
    const url: string = cF.util.bindUrl(Url.JOURNAL_ANNUAL_TAGS, { yy }) + `?type=${type}`;
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const { rsltList = [] } = res;
        renderTagList(rsltList, type);
    });
}

/**
 * 태그 리스트 렌더
 * 변경 전: journal_annual_module.renderTagList.
 * 변경(A-7-δ): `cF.handlebars.compile` 제거 — `JournalAnnualEntryTagListVueApp.applyTagRow` 브리지로 Vue 반영.
 */
export function renderTagList(list: Record<string, any>[] = [], type: "DAY"|"DIARY"|"DREAM"): void {
    const config: Record<string, any> | undefined = getTagListConfigs()[type];
    if (config == null) {
        console.error("[journalAnnualStateService] tag list config 없음(type=", type, ") — JournalEntry 준비 여부 확인.");
        return;
    }
    const payload: Record<string, any> = { list, ...config };
    const bridge = (window as any).JournalAnnualEntryTagListVueApp as {
        mounted?: boolean;
        applyTagRow?: (kind: "DAY"|"DIARY"|"DREAM", p: Record<string, any>) => void;
        pendingByType?: Partial<Record<"DAY"|"DIARY"|"DREAM", Record<string, any>>>;
    } | undefined;
    if (bridge?.mounted === true && typeof bridge.applyTagRow === "function") {
        bridge.applyTagRow(type, payload);
        return;
    }
    if (bridge) {
        bridge.pendingByType = bridge.pendingByType ?? {};
        bridge.pendingByType[type] = payload;
        console.log("[journalAnnualStateService.renderTagList] JournalAnnualEntryTagListVueApp pending:", type);
        return;
    }
    console.error("[journalAnnualStateService.renderTagList] JournalAnnualEntryTagListVueApp unavailable — 적재 순서 확인.");
}

/**
 * 글로벌 노출. classic `journal_annual_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalAnnualStateService.<method>(...)` 로 호출한다.
 */
const journalAnnualStateService = {
    toggleParam,
    getAnnualDiaryListAjax,
    getAnnualDreamListAjax,
    renderEntryList,
    getTagListAjax,
    renderTagList,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalAnnualStateService = journalAnnualStateService;
})();

export default journalAnnualStateService;
