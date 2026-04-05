/**
 * jrnl_day_aside_module.ts
 * jrnl_day aside script module
 *
 * @author nichefish
 */
if (typeof dF === "undefined") { var dF = {} as any; }
dF.JrnlDayAside = (function(): dfModule {
    return {
        initialized: false,
        pinpointSearchParams: null,

        /**
         * initialize module
         */
        init: function(): void {
            if (dF.JrnlDayAside.initialized) return;

            dF.JrnlDayAside.initYyMnth();
            dF.JrnlDayAside.setPinnedYyMnth();

            document.querySelector("#jrnl_aside #left")?.addEventListener("click", dF.JrnlDayAside.left);
            document.querySelector("#jrnl_aside #right")?.addEventListener("click", dF.JrnlDayAside.right);

            cF.util.enterKey("#diaryFilterKeyword", dF.JrnlDay.applyKeywordFilters);
            cF.util.enterKey("#dreamFilterKeyword", dF.JrnlDay.applyKeywordFilters);

            dF.JrnlDayAside.syncWeekRangeLabel();
            dF.JrnlDayAside.initialized = true;
            console.log("'dF.JrnlDayAside' module initialized.");
        },

        /**
         * move to the month containing today
         */
        today: function(): void {
            dF.JrnlDayAside.todayMonth();
        },

        /**
         * move to the month containing today
         */
        todayMonth: function(): void {
            const today: string = cF.date.getCurrDateStr(cF.date.ptnDate);
            const yy: string = today.substring(0, 4);
            const mnth: string = String(parseInt(today.substring(5, 7), 10));

            dF.JrnlDay.initSearchParams();
            dF.JrnlDay.currentSearchParams.stdrdDt = today;
            dF.JrnlDayAside.yyMnth(yy, mnth, dF.JrnlDay.currentSearchParams.sort);
        },

        /**
         * move to the week containing today
         */
        todayWeek: function(): void {
            dF.JrnlDayAside.navigateToWeek(cF.date.getCurrDateStr(cF.date.ptnDate));
        },

        /**
         * year select change
         */
        changeYy: function(): void {
            cF.handlebars.template(null, "jrnl_day_list");
            cF.handlebars.template([], "jrnl_day_tag_list");
            cF.handlebars.template([], "jrnl_diary_tag_list");
            cF.handlebars.template([], "jrnl_dream_tag_list");
            dF.JrnlDream.inKeywordSearchMode = false;

            const yyElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const selectedYear: string = yyElement.value;
            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("jrnl_day_sort") ?? "DESC";

            if (selectedYear === "2010") {
                dF.JrnlDayAside.yyMnth(selectedYear, 99, sort);
            }

            const mnthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            mnthElement.value = "";
        },

        /**
         * month select change
         */
        changeMnth: function(): void {
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const selectedYear: string = yearElement.value;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const selectedMnth: string = monthElement.value;
            if (selectedMnth === "") return;

            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("jrnl_day_sort") ?? "DESC";
            dF.JrnlDayAside.yyMnth(selectedYear, selectedMnth, sort);
        },

        /**
         * year-month move
         * @param {string} yy
         * @param {string|number} mnth
         * @param {string} [sort]
         */
        yyMnth: function(yy: string, mnth: string|number, sort?: string): void {
            const mnthStr: string = String(mnth);
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            yearElement.value = yy;
            monthElement.value = mnthStr;

            $("#jrnl_aside #jrnl_diary_reset_btn").remove();
            $("#jrnl_aside #jrnl_dream_reset_btn").remove();

            dF.JrnlDay.initSearchParams();
            dF.JrnlDay.currentSearchParams.yy = yy;
            dF.JrnlDay.currentSearchParams.mnth = mnthStr;
            dF.JrnlDay.currentSearchParams.stdrdDt = dF.JrnlDayAside.buildAnchorDateForMonth(yy, mnthStr);
            if (sort) dF.JrnlDay.currentSearchParams.sort = sort;

            localStorage.setItem("jrnl_yy", yy);
            localStorage.setItem("jrnl_mnth", mnthStr);
            dF.JrnlDayAside.syncWeekRangeLabel(dF.JrnlDay.currentSearchParams.stdrdDt);

            if (dF.JrnlDay.viewType === "WEEKLY") {
                dF.JrnlDream.inKeywordSearchMode = false;
                cF.ui.blockUIReplace(dF.JrnlDay.buildViewUrl(Url.JRNL_DAY_MONTHLY));
                Layout.toPageTop();
                return;
            }

            const isCalendar: boolean = Page?.calendar != null;
            if (isCalendar) {
                Page.calDt = new Date(Number(yy), Number(mnthStr) - 1, 1);
                Page.calendar.gotoDate(Page.calDt);
                Page.refreshEventList(Page.calDt);
            } else {
                dF.JrnlDay.yyMnthListAjax();
            }

            dF.JrnlDream.inKeywordSearchMode = false;
            Layout.toPageTop();
        },

        /**
         * previous month
         */
        left: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth") ?? "";
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

            dF.JrnlDayAside.yyMnth(toBeYy, toBeMnth);
        },

        /**
         * next month
         */
        right: function(): void {
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth") ?? "";
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

            dF.JrnlDayAside.yyMnth(toBeYy, toBeMnth);
        },

        /**
         * previous week
         */
        leftWeek: function(): void {
            dF.JrnlDayAside.navigateWeek("prev");
        },

        /**
         * next week
         */
        rightWeek: function(): void {
            dF.JrnlDayAside.navigateWeek("next");
        },

        /**
         * week move using anchor date
         * @param {"prev"|"next"} type
         */
        navigateWeek: function(type: "prev"|"next"): void {
            const anchorDate: string = dF.JrnlDayAside.getCurrentAnchorDate();
            const nextDate: string = cF.date.navigateDateStr("week", anchorDate, type, cF.date.ptnDate) ?? anchorDate;
            dF.JrnlDayAside.navigateToWeek(nextDate);
        },

        /**
         * move to weekly view using anchor date
         * @param {string} stdrdDt
         */
        navigateToWeek: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            if (dF.JrnlDay.viewType === "WEEKLY") {
                dF.JrnlDayAside.setAnchorDateForCurrentView(stdrdDt);
                return;
            }

            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            const yearElement: HTMLSelectElement | null = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement | null;
            const monthElement: HTMLSelectElement | null = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement | null;
            if (yearElement) yearElement.value = yy;
            if (monthElement) monthElement.value = mnth;

            localStorage.setItem("jrnl_yy", yy);
            localStorage.setItem("jrnl_mnth", mnth);

            dF.JrnlDay.initSearchParams();
            dF.JrnlDay.currentSearchParams.yy = yy;
            dF.JrnlDay.currentSearchParams.mnth = mnth;
            dF.JrnlDay.currentSearchParams.stdrdDt = stdrdDt;
            dF.JrnlDayAside.syncWeekRangeLabel(stdrdDt);

            cF.ui.blockUIReplace(dF.JrnlDay.buildViewUrl(Url.JRNL_DAY_WEEKLY));
        },

        /**
         * resolve current anchor date
         */
        getCurrentAnchorDate: function(): string {
            dF.JrnlDay.initSearchParams();
            const currentParams: Record<string, any> = dF.JrnlDay.currentSearchParams ?? {};
            if (cF.util.isNotEmpty(currentParams.stdrdDt)) return currentParams.stdrdDt;
            if (dF.JrnlDay.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.stdrdDt)) return Page.stdrdDt;

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
            const baseAnchor: string = dF.JrnlDayAside.getCurrentAnchorDate();
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
         */
        setAnchorDateForCurrentView: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            const yearElement: HTMLSelectElement | null = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement | null;
            const monthElement: HTMLSelectElement | null = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement | null;
            if (yearElement) yearElement.value = yy;
            if (monthElement) monthElement.value = mnth;

            localStorage.setItem("jrnl_yy", yy);
            localStorage.setItem("jrnl_mnth", mnth);

            dF.JrnlDay.initSearchParams();
            dF.JrnlDay.currentSearchParams.yy = yy;
            dF.JrnlDay.currentSearchParams.mnth = mnth;
            dF.JrnlDay.currentSearchParams.stdrdDt = stdrdDt;
            dF.JrnlDayAside.syncWeekRangeLabel(stdrdDt);

            if (dF.JrnlDay.viewType === "WEEKLY") {
                Page.loadWeek(stdrdDt);
                return;
            }

            dF.JrnlDayAside.yyMnth(yy, mnth, dF.JrnlDay.currentSearchParams.sort);
        },

        /**
         * save pinned yy/mnth
         */
        pinpoint: function(): void {
            const { yy, mnth } = dF.JrnlDay.currentSearchParams;

            localStorage.setItem("jrnl_pinned_yy", yy);
            localStorage.setItem("jrnl_pinned_mnth", mnth);
            $("#jrnl_aside_pinText #pinnedYy").text(yy);
            $("#jrnl_aside_pinText #pinnedMnth").text(mnth);
        },

        /**
         * move back to pinned yy/mnth
         */
        turnback: function(): void {
            const pinnedYy: string = localStorage.getItem("jrnl_pinned_yy") ?? "";
            const pinnedMnth: string = localStorage.getItem("jrnl_pinned_mnth") ?? "";
            if (cF.util.isEmpty(pinnedYy) || cF.util.isEmpty(pinnedMnth)) return;

            dF.JrnlDay.currentSearchParams.yy = pinnedYy;
            dF.JrnlDay.currentSearchParams.mnth = pinnedMnth;
            dF.JrnlDay.currentSearchParams.stdrdDt = dF.JrnlDayAside.buildAnchorDateForMonth(pinnedYy, pinnedMnth, 1);

            dF.JrnlDayAside.yyMnth(pinnedYy, pinnedMnth);
        },

        /**
         * sort current list
         * @param {"ASC"|"DESC"} [toBe]
         */
        sort: function(toBe: string): void {
            const asIs: string = cF.util.getInputValue("#jrnl_aside #sort");
            if (toBe == null) toBe = (asIs !== "ASC") ? "ASC" : "DESC";

            localStorage.setItem("jrnl_day_sort", toBe);
            $("#jrnl_aside #sort").val(toBe);

            if (toBe === "DESC") {
                $("#jrnl_aside_header #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
            } else {
                $("#jrnl_aside_header #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
            }

            const container: HTMLElement = document.querySelector("#jrnl_day_list_div") as HTMLElement;
            if (!container) return;

            const days: HTMLElement[] = Array.from(container.querySelectorAll(".jrnl-day")) as HTMLElement[];
            days.sort((a: HTMLDivElement, b: HTMLDivElement): number => {
                const dateA: Date = new Date(a.querySelector(".jrnl-day-header")?.getAttribute("data-date") ?? "");
                const dateB: Date = new Date(b.querySelector(".jrnl-day-header")?.getAttribute("data-date") ?? "");
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
            const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy") ?? "";
            const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth") ?? "";
            const sort: string = cF.util.getUrlParam("sort") ?? localStorage.getItem("jrnl_day_sort") ?? "";

            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            yearElement.value = yy === "" ? cF.date.getCurrYyStr() : yy;

            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            monthElement.value = mnth;

            const sortElement: HTMLInputElement | null = document.querySelector("#jrnl_aside #sort") as HTMLInputElement | null;
            if (sort !== "" && sortElement != null) sortElement.value = sort;

            dF.JrnlDayAside.syncWeekRangeLabel();
        },

        /**
         * update week range label
         * @param {string} [stdrdDt]
         */
        syncWeekRangeLabel: function(stdrdDt?: string): void {
            const labelElement: HTMLElement | null = document.querySelector("#jrnl_aside #jrnlAsideWeekRange");
            if (labelElement == null) return;

            const targetDate: string = stdrdDt ?? dF.JrnlDayAside.getCurrentAnchorDate();
            if (cF.util.isEmpty(targetDate)) {
                labelElement.textContent = "----";
                return;
            }

            const weekStartDt: string = cF.date.getWeekdayDateStr(targetDate, 1, cF.date.ptnDate) ?? targetDate;
            const weekEndDt: string = cF.date.getDateAddDayStr(weekStartDt, 6, cF.date.ptnDate) ?? weekStartDt;
            labelElement.textContent = `${weekStartDt.substring(5)} ~ ${weekEndDt.substring(5)}`;
        },

        /**
         * initialize pinned yy/mnth label
         */
        setPinnedYyMnth: function(): void {
            const pinnedYy: string | null = localStorage.getItem("jrnl_pinned_yy");
            if (pinnedYy != null) {
                document.querySelector("#jrnl_aside #pinnedYy")!.textContent = pinnedYy;
            }

            const pinnedMnth: string | null = localStorage.getItem("jrnl_pinned_mnth");
            if (pinnedMnth != null) {
                document.querySelector("#jrnl_aside #pinnedMnth")!.textContent = pinnedMnth;
            }
        },
    };
})();
