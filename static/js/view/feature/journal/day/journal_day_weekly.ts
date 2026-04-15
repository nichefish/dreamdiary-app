/**
 * journal_day_weekly.ts
 * 저널 주간 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        stdrdDt: null,
        weekStartDt: null,
        weekEndDt: null,
        targetDt: null,

        init: function(): void {
            dF.JournalDay.init('WEEKLY');
            dF.JournalDiary.init('WEEKLY');
            dF.JournalDream.init('WEEKLY');
            // dF.JournalTodo.init();
            dF.Comment.modal.init({
                "refreshFunc": function(): void {
                    Page.loadWeek(Page.stdrdDt);
                }
            });
            dF.State.init();

            const stdrdDt: string = window.JOURNAL?.stdrdDt ?? cF.date.getCurrDateStr(cF.date.ptnDate);
            const targetDt: string = cF.util.getUrlParam("target") ?? "";
            Page.stdrdDt = stdrdDt;
            Page.targetDt = cF.util.isNotEmpty(targetDt) ? targetDt : null;
            Page.syncAsidePeriodState(stdrdDt);
            dF.JournalDayAside.init();
            cF.util.enterKey("#diarySearchKeyword", dF.JournalDiary.searchPopup);
            cF.util.enterKey("#dreamSearchKeyword", dF.JournalDream.searchPopup);

            Page.loadWeek(stdrdDt, cF.util.isNotEmpty(targetDt) ? targetDt : undefined);
        },

        changeView: function(url: string): void {
            cF.ui.blockUIReplace(dF.JournalDay.buildViewUrl(url));
        },

        syncAsidePeriodState: function(stdrdDt: string): void {
            if (cF.util.isEmpty(stdrdDt)) return;

            const yy: string = stdrdDt.substring(0, 4);
            const mnth: string = String(parseInt(stdrdDt.substring(5, 7), 10));
            localStorage.setItem("journal_yy", yy);
            localStorage.setItem("journal_mnth", mnth);

            dF.JournalDay.initSearchParams();
            dF.JournalDay.currentSearchParams.yy = yy;
            dF.JournalDay.currentSearchParams.mnth = mnth;
            dF.JournalDay.currentSearchParams.stdrdDt = stdrdDt;
            dF.JournalDay.currentSearchParams.weekStartDt = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
            dF.JournalDay.currentSearchParams.sort = localStorage.getItem("journal_day_sort") ?? "DESC";

            const yyElement: HTMLSelectElement | null = document.querySelector("#journal_aside #yy");
            const mnthElement: HTMLSelectElement | null = document.querySelector("#journal_aside #mnth");
            if (yyElement) yyElement.value = yy;
            if (mnthElement) mnthElement.value = mnth;
        },

        toggleAside: function(): void {
            const asideToggle: HTMLElement | null = document.querySelector("#kt_app_engage_primary_btn");
            asideToggle?.dispatchEvent(new MouseEvent("click", { bubbles: true, cancelable: true }));
        },

        loadWeek: function(stdrdDt: string, targetDt?: string): void {
            const resolvedTargetDt: string|undefined = cF.util.isNotEmpty(targetDt) ? targetDt : undefined;
            Page.stdrdDt = stdrdDt;
            Page.targetDt = resolvedTargetDt ?? null;
            Page.weekStartDt = cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
            Page.weekEndDt = cF.date.getDateAddDayStr(Page.weekStartDt, 6, cF.date.ptnDate) ?? Page.weekStartDt;
            Page.syncAsidePeriodState(stdrdDt);
            window.history.replaceState(null, "", dF.JournalDay.buildWeeklyViewUrl(stdrdDt, resolvedTargetDt));

            dF.JournalDay.initSearchParams();
            const searchParams: Record<string, any> = dF.JournalDay.currentSearchParams ?? {};
            const showDiaries: boolean = searchParams.showDiaries !== false;
            const showDreams: boolean = searchParams.showDreams !== false;

            const ajaxData: Record<string, any> = {
                viewType: "weekly",
                weekStartDt: Page.weekStartDt,
                stdrdDt: Page.stdrdDt,
                showDiaries,
                showDreams,
                diaryKeyword: searchParams.diaryKeyword ?? "",
                dreamKeyword: searchParams.dreamKeyword ?? "",
                chapterCtgrCds: searchParams.chapterCtgrCds ?? [],
            };
            cF.ajax.get(Url.JOURNAL_DAYS, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const sort: string = searchParams.sort ?? "DESC";
                const weeklyList: Record<string, any>[] = Page.normalizeWeekDays(res.rsltList ?? [], sort);
                cF.ui.closeModal();
                cF.handlebars.template({
                    list: weeklyList,
                    showDiaries,
                    showDreams
                }, "journal_day_list");
                dF.JournalDayAside.syncWeekNavigator(stdrdDt, weeklyList);
                $("#journal_tag_header").toggle(searchParams.showTagCloud !== false);
                if (searchParams.showTagCloud !== false) {
                    dF.JournalDayTag.listAjax();
                    if (showDiaries) dF.JournalDiaryTag.listAjax();
                    if (showDreams) dF.JournalDreamTag.listAjax();
                }
                KTMenu.createInstances();
                Page.scrollToTarget(Page.targetDt);
            }, "block");
        },

        scrollToTarget: function(targetDt?: string): void {
            if (cF.util.isEmpty(targetDt)) return;

            const targetElement: HTMLElement | null = document.querySelector(`.journal-day[data-stdrd-dt="${targetDt}"]`);
            if (targetElement == null) return;

            window.requestAnimationFrame(function(): void {
                targetElement.scrollIntoView({ behavior: "smooth", block: "start" });
            });
        },

        normalizeWeekDays: function(rsltList: Record<string, any>[], sort: string = "DESC"): Record<string, any>[] {
            return (rsltList ?? [])
                .map((day: Record<string, any>): Record<string, any> => {
                const journalChapterList: Record<string, any>[] = Array.isArray(day?.journalChapterList) ? day.journalChapterList : [];
                const journalDreamList: Record<string, any>[] = Array.isArray(day?.journalDreamList) ? day.journalDreamList : [];
                const journalElseDreamList: Record<string, any>[] = Array.isArray(day?.journalElseDreamList) ? day.journalElseDreamList : [];

                return {
                    ...day,
                    journalDtWeekDay: day?.journalDtWeekDay ?? cF.date.getDayweekStr(day?.stdrdDt, "KO"),
                    tag: day?.tag ?? { list: [] },
                    journalChapterList,
                    journalDreamList,
                    journalElseDreamList,
                    hasDream: (journalDreamList.length + journalElseDreamList.length) > 0,
                };
                })
                .sort((a: Record<string, any>, b: Record<string, any>): number => {
                    const dateA: number = new Date(a?.stdrdDt ?? "").getTime();
                    const dateB: number = new Date(b?.stdrdDt ?? "").getTime();
                    return sort === "ASC" ? dateA - dateB : dateB - dateA;
                });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
