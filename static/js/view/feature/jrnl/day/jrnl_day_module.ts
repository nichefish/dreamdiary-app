/**
 * jrnl_day_module.ts
 * 저널 일자 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDay = (function(): dfModule {
    return {
        initialized: false,
        viewType: null,
        tagTagify: null,
        metaTagify: null,
        currentSearchParams: null,
        ENTRY_CTGR_NONE: "__NONE__",
        ENTRY_CTGR_DEFAULT: "SUMMARY",

        /**
         * initializes module.
         * @param {"LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"} viewType
         */
        init: function(viewType: "LIST"|"CAL"|"DAILY"|"WEEKLY"|"SEARCH"): void {
            if (dF.JrnlDay.initialized) return;

            dF.JrnlDay.viewType = viewType;

            /* initialize submodules. */
            dF.JrnlDayTag.init();
            dF.JrnlDayMeta.init();

            dF.JrnlDay.initialized = true;
            console.log("'dF.JrnlDay' module initialized.");
        },

        /**
         * refresh
         */
        refresh: function(): void {
            switch (dF.JrnlDay.viewType) {
                case "LIST":
                    dF.JrnlDay.yyMnthListAjax();
                    break;
                case "CAL":
                    Page.refreshEventList();
                    dF.JrnlDayTag.listAjax();     // 태그 refresh
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
            switch (dF.JrnlDay.viewType) {
                case "LIST":
                    dF.JrnlDay.yyMnthListAjax();
                    break;
                case "WEEKLY":
                    Page.loadWeek(Page.stdrdDt);
                    break;
                default:
                    dF.JrnlDay.refresh();
                    break;
            }
        },

        /**
         * resolve anchor date for cross-view navigation
         */
        resolveAnchorDateForView: function(): string {
            if (dF.JrnlDay.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.stdrdDt)) {
                return Page.stdrdDt;
            }

            if (dF.JrnlDay.viewType === "CAL" && Page?.calDt instanceof Date) {
                return cF.date.dateToStr(Page.calDt, cF.date.ptnDate) ?? "";
            }

            dF.JrnlDay.initSearchParams();
            const currentParams: Record<string, any> = dF.JrnlDay.currentSearchParams ?? {};
            if (cF.util.isNotEmpty(currentParams.stdrdDt)) return currentParams.stdrdDt;

            const yy: string = currentParams.yy ?? localStorage.getItem("jrnl_yy") ?? cF.date.getCurrYyStr();
            const mnth: string = currentParams.mnth ?? localStorage.getItem("jrnl_mnth") ?? cF.date.getCurrMnthStr();
            return dF.JrnlDayAside.buildAnchorDateForMonth(yy, mnth, 1);
        },

        /**
         * build view url with current period/filter state
         * @param {string} baseUrl
         */
        buildViewUrl: function(baseUrl: string): string {
            dF.JrnlDay.initSearchParams();

            const currentParams: Record<string, any> = dF.JrnlDay.currentSearchParams ?? {};
            const anchorDate: string = dF.JrnlDay.resolveAnchorDateForView();
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
            if (cF.util.isNotEmpty(currentParams.diaryKeyword)) targetUrl.searchParams.set("diaryKeyword", currentParams.diaryKeyword);
            if (cF.util.isNotEmpty(currentParams.dreamKeyword)) targetUrl.searchParams.set("dreamKeyword", currentParams.dreamKeyword);
            if (Array.isArray(currentParams.entryCtgrCds) && currentParams.entryCtgrCds.length > 0) {
                targetUrl.searchParams.set("entryCtgrCds", currentParams.entryCtgrCds.join(","));
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
            cF.handlebars.modal(obj, "jrnl_day_reg");

            /* jquery validation */
            const form: HTMLFormElement = document.querySelector("#jrnlDayRegForm") as HTMLFormElement;
            cF.validate.validateForm(form, dF.JrnlDay.regAjax, {
                rules: {
                    jrnlDt: {
                        required: function(): boolean {
                            const dtUnknownYn: HTMLInputElement = document.querySelector("#jrnlDayRegForm #dtUnknownYn");
                            return !dtUnknownYn?.checked;
                        }
                    },
                    aprxmtDt: {
                        required: function(): boolean {
                            const dtUnknownYn: HTMLInputElement = document.querySelector("#jrnlDayRegForm #dtUnknownYn");
                            return dtUnknownYn?.checked;
                        }
                    },
                },
                ignore: undefined
            });
            // 체크박스 상태 변경시 필드 재검증
            $("#dtUnknownYn").change(function(): void {
                $("#jrnlDt").valid();
                $("#aprxmtDt").valid();
            });
            // 일자 datepicker 날짜 검색 init : 현재 조회중인 yyyy-MM으로 처리
            cF.datepicker.singleDatePicker("#jrnlDt", "yyyy-MM-DD", obj.jrnlDt);
            // 날짜미상 datepicker 날짜 검색 init
            cF.datepicker.singleDatePicker("#aprxmtDt", "yyyy-MM-DD", obj.aprxmtDt);
            // checkbox init
            cF.ui.chckboxLabel("#jrnlDayRegForm #diaryResolvedYn", "완료//미완료", "blue//gray");
            cF.ui.chckboxLabel("#jrnlDayRegForm #dtUnknownYn", "날짜미상//날짜미상", "blue//gray", function(): void {
                $("#jrnlDayRegForm #jrnlDtDiv").addClass("d-none");
                $("#jrnlDayRegForm #aprxmtDtDiv").removeClass("d-none");
                $("#jrnlDayRegForm #aprxmtDt").val($("#jrnlDayRegForm #jrnlDt").val());
            }, function(): void {
                $("#jrnlDayRegForm #jrnlDtDiv").removeClass("d-none");
                $("#jrnlDayRegForm #aprxmtDtDiv").addClass("d-none");
                $("#jrnlDayRegForm #jrnlDt").val($("#jrnlDayRegForm #aprxmtDt").val());
            });
            /* tagify */
            dF.JrnlDay.tagTagify = cF.tagify.initWithCtgr("#jrnlDayRegForm #tagListStr", dF.JrnlDayTag.ctgrMap);
            dF.JrnlDay.metaTagify = cF.tagify.initMeta("#jrnlDayRegForm #metaListStr", dF.JrnlDayMeta.ctgrMap);
        },

        /**
         * URL 파라미터로부터 파라미터 객체 초기화
         */
        initSearchParams: function(): void {
            if (dF.JrnlDay.currentSearchParams) return;

            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy") ?? "9999";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth") ?? "99";
            const showDiaries = cF.util.getUrlParam("showDiaries") !== "false";
            const showDreams = cF.util.getUrlParam("showDreams") !== "false";
            const showTagCloud = cF.util.getUrlParam("showTagCloud") !== "false";
            const rawEntryCtgr = cF.util.getUrlParam("entryCtgrCds") ?? cF.util.getUrlParam("entryCtgrCd") ?? "";
            const entryCtgrCds: string[] = dF.JrnlDay.parseEntryCtgrCds(rawEntryCtgr);
            const diaryKeyword: string = cF.util.getUrlParam("diaryKeyword") ?? "";
            const dreamKeyword: string = cF.util.getUrlParam("dreamKeyword") ?? "";
            const stdrdDt: string = cF.util.getUrlParam("stdrdDt") ?? "";

            dF.JrnlDay.currentSearchParams = {
                "viewType": "list",
                yy,
                mnth,
                stdrdDt,
                showDiaries,
                showDreams,
                showTagCloud,
                entryCtgrCds,
                diaryKeyword,
                dreamKeyword
            };

            // DOM에 상태 반영
            $("#toggleDiaries").prop("checked", showDiaries);
            $("#toggleDreams").prop("checked", showDreams);
            $("#toggleTagCloud").prop("checked", showTagCloud);
            $("#toggleEntryCtgr").prop("checked", entryCtgrCds.length > 0);
            $("#entryCtgrFilter").val(entryCtgrCds);
            dF.JrnlDay.currentSearchParams.entryCtgrCds = entryCtgrCds;
            dF.JrnlDay.syncEntryCtgrState(showDiaries);
            $("#diaryFilterKeyword").val(diaryKeyword);
            $("#dreamFilterKeyword").val(dreamKeyword);
            dF.JrnlDay.syncKeywordFilterState();
        },

        /**
         * URL 파라미터를 다중 카테고리 배열로 파싱
         */
        parseEntryCtgrCds: function(rawValue: string): string[] {
            if (cF.util.isEmpty(rawValue)) return [];

            return rawValue
                .split(",")
                .map((value: string): string => value.trim())
                .filter((value: string): boolean => value.length > 0);
        },

        /**
         * URL 파라미터로부터 파라미터 객체 초기화
         */
        toggleParam: function(): void {
            const showDiaries: boolean = $("#toggleDiaries").is(":checked");
            const showDreams: boolean = $("#toggleDreams").is(":checked");
            const showTagCloud: boolean = $("#toggleTagCloud").is(":checked");

            dF.JrnlDay.currentSearchParams.showDiaries = showDiaries;
            dF.JrnlDay.currentSearchParams.showDreams = showDreams;
            dF.JrnlDay.currentSearchParams.showTagCloud = showTagCloud;
            dF.JrnlDay.syncEntryCtgrState(showDiaries);
            dF.JrnlDay.syncKeywordFilterState();

            // URL 동기화
            const url: URL = new URL(window.location.href);
            url.searchParams.set("showDiaries", String(showDiaries));
            url.searchParams.set("showDreams", String(showDreams));
            url.searchParams.set("showTagCloud", String(showTagCloud));
            window.history.replaceState(null, "", url.toString());

            // 재조회
            dF.JrnlDay.reloadByView();
        },

        /**
         * diary/dream keyword filter state sync
         */
        syncKeywordFilterState: function(): void {
            const showDiaries: boolean = dF.JrnlDay.currentSearchParams?.showDiaries === true;
            const showDreams: boolean = dF.JrnlDay.currentSearchParams?.showDreams === true;
            const diaryElmt = $("#diaryFilterKeyword");
            const dreamElmt = $("#dreamFilterKeyword");

            diaryElmt.prop("disabled", !showDiaries);
            dreamElmt.prop("disabled", !showDreams);

            if (!showDiaries) {
                diaryElmt.val("");
                dF.JrnlDay.currentSearchParams.diaryKeyword = "";
            }
            if (!showDreams) {
                dreamElmt.val("");
                dF.JrnlDay.currentSearchParams.dreamKeyword = "";
            }
        },

        /**
         * diary/dream keyword filter apply
         */
        applyKeywordFilters: function(): void {
            if (!dF.JrnlDay.currentSearchParams) dF.JrnlDay.initSearchParams();

            const showDiaries: boolean = dF.JrnlDay.currentSearchParams.showDiaries === true;
            const showDreams: boolean = dF.JrnlDay.currentSearchParams.showDreams === true;

            const diaryKeyword: string = showDiaries
                ? String($("#diaryFilterKeyword").val() ?? "").trim()
                : "";
            const dreamKeyword: string = showDreams
                ? String($("#dreamFilterKeyword").val() ?? "").trim()
                : "";

            dF.JrnlDay.currentSearchParams.diaryKeyword = diaryKeyword;
            dF.JrnlDay.currentSearchParams.dreamKeyword = dreamKeyword;

            dF.JrnlDay.reloadByView();
        },

        /**
         * entry 카테고리 필터 상태 동기화
         */
        syncEntryCtgrState: function(showDiaries?: boolean): void {
            const showDiaryFilter: boolean = (showDiaries ?? dF.JrnlDay.currentSearchParams?.showDiaries) === true;
            const toggleElmt = $("#toggleEntryCtgr");
            const selectElmt = $("#entryCtgrFilter");
            const sectionElmt = $("#entryCtgrFilterSection");

            if (!showDiaryFilter) {
                if (sectionElmt.length) sectionElmt.addClass("d-none");
                toggleElmt.prop("checked", false);
                toggleElmt.prop("disabled", true);
                selectElmt.prop("disabled", true);
                selectElmt.val([]);
                if (dF.JrnlDay.currentSearchParams) dF.JrnlDay.currentSearchParams.entryCtgrCds = [];
                return;
            }

            if (sectionElmt.length) sectionElmt.removeClass("d-none");
            toggleElmt.prop("disabled", false);
            const enabled: boolean = toggleElmt.is(":checked");
            selectElmt.prop("disabled", !enabled);

            if (!enabled) {
                selectElmt.val([]);
                if (dF.JrnlDay.currentSearchParams) dF.JrnlDay.currentSearchParams.entryCtgrCds = [];
                return;
            }

            let selectedCtgrCds: string[] = (selectElmt.val() as string[] | null) ?? [];
            if (selectedCtgrCds.length === 0) {
                selectedCtgrCds = [dF.JrnlDay.ENTRY_CTGR_DEFAULT];
                selectElmt.val(selectedCtgrCds);
            }
            if (dF.JrnlDay.currentSearchParams) dF.JrnlDay.currentSearchParams.entryCtgrCds = selectedCtgrCds;
        },

        /**
         * entry 카테고리 필터 토글
         */
        toggleEntryCtgr: function(): void {
            if (!dF.JrnlDay.currentSearchParams) dF.JrnlDay.initSearchParams();

            if (!dF.JrnlDay.currentSearchParams.showDiaries) {
                dF.JrnlDay.syncEntryCtgrState(false);
                return;
            }

            const enabled: boolean = $("#toggleEntryCtgr").is(":checked");
            const selectElmt = $("#entryCtgrFilter");

            if (!enabled) {
                selectElmt.prop("disabled", true);
                selectElmt.val([]);
                dF.JrnlDay.currentSearchParams.entryCtgrCds = [];
                dF.JrnlDay.reloadByView();
                return;
            }

            selectElmt.prop("disabled", false);
            let selectedCtgrCds: string[] = (selectElmt.val() as string[] | null) ?? [];
            if (selectedCtgrCds.length === 0) {
                selectedCtgrCds = [dF.JrnlDay.ENTRY_CTGR_DEFAULT];
                selectElmt.val(selectedCtgrCds);
            }
            dF.JrnlDay.currentSearchParams.entryCtgrCds = selectedCtgrCds;
            dF.JrnlDay.reloadByView();
        },

        /**
         * 저널 항목 카테고리 필터 변경
         */
        changeEntryCtgr: function(): void {
            if (!dF.JrnlDay.currentSearchParams) dF.JrnlDay.initSearchParams();

            if (!dF.JrnlDay.currentSearchParams.showDiaries) {
                dF.JrnlDay.syncEntryCtgrState(false);
                return;
            }

            const enabled: boolean = $("#toggleEntryCtgr").is(":checked");
            const selectElmt = $("#entryCtgrFilter");
            if (!enabled) {
                selectElmt.val([]);
                dF.JrnlDay.currentSearchParams.entryCtgrCds = [];
                dF.JrnlDay.reloadByView();
                return;
            }

            let selectedCtgrCds: string[] = (selectElmt.val() as string[] | null) ?? [];
            if (selectedCtgrCds.length === 0) {
                selectedCtgrCds = [dF.JrnlDay.ENTRY_CTGR_DEFAULT];
                selectElmt.val(selectedCtgrCds);
            }
            dF.JrnlDay.currentSearchParams.entryCtgrCds = selectedCtgrCds;
            dF.JrnlDay.reloadByView();
        },

        /**
         * 저널 항목 카테고리 필터 적용
         */
        filterByEntryCtgr: function(list: Record<string, any>[]): Record<string, any>[] {
            if (!Array.isArray(list) || list.length === 0) return list;

            const selectedCtgrCds: string[] = dF.JrnlDay.currentSearchParams?.entryCtgrCds ?? [];
            if (!Array.isArray(selectedCtgrCds) || selectedCtgrCds.length === 0) return list;

            const hasNoneCategory: boolean = selectedCtgrCds.includes(dF.JrnlDay.ENTRY_CTGR_NONE);
            const ctgrSet: Set<string> = new Set(
                selectedCtgrCds.filter((ctgrCd: string): boolean => ctgrCd !== dF.JrnlDay.ENTRY_CTGR_NONE)
            );

            return list.map((day: Record<string, any>): Record<string, any> => {
                const jrnlEntryList: Record<string, any>[] = Array.isArray(day.jrnlEntryList) ? day.jrnlEntryList : [];
                const filteredEntryList: Record<string, any>[] = jrnlEntryList.filter((entry: Record<string, any>): boolean => {
                    const ctgrCd: string = (entry?.ctgrCd ?? "").trim();
                    if (cF.util.isEmpty(ctgrCd)) return hasNoneCategory;

                    return ctgrSet.has(ctgrCd);
                });

                return { ...day, jrnlEntryList: filteredEntryList };
            });
        },

        /**
         * diary/dream keyword filter apply (list-level)
         */
        filterByKeyword: function(list: Record<string, any>[]): Record<string, any>[] {
            if (!Array.isArray(list) || list.length === 0) return list;

            const diaryKeyword: string = String(dF.JrnlDay.currentSearchParams?.diaryKeyword ?? "").trim().toLowerCase();
            const dreamKeyword: string = String(dF.JrnlDay.currentSearchParams?.dreamKeyword ?? "").trim().toLowerCase();
            const filterDiaries: boolean = diaryKeyword.length > 0 && dF.JrnlDay.currentSearchParams?.showDiaries === true;
            const filterDreams: boolean = dreamKeyword.length > 0 && dF.JrnlDay.currentSearchParams?.showDreams === true;

            if (!filterDiaries && !filterDreams) return list;

            const containsKeyword = (value: any, keyword: string): boolean => {
                if (typeof value !== "string") return false;
                return value.toLowerCase().includes(keyword);
            };

            return list
                .map((day: Record<string, any>): Record<string, any> => {
                    const nextDay = { ...day };

                    if (filterDiaries) {
                        const jrnlEntryList: Record<string, any>[] = Array.isArray(day.jrnlEntryList) ? day.jrnlEntryList : [];
                        const filteredEntryList: Record<string, any>[] = jrnlEntryList
                            .map((entry: Record<string, any>): Record<string, any> | null => {
                                const jrnlDiaryList: Record<string, any>[] = Array.isArray(entry?.jrnlDiaryList) ? entry.jrnlDiaryList : [];
                                const filteredDiaryList: Record<string, any>[] = jrnlDiaryList.filter((diary: Record<string, any>): boolean => {
                                    return containsKeyword(diary?.cn, diaryKeyword);
                                });
                                if (filteredDiaryList.length === 0) return null;
                                return { ...entry, jrnlDiaryList: filteredDiaryList };
                            })
                            .filter((entry): entry is Record<string, any> => entry !== null);
                        nextDay.jrnlEntryList = filteredEntryList;
                    }

                    if (filterDreams) {
                        const jrnlDreamList: Record<string, any>[] = Array.isArray(day.jrnlDreamList) ? day.jrnlDreamList : [];
                        const jrnlElseDreamList: Record<string, any>[] = Array.isArray(day.jrnlElseDreamList) ? day.jrnlElseDreamList : [];
                        nextDay.jrnlDreamList = jrnlDreamList.filter((dream: Record<string, any>): boolean => {
                            return containsKeyword(dream?.cn, dreamKeyword);
                        });
                        nextDay.jrnlElseDreamList = jrnlElseDreamList.filter((dream: Record<string, any>): boolean => {
                            return containsKeyword(dream?.cn, dreamKeyword);
                        });
                        nextDay.hasDream = (nextDay.jrnlDreamList.length + nextDay.jrnlElseDreamList.length) > 0;
                    }

                    return nextDay;
                })
                .filter((day: Record<string, any>): boolean => {
                    if (filterDiaries) {
                        const hasEntry: boolean = Array.isArray(day.jrnlEntryList) && day.jrnlEntryList.length > 0;
                        if (!hasEntry) return false;
                    }
                    if (filterDreams) {
                        const hasDream: boolean = (Array.isArray(day.jrnlDreamList) && day.jrnlDreamList.length > 0)
                            || (Array.isArray(day.jrnlElseDreamList) && day.jrnlElseDreamList.length > 0);
                        if (!hasDream) return false;
                    }
                    return true;
                });
        },

        /**
         * 년도-월 목록 조회 (Ajax)
         */
        yyMnthListAjax: function(): void {
            dF.JrnlDay.initSearchParams();

            // 🔹 1. URL 동기화
            const urlObj: URL = new URL(window.location.href);
            const params: Record<string, any> = dF.JrnlDay.currentSearchParams;

            Object.keys(params).forEach(key => {
                urlObj.searchParams.set(key, String(params[key]));
            });

            window.history.replaceState(null, "", urlObj.toString());

            const url: string = Url.JRNL_DAYS;
            cF.ajax.get(url, dF.JrnlDay.currentSearchParams, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltList } = res;
                const filteredList: Record<string, any>[] = rsltList;
                // 정렬 처리
                const sortStr: string = $("#jrnl_aside #sort").val() as string;
                if (sortStr === "ASC") {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
                } else {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
                    if (cF.util.isNotEmpty(filteredList)) filteredList.reverse();
                }
                $("#jrnl_diary_list_div").empty();
                $("#jrnl_dream_list_div").empty();
                cF.ui.closeModal();
                const renderModel = {
                    list: filteredList,
                    showDiaries: dF.JrnlDay.currentSearchParams.showDiaries,
                    showDreams: dF.JrnlDay.currentSearchParams.showDreams
                };
                cF.handlebars.template(renderModel, "jrnl_day_list");
                KTMenu.createInstances();

                const { showDiaries, showDreams, showTagCloud } = dF.JrnlDay.currentSearchParams;
                $("#jrnl_tag_header").toggle(showTagCloud);
                if (showTagCloud) {
                    dF.JrnlDayTag.listAjax();
                    $("#jrnl_diary_tag_header").toggleClass("d-none", !showDiaries);
                    if (showDiaries) dF.JrnlDiaryTag.listAjax();
                    $("#jrnl_dream_tag_header").toggleClass("d-none", !showDreams);
                    if (showDreams) dF.JrnlDreamTag.listAjax();
                }
            }, "block");
        },

        /**
         * 일자 조회 새 창 열기
         * @param {string} stdrdDt 기준 일자
         */
        openDetatched: function(stdrdDt: string): void {
            const url: string = cF.util.bindUrl(Url.JRNL_DAY_DAILY_VIEW, { stdrdDt });
            window.open(url, '_blank', 'noopener,noreferrer');
        },

        /**
         * build weekly view url with current filter state
         * @param {string} stdrdDt 湲곗? ?쇱옄
         * @return {string}
         */
        buildWeeklyViewUrl: function(stdrdDt: string, targetDt?: string): string {
            dF.JrnlDay.initSearchParams();

            const currentParams: Record<string, any> = dF.JrnlDay.currentSearchParams ?? {};
            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            const targetUrl: URL = new URL(Url.JRNL_DAY_WEEKLY, window.location.origin);

            targetUrl.searchParams.set("stdrdDt", stdrdDt);
            targetUrl.searchParams.set("yy", yy);
            targetUrl.searchParams.set("mnth", mnth);
            if (cF.util.isNotEmpty(targetDt)) targetUrl.searchParams.set("target", targetDt);
            if (typeof currentParams.showDiaries === "boolean") targetUrl.searchParams.set("showDiaries", String(currentParams.showDiaries));
            if (typeof currentParams.showDreams === "boolean") targetUrl.searchParams.set("showDreams", String(currentParams.showDreams));
            if (typeof currentParams.showTagCloud === "boolean") targetUrl.searchParams.set("showTagCloud", String(currentParams.showTagCloud));
            if (cF.util.isNotEmpty(currentParams.diaryKeyword)) targetUrl.searchParams.set("diaryKeyword", currentParams.diaryKeyword);
            if (cF.util.isNotEmpty(currentParams.dreamKeyword)) targetUrl.searchParams.set("dreamKeyword", currentParams.dreamKeyword);
            if (Array.isArray(currentParams.entryCtgrCds) && currentParams.entryCtgrCds.length > 0) {
                targetUrl.searchParams.set("entryCtgrCds", currentParams.entryCtgrCds.join(","));
            }
            if (cF.util.isNotEmpty(currentParams.sort)) targetUrl.searchParams.set("sort", currentParams.sort);

            return `${targetUrl.pathname}${targetUrl.search}${targetUrl.hash}`;
        },

        /**
         * 주간 뷰로 이동
         * @param {string} stdrdDt 湲곗? ?쇱옄
         */
        moveToWeeklyView: function(stdrdDt: string): void {
            cF.ui.blockUIReplace(dF.JrnlDay.buildWeeklyViewUrl(stdrdDt));
        },

        /**
         * 상세 일자 데이터 조회 (Ajax)
         * @param {string} stdrdDt 기준 일자
         */
        getStdrdData: function(stdrdDt: string): void {
            const url: string = Url.JRNL_DAYS + `?viewType=daily&stdrdDt=${stdrdDt}`;
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
                cF.handlebars.template(renderModel, "jrnl_day_list");
                KTMenu.createInstances();
            }, "block");
        },

        /**
         * 등록 모달 호출
         */
        regModal: function(): void {
            const obj: Record<string, any> = { "jrnlDt": dF.JrnlDay.validDt() };
            /* initialize form. */
            dF.JrnlDay.initForm(obj);

            /* modal history push */
            ModalHistory.push(this, arguments.callee.name, Array.from(arguments));
        },

        /**
         * 사이드바 기준으로 등록 모달 날짜 계산:: 메소드 분리
         */
        validDt: function(): string {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy");
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth");

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
            const iconClassElmt: HTMLInputElement = document.querySelector("#jrnlDayRegForm #weather");
            if (!iconClassElmt) return;

            // val() 메서드는 string | null을 반환하므로, null 체크 필요
            const iconVal: string = iconClassElmt.value;
            if (cF.util.isNotEmpty(iconVal)) {
                const weatherIconDiv: HTMLElement = document.querySelector("#jrnlDayRegForm #weather_icon_div") as HTMLElement;
                if (weatherIconDiv) weatherIconDiv.innerHTML = iconVal;
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#jrnlDayRegForm").submit();
        },

        /**
         * 등록/수정 처리 (Ajax)
         */
        regAjax: function(): void {
            const postNo: string = cF.util.getInputValue("#jrnlDayRegForm [name='postNo']");
            const isMdf: boolean = cF.util.isNotEmpty(postNo);

            // 등록 클릭시 입력 중이던 메타 추가
            if (dF.JrnlDay.metaTagify?.draft?.value) {
                const meta: string = cF.util.getInputValue("#meta_value");
                const { value, ctgr } = dF.JrnlDay.metaTagify?.draft;
                if (value && meta) {
                    cF.tagify.commitTag(dF.JrnlDay.metaTagify, value, ctgr, meta);
                }
            }
            setTimeout((): void => {
                Swal.fire({
                    text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
                    showCancelButton: true,
                }).then(function(result: SwalResult): void {
                    if (!result.value) return;

                    const url: string = isMdf ? cF.util.bindUrl(Url.JRNL_DAY, { postNo }) : Url.JRNL_DAYS;
                    const ajaxData: FormData = new FormData(document.getElementById("jrnlDayRegForm") as HTMLFormElement);
                    cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                        Swal.fire({ text: res.message })
                            .then(function(): void {
                                if (!res.rslt) return;

                                dF.JrnlDay.refresh();
                            });
                    }, "block");
                });
            }, 0);

        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_DAY, { postNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* show modal */
                cF.handlebars.modal(rsltObj, "jrnl_day_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 수정 모달 호출
         * @param {string|number} postNo 글 번호.
         */
        mdfModal: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_DAY, { postNo })
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const rsltObj: Record<string, any> = res.rsltObj;
                /* initialize form. */
                dF.JrnlDay.initForm(rsltObj);
                
                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} postNo - 글 번호.
         */
        delAjax: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.JRNL_DAY, { postNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (!res.rslt) return;

                            dF.JrnlDay.refresh();
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
