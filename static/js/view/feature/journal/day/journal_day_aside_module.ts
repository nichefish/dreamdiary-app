/**
 * journal_day_aside_module.ts
 * journal_day aside script module
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.JournalDayAside = (function(): dfModule {
    return {
        initialized: false,
        pinpointSearchParams: null,
        weekNavRequestKey: null,

        /**
         * initialize module
         */
        init: function(): void {
            if (dF.JournalDayAside.initialized) return;

            dF.JournalDayAside.initYyMnth();
            dF.JournalDayAside.setPinnedYyMnth();

            document.querySelector("#journal_aside #left")?.addEventListener("click", dF.JournalDayAside.left);
            document.querySelector("#journal_aside #right")?.addEventListener("click", dF.JournalDayAside.right);

            cF.util.enterKey("#diaryFilterKeyword", dF.JournalDay.applyKeywordFilters);
            cF.util.enterKey("#dreamFilterKeyword", dF.JournalDay.applyKeywordFilters);

            if (dF.JournalDay.viewType === "WEEKLY") {
                dF.JournalDayAside.syncWeekRangeLabel();
            } else {
                dF.JournalDayAside.syncWeekNavigator();
            }
            dF.JournalDayAside.initialized = true;
            console.log("'dF.JournalDayAside' module initialized.");
        },

        /**
         * move to the month containing today
         */
        today: function(): void {
            dF.JournalDayAside.todayMonth();
        },

        /**
         * move to the month containing today
         */
        todayMonth: function(): void {
            const today: string = cF.date.getCurrDateStr(cF.date.ptnDate);
            const yy: string = today.substring(0, 4);
            const mnth: string = String(parseInt(today.substring(5, 7), 10));

            dF.JournalDay.initSearchParams();
            dF.JournalDay.currentSearchParams.stdrdDt = today;
            dF.JournalDayAside.yyMnth(yy, mnth, dF.JournalDay.currentSearchParams.sort);
        },

        /**
         * move to the week containing today
         */
        todayWeek: function(): void {
            dF.JournalDayAside.navigateToWeek(cF.date.getCurrDateStr(cF.date.ptnDate));
        },

        /**
         * year select change
         */
        changeYy: function(): void {
            cF.handlebars.template(null, "journal_day_list");
            cF.handlebars.template([], "journal_day_tag_list");
            cF.handlebars.template([], "journal_diary_tag_list");
            cF.handlebars.template([], "journal_dream_tag_list");
            dF.JournalDream.inKeywordSearchMode = false;

            const yyElement: HTMLSelectElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement;
            const selectedYear: string = yyElement.value;
            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("journal_day_sort") ?? "DESC";

            if (selectedYear === "2010") {
                dF.JournalDayAside.yyMnth(selectedYear, 99, sort);
            }

            const mnthElement: HTMLSelectElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement;
            mnthElement.value = "";
        },

        /**
         * month select change
         */
        changeMnth: function(): void {
            const yearElement: HTMLSelectElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement;
            const selectedYear: string = yearElement.value;
            const monthElement: HTMLSelectElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement;
            const selectedMnth: string = monthElement.value;
            if (selectedMnth === "") return;

            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("journal_day_sort") ?? "DESC";
            dF.JournalDayAside.yyMnth(selectedYear, selectedMnth, sort);
        },

        /**
         * year-month move
         * @param {string} yy
         * @param {string|number} mnth
         * @param {string} [sort]
         */
        yyMnth: function(yy: string, mnth: string|number, sort?: string): void {
            const mnthStr: string = String(mnth);
            const yearElement: HTMLSelectElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement;
            yearElement.value = yy;
            monthElement.value = mnthStr;

            $("#journal_aside #journal_diary_reset_btn").remove();
            $("#journal_aside #journal_dream_reset_btn").remove();

            dF.JournalDay.initSearchParams();
            dF.JournalDay.currentSearchParams.yy = yy;
            dF.JournalDay.currentSearchParams.mnth = mnthStr;
            dF.JournalDay.currentSearchParams.stdrdDt = dF.JournalDayAside.buildAnchorDateForMonth(yy, mnthStr);
            if (sort) dF.JournalDay.currentSearchParams.sort = sort;

            localStorage.setItem("journal_yy", yy);
            localStorage.setItem("journal_mnth", mnthStr);
            dF.JournalDayAside.syncWeekNavigator(dF.JournalDay.currentSearchParams.stdrdDt);

            if (dF.JournalDay.viewType === "WEEKLY") {
                dF.JournalDream.inKeywordSearchMode = false;
                // buildViewUrl → resolveAnchorDateForView가 Page.stdrdDt를 우선 참조하므로, 목적지 날짜로 미리 동기화해야 올바른 월로 이동한다.
                Page.stdrdDt = dF.JournalDay.currentSearchParams.stdrdDt;
                cF.ui.blockUIReplace(dF.JournalDay.buildViewUrl(Url.JOURNAL_DAY_MONTHLY));
                Layout.toPageTop();
                return;
            }

            const isCalendar: boolean = Page?.calendar != null;
            if (isCalendar) {
                Page.calDt = new Date(Number(yy), Number(mnthStr) - 1, 1);
                Page.calendar.gotoDate(Page.calDt);
                Page.refreshEventList(Page.calDt);
            } else {
                dF.JournalDay.yyMnthListAjax();
            }

            dF.JournalDream.inKeywordSearchMode = false;
            Layout.toPageTop();
        },

        /**
         * previous month
         */
        left: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "";
            if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) return;
            if (yy === "2010" && parseInt(mnth, 10) === 1) return;

            let toBeYy: string = yy;
            let toBeMnth: string;
            if (parseInt(mnth, 10) === 1) {
                toBeYy = String(parseInt(yy, 10) - 1);
                toBeMnth = "12";
            } else {
                toBeMnth = String(parseInt(mnth, 10) - 1);
            }

            dF.JournalDayAside.yyMnth(toBeYy, toBeMnth);
        },

        /**
         * next month
         */
        right: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "";
            if (cF.util.isEmpty(yy) || cF.util.isEmpty(mnth)) return;
            if (yy === "2010" && parseInt(mnth, 10) === 1) return;

            let toBeYy: string = yy;
            let toBeMnth: string;
            if (parseInt(mnth, 10) === 12) {
                toBeYy = String(parseInt(yy, 10) + 1);
                toBeMnth = "1";
            } else {
                toBeMnth = String(parseInt(mnth, 10) + 1);
            }

            dF.JournalDayAside.yyMnth(toBeYy, toBeMnth);
        },

        /**
         * previous week
         */
        leftWeek: function(): void {
            dF.JournalDayAside.navigateWeek("prev");
        },

        /**
         * next week
         */
        rightWeek: function(): void {
            dF.JournalDayAside.navigateWeek("next");
        },

        /**
         * week move using anchor date
         * @param {"prev"|"next"} type
         */
        navigateWeek: function(type: "prev"|"next"): void {
            const anchorDate: string = dF.JournalDayAside.getCurrentAnchorDate();
            const currentWeekStartDt: string = cF.date.getWeekdayDateStr(anchorDate, 1, cF.date.ptnDate) ?? anchorDate;
            const nextDate: string = cF.date.navigateDateStr("week", currentWeekStartDt, type, cF.date.ptnDate) ?? currentWeekStartDt;
            dF.JournalDayAside.navigateToWeek(nextDate);
        },

        /**
         * move to weekly view using anchor date
         * @param {string} stdrdDt
         */
        navigateToWeek: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            if (dF.JournalDay.viewType === "WEEKLY") {
                dF.JournalDayAside.setAnchorDateForCurrentView(stdrdDt, false);
                return;
            }

            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            const yearElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
            const monthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
            if (yearElement) yearElement.value = yy;
            if (monthElement) monthElement.value = mnth;

            localStorage.setItem("journal_yy", yy);
            localStorage.setItem("journal_mnth", mnth);

            dF.JournalDay.initSearchParams();
            dF.JournalDay.currentSearchParams.yy = yy;
            dF.JournalDay.currentSearchParams.mnth = mnth;
            dF.JournalDay.currentSearchParams.stdrdDt = stdrdDt;
            dF.JournalDayAside.syncWeekNavigator(stdrdDt);

            cF.ui.blockUIReplace(dF.JournalDay.buildViewUrl(Url.JOURNAL_DAY_WEEKLY));
        },

        /**
         * move to selected week day
         * @param {string} stdrdDt
         */
        navigateToWeekDay: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            if (dF.JournalDay.viewType === "WEEKLY") {
                dF.JournalDayAside.setAnchorDateForCurrentView(stdrdDt, true);
                return;
            }

            cF.ui.blockUIReplace(dF.JournalDay.buildWeeklyViewUrl(stdrdDt, stdrdDt));
        },

        /**
         * resolve current anchor date
         */
        getCurrentAnchorDate: function(): string {
            dF.JournalDay.initSearchParams();
            const currentParams: Record<string, any> = dF.JournalDay.currentSearchParams ?? {};
            if (cF.util.isNotEmpty(currentParams.stdrdDt)) return currentParams.stdrdDt;
            if (dF.JournalDay.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.stdrdDt)) return Page.stdrdDt;

            const yy: string = currentParams.yy ?? cF.date.getCurrYyStr();
            const mnth: string = currentParams.mnth ?? cF.date.getCurrMnthStr();
            const currYy: string = cF.date.getCurrYyStr();
            const currMnth: string = cF.date.getCurrMnthStr();
            if (yy === currYy && String(parseInt(mnth, 10)) === String(parseInt(currMnth, 10))) {
                return cF.date.getCurrDateStr(cF.date.ptnDate);
            }

            const parsedMonthNo: number = parseInt(mnth, 10);
            const monthNo: number = (parsedMonthNo >= 1 && parsedMonthNo <= 12) ? parsedMonthNo : 1;
            return `${yy}-${String(monthNo).padStart(2, "0")}-01`;
        },

        /**
         * build anchor date inside target month
         * @param {string} yy
         * @param {string} mnth
         * @param {number} [fallbackDay]
         */
        buildAnchorDateForMonth: function(yy: string, mnth: string, fallbackDay?: number): string {
            const baseAnchor: string = dF.JournalDayAside.getCurrentAnchorDate();
            const baseDay: number = parseInt(baseAnchor?.substring(8, 10) ?? String(fallbackDay ?? 1), 10);
            const parsedMonthNo: number = parseInt(mnth, 10);
            const monthNo: number = (parsedMonthNo >= 1 && parsedMonthNo <= 12) ? parsedMonthNo : 1;
            const lastDay: number = new Date(Number(yy), monthNo, 0).getDate();
            const safeDay: string = String(Math.min(baseDay, lastDay)).padStart(2, "0");
            return `${yy}-${String(monthNo).padStart(2, "0")}-${safeDay}`;
        },

        /**
         * apply anchor date to active view
         * @param {string} stdrdDt
         * @param {boolean} [useTarget]
         */
        setAnchorDateForCurrentView: function(stdrdDt: string, useTarget?: boolean): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            const yearElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy") as HTMLSelectElement | null;
            const monthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth") as HTMLSelectElement | null;
            if (yearElement) yearElement.value = yy;
            if (monthElement) monthElement.value = mnth;

            localStorage.setItem("journal_yy", yy);
            localStorage.setItem("journal_mnth", mnth);

            dF.JournalDay.initSearchParams();
            dF.JournalDay.currentSearchParams.yy = yy;
            dF.JournalDay.currentSearchParams.mnth = mnth;
            dF.JournalDay.currentSearchParams.stdrdDt = stdrdDt;
            dF.JournalDayAside.syncWeekNavigator(stdrdDt);

            if (dF.JournalDay.viewType === "WEEKLY") {
                Page.loadWeek(stdrdDt, useTarget ? stdrdDt : undefined);
                return;
            }

            dF.JournalDayAside.yyMnth(yy, mnth, dF.JournalDay.currentSearchParams.sort);
        },

        /**
         * save pinned yy/mnth
         */
        pinpoint: function(): void {
            const { yy, mnth } = dF.JournalDay.currentSearchParams;

            localStorage.setItem("journal_pinned_yy", yy);
            localStorage.setItem("journal_pinned_mnth", mnth);
            $("#journal_aside_pinText #pinnedYy").text(yy);
            $("#journal_aside_pinText #pinnedMnth").text(mnth);
        },

        /**
         * move back to pinned yy/mnth
         */
        turnback: function(): void {
            const pinnedYy: string = localStorage.getItem("journal_pinned_yy") ?? "";
            const pinnedMnth: string = localStorage.getItem("journal_pinned_mnth") ?? "";
            if (cF.util.isEmpty(pinnedYy) || cF.util.isEmpty(pinnedMnth)) return;

            dF.JournalDay.currentSearchParams.yy = pinnedYy;
            dF.JournalDay.currentSearchParams.mnth = pinnedMnth;
            dF.JournalDay.currentSearchParams.stdrdDt = dF.JournalDayAside.buildAnchorDateForMonth(pinnedYy, pinnedMnth, 1);

            dF.JournalDayAside.yyMnth(pinnedYy, pinnedMnth);
        },

        /**
         * sort current list
         * @param {"ASC"|"DESC"} [toBe]
         */
        sort: function(toBe: string): void {
            const asIs: string = cF.util.getInputValue("#journal_aside #sort");
            if (toBe == null) toBe = (asIs !== "ASC") ? "ASC" : "DESC";

            localStorage.setItem("journal_day_sort", toBe);
            $("#journal_aside #sort").val(toBe);

            if (toBe === "DESC") {
                $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
            } else {
                $("#journal_aside_header #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
            }

            const container: HTMLElement = document.querySelector("#journal_day_list_div") as HTMLElement;
            if (!container) return;

            const days: HTMLElement[] = Array.from(container.querySelectorAll(".journal-day")) as HTMLElement[];
            days.sort((a: HTMLDivElement, b: HTMLDivElement): number => {
                const dateA: Date = new Date(a.querySelector(".journal-day-header")?.getAttribute("data-date") ?? "");
                const dateB: Date = new Date(b.querySelector(".journal-day-header")?.getAttribute("data-date") ?? "");
                return (toBe === "ASC") ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
            });

            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }

            days.forEach((day: HTMLElement): void => {
                container.appendChild(day);
            });
        },

        /**
         * initialize yy/mnth state
         */
        initYyMnth: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("journal_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("journal_mnth") ?? "";
            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("journal_day_sort") ?? "";

            const yearElement: HTMLSelectElement = document.querySelector("#journal_aside #yy") as HTMLSelectElement;
            yearElement.value = yy === "" ? cF.date.getCurrYyStr() : yy;

            const monthElement: HTMLSelectElement = document.querySelector("#journal_aside #mnth") as HTMLSelectElement;
            monthElement.value = mnth;

            const sortElement: HTMLInputElement | null = document.querySelector("#journal_aside #sort") as HTMLInputElement | null;
            if (sort !== "" && sortElement != null) sortElement.value = sort;

            if (dF.JournalDay.viewType === "WEEKLY") {
                dF.JournalDayAside.syncWeekRangeLabel();
            } else {
                dF.JournalDayAside.syncWeekNavigator();
            }
        },

        /**
         * update week range label
         * @param {string} [stdrdDt]
         */
        syncWeekRangeLabel: function(stdrdDt?: string): void {
            const labelElement: HTMLElement | null = document.querySelector("#journal_aside #journalAsideWeekRange");
            if (labelElement == null) return;

            const targetDate: string = stdrdDt ?? dF.JournalDayAside.getCurrentAnchorDate();
            if (cF.util.isEmpty(targetDate)) {
                labelElement.textContent = "----";
                return;
            }

            const weekStartDt: string = cF.date.getWeekdayDateStr(targetDate, 1, cF.date.ptnDate) ?? targetDate;
            const weekEndDt: string = cF.date.getDateAddDayStr(weekStartDt, 6, cF.date.ptnDate) ?? weekStartDt;
            labelElement.textContent = `${weekStartDt.substring(5)} ~ ${weekEndDt.substring(5)}`;
        },

        /**
         * sync week range label + week day buttons
         * @param {string} [stdrdDt]
         * @param {Record<string, any>[]} [weeklyList]
         */
        syncWeekNavigator: function(stdrdDt?: string, weeklyList?: Record<string, any>[]): void {
            const targetDate: string = stdrdDt ?? dF.JournalDayAside.getCurrentAnchorDate();
            dF.JournalDayAside.syncWeekRangeLabel(targetDate);

            if (cF.util.isEmpty(targetDate)) {
                dF.JournalDayAside.renderWeekNavigator("", []);
                return;
            }

            if (Array.isArray(weeklyList)) {
                dF.JournalDayAside.renderWeekNavigator(targetDate, weeklyList);
                return;
            }

            dF.JournalDayAside.loadWeekNavigator(targetDate);
        },

        /**
         * fetch week day availability for week navigation
         * @param {string} stdrdDt
         */
        loadWeekNavigator: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            const weekStartDt: string = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
            const requestKey: string = `${weekStartDt}:${stdrdDt}`;
            dF.JournalDayAside.weekNavRequestKey = requestKey;
            dF.JournalDayAside.renderWeekNavigator(stdrdDt, null);

            cF.ajax.get(Url.JOURNAL_DAYS, {
                viewType: "weekly",
                weekStartDt,
                stdrdDt,
            }, function(res: AjaxResponse): void {
                if (dF.JournalDayAside.weekNavRequestKey !== requestKey) return;

                if (!res.rslt) {
                    dF.JournalDayAside.renderWeekNavigator(stdrdDt, []);
                    return;
                }

                dF.JournalDayAside.renderWeekNavigator(stdrdDt, Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        /**
         * render week navigation buttons
         * @param {string} stdrdDt
         * @param {Record<string, any>[]|null} weeklyList
         */
        renderWeekNavigator: function(stdrdDt: string, weeklyList: Record<string, any>[]|null): void {
            const container: HTMLElement | null = document.querySelector("#journal_aside #journalAsideWeekDays");
            if (container == null) return;

            if (cF.util.isEmpty(stdrdDt)) {
                container.innerHTML = "";
                return;
            }

            const weekStartDt: string = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
            const weekDayLabels: string[] = ["월", "화", "수", "목", "금", "토", "일"];
            const dayMap: Map<string, Record<string, any>> = new Map<string, Record<string, any>>();
            const loaded: boolean = Array.isArray(weeklyList);

            if (loaded) {
                weeklyList.forEach((day: Record<string, any>): void => {
                    const dateKey: string = day?.stdrdDt ?? "";
                    if (cF.util.isNotEmpty(dateKey)) dayMap.set(dateKey, day);
                });
            }

            container.innerHTML = weekDayLabels.map((label: string, idx: number): string => {
                const dateStr: string = cF.date.getDateAddDayStr(weekStartDt, idx, cF.date.ptnDate) ?? weekStartDt;
                const hasDay: boolean = loaded && dayMap.has(dateStr);
                const isActive: boolean = dateStr === stdrdDt;
                const disabledAttr: string = hasDay ? "" : " disabled";
                const activeClass: string = isActive ? " is-active" : "";
                const onClickAttr: string = hasDay ? ` onclick="dF.JournalDayAside.navigateToWeekDay('${dateStr}');"` : "";

                return `<button type="button" class="journal-aside-week-day${activeClass}"${disabledAttr}${onClickAttr}>`
                    + `<span class="journal-aside-week-day__label">${label}</span>`
                    + `<span class="journal-aside-week-day__date">${dateStr.substring(8, 10)}</span>`
                    + `</button>`;
            }).join("");
        },

        /**
         * initialize pinned yy/mnth label
         */
        setPinnedYyMnth: function(): void {
            const pinnedYy: string | null = localStorage.getItem("journal_pinned_yy");
            if (pinnedYy != null) {
                document.querySelector("#journal_aside #pinnedYy")!.textContent = pinnedYy;
            }

            const pinnedMnth: string | null = localStorage.getItem("journal_pinned_mnth");
            if (pinnedMnth != null) {
                document.querySelector("#journal_aside #pinnedMnth")!.textContent = pinnedMnth;
            }
        },
    };
})();
