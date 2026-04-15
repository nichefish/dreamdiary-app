/**
 * journal_day_module.ts
 * 저널 일자 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalDay = (function(): dfModule {
    return {
        initialized: false,
        viewType: null,
        tagTagify: null,
        metaTagify: null,
        currentSearchParams: null,
        CHAPTER_CTGR_ALL: "__ALL__",
        CHAPTER_CTGR_NONE: "__NONE__",

        /**
         * initializes module.
         * @param {"LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"} viewType
         */
        init: function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): void {
            if (dF.JournalDay.initialized) return;

            dF.JournalDay.viewType = viewType;

            /* initialize submodules. */
            dF.JournalDayTag.init();
            dF.JournalDayMeta.init();

            dF.JournalDay.initialized = true;
            console.log("'dF.JournalDay' module initialized.");
        },

        /**
         * refresh
         */
        refresh: function(): void {
            switch (dF.JournalDay.viewType) {
                case "LIST":
                    dF.JournalDay.yyMnthListAjax();
                    break;
                case "CAL":
                    Page.refreshEventList();
                    dF.JournalDayTag.listAjax();     // 태그 refresh
                    break;
                case "DAILY":
                    location.reload();
                    break;
                case "WEEKLY":
                    Page.loadWeek(Page.stdrdDt);
                    break;
            }
            cF.ui.unblockUI();
            /* modal history pop */
            ModalHistory.reset();
        },

        reloadByView: function(): void {
            switch (dF.JournalDay.viewType) {
                case "LIST":
                    dF.JournalDay.yyMnthListAjax();
                    break;
                case "WEEKLY":
                    Page.loadWeek(Page.stdrdDt);
                    break;
                default:
                    dF.JournalDay.refresh();
                    break;
            }
        },

        /**
         * resolve anchor date for cross-view navigation
         */
        resolveAnchorDateForView: function(): string {
            if (dF.JournalDay.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.stdrdDt)) {
                return Page.stdrdDt;
            }

            if (dF.JournalDay.viewType === "CAL" && Page?.calDt instanceof Date) {
                return cF.date.dateToStr(Page.calDt, cF.date.ptnDate) ?? "";
            }

            dF.JournalDay.initSearchParams();
            const currentParams: Record<string, any> = dF.JournalDay.currentSearchParams ?? {};
            if (cF.util.isNotEmpty(currentParams.stdrdDt)) return currentParams.stdrdDt;

            const yy: string = currentParams.yy ?? localStorage.getItem("journal_yy") ?? cF.date.getCurrYyStr();
            const mnth: string = currentParams.mnth ?? localStorage.getItem("journal_mnth") ?? cF.date.getCurrMnthStr();
            return dF.JournalDayAside.buildAnchorDateForMonth(yy, mnth, 1);
        },

        /**
         * build view url with current period/filter state
         * @param {string} baseUrl
         */
        buildViewUrl: function(baseUrl: string): string {
            dF.JournalDay.initSearchParams();

            const currentParams: Record<string, any> = dF.JournalDay.currentSearchParams ?? {};
            const anchorDate: string = dF.JournalDay.resolveAnchorDateForView();
            const yy: string = anchorDate?.substring(0, 4) || currentParams.yy || cF.date.getCurrYyStr();
            const mnth: string = anchorDate
                ? String(parseInt(anchorDate.substring(5, 7), 10))
                : (currentParams.mnth || cF.date.getCurrMnthStr());

            const targetUrl: URL = new URL(baseUrl, window.location.origin);
            targetUrl.searchParams.set("yy", yy);
            targetUrl.searchParams.set("mnth", mnth);

            if (cF.util.isNotEmpty(anchorDate)) targetUrl.searchParams.set("stdrdDt", anchorDate);
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
        },

        /**
         * form init
         * @param {Record<string, any>} obj - 폼에 바인딩할 데이터.
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "journal_day_reg");

            /* jquery validation */
            const form: HTMLFormElement = document.querySelector("#journalDayRegForm") as HTMLFormElement;
            cF.validate.validateForm(form, dF.JournalDay.regAjax, {
                rules: {
                    journalDt: {
                        required: function(): boolean {
                            const dtUnknownYn: HTMLInputElement = document.querySelector("#journalDayRegForm #dtUnknownYn");
                            return !dtUnknownYn?.checked;
                        }
                    },
                    aprxmtDt: {
                        required: function(): boolean {
                            const dtUnknownYn: HTMLInputElement = document.querySelector("#journalDayRegForm #dtUnknownYn");
                            return dtUnknownYn?.checked;
                        }
                    },
                },
                ignore: undefined
            });
            // 체크박스 상태 변경시 필드 재검증
            $("#dtUnknownYn").change(function(): void {
                $("#journalDt").valid();
                $("#aprxmtDt").valid();
            });
            // 일자 datepicker 날짜 검색 init : 현재 조회중인 yyyy-MM으로 처리
            cF.datepicker.singleDatePicker("#journalDt", "yyyy-MM-DD", obj.journalDt);
            // 날짜미상 datepicker 날짜 검색 init
            cF.datepicker.singleDatePicker("#aprxmtDt", "yyyy-MM-DD", obj.aprxmtDt);
            // checkbox init
            cF.ui.chckboxLabel("#journalDayRegForm #diaryResolvedYn", "완료//미완료", "blue//gray");
            cF.ui.chckboxLabel("#journalDayRegForm #dtUnknownYn", "날짜미상//날짜미상", "blue//gray", function(): void {
                $("#journalDayRegForm #journalDtDiv").addClass("d-none");
                $("#journalDayRegForm #aprxmtDtDiv").removeClass("d-none");
                $("#journalDayRegForm #aprxmtDt").val($("#journalDayRegForm #journalDt").val());
            }, function(): void {
                $("#journalDayRegForm #journalDtDiv").removeClass("d-none");
                $("#journalDayRegForm #aprxmtDtDiv").addClass("d-none");
                $("#journalDayRegForm #journalDt").val($("#journalDayRegForm #aprxmtDt").val());
            });
            /* tagify */
            dF.JournalDay.tagTagify = cF.tagify.initWithCtgr("#journalDayRegForm #tagListStr", dF.JournalDayTag.ctgrMap);
            dF.JournalDay.metaTagify = cF.tagify.initMeta("#journalDayRegForm #metaListStr", dF.JournalDayMeta.ctgrMap);
        },

        /**
         * URL 파라미터로부터 파라미터 객체 초기화
         */
        initSearchParams: function(): void {
            if (dF.JournalDay.currentSearchParams) return;

            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "9999";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "99";
            const showDiaries = cF.util.getUrlParam("showDiaries") !== "false";
            const showDreams = cF.util.getUrlParam("showDreams") !== "false";
            const showTagCloud = cF.util.getUrlParam("showTagCloud") !== "false";
            const showChapterCtgr = cF.util.getUrlParam("showChapterCtgr") !== "false";
            const rawEntryCtgr = cF.util.getUrlParam("chapterCtgrCds") ?? cF.util.getUrlParam("chapterCtgrCd") ?? "";
            const chapterCtgrCds: string[] = dF.JournalDay.parseChapterCtgrCds(rawEntryCtgr);
            const diaryKeyword: string = cF.util.getUrlParam("diaryKeyword") ?? "";
            const dreamKeyword: string = cF.util.getUrlParam("dreamKeyword") ?? "";
            const stdrdDt: string = cF.util.getUrlParam("stdrdDt") ?? "";

            dF.JournalDay.currentSearchParams = {
                "viewType": "list",
                yy,
                mnth,
                stdrdDt,
                showDiaries,
                showDreams,
                showTagCloud,
                showChapterCtgr,
                chapterCtgrCds,
                diaryKeyword,
                dreamKeyword
            };

            // DOM에 상태 반영
            $("#toggleDiaries").prop("checked", showDiaries);
            $("#toggleDreams").prop("checked", showDreams);
            $("#toggleTagCloud").prop("checked", showTagCloud);
            $("#toggleChapterCtgr").prop("checked", showChapterCtgr);
            if (showChapterCtgr && chapterCtgrCds.length === 0) {
                const allCds: string[] = dF.JournalDay.getSelectableChapterCtgrCds();
                dF.JournalDay.currentSearchParams.chapterCtgrCds = dF.JournalDay.syncChapterCtgrSelectUi(allCds);
            } else {
                dF.JournalDay.currentSearchParams.chapterCtgrCds = dF.JournalDay.syncChapterCtgrSelectUi(chapterCtgrCds);
            }
            dF.JournalDay.syncChapterCtgrState(showDiaries);
            $("#diaryFilterKeyword").val(diaryKeyword);
            $("#dreamFilterKeyword").val(dreamKeyword);
            dF.JournalDay.syncKeywordFilterState();
        },

        /**
         * URL 파라미터를 다중 카테고리 배열로 파싱
         */
        parseChapterCtgrCds: function(rawValue: string): string[] {
            if (cF.util.isEmpty(rawValue)) return [];

            return rawValue
                .split(",")
                .map((value: string): string => value.trim())
                .filter((value: string): boolean => value.length > 0);
        },

        getSelectableChapterCtgrCds: function(): string[] {
            return $("#chapterCtgrFilter").find("option").map(function(): string {
                return String($(this).val() ?? "").trim();
            }).get().filter((value: string): boolean => {
                return value.length > 0
                    && value !== dF.JournalDay.CHAPTER_CTGR_ALL
                    && value !== dF.JournalDay.CHAPTER_CTGR_NONE;
            });
        },

        normalizeChapterCtgrCds: function(selectedCtgrCds: string[] = [], emptyFallback: string[] = []): string[] {
            const selectableCtgrCds: string[] = dF.JournalDay.getSelectableChapterCtgrCds();
            const uniqueSelectedCtgrCds: string[] = Array.from(new Set(
                (selectedCtgrCds ?? [])
                    .map((value: string): string => String(value ?? "").trim())
                    .filter((value: string): boolean => value.length > 0)
            ));
            const selectedRealCtgrCds: string[] = uniqueSelectedCtgrCds.filter((value: string): boolean => {
                return selectableCtgrCds.includes(value);
            });

            if (selectedRealCtgrCds.length === selectableCtgrCds.length && selectableCtgrCds.length > 0) {
                return selectableCtgrCds;
            }

            if (uniqueSelectedCtgrCds.includes(dF.JournalDay.CHAPTER_CTGR_ALL) && selectedRealCtgrCds.length === 0) {
                return selectableCtgrCds;
            }

            if (uniqueSelectedCtgrCds.includes(dF.JournalDay.CHAPTER_CTGR_NONE) && selectedRealCtgrCds.length === 0) {
                return [dF.JournalDay.CHAPTER_CTGR_NONE];
            }

            if (selectedRealCtgrCds.length === 0) return [...emptyFallback];

            return selectedRealCtgrCds;
        },

        syncChapterCtgrSelectUi: function(selectedCtgrCds: string[] = []): string[] {
            const selectableCtgrCds: string[] = dF.JournalDay.getSelectableChapterCtgrCds();
            const normalizedCtgrCds: string[] = dF.JournalDay.normalizeChapterCtgrCds(selectedCtgrCds);
            const isAllSelected: boolean = selectableCtgrCds.length > 0
                && normalizedCtgrCds.length === selectableCtgrCds.length
                && selectableCtgrCds.every((ctgrCd: string): boolean => normalizedCtgrCds.includes(ctgrCd));
            const uiSelectedCtgrCds: string[] = isAllSelected
                ? [dF.JournalDay.CHAPTER_CTGR_ALL, ...selectableCtgrCds]
                : normalizedCtgrCds.filter((value: string): boolean => value !== dF.JournalDay.CHAPTER_CTGR_NONE);

            $("#chapterCtgrFilter").val(uiSelectedCtgrCds);
            return normalizedCtgrCds;
        },

        isAllChapterCtgrSelected: function(selectedCtgrCds?: string[]): boolean {
            const selectableCtgrCds: string[] = dF.JournalDay.getSelectableChapterCtgrCds();
            const normalizedCtgrCds: string[] = dF.JournalDay.normalizeChapterCtgrCds(
                selectedCtgrCds ?? dF.JournalDay.currentSearchParams?.chapterCtgrCds ?? []
            );

            return selectableCtgrCds.length > 0
                && normalizedCtgrCds.length === selectableCtgrCds.length
                && selectableCtgrCds.every((ctgrCd: string): boolean => normalizedCtgrCds.includes(ctgrCd));
        },

        handleChapterCtgrMouseDown: function(event: MouseEvent): boolean {
            const target = event.target as HTMLElement | null;
            if (!(target instanceof HTMLOptionElement) || target.value !== dF.JournalDay.CHAPTER_CTGR_ALL) return true;

            event.preventDefault();

            if (!dF.JournalDay.currentSearchParams) dF.JournalDay.initSearchParams();

            const nextCtgrCds: string[] = dF.JournalDay.isAllChapterCtgrSelected()
                ? [dF.JournalDay.CHAPTER_CTGR_NONE]
                : dF.JournalDay.getSelectableChapterCtgrCds();

            dF.JournalDay.currentSearchParams.chapterCtgrCds = nextCtgrCds;
            dF.JournalDay.syncChapterCtgrSelectUi(nextCtgrCds);
            dF.JournalDay.changeChapterCtgr();

            return false;
        },

        /**
         * URL 파라미터로부터 파라미터 객체 초기화
         */
        toggleParam: function(): void {
            const showDiaries: boolean = $("#toggleDiaries").is(":checked");
            const showDreams: boolean = $("#toggleDreams").is(":checked");
            const showTagCloud: boolean = $("#toggleTagCloud").is(":checked");

            dF.JournalDay.currentSearchParams.showDiaries = showDiaries;
            dF.JournalDay.currentSearchParams.showDreams = showDreams;
            dF.JournalDay.currentSearchParams.showTagCloud = showTagCloud;
            dF.JournalDay.syncChapterCtgrState(showDiaries);
            dF.JournalDay.syncKeywordFilterState();

            // URL 동기화
            const url: URL = new URL(window.location.href);
            url.searchParams.set("showDiaries", String(showDiaries));
            url.searchParams.set("showDreams", String(showDreams));
            url.searchParams.set("showTagCloud", String(showTagCloud));
            window.history.replaceState(null, "", url.toString());

            // 재조회
            dF.JournalDay.reloadByView();
        },

        /**
         * diary/dream keyword filter state sync
         */
        syncKeywordFilterState: function(): void {
            const showDiaries: boolean = dF.JournalDay.currentSearchParams?.showDiaries === true;
            const showDreams: boolean = dF.JournalDay.currentSearchParams?.showDreams === true;
            const diaryElmt = $("#diaryFilterKeyword");
            const dreamElmt = $("#dreamFilterKeyword");

            diaryElmt.prop("disabled", !showDiaries);
            dreamElmt.prop("disabled", !showDreams);

            if (!showDiaries) {
                diaryElmt.val("");
                dF.JournalDay.currentSearchParams.diaryKeyword = "";
            }
            if (!showDreams) {
                dreamElmt.val("");
                dF.JournalDay.currentSearchParams.dreamKeyword = "";
            }
        },

        /**
         * diary/dream keyword filter apply
         */
        applyKeywordFilters: function(): void {
            if (!dF.JournalDay.currentSearchParams) dF.JournalDay.initSearchParams();

            const showDiaries: boolean = dF.JournalDay.currentSearchParams.showDiaries === true;
            const showDreams: boolean = dF.JournalDay.currentSearchParams.showDreams === true;

            const diaryKeyword: string = showDiaries
                ? String($("#diaryFilterKeyword").val() ?? "").trim()
                : "";
            const dreamKeyword: string = showDreams
                ? String($("#dreamFilterKeyword").val() ?? "").trim()
                : "";

            dF.JournalDay.currentSearchParams.diaryKeyword = diaryKeyword;
            dF.JournalDay.currentSearchParams.dreamKeyword = dreamKeyword;

            dF.JournalDay.reloadByView();
        },

        /**
         * chapter 카테고리 필터 상태 동기화
         */
        syncChapterCtgrState: function(showDiaries?: boolean): void {
            const showDiaryFilter: boolean = (showDiaries ?? dF.JournalDay.currentSearchParams?.showDiaries) === true;
            const toggleElmt = $("#toggleChapterCtgr");
            const selectElmt = $("#chapterCtgrFilter");
            const sectionElmt = $("#chapterCtgrFilterSection");

            if (!showDiaryFilter) {
                if (sectionElmt.length) sectionElmt.addClass("d-none");
                toggleElmt.prop("checked", false);
                toggleElmt.prop("disabled", true);
                selectElmt.prop("disabled", true);
                selectElmt.val([]);
                if (dF.JournalDay.currentSearchParams) dF.JournalDay.currentSearchParams.chapterCtgrCds = [];
                return;
            }

            if (sectionElmt.length) sectionElmt.removeClass("d-none");
            toggleElmt.prop("disabled", false);
            const enabled: boolean = toggleElmt.is(":checked");
            selectElmt.prop("disabled", !enabled);

            if (!enabled) {
                selectElmt.val([]);
                if (dF.JournalDay.currentSearchParams) dF.JournalDay.currentSearchParams.chapterCtgrCds = [];
                return;
            }

            let selectedCtgrCds: string[] = dF.JournalDay.currentSearchParams?.chapterCtgrCds ?? [];
            if (selectedCtgrCds.length === 0) {
                selectedCtgrCds = dF.JournalDay.getSelectableChapterCtgrCds();
            }
            const normalizedCtgrCds: string[] = dF.JournalDay.syncChapterCtgrSelectUi(selectedCtgrCds);
            if (dF.JournalDay.currentSearchParams) dF.JournalDay.currentSearchParams.chapterCtgrCds = normalizedCtgrCds;
        },

        /**
         * chapter 카테고리 필터 토글
         */
        toggleChapterCtgr: function(): void {
            if (!dF.JournalDay.currentSearchParams) dF.JournalDay.initSearchParams();

            if (!dF.JournalDay.currentSearchParams.showDiaries) {
                dF.JournalDay.syncChapterCtgrState(false);
                return;
            }

            const enabled: boolean = $("#toggleChapterCtgr").is(":checked");
            const selectElmt = $("#chapterCtgrFilter");

            if (!enabled) {
                selectElmt.prop("disabled", true);
                selectElmt.val([]);
                dF.JournalDay.currentSearchParams.chapterCtgrCds = [];
                dF.JournalDay.currentSearchParams.showChapterCtgr = false;
                const url: URL = new URL(window.location.href);
                url.searchParams.set("showChapterCtgr", "false");
                url.searchParams.delete("chapterCtgrCds");
                window.history.replaceState(null, "", url.toString());
                dF.JournalDay.reloadByView();
                return;
            }

            selectElmt.prop("disabled", false);
            dF.JournalDay.currentSearchParams.showChapterCtgr = true;
            let selectedCtgrCds: string[] = dF.JournalDay.currentSearchParams.chapterCtgrCds ?? [];
            if (selectedCtgrCds.length === 0) {
                selectedCtgrCds = dF.JournalDay.getSelectableChapterCtgrCds();
            }
            dF.JournalDay.currentSearchParams.chapterCtgrCds = dF.JournalDay.syncChapterCtgrSelectUi(selectedCtgrCds);
            const urlOn: URL = new URL(window.location.href);
            urlOn.searchParams.delete("showChapterCtgr");
            window.history.replaceState(null, "", urlOn.toString());
            dF.JournalDay.reloadByView();
        },

        /**
         * 저널 챕터 카테고리 필터 변경
         */
        changeChapterCtgr: function(): void {
            if (!dF.JournalDay.currentSearchParams) dF.JournalDay.initSearchParams();

            if (!dF.JournalDay.currentSearchParams.showDiaries) {
                dF.JournalDay.syncChapterCtgrState(false);
                return;
            }

            const enabled: boolean = $("#toggleChapterCtgr").is(":checked");
            const selectElmt = $("#chapterCtgrFilter");
            if (!enabled) {
                selectElmt.val([]);
                dF.JournalDay.currentSearchParams.chapterCtgrCds = [];
                dF.JournalDay.reloadByView();
                return;
            }

            const rawSelectedCtgrCds: string[] = (selectElmt.val() as string[] | null) ?? [];
            const selectedCtgrCds: string[] = dF.JournalDay.normalizeChapterCtgrCds(
                rawSelectedCtgrCds,
                [dF.JournalDay.CHAPTER_CTGR_NONE]
            );
            dF.JournalDay.currentSearchParams.chapterCtgrCds = selectedCtgrCds;
            dF.JournalDay.syncChapterCtgrSelectUi(selectedCtgrCds);
            dF.JournalDay.reloadByView();
        },

        /**
         * 저널 챕터 카테고리 필터 적용
         */
        filterByChapterCtgr: function(list: Record<string, any>[]): Record<string, any>[] {
            if (!Array.isArray(list) || list.length === 0) return list;

            const selectedCtgrCds: string[] = dF.JournalDay.currentSearchParams?.chapterCtgrCds ?? [];
            if (!Array.isArray(selectedCtgrCds) || selectedCtgrCds.length === 0) return list;

            // ?�택??비어 ?�으�?__NONE__ ?�로 ?�석?�고, ?�제 카테고리�??�터링합?�다.
            const hasNoneCategory: boolean = selectedCtgrCds.includes(dF.JournalDay.CHAPTER_CTGR_NONE);
            const ctgrSet: Set<string> = new Set(
                selectedCtgrCds.filter((ctgrCd: string): boolean => {
                    return ctgrCd !== dF.JournalDay.CHAPTER_CTGR_NONE && ctgrCd !== dF.JournalDay.CHAPTER_CTGR_ALL;
                })
            );

            if (!hasNoneCategory && ctgrSet.size === 0) return list;

            return list.map((day: Record<string, any>): Record<string, any> => {
                const journalChapterList: Record<string, any>[] = Array.isArray(day.journalChapterList) ? day.journalChapterList : [];
                const filteredChapterList: Record<string, any>[] = journalChapterList.filter((chapter: Record<string, any>): boolean => {
                    const ctgrCd: string = (chapter?.ctgrCd ?? "").trim();
                    if (cF.util.isEmpty(ctgrCd)) return true;

                    return ctgrSet.has(ctgrCd);
                });

                return { ...day, journalChapterList: filteredChapterList };
            });
        },

        /**
         * diary/dream keyword filter apply (list-level)
         */
        filterByKeyword: function(list: Record<string, any>[]): Record<string, any>[] {
            if (!Array.isArray(list) || list.length === 0) return list;

            const diaryKeyword: string = String(dF.JournalDay.currentSearchParams?.diaryKeyword ?? "").trim().toLowerCase();
            const dreamKeyword: string = String(dF.JournalDay.currentSearchParams?.dreamKeyword ?? "").trim().toLowerCase();
            const filterDiaries: boolean = diaryKeyword.length > 0 && dF.JournalDay.currentSearchParams?.showDiaries === true;
            const filterDreams: boolean = dreamKeyword.length > 0 && dF.JournalDay.currentSearchParams?.showDreams === true;

            if (!filterDiaries && !filterDreams) return list;

            const containsKeyword = (value: any, keyword: string): boolean => {
                if (typeof value !== "string") return false;
                return value.toLowerCase().includes(keyword);
            };

            return list
                .map((day: Record<string, any>): Record<string, any> => {
                    const nextDay = { ...day };

                    if (filterDiaries) {
                        const journalChapterList: Record<string, any>[] = Array.isArray(day.journalChapterList) ? day.journalChapterList : [];
                        nextDay.journalChapterList = journalChapterList
                            .map((entry: Record<string, any>): Record<string, any> | null => {
                                const journalDiaryList: Record<string, any>[] = Array.isArray(entry?.journalDiaryList) ? entry.journalDiaryList : [];
                                const filteredDiaryList: Record<string, any>[] = journalDiaryList.filter((diary: Record<string, any>): boolean => {
                                    return containsKeyword(diary?.cn, diaryKeyword);
                                });
                                if (filteredDiaryList.length === 0) return null;
                                return {...entry, journalDiaryList: filteredDiaryList};
                            })
                            .filter((entry): entry is Record<string, any> => entry !== null);
                    }

                    if (filterDreams) {
                        const journalDreamList: Record<string, any>[] = Array.isArray(day.journalDreamList) ? day.journalDreamList : [];
                        const journalElseDreamList: Record<string, any>[] = Array.isArray(day.journalElseDreamList) ? day.journalElseDreamList : [];
                        nextDay.journalDreamList = journalDreamList.filter((dream: Record<string, any>): boolean => {
                            return containsKeyword(dream?.cn, dreamKeyword);
                        });
                        nextDay.journalElseDreamList = journalElseDreamList.filter((dream: Record<string, any>): boolean => {
                            return containsKeyword(dream?.cn, dreamKeyword);
                        });
                        nextDay.hasDream = (nextDay.journalDreamList.length + nextDay.journalElseDreamList.length) > 0;
                    }

                    return nextDay;
                })
                .filter((day: Record<string, any>): boolean => {
                    if (filterDiaries) {
                        const hasEntry: boolean = Array.isArray(day.journalChapterList) && day.journalChapterList.length > 0;
                        if (!hasEntry) return false;
                    }
                    if (filterDreams) {
                        const hasDream: boolean = (Array.isArray(day.journalDreamList) && day.journalDreamList.length > 0)
                            || (Array.isArray(day.journalElseDreamList) && day.journalElseDreamList.length > 0);
                        if (!hasDream) return false;
                    }
                    return true;
                });
        },

        /**
         * 년도-월 목록 조회 (Ajax)
         */
        yyMnthListAjax: function(): void {
            dF.JournalDay.initSearchParams();

            // 🔹 1. URL 동기화
            const urlObj: URL = new URL(window.location.href);
            const params: Record<string, any> = dF.JournalDay.currentSearchParams;

            Object.keys(params).forEach(key => {
                urlObj.searchParams.set(key, String(params[key]));
            });

            window.history.replaceState(null, "", urlObj.toString());

            const url: string = Url.JOURNAL_DAYS;
            cF.ajax.get(url, dF.JournalDay.currentSearchParams, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                const filteredList: Record<string, any>[] = rsltList;
                // 정렬 처리
                const sortStr: string = $("#journal_aside #sort").val() as string;
                if (sortStr === "ASC") {
                    $("#journal_aside #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
                } else {
                    $("#journal_aside #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
                    if (cF.util.isNotEmpty(filteredList)) filteredList.reverse();
                }
                $("#journal_diary_list_div").empty();
                $("#journal_dream_list_div").empty();
                cF.ui.closeModal();
                const renderModel = {
                    list: filteredList,
                    showDiaries: dF.JournalDay.currentSearchParams.showDiaries,
                    showDreams: dF.JournalDay.currentSearchParams.showDreams
                };
                cF.handlebars.template(renderModel, "journal_day_list");
                KTMenu.createInstances();

                const { showDiaries, showDreams, showTagCloud } = dF.JournalDay.currentSearchParams;
                $("#journal_tag_header").toggle(showTagCloud);
                if (showTagCloud) {
                    dF.JournalDayTag.listAjax();
                    $("#journal_diary_tag_header").toggleClass("d-none", !showDiaries);
                    if (showDiaries) dF.JournalDiaryTag.listAjax();
                    $("#journal_dream_tag_header").toggleClass("d-none", !showDreams);
                    if (showDreams) dF.JournalDreamTag.listAjax();
                }
            }, "block");
        },

        /**
         * 일자 조회 새 창 열기
         * @param {string} stdrdDt 기준 일자
         */
        openDetatched: function(stdrdDt: string): void {
            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY_DAILY_VIEW, { stdrdDt });
            window.open(url, '_blank', 'noopener,noreferrer');
        },

        /**
         * build weekly view url with current filter state
         * @param {string} stdrdDt 湲곗? ?쇱옄
         * @return {string}
         */
        buildWeeklyViewUrl: function(stdrdDt: string, targetDt?: string): string {
            dF.JournalDay.initSearchParams();

            const currentParams: Record<string, any> = dF.JournalDay.currentSearchParams ?? {};
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
        },

        /**
         * 주간 뷰로 이동
         * @param {string} stdrdDt 湲곗? ?쇱옄
         */
        moveToWeeklyView: function(stdrdDt: string): void {
            cF.ui.blockUIReplace(dF.JournalDay.buildWeeklyViewUrl(stdrdDt));
        },

        /**
         * 상세 일자 데이터 조회 (Ajax)
         * @param {string} stdrdDt 기준 일자
         */
        getStdrdData: function(stdrdDt: string): void {
            const url: string = Url.JOURNAL_DAYS + `?viewType=daily&stdrdDt=${stdrdDt}`;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                const renderModel = {
                    list: rsltList,
                    showDiaries: true,
                    showDreams: true
                };
                cF.handlebars.template(renderModel, "journal_day_list");
                KTMenu.createInstances();
            }, "block");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            const obj: Record<string, any> = { "journalDt": dF.JournalDay.validDt() };
            /* initialize form. */
            dF.JournalDay.initForm(obj);

            /* modal history push */
            ModalHistory.push(this, arguments.callee.name, Array.from(arguments));
        },

        /**
         * 사이드바 기준으로 등록 모달 날짜 계산:: 메소드 분리
         */
        validDt: function(): string {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy");
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth");

            const year: number = parseInt(yy, 10);
            let month: number = parseInt(mnth, 10);
            if (month === 99) month = 1;
            let day: number = parseInt(cF.date.getCurrDayStr(2), 10);

            // 만약 day가 해당 월의 마지막 날을 초과하면 마지막 날로 설정
            const lastDay: number = new Date(year, month, 0).getDate();
            if (day > lastDay) day = lastDay;

            // 결과를 yyyy-mm-dd 형식으로 반환
            const yyyyStr: string = year.toString();
            const mmStr: string = month.toString().padStart(2, '0'); // 월을 두 자리로 포맷팅
            const ddStr: string = day.toString().padStart(2, '0');   // 일을 두 자리로 포맷팅

            return yyyyStr + '-' + mmStr + '-' + ddStr;
        },

        /**
         * 아이콘 새로고침
         */
        refreshIcon: function(): void {
            const iconClassElmt: HTMLInputElement = document.querySelector("#journalDayRegForm #weather");
            if (!iconClassElmt) return;

            // val() 메서드는 string | null을 반환하므로, null 체크 필요
            const iconVal: string = iconClassElmt.value;
            if (cF.util.isNotEmpty(iconVal)) {
                const weatherIconDiv: HTMLElement = document.querySelector("#journalDayRegForm #weather_icon_div") as HTMLElement;
                if (weatherIconDiv) weatherIconDiv.innerHTML = iconVal;
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#journalDayRegForm").submit();
        },

        /**
         * 등록/수정 처리 (Ajax)
         */
        regAjax: function(): void {
            const id: string = cF.util.getInputValue("#journalDayRegForm [name='id']");
            const isMdf: boolean = cF.util.isNotEmpty(id);

            // 등록 클릭시 입력 중이던 메타 추가
            if (dF.JournalDay.metaTagify?.draft?.value) {
                const meta: string = cF.util.getInputValue("#meta_value");
                const { value, ctgr } = dF.JournalDay.metaTagify?.draft;
                if (value && meta) {
                    cF.tagify.commitTag(dF.JournalDay.metaTagify, value, ctgr, meta);
                }
            }
            setTimeout((): void => {
                Swal.fire({
                    text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_DAY, { id }) : Url.JOURNAL_DAYS;
                    const ajaxData: FormData = new FormData(document.getElementById("journalDayRegForm") as HTMLFormElement);
                    cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                        Swal.fire({ text: res.message })
                            .then(function(): void {
                                if (!res.rslt) return;

                                dF.JournalDay.refresh();
                            });
                    }, "block");
                });
            }, 0);

        },

        /**
         * 상세 모달 호출
         * @param {string|number} id 글 번호.
         */
        dtlModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "journal_day_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} id 글 번호.
         */
        mdfModal: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id })
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* initialize form. */
                dF.JournalDay.initForm(rsltObj);
                
                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} id - 글 번호.
         */
        delAjax: function(id: string|number): void {
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JournalDay.refresh();
                        });
                }, "block");
            });
        },

        /**
         * 모달 닫기 시 수행할 로직
         */
        closeModal: function(): void {
            /* modal history pop */
            ModalHistory.prev();
        }
    }
})();
