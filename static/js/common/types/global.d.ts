/**
 * global.d.ts
 *
 * @author nichefish
 */

/* ----- */
/**
 * cF : 怨듯넻 ?좏떥由ы떚 ?⑥닔 紐⑤뱢
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
 * dF : 怨듯넻 ?꾨찓???⑥닔 紐⑤뱢
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
 * Module : 湲곕뒫 ?⑥쐞 ?⑥닔 臾띠쓬.
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
 * Page : ?섏씠吏 ?꾪슺 ?⑥닔 臾띠쓬.
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
 * Model : Spring 而⑦뀓?ㅽ듃?먯꽌 Model??異붽????붿냼??
 */
declare const Model: {
    [key: string]: any;
};
/**
 * Message : Spring Boot?먯꽌 硫붿꽭吏 踰덈뱾濡?愿由щ릺??Message ?붿냼??
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
 * Url : Spring Boot?먯꽌 ?뺤쟻?쇰줈 愿由щ릺??Url ?붿냼??
 */
declare const Url: {
    [key: string]: string;
};
/**
 * Message : Spring Boot?먯꽌 硫붿꽭吏 踰덈뱾濡?愿由щ릺??Message ?붿냼??
 */
declare const Message: {
    get: Function
};
/**
 * Constant : Spring Boot?먯꽌 ?뺤쟻?쇰줈 愿由щ릺???곸닔??
 */
declare const Constant: {
    [key: string]: string;
};
/**
 * Constant : Spring Boot?먯꽌 ?뺤쟻?쇰줈 愿由щ릺???곸닔??
 */
declare const Code: {
    [key: string]: string;
};
/**
 * AjaxResponse : Spring Boot?먯꽌 Ajax ?붿껌??諛섑솚?섎뒗 ?묐떟 媛앹껜
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
 * Journal day ?붋룹＜쨌??紐⑸줉 Vue 釉뚮━吏 ?뺥깭 (<code>JournalDayMonthlyApp</code> ??留덉슫?????ㅼ젙).
 * 蹂寃??? ?⑥씪 <code>JournalDayVueApp</code> ?꾩뿭 ?쒓굅, ?섏씠吏蹂??꾩뿭?쇰줈 遺꾨━.
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
    /** 二쇨컙 ?ㅻ퉬 ?숆린????ListApp?먯꽌 WeekNavigatorService 吏곸젒 ?몄텧(Aside ?섑띁 ?쒓굅 寃쎈줈) */
    syncAsideWeekNavigator?: (stdrdDt?: string, weeklyList?: Record<string, any>[]) => void;
    /** Aside DOM/二쇨컙 ?ㅻ퉬 珥덇린????Vue 釉뚮━吏 ?뺤젙 ??ListApp?먯꽌 ?몄텧 */
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
    /** Phase 9: ?쇱옄 ?곸꽭瑜???李??쇨컙 ?붾㈃?쇰줈 ?닿린 */
    openDetached?: (stdrdDt: string) => void;
    /** Phase 9: 二쇨컙 ?붾㈃?쇰줈 ?대룞 */
    moveToWeeklyView?: (stdrdDt: string) => void;
    /** Phase 15: 二쇨컙 ?좎쭨 ? ?대┃ ??二쇨컙 ?붾㈃?쇰줈 ?대룞 (target ?뚮씪誘명꽣 ?ы븿) */
    navigateToWeekDay?: (stdrdDt: string) => void;
    /** Phase 5: ?ㅼ씠?대━/轅??쒓렇 ?쒖떆 ?щ? ?좉? */
    toggleParam?: () => void;
    /** Phase 5: ?ㅼ썙???꾪꽣 ?곸슜 */
    applyKeywordFilters?: () => void;
    /** Phase 5: 梨뺥꽣 移댄뀒怨좊━ ?꾪꽣 ?좉? */
    toggleChapterCtgr?: () => void;
    /** Phase 5: 梨뺥꽣 移댄뀒怨좊━ ?꾪꽣 蹂寃??곸슜 */
    changeChapterCtgr?: () => void;
    /** Phase 5: 梨뺥꽣 移댄뀒怨좊━ "?꾩껜" ?듭뀡 mousedown 泥섎━ */
    handleChapterCtgrMouseDown?: (event: MouseEvent) => boolean;
    [key: string]: unknown;
}

interface Window {
    /** Aside ?꾩썡 Vue ??FTL???ｋ뒗 ?곕룄 紐⑸줉(SSOT? ?쒕쾭 猷⑦봽? ?숈씪). */
    __journalAsideYyMnthBootstrap?: { yyOptions: Array<{ value: string; label: string }> };
    /** ???寃곗궛 Aside ?⑤꼸 ??FTL `_journal_annual_aside_base.ftlh` 媛 ?ｋ뒗 ?곕룄/???듭뀡쨌?쇰꺼(蹂寃?A-5-棺-2). */
    __journalAnnualAsideBootstrap?: {
        yyOptions?: Array<{ value: string; label: string }>;
        mnthOptions?: Array<{ value: string; label: string }>;
        labels?: { yy: string; mnth: string; allYears: string; allMonths: string };
    };
    /** ?꾩썡쨌Week쨌Pinpoint Aside 泥?겕 Vue 留덉슫???꾨즺 ?뚮옒洹? */
    JournalDayAsideYyMnthVueApp?: { mounted?: boolean };
    /** TAGCLOUD쨌?쇨린쨌轅??꾪꽣 Aside 泥?겕 Vue 留덉슫???꾨즺 ?뚮옒洹? */
    JournalDayAsideEntryFiltersVueApp?: { mounted?: boolean };
    /** ?꾪꽣 移대뱶 ?ㅻ뜑(?뺣젹) Aside 泥?겕 Vue 留덉슫???꾨즺 ?뚮옒洹? */
    JournalDayAsideFilterHeaderVueApp?: { mounted?: boolean };
    /**
     * Aside TODO 移대뱶 釉뚮━吏 ??Vue 誘몃쭏?댄듃 ??紐⑸줉 Ajax 寃곌낵瑜?pending ?쇰줈 蹂닿? ??留덉슫????諛섏쁺?쒕떎.
     * @keepInSync static/js/view/feature/journal/todo/journal_todo_module.ts applyJournalTodoAsideListPayload
     */
    JournalDayAsideTodoVueApp?: {
        mounted?: boolean;
        pendingTodoListPayload?: unknown;
        applyTodoListPayload?: (payload: unknown) => void;
    };
    /** Aside ?쇨린 ?꾪꽣 梨뺥꽣 移댄뀒怨좊━ ?듭뀡(FTL ?곸옱 ??EntryFilters ?쒗뵆由?. */
    __journalAsideEntryFiltersBootstrap?: { chapterCtgrOptions: Array<{ code: string; codeName: string }> };
    /** 梨뺥꽣 ?깅줉 紐⑤떖 Vue 痢≪뿉 ?곸옱?섎뒗 移댄뀒怨좊━ ?듭뀡(FTL JOURNAL_CHAPTER_CTGR_CD ??JS ?⑥씪 SSOT). */
    __journalChapterRegistBootstrap?: { categoryOptions: Array<{ code: string; codeName: string }> };
    Page?: {
        [key: string]: any;
    };
    /** ????쇱옄 ?붽컙 紐⑸줉 ?섏씠吏 ?꾩슜 Vue 釉뚮━吏 */
    JournalDayMonthlyApp?: JournalDayListAppBridge;
    /** ????쇱옄 二쇨컙 紐⑸줉 ?섏씠吏 ?꾩슜 Vue 釉뚮━吏 */
    JournalDayWeeklyApp?: JournalDayListAppBridge;
    /** ????쇱옄 ?쇨컙 紐⑸줉 ?섏씠吏 ?꾩슜 Vue 釉뚮━吏 */
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
    /** ????쇱옄 ?곸꽭 紐⑤떖(`journal_day_dtl`) Vue 蹂몃Ц 釉뚮━吏. */
    JournalDayDetailVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ????쇱옄 ?깅줉 紐⑤떖(`journal_day_reg`) Vue 蹂몃Ц 釉뚮━吏. */
    JournalDayRegVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ???梨뺥꽣 ?깅줉 紐⑤떖(`journal_chapter_regist`) Vue 蹂몃Ц 釉뚮━吏. */
    JournalChapterRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ???entry ?깅줉 紐⑤떖(`journal_*_reg`) Vue ?ㅻ뜑/蹂몃Ц 釉뚮━吏. */
    JournalEntryRegVueApp?: {
        mounted?: boolean;
        open?: (contentType: string, model: Record<string, any>) => void;
        submit?: (contentType: string) => void;
        preview?: (contentType: string) => void;
        pendingPayloads?: Record<string, Record<string, any> | null | undefined>;
    };
    /** ????댁꽍 ?깅줉 紐⑤떖(`journal_interpretation_regist`) Vue ?ㅻ뜑/蹂몃Ц 釉뚮━吏. */
    JournalInterpretationRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ???寃곗궛 ?깅줉 紐⑤떖(`journal_annual_regist`) Vue ?ㅻ뜑/蹂몃Ц 釉뚮━吏. (蹂寃?A-3) */
    JournalAnnualRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ???寃곗궛 由щ럭 ?깅줉 紐⑤떖(`journal_annual_review_regist`) Vue ?ㅻ뜑/蹂몃Ц 釉뚮━吏. (蹂寃?A-3) */
    JournalAnnualReviewRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /**
     * ???寃곗궛 紐⑸줉 ?섏씠吏(`journal_annual_list`) Vue 釉뚮━吏. (蹂寃?A-5-慣)
     * `journalAnnualCrudService.listAjax` 媛 `cF.handlebars.template` ???蹂?釉뚮━吏??`setList(...)` 濡?移대뱶瑜??뚮뜑?쒕떎.
     */
    JournalAnnualListVueApp?: {
        mounted?: boolean;
        setList?: (list: Record<string, any>[]) => void;
        pendingList?: Record<string, any>[] | null;
    };
    /**
     * ???寃곗궛 ?ъ씠???⑤꼸(`_journal_annual_aside_base`) Vue 留덉슫???쒖?. (蹂寃?A-5-棺-2)
     * yy/mnth select onchange ??`dF.JournalAnnualAside.yyMnth` ?꾩엫. dummy ?꾪꽣쨌dead 踰꾪듉 留덊겕?낆? 蹂댁〈?쒕떎.
     */
    JournalAnnualAsidePanelVueApp?: {
        mounted?: boolean;
    };
    /**
     * ???寃곗궛 ?곸꽭 移대뱶(`journal_annual_detail`) Vue 釉뚮━吏. (蹂寃?A-7-棺)
     * `journalAnnualCrudService.detailAjax` 媛 `cF.handlebars.template` ???蹂?釉뚮━吏??`setModel(...)` 濡?移대뱶 蹂몃Ц??諛섏쁺?쒕떎.
     */
    JournalAnnualDetailVueApp?: {
        mounted?: boolean;
        setModel?: (obj: Record<string, any>) => void;
        pendingModel?: Record<string, any> | null;
    };
    /**
     * ???寃곗궛 ?곸꽭 ?쒓렇 ?ㅻ뜑(DAY/DIARY/DREAM 3?? Vue 釉뚮━吏. (蹂寃?A-7-灌)
     * `journalAnnualStateService.renderTagList` 媛 Handlebars ???蹂?釉뚮━吏??`applyTagRow(kind, payload)` 濡?諛섏쁺?쒕떎.
     */
    JournalAnnualEntryTagListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DAY" | "DIARY" | "DREAM", Record<string, any>>> | null;
        applyTagRow?: (kind: "DAY" | "DIARY" | "DREAM", payload: Record<string, any>) => void;
    };
    /**
     * ???寃곗궛 ?곸꽭 ?뷀듃由?由ъ뒪??DIARY/DREAM 2 而⑦뀒?대꼫) Vue 釉뚮━吏. (蹂寃?A-7-款)
     * `journalAnnualStateService.renderEntryList` 媛 Handlebars ???蹂?釉뚮━吏??     * `setList(kind, list, config)` 濡?諛섏쁺?쒕떎. ?곸옱 寃쏀빀 ??`pendingByType` ?먯엵.
     */
    JournalAnnualEntryListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DIARY" | "DREAM", { list: Record<string, any>[]; config: Record<string, any> }>> | null;
        setList?: (kind: "DIARY" | "DREAM", list: Record<string, any>[], config: Record<string, any>) => void;
    };
    /**
     * ????쇱옄(monthly/weekly/daily/cal/meta) + ?뷀듃由?寃???섏씠吏???쇨린/轅??쒓렇 ?ㅻ뜑 Vue 釉뚮━吏. (蹂寃?A-9)
     * `journalEntryTagService.renderList` 媛 Handlebars `journal_entry_tag_list` 而댄뙆?????     * 蹂?釉뚮━吏??`setList(kind, list, config)` 濡?諛섏쁺?쒕떎(`config.module` ? `tagModuleExpr` 臾몄옄??.
     * ?곸옱 寃쏀빀 ??`pendingByType` ?먯엵 ??留덉슫?????≪닔.
     */
    JournalDayEntryTagListVueApp?: {
        mounted?: boolean;
        pendingByType?: Partial<Record<"DIARY" | "DREAM", { list: Record<string, any>[]; config: { module: string } }>> | null;
        setList?: (kind: "DIARY" | "DREAM", list: Record<string, any>[], config: { module: string }) => void;
    };
    /**
     * ???寃곗궛 CRUD/紐⑤떖/Ajax ?쒕퉬??(A-4-慣 Vue ?쒕퉬??湲濡쒕쾶).
     * 蹂寃?A-4-棺): `journalAnnualService` ?쒕㈃???꾩엫?쒕떎.
     */
    JournalAnnualCrudService?: {
        listAjax: () => void;
        detailView: (yy: string | number) => void;
        detailViewWithSection: (section: "DIARY" | "DREAM") => void;
        detailAjax: (yy: string | number) => void;
        list: () => void;
        makeYyAnnualAjax: (yy: string | number) => void;
        makeTotalAnnualAjax: () => void;
        comptAjax: (id: string | number) => void;
        submit: () => void;
        modifyModal: (yy: string | number) => void;
        registAjax: () => void;
    };
    /**
     * ???寃곗궛 Ajax 紐⑸줉/?쒓렇/?뚮뜑 ?쒕퉬??(A-4-慣 Vue ?쒕퉬??湲濡쒕쾶).
     * lazy entry/tag list config 罹먯떆??蹂?service ??紐⑤뱢 ?ㅼ퐫?꾩뿉 ?덈떎(annual_known_break ?뚰뵾 蹂댁〈).
     */
    JournalAnnualStateService?: {
        toggleParam: () => void;
        getAnnualDiaryListAjax: (yy: string | number) => void;
        getAnnualDreamListAjax: (yy: string | number) => void;
        renderEntryList: (list: Record<string, any>[], type: "DIARY" | "DREAM") => void;
        getTagListAjax: (yy: string | number, type: "DAY" | "DIARY" | "DREAM") => void;
        renderTagList: (list: Record<string, any>[], type: "DAY" | "DIARY" | "DREAM") => void;
    };
    /** ???寃곗궛 由щ럭 CRUD/紐⑤떖 ?쒕퉬??(A-4-慣 Vue ?쒕퉬??湲濡쒕쾶). */
    JournalAnnualReviewCrudService?: {
        submit: () => void;
        registModal: (params: { journalAnnualId: string | number }) => void;
        modifyModal: (id: string | number) => void;
        registAjax: () => void;
        deleteAjax: (id: string | number) => void;
    };
    /**
     * ????댁꽍 CRUD/紐⑤떖 ?≪뀡(I-3 Vue ?쒕퉬??湲濡쒕쾶).
     * 蹂寃??대젰: I-5 ??detailModal ?쒓굅(誘몄〈??HBS ?몄텧 dead 寃쎈줈 ?뺣━).
     */
    JournalInterpretationCrudService?: {
        registModal: (params: {
            journalDayId: string | number;
            refId: string | number;
            refContentType: string;
            stdrdDt: string;
            journalDateWeekDay: string;
        }) => void;
        submit: () => void;
        registAjax: () => void;
        modifyModal: (id: string | number) => void;
        deleteAjax: (id: string | number) => void;
        copy: (id: string | number) => void;
    };
    /** ????댁꽍 ?곹깭/?쇱씠?꾩궗?댄겢(I-3 Vue ?쒕퉬??湲濡쒕쾶). */
    JournalInterpretationStateService?: {
        resolveAjax: (id: string | number, trigger?: HTMLInputElement) => void;
        setLifecycleAjax: (id: string | number, lifecycleKey: string) => void;
        collapse: (id: string | number, collapsedYn: "Y" | "N") => void;
        toggle: (id: string | number, trigger: HTMLElement) => void;
        initCollapseState: () => void;
    };
    /** ????좎씪 ?깅줉 紐⑤떖(`journal_todo_regist`) Vue 蹂몃Ц 釉뚮━吏. */
    JournalTodoRegistVueApp?: {
        mounted?: boolean;
        open?: (model: Record<string, any>) => void;
        submit?: () => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** ???硫뷀? 議고쉶 紐⑤떖(`journal_day_meta`) Vue 蹂몃Ц 釉뚮━吏. */
    JournalDayMetaVueApp?: {
        mounted?: boolean;
        open?: (payload: Record<string, any>) => void;
        pendingPayload?: Record<string, any> | null;
    };
    /** 硫뷀? 酉??섏씠吏 ?ㅻ뜑 紐⑸줉쨌?ㅼ젙 ?ㅽ듃由?(`journal_day_meta_list`, `journal_day_meta_config`) Vue 釉뚮━吏. */
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
     * 移댄뀒怨좊━ 留?Ajax媛 Vue ?쒕퉬??濡쒕뱶蹂대떎 癒쇱? ?앸궃 寃쎌슦:
     * <code>dF.JournalDayTagService</code> ?깅줉 ??<code>hydrateDayTagCategoryMap</code>?쇰줈 ?≪닔?쒕떎.
     */
    __journalDayTagCategoryMapPendingHydrate?: Record<string, any>;
}
