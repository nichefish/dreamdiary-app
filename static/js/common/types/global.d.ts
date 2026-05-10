/**
 * global.d.ts
 *
 * @author nichefish
 */

/* ----- */
/**
 * cF : 공통 유틸리티 함수 모듈
 */
declare namespace cF {
    interface Module {
        [key: string]: Module;
    }
}
declare const cF: {
    [key: string]: Module;
};

/**
 * dF : 공통 도메인 함수 모듈
 */
declare namespace dF {
    interface Module {
        [key: string]: Module;
    }
}
declare const cF: {
    init: Function;
    [key: string]: Module;
};
/**
 * Module : 기능 단위 함수 묶음.
 */
declare interface Module {
    [key: string]: any;
}
declare interface dfModule extends Module {
    init: Function<any>;
    initialized: boolean;
    [key: string]: any;
}
/**
 * Page : 페이지 전횽 함수 묶음.
 */
declare interface Page extends Module {
    init: Function<any>;
    [key: string]: any;
}
declare var Page: {
    [key: string]: any;
}

declare interface Tagify {
    [key: string]: any;
}

/**
 * Model : Spring 컨텍스트에서 Model에 추가된 요소들.
 */
declare const Model: {
    [key: string]: any;
};
/**
 * Message : Spring Boot에서 메세지 번들로 관리되는 Message 요소들.
 */
declare const AuthInfo: {
    username: string,
    nickname: string,
    email: string,
    profileImageUrl: string,
    isMngr: boolean,
    roles: Array<{ roleKey: string, roleName: string }>
};
/**
 * Url : Spring Boot에서 정적으로 관리되는 Url 요소들.
 */
declare const Url: {
    [key: string]: string;
};
/**
 * Message : Spring Boot에서 메세지 번들로 관리되는 Message 요소들.
 */
declare const Message: {
    get: Function
};
/**
 * Constant : Spring Boot에서 정적으로 관리되는 상수들.
 */
declare const Constant: {
    [key: string]: string;
};
/**
 * Constant : Spring Boot에서 정적으로 관리되는 상수들.
 */
declare const Code: {
    [key: string]: string;
};
/**
 * AjaxResponse : Spring Boot에서 Ajax 요청에 반환되는 응답 객체
 */
declare interface AjaxResponse {
    rslt: boolean;
    message: string;
    status: number;

    rsltObj?: {
        [key: string]: any;
    };
    rsltList?: object[];
    rsltMap?: Record<string, any>;
    rsltVal?: number;
    rsltStr?: string;
    rsltSts?: string;
    url?: string;
}

/**
 * metronic
 */
declare const KTMenu: any;

/**
 * Journal day 월·주·일 목록 Vue 브리지 형태 (<code>JournalDayMonthlyApp</code> 등 마운트 시 설정).
 * 변경 후: 단일 <code>JournalDayVueApp</code> 전역 제거, 페이지별 전역으로 분리.
 */
interface JournalDayListAppBridge {
    mounted?: boolean;
    viewType?: string;
    refresh?: () => void;
    applySearchParamsAndReload?: (patch: Record<string, any>, scope?: "CURRENT" | "MONTHLY" | "WEEKLY" | "DAILY") => void;
    getSearchParams?: () => Record<string, any>;
    patchSearchParams?: (patch: Record<string, any>) => Record<string, any>;
    getFilterSnapshot?: () => Record<string, unknown>;
    getCurrentSort?: () => string;
    getCurrentPeriod?: () => { yy: string; mnth: string };
    getCurrentAnchorDate?: () => string;
    buildAnchorDateForMonth?: (yy: string, mnth: string, fallbackDay?: number) => string;
    initAsideYyMnth?: () => void;
    pinpointAside?: () => void;
    turnbackAside?: () => void;
    sortAside?: (toBe?: string) => void;
    runYyMnth?: (yy: string, mnth: string | number, sort?: string) => void;
    runNavigateToWeek?: (stdrdDt: string) => void;
    runSetAnchorDateForCurrentView?: (stdrdDt: string, useTarget?: boolean) => void;
    /** 주간 네비 동기화 — ListApp에서 WeekNavigatorService 직접 호출(Aside 래퍼 제거 경로) */
    syncAsideWeekNavigator?: (stdrdDt?: string, weeklyList?: Record<string, any>[]) => void;
    /** Aside DOM/주간 네비 초기화 — Vue 브리지 확정 후 ListApp에서 호출 */
    initJournalDayAsideShell?: () => void;
    regModal?: () => void;
    mdfModal?: (id: string | number) => void;
    dtlModal?: (id: string | number) => void;
    delAjax?: (id: string | number) => void;
    pendingLoad?: {
        type: "monthly" | "weekly" | "daily" | "refresh" | "reload";
        scope?: "CURRENT" | "MONTHLY" | "WEEKLY" | "DAILY";
        patch?: Record<string, any>;
        stdrdDt?: string;
        targetDt?: string;
    } | null;
    /** Phase 9: 일자 상세를 새 창 일간 화면으로 열기 */
    openDetached?: (stdrdDt: string) => void;
    /** Phase 9: 주간 화면으로 이동 */
    moveToWeeklyView?: (stdrdDt: string) => void;
    /** Phase 15: 주간 날짜 셀 클릭 → 주간 화면으로 이동 (target 파라미터 포함) */
    navigateToWeekDay?: (stdrdDt: string) => void;
    /** Phase 5: 다이어리/꿈/태그 표시 여부 토글 */
    toggleParam?: () => void;
    /** Phase 5: 키워드 필터 적용 */
    applyKeywordFilters?: () => void;
    /** Phase 5: 챕터 카테고리 필터 토글 */
    toggleChapterCtgr?: () => void;
    /** Phase 5: 챕터 카테고리 필터 변경 적용 */
    changeChapterCtgr?: () => void;
    /** Phase 5: 챕터 카테고리 "전체" 옵션 mousedown 처리 */
    handleChapterCtgrMouseDown?: (event: MouseEvent) => boolean;
    [key: string]: unknown;
}

interface Window {
    /** Aside 년월 Vue — FTL이 싣는 연도 목록(SSOT은 서버 루프와 동일). */
    __journalAsideYyMnthBootstrap?: { yyOptions: Array<{ value: string; label: string }> };
    /** 저널 결산 Aside 패널 — FTL `_journal_annual_aside_base.ftlh` 가 싣는 연도/월 옵션·라벨(변경 A-5-β-2). */
    __journalAnnualAsideBootstrap?: {
        yyOptions?: Array<{ value: string; label: string }>;
        mnthOptions?: Array<{ value: string; label: string }>;
        labels?: { yy: string; mnth: string; allYears: string; allMonths: string };
    };
    /** 년월·Week·Pinpoint Aside 청크 Vue 마운트 완료 플래그. */
    JournalDayAsideYyMnthVueApp?: { mounted?: boolean };
    /** TAGCLOUD·일기·꿈 필터 Aside 청크 Vue 마운트 완료 플래그. */
    JournalDayAsideEntryFiltersVueApp?: { mounted?: boolean };
    /** 필터 카드 헤더(정렬) Aside 청크 Vue 마운트 완료 플래그. */
    JournalDayAsideFilterHeaderVueApp?: { mounted?: boolean };
    /**
     * Aside TODO 카드 브리지 — Vue 미마운트 시 목록 Ajax 결과를 pending 으로 보관 후 마운트 시 반영한다.
     * @keepInSync static/js/view/feature/journal/todo/journal_todo_module.ts applyJournalTodoAsideListPayload
     */
    JournalDayAsideTodoVueApp?: {
        mounted?: boolean;
        pendingTodoListPayload?: unknown;
        applyTodoListPayload?: (payload: unknown) => void;
    };
    /** Aside 일기 필터 챕터 카테고리 옵션(FTL 적재 → EntryFilters 템플릿). */
    __journalAsideEntryFiltersBootstrap?: { chapterCtgrOptions: Array<{ code: string; codeName: string }> };
    /** 챕터 등록 모달 Vue 측에 적재되는 카테고리 옵션(FTL JOURNAL_CHAPTER_CTGR_CD → JS 단일 SSOT). */
    __journalChapterRegBootstrap?: { categoryOptions: Array<{ code: string; codeName: string }> };
    Page?: {
        [key: string]: any;
    };
    /** 저널 일자 월간 목록 페이지 전용 Vue 브리지 */
    JournalDayMonthlyApp?: JournalDayListAppBridge;
    /** 저널 일자 주간 목록 페이지 전용 Vue 브리지 */
    JournalDayWeeklyApp?: JournalDayListAppBridge;
    /** 저널 일자 일간 목록 페이지 전용 Vue 브리지 */
    JournalDayDailyApp?: JournalDayListAppBridge;
    JournalDayCalVueApp?: {
        mounted?: boolean;
        calendar?: FullCalendar.Calendar | null;
        calDt?: Date | null;
        refresh?: () => void;
        refreshEventList?: (calDt?: Date | null) => void;
        chkbxProp?: (obj: HTMLInputElement) => void;
        moveMonth?: (yy: string, mnth: string | number) => void;
        regModal?: () => void;
        mdfModal?: (id: string | number) => void;
        dtlModal?: (id: string | number) => void;
        delAjax?: (id: string | number) => void;
        getCalendarDate?: () => Date | null;
    };
    JournalDayTagPanelVueApp?: {
        mounted?: boolean;
        setDayTagList?: (list: Record<string, any>[]) => void;
        openTagListModal?: (list: Record<string, any>[]) => void;
        pendingDayTagList?: Record<string, any>[] | null;
        pendingModalTagList?: Record<string, any>[] | null;
    };
    JournalDayTagProfileVueApp?: {
        mounted?: boolean;
        open?: (payload: Record<string, any>) => boolean;
        pendingPayload?: Record<string, any> | null;
    };
    JournalDayAsideWeekNavigatorVueApp?: {
        mounted?: boolean;
        setWeekDays?: (payload: {
            stdrdDt: string;
            days: Array<{
                label: string;
                dateStr: string;
                hasDay: boolean;
                isActive: boolean;
            }>;
        }) => void;
        syncWeekRangeLabel?: (stdrdDt?: string) => void;
        syncWeekNavigator?: (stdrdDt?: string, weeklyList?: Record<string, any>[]) => void;
        loadWeekNavigator?: (stdrdDt: string) => void;
        pendingPayload?: {
            stdrdDt: string;
            days: Array<{
                label: string;
                dateStr: string;
                hasDay: boolean;
                isActive: boolean;
            }>;
        } | null;
        pendingSyncRequest?: {
            stdrdDt?: string;
            weeklyList?: Record<string, any>[];
        } | null;
    };
    JournalDayTagDetailVueApp?: {
        mounted?: boolean;
        open?: (payload: {
            tagId: string | number;
            name: string;
            yy: string;
            yearOptions: Array<{ value: string; label: string; selected?: boolean }>;
            list: Record<string, any>[];
            weekMode?: boolean;
        }) => void;
        pendingPayload?: {
            tagId: string | number;
            name: string;
            yy: string;
            yearOptions: Array<{ value: string; label: string; selected?: boolean }>;
            list: Record<string, any>[];
            weekMode?: boolean;
        } | null;
    };
    /** 저널 일자 상세 모달(`journal_day_dtl`) Vue 본문 브리지. */
    JournalDayDetailVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 일자 등록 모달(`journal_day_reg`) Vue 본문 브리지. */
    JournalDayRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 챕터 등록 모달(`journal_chapter_reg`) Vue 본문 브리지. */
    JournalChapterRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 entry 등록 모달(`journal_*_reg`) Vue 헤더/본문 브리지. */
    JournalEntryRegVueApp?: {
        mounted?: boolean;
        open?: (contentType: string, model: Record<string, any>) => void;
        submit?: (contentType: string) => void;
        preview?: (contentType: string) => void;
        pendingPayloads?: Record<string, Record<string, any> | null | undefined>;
    };
    /** 저널 해석 등록 모달(`journal_interpretation_reg`) Vue 헤더/본문 브리지. */
    JournalInterpretationRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 결산 등록 모달(`journal_annual_reg`) Vue 헤더/본문 브리지. (변경 A-3) */
    JournalAnnualRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 결산 리뷰 등록 모달(`journal_annual_review_reg`) Vue 헤더/본문 브리지. (변경 A-3) */
    JournalAnnualReviewRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /**
     * 저널 결산 목록 페이지(`journal_annual_list`) Vue 브리지. (변경 A-5-α)
     * `journalAnnualCrudService.listAjax` 가 `cF.handlebars.template` 대신 본 브리지의 `setList(...)` 로 카드를 렌더한다.
     */
    JournalAnnualListVueApp?: {
        mounted?: boolean;
        setList?: (list: Record<string, any>[]) => void;
        pendingList?: Record<string, any>[] | null;
    };
    /**
     * 저널 결산 사이드 패널(`_journal_annual_aside_base`) Vue 마운트 표지. (변경 A-5-β-2)
     * yy/mnth select onchange → `dF.JournalAnnualAside.yyMnth` 위임. dummy 필터·dead 버튼 마크업은 보존한다.
     */
    JournalAnnualAsidePanelVueApp?: {
        mounted?: boolean;
    };
    /**
     * 저널 결산 상세 카드(`journal_annual_dtl`) Vue 브리지. (변경 A-7-β)
     * `journalAnnualCrudService.dtlAjax` 가 `cF.handlebars.template` 대신 본 브리지의 `setModel(...)` 로 카드 본문을 반영한다.
     */
    JournalAnnualDtlVueApp?: {
        mounted?: boolean;
        setModel?: (obj: Record<string, any>) => void;
        pendingModel?: Record<string, any> | null;
    };
    /**
     * 저널 결산 상세 태그 헤더(DAY/DIARY/DREAM 3행) Vue 브리지. (변경 A-7-δ)
     * `journalAnnualStateService.renderTagList` 가 Handlebars 대신 본 브리지의 `applyTagRow(kind, payload)` 로 반영한다.
     */
    JournalAnnualEntryTagListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DAY" | "DIARY" | "DREAM", Record<string, any>>> | null;
        applyTagRow?: (kind: "DAY" | "DIARY" | "DREAM", payload: Record<string, any>) => void;
    };
    /**
     * 저널 결산 상세 엔트리 리스트(DIARY/DREAM 2 컨테이너) Vue 브리지. (변경 A-7-γ)
     * `journalAnnualStateService.renderEntryList` 가 Handlebars 대신 본 브리지의
     * `setList(kind, list, config)` 로 반영한다. 적재 경합 시 `pendingByType` 큐잉.
     */
    JournalAnnualEntryListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DIARY" | "DREAM", { list: Record<string, any>[]; config: Record<string, any> }>> | null;
        setList?: (kind: "DIARY" | "DREAM", list: Record<string, any>[], config: Record<string, any>) => void;
    };
    /**
     * 저널 일자(monthly/weekly/daily/cal/meta) + 엔트리 검색 페이지의 일기/꿈 태그 헤더 Vue 브리지. (변경 A-9)
     * `journalEntryTagService.renderList` 가 Handlebars `journal_entry_tag_list` 컴파일 대신
     * 본 브리지의 `setList(kind, list, config)` 로 반영한다(`config.module` 은 `tagModuleExpr` 문자열).
     * 적재 경합 시 `pendingByType` 큐잉 후 마운트 시 흡수.
     */
    JournalDayEntryTagListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DIARY" | "DREAM", { list: Record<string, any>[]; config: { module: string } }>> | null;
        setList?: (kind: "DIARY" | "DREAM", list: Record<string, any>[], config: { module: string }) => void;
    };
    /**
     * 저널 결산 CRUD/모달/Ajax 서비스 (A-4-α Vue 서비스 글로벌).
     * 변경(A-4-β): `journalAnnualService` 표면이 위임한다.
     */
    JournalAnnualCrudService?: {
        listAjax: () => void;
        dtlView: (yy: string | number) => void;
        dtlViewWithSection: (section: "DIARY" | "DREAM") => void;
        dtlAjax: (yy: string | number) => void;
        list: () => void;
        makeYyAnnualAjax: (yy: string | number) => void;
        makeTotalAnnualAjax: () => void;
        comptAjax: (id: string | number) => void;
        submit: () => void;
        mdfModal: (yy: string | number) => void;
        regAjax: () => void;
    };
    /**
     * 저널 결산 Ajax 목록/태그/렌더 서비스 (A-4-α Vue 서비스 글로벌).
     * lazy entry/tag list config 캐시는 본 service 의 모듈 스코프에 있다(annual_known_break 회피 보존).
     */
    JournalAnnualStateService?: {
        toggleParam: () => void;
        getAnnualDiaryListAjax: (yy: string | number) => void;
        getAnnualDreamListAjax: (yy: string | number) => void;
        renderEntryList: (list: Record<string, any>[], type: "DIARY" | "DREAM") => void;
        getTagListAjax: (yy: string | number, type: "DAY" | "DIARY" | "DREAM") => void;
        renderTagList: (list: Record<string, any>[], type: "DAY" | "DIARY" | "DREAM") => void;
    };
    /** 저널 결산 리뷰 CRUD/모달 서비스 (A-4-α Vue 서비스 글로벌). */
    JournalAnnualReviewCrudService?: {
        submit: () => void;
        regModal: (params: { journalAnnualId: string | number }) => void;
        mdfModal: (id: string | number) => void;
        regAjax: () => void;
        delAjax: (id: string | number) => void;
    };
    /**
     * 저널 해석 CRUD/모달 액션(I-3 Vue 서비스 글로벌).
     * 변경 이력: I-5 — dtlModal 제거(미존재 HBS 호출 dead 경로 정리).
     */
    JournalInterpretationCrudService?: {
        regModal: (params: {
            journalDayId: string | number;
            refId: string | number;
            refContentType: string;
            stdrdDt: string;
            journalDateWeekDay: string;
        }) => void;
        submit: () => void;
        regAjax: () => void;
        mdfModal: (id: string | number) => void;
        delAjax: (id: string | number) => void;
        copy: (id: string | number) => void;
    };
    /** 저널 해석 상태/라이프사이클(I-3 Vue 서비스 글로벌). */
    JournalInterpretationStateService?: {
        resolveAjax: (id: string | number, trigger?: HTMLInputElement) => void;
        setLifecycleAjax: (id: string | number, lifecycleKey: string) => void;
        collapse: (id: string | number, collapsedYn: "Y" | "N") => void;
        toggle: (id: string | number, trigger: HTMLElement) => void;
        initCollapseState: () => void;
    };
    /** 저널 할일 등록 모달(`journal_todo_regist`) Vue 본문 브리지. */
    JournalTodoRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 저널 메타 조회 모달(`journal_day_meta`) Vue 본문 브리지. */
    JournalDayMetaVueApp?: {
        mounted?: boolean;
        open?: (payload: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 메타 뷰 페이지 헤더 목록·설정 스트립 (`journal_day_meta_list`, `journal_day_meta_config`) Vue 브리지. */
    JournalDayMetaPageVueApp?: {
        mounted?: boolean;
        setMetaList?: (list: Record<string, any>[]) => void;
        setSelectedConfig?: (obj: Record<string, any> | null) => void;
        pendingList?: Record<string, any>[] | null;
        pendingConfig?: Record<string, any> | null;
    };
    __journalDaySearchParamsStore?: {
        searchParams: Record<string, any> | null;
    };
    /**
     * 카테고리 맵 Ajax가 Vue 서비스 로드보다 먼저 끝난 경우:
     * <code>dF.JournalDayTagService</code> 등록 시 <code>hydrateDayTagCategoryMap</code>으로 흡수한다.
     */
    __journalDayTagCategoryMapPendingHydrate?: Record<string, any>;
}
