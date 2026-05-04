/**
 * journal_entry_search.ts
 * Common search page bootstrap for journal diary/dream entries.
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        init: async function(): Promise<void> {
            const contentType: string = (window as any).journalEntrySearchContentType;
            const searchModule = dF.JournalEntrySearch.get(contentType);

            await dF.JournalEntry.initAll("SEARCH");
            searchModule.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JournalDay.yyMnthListAjax
            });
            dF.State.init();
            searchModule.initSearch();

            const input: HTMLElement = document.getElementById("keywordInput");
            input.addEventListener("keydown", (e: KeyboardEvent): void => {
                if (e.key === "Enter" && !e.isComposing) {
                    e.preventDefault();
                    searchModule.addKeyword();
                }
            });
        },
    };
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});
