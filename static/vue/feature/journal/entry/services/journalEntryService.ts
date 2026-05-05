/**
 * journalEntryService.ts
 * 저널 entry 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-4-δ-1):
 *   - classic `static/js/view/feature/journal/entry/journal_entry_module.ts` 를 본 service 로 이전한다.
 *   - 외부 호출 시그니처 보존:
 *     · `dF.JournalEntry.<root method>` (`get / getMeta / getContentTypes /
 *        getTaggableContentTypes / getSearchPopupContentTypes / initAll /
 *        bindSearchPopupEnterKeys / resolveJournalDayPreviewWidth`)
 *     · `dF.JournalEntry.get(ct).<instance method>` (init / refresh / initForm /
 *        resolveChapterList / resolveDreamChapterList / regModal / submit /
 *        preview / submitHandler / regAjax / mdfModal / delAjax /
 *        copy / searchPopup / openRegModalWithDayContext /
 *        createDreamChapterAndOpenModal / toggleStateAjax / collapseAjax /
 *        resolveAjax / setLifecycleAjax / imprtcAjax / refrncAjax / nhtmrAjax /
 *        hallucAjax / toggle / initCollapseState / buildViewModel)
 *     · 변경 이력 비고: I-5 — dtlModal 인스턴스 메서드와 meta `detailModalKey` 두 개(diary/dream) 정식 제거.
 *       이전 표기에 포함되어 있던 dtlModal 은 dead 경로 정리.
 *     · 인스턴스 필드(`inKeywordSearchMode` 등) 직접 read/write 도 동일 유지
 *       — `journalDayListAppMount.ts` / `JournalDayAsideYyMnthApp.ts` 가 직접 쓴다.
 *   - 인스턴스 메서드 중 CRUD/상태/검색 위임 thin wrapper(`dfNs.JournalEntryStateService` /
 *     `JournalEntryCrudService` / `JournalEntrySearchPopupService`) 는 그대로 보존한다.
 *     화면 수명주기·폼 브리지·챕터 해석 shell 은 `journalEntryShellService`(E-4-δ-2) 로 분리했다.
 *   - 적재 순서:
 *     · 변경(E-4-δ-1): `_journal_entry_module_script.ftlh` 의 classic
 *       `<script type="text/javascript">` 라인을 본 ES module(`type="module"`) 적재로 교체했다.
 *     · 변경(E-4-δ-3): 8개 ftlh 의 partial include 를 제거하고, `_journal_entry_reg_modal.ftlh`
 *       의 `journalEntryRegVueScriptDone` 가드 안으로 적재를 단일 수렴했다.
 *     · ES module 은 `defer` 와 동등 평가이므로 페이지의 `Url` / `Message` / `cF` 등
 *       글로벌이 등록된 이후에 평가된다(기존 classic 시점 이후).
 *
 * 변경(E-4-δ-2):
 *   - 인스턴스 전용 로직(`init`/`refresh`/`initForm`/`resolveChapterList`) 은
 *     `journalEntryShellService.ts` 로 추출했다. 본 파일은 동일 시그니처 thin wrapper 만 유지한다.
 *
 * @author nichefish
 */

import {
    openJournalEntryRegForm,
    resolveJournalEntryChapterList,
    runJournalEntryInit,
    runJournalEntryRefresh,
} from "./journalEntryShellService.js";

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

const profile = {
    LIST: { collapsed: true },
    TAG: { collapsed: false },
    SEARCH: { collapsed: false },
    ANNUAL: { collapsed: false },
};

/* 변경(E-3-α): closeOpenModals 헬퍼는 journalEntryCrudService 로 이전. 모듈 내부에서는 더 이상 사용하지 않는다. */
/* 변경(E-3-β): hasState 헬퍼는 journalEntryStateService 로 이전. 모듈 내부에서는 더 이상 사용하지 않는다. */
/* 변경(E-3-γ): resolveSearchUrl 헬퍼는 journalEntrySearchPopupService 로 이전. 모듈 내부에서는 더 이상 사용하지 않는다. */

/**
 * 타입별 행동 플러그인.
 * create() 내부에서 contentType 분기 대신 이 플러그인을 호출한다.
 */
interface JournalEntryPlugin {
    /** 폼 검증 및 타입 전용 UI 초기화. */
    setupFormValidation(config: Record<string, any>, module: dfModule): void;
    /** 접힘 상태에 사용할 CSS 클래스. */
    collapseClass: string;
    /** toggle 시 localStorage에 접힘 ID를 저장할지 여부. */
    persistToggleToStorage: boolean;
    /** copy 시 제목 라인 추출 (없으면 빈 문자열 반환). */
    extractTitleLine(rsltObj: Record<string, any>): string;
}

const diaryPlugin: JournalEntryPlugin = {
    setupFormValidation(config: Record<string, any>, module: dfModule): void {
        cF.validate.validateForm(config.formSelector, module.submitHandler);
    },
    collapseClass: "collapsed",
    persistToggleToStorage: false,
    extractTitleLine(_rsltObj: Record<string, any>): string { return ""; },
};

const dreamPlugin: JournalEntryPlugin = {
    setupFormValidation(config: Record<string, any>, module: dfModule): void {
        cF.validate.validateForm(config.formSelector, module.submitHandler, {
            rules: {
                elseDreamerNm: {
                    required(): boolean {
                        return $(`${config.formSelector} #elseDreamYn`).is(":checked");
                    },
                },
            },
            ignore: undefined,
        });
        $("#elseDreamYn").change(function(): void {
            $("#elseDreamerNm").valid();
        });
        cF.ui.chckboxLabel(`${config.formSelector} #imprtcYn`, "중요//해당없음", "red//gray");
        cF.ui.chckboxLabel(`${config.formSelector} #elseDreamYn`, "해당//미해당", "blue//gray", function(): void {
            $("#elseDreamerNmDiv").removeClass("d-none");
        }, function(): void {
            $("#elseDreamerNmDiv").addClass("d-none");
        });
    },
    collapseClass: "collapsed collapse-4",
    persistToggleToStorage: true,
    extractTitleLine(_rsltObj: Record<string, any>): string { return ""; },
};


const configs: Record<string, Record<string, any>> = {
    JOURNAL_DIARY: {
        moduleName: "JournalEntry[JOURNAL_DIARY]",
        contentType: "JOURNAL_DIARY",
        entryType: "DIARY",
        moduleExpr: "dF.JournalEntry.get('JOURNAL_DIARY')",
        tagModuleExpr: "dF.JournalEntryTag.get('JOURNAL_DIARY')",
        contentLabel: Message.get("txt.journal.diary"),
        emptyLabel: Message.get("txt.journal.diary"),
        chapterType: null,
        listUrl: Url.JOURNAL_ENTRIES,
        itemUrl: Url.JOURNAL_ENTRY,
        searchUrl: Url.JOURNAL_DIARY_SEARCH,
        searchInputSelector: "#diarySearchKeyword",
        exportUrl: Url.JOURNAL_ENTRIES_EXPORT,
        tagCtgrMapUrl: Url.JOURNAL_ENTRY_TAG_CTGR_MAP,
        tagsUrl: Url.JOURNAL_ENTRY_TAGS,
        tagListTargetId: "journal_diary_tag_list_div",
        popupName: "diary search",
        modalKey: "journal_diary_reg",
        /* 변경(I-5): detailModalKey "journal_diary_dtl" 제거 — 미존재 HBS 호출(dead) 이었던 dtlModal 본체가 함께 제거됨. */
        formSelector: "#journalDiaryRegForm",
        tinymceId: "tinymce_journalDiaryCn",
        tagInputSelector: "#journalDiaryRegForm #tagListStr",
        itemClass: "journal-diary-item",
        contentClass: "journal-diary-content",
        cssPrefix: "diary",
        toggleIconSelector: ".diary-toggle-icon",
        storageKey: "collapsedJournalDiaryIds",
        rightBorderClass: "ms-4",
        useTag: true,
        plugin: diaryPlugin,
    },
    JOURNAL_DREAM: {
        moduleName: "JournalEntry[JOURNAL_DREAM]",
        contentType: "JOURNAL_DREAM",
        entryType: "DREAM",
        moduleExpr: "dF.JournalEntry.get('JOURNAL_DREAM')",
        tagModuleExpr: "dF.JournalEntryTag.get('JOURNAL_DREAM')",
        contentLabel: Message.get("txt.journal.dream"),
        emptyLabel: Message.get("txt.journal.dream"),
        chapterType: "DREAM",
        listUrl: Url.JOURNAL_ENTRIES,
        itemUrl: Url.JOURNAL_ENTRY,
        searchUrl: Url.JOURNAL_DREAM_SEARCH,
        searchInputSelector: "#dreamSearchKeyword",
        exportUrl: Url.JOURNAL_ENTRIES_EXPORT,
        tagCtgrMapUrl: Url.JOURNAL_ENTRY_TAG_CTGR_MAP,
        tagsUrl: Url.JOURNAL_ENTRY_TAGS,
        tagListTargetId: "journal_dream_tag_list_div",
        popupName: "dream search",
        modalKey: "journal_dream_reg",
        /* 변경(I-5): detailModalKey "journal_dream_dtl" 제거 — 미존재 HBS 호출(dead) 이었던 dtlModal 본체가 함께 제거됨. */
        formSelector: "#journalDreamRegForm",
        tinymceId: "tinymce_journalDreamCn",
        tagInputSelector: "#journalDreamRegForm #tagListStr",
        itemClass: "journal-dream-item",
        contentClass: "journal-dream-content",
        cssPrefix: "dream",
        toggleIconSelector: "#dream-toggle-icon-",
        iconIdPrefix: "dream-toggle-icon-",
        storageKey: "collapsedJournalDreamIds",
        useTag: true,
        autoCreateChapterUrl: Url.JOURNAL_CHAPTER_DREAM_AUTO,
        hideSortWhenElseDream: true,
        hasDreamStates: true,
        highlightImportant: true,
        plugin: dreamPlugin,
    },
};

const create = function(config: Record<string, any>): dfModule {
    const module: dfModule = {
        STORAGE_KEY: config.storageKey,
        PROFILE: profile,
        profile: null,
        initialized: false,
        initPromise: null,
        inKeywordSearchMode: false,
        tagify: null,
        submitMode: "",

        init: async function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): Promise<void> {
            return runJournalEntryInit(this, config, viewType);
        },

        refresh: function(): void {
            runJournalEntryRefresh(this, config);
        },

        initForm: function(obj: Record<string, any> = {}): void {
            openJournalEntryRegForm(config, obj);
        },

        resolveChapterList: function(day: Record<string, any> = {}): Record<string, any>[] {
            return resolveJournalEntryChapterList(config, day);
        },

        resolveDreamChapterList: function(day: Record<string, any> = {}): Record<string, any>[] {
            return resolveJournalEntryChapterList(config, day);
        },

        /**
         * 변경(E-3-α): 본 메서드들은 journalEntryCrudService 로 이전되었다.
         * 모듈 표면(`dF.JournalEntry.get(ct).<method>(...)`)은 외부 호출 시그니처 보존을 위해
         * 동일 인자/반환 형태로 service 진입만 한다. 호출 그래프(HBS onclick / Vue / related_content) 보존.
         */
        createDreamChapterAndOpenModal: function(
            journalDayId: string|number,
            stdrdDt: string,
            journalDateWeekDay: string,
            onReady?: () => void
        ): void {
            dfNs.JournalEntryCrudService?.createDreamChapterAndOpenModal?.(
                config.contentType, journalDayId, stdrdDt, journalDateWeekDay, onReady,
            );
        },

        openRegModalWithDayContext: function(
            journalDayId: string|number,
            journalChapterId: string|number|undefined,
            stdrdDt: string,
            journalDateWeekDay: string,
            onReady?: () => void,
            initialObj: Record<string, any> = {}
        ): void {
            dfNs.JournalEntryCrudService?.openRegModalWithDayContext?.(
                config.contentType, journalDayId, journalChapterId, stdrdDt, journalDateWeekDay, onReady, initialObj,
            );
        },

        /**
         * 변경(E-3-γ): 본 메서드는 journalEntrySearchPopupService 로 이전되었다.
         * 모듈 표면(`dF.JournalEntry.get(ct).searchPopup()`) 은 외부 호출 시그니처 보존을 위해
         * 동일 인자/반환 형태로 service 진입만 한다. 호출 그래프(HBS onclick) 보존.
         */
        searchPopup: function(): void {
            dfNs.JournalEntrySearchPopupService?.searchPopup?.(config.contentType);
        },

        regModal: function(params: {
            journalDayId: string|number;
            journalChapterId?: string|number;
            stdrdDt: string;
            journalDateWeekDay: string;
        }): void {
            dfNs.JournalEntryCrudService?.regModal?.(config.contentType, params);
        },

        submit: function(): void {
            dfNs.JournalEntryCrudService?.submit?.(config.contentType);
        },

        preview: function(): void {
            dfNs.JournalEntryCrudService?.preview?.(config.contentType);
        },

        submitHandler: function(): boolean {
            return dfNs.JournalEntryCrudService?.submitHandler?.(config.contentType) ?? false;
        },

        regAjax: function(): void {
            dfNs.JournalEntryCrudService?.regAjax?.(config.contentType);
        },

        /* (변경 I-5) dtlModal thin wrapper 제거됨.
         *   - 변경 전: dfNs.JournalEntryCrudService.dtlModal 위임. 본체가 미존재 HBS 호출(dead) 이었고 외부 활성 호출자 0.
         *   - related_content_module.openTarget 은 가드(typeof === "function") + mdfModal 폴백을 보유하므로 부재 시 자연 폴백.
         */
        mdfModal: function(id: string|number): void {
            dfNs.JournalEntryCrudService?.mdfModal?.(config.contentType, id);
        },

        delAjax: function(id: string|number): void {
            dfNs.JournalEntryCrudService?.delAjax?.(config.contentType, id);
        },

        /**
         * 변경(E-3-β): 본 메서드들은 journalEntryStateService 로 이전되었다.
         * 모듈 표면(`dF.JournalEntry.get(ct).<method>(...)`) 은 외부 호출 시그니처 보존을 위해
         * 동일 인자/반환 형태로 service 진입만 한다. 호출 그래프(HBS onclick / Vue / annual / search) 보존.
         */
        toggleStateAjax: function(id: string|number, stateKey: string, { onOffFunc }: { onOffFunc: Function }): void {
            dfNs.JournalEntryStateService?.toggleStateAjax?.(config.contentType, id, stateKey, onOffFunc);
        },

        collapseAjax: function(id: string|number): void {
            dfNs.JournalEntryStateService?.collapseAjax?.(config.contentType, id);
        },

        /**
         * 컨텍스트 메뉴 스위치 값에 따라 일기 라이프사이클을 RESOLVED 또는 OPEN으로 설정한다.
         *
         * RESOLVED는 더 이상 상태 토글이 아니다. 이 스위치는 라이프사이클을 저장하고,
         * 백엔드가 영속화하는 글접기 파생 동작을 화면에도 즉시 반영한다.
         *
         * @param id journal entry ID
         * @param trigger 원하는 완료 여부를 나타내는 checkbox
         */
        resolveAjax: function(id: string|number, trigger?: HTMLInputElement): void {
            dfNs.JournalEntryStateService?.resolveAjax?.(config.contentType, id, trigger);
        },

        /**
         * 일기 라이프사이클을 명시적으로 설정한다.
         *
         * @param id 저널 entry ID
         * @param lifecycleKey 설정할 라이프사이클 키
         */
        setLifecycleAjax: function(id: string|number, lifecycleKey: string): void {
            dfNs.JournalEntryStateService?.setLifecycleAjax?.(config.contentType, id, lifecycleKey);
        },

        imprtcAjax: function(id: string|number): void {
            dfNs.JournalEntryStateService?.imprtcAjax?.(config.contentType, id);
        },

        refrncAjax: function(id: string|number): void {
            dfNs.JournalEntryStateService?.refrncAjax?.(config.contentType, id);
        },

        nhtmrAjax: function(id: string|number): void {
            dfNs.JournalEntryStateService?.nhtmrAjax?.(config.contentType, id);
        },

        hallucAjax: function(id: string|number): void {
            dfNs.JournalEntryStateService?.hallucAjax?.(config.contentType, id);
        },

        toggle: function(id: string|number, trigger: HTMLElement): void {
            dfNs.JournalEntryStateService?.toggle?.(config.contentType, id, trigger);
        },

        initCollapseState: function(): void {
            dfNs.JournalEntryStateService?.initCollapseState?.(config.contentType);
        },

        copy: function(id: string|number): void {
            dfNs.JournalEntryCrudService?.copy?.(config.contentType, id);
        },

        buildViewModel: function(entry: Record<string, any>, profileName: string): Record<string, any> {
            return dfNs.JournalEntryStateService?.buildViewModel?.(config.contentType, entry, profileName);
        },
    };

    return module;
};

const modules: Record<string, any> = {};
Object.keys(configs).forEach(function(contentType: string): void {
    modules[contentType] = create(configs[contentType]);
});

const journalEntryRoot: dfModule = {
    initialized: true,
    init: function(): void {},
    get: function(contentType: string): dfModule {
        return modules[contentType];
    },
    getMeta: function(contentType: string): Record<string, any> {
        return configs[contentType];
    },
    getContentTypes: function(): string[] {
        return Object.keys(configs);
    },
    getTaggableContentTypes: function(): string[] {
        return Object.keys(configs).filter(function(contentType: string): boolean {
            return configs[contentType]?.useTag === true;
        });
    },
    getSearchPopupContentTypes: function(): string[] {
        return Object.keys(configs).filter(function(contentType: string): boolean {
            return cF.util.isNotEmpty(configs[contentType]?.searchInputSelector);
        });
    },
    initAll: function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): Promise<void[]> {
        return Promise.all(
            Object.keys(configs).map(function(contentType: string): Promise<void> {
                return modules[contentType].init(viewType);
            })
        );
    },
    /**
     * 변경(E-3-γ): 본 메서드는 journalEntrySearchPopupService 로 이전되었다.
     * 외부 호출(`dF.JournalEntry.bindSearchPopupEnterKeys()`) 은 외부 호출 시그니처 보존을 위해
     * 동일 인자/반환 형태로 service 진입만 한다. 호출 그래프(Vue mount 진입점) 보존.
     */
    bindSearchPopupEnterKeys: function(): void {
        dfNs.JournalEntrySearchPopupService?.bindSearchPopupEnterKeys?.();
    },

    /**
     * 저널 일자 화면 본문 영역(월간/주간/달력/메타 래퍼) 너비에 맞춘 미리보기 팝업 폭.
     */
    resolveJournalDayPreviewWidth: function(): number {
        // 본문 줄바꿈 기준과 최대한 일치시키기 위해 페이지 래퍼보다
        // 실제 본문이 렌더링되는 카드/리스트 컨테이너 폭을 우선 사용한다.
        const contentShell: HTMLElement | null = document.querySelector(
            "#journal_day_list_div, .journal-day-monthly-page .card.post, .journal-day-weekly-page .card.post, .journal-day-calendar-page .card.post, .journal-day-meta-page .card.post"
        );
        if (contentShell) {
            const shellWidth: number = Math.round(contentShell.getBoundingClientRect().width);
            if (shellWidth > 320) return shellWidth;
        }

        const journalShell: HTMLElement | null = document.querySelector(
            ".journal-day-monthly-page, .journal-day-weekly-page, .journal-day-calendar-page, .journal-day-meta-page"
        );
        if (journalShell) {
            const shellWidth: number = Math.round(journalShell.getBoundingClientRect().width);
            // 페이지 래퍼 폭은 본문보다 넓을 수 있어 보정값을 적용한다.
            if (shellWidth > 320) return Math.max(480, shellWidth - 64);
        }

        const container: HTMLElement | null = document.querySelector("#kt_app_content_container");
        if (container) {
            const containerWidth: number = Math.round(container.getBoundingClientRect().width);
            if (containerWidth > 320) return Math.max(480, containerWidth - 64);
        }
        return Math.min(Math.max(Math.round(window.innerWidth * 0.92), 480), 1600);
    },
};

dfNs.JournalEntry = journalEntryRoot;

export default journalEntryRoot;
