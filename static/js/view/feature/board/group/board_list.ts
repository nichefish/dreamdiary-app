/**
 * board_list.ts
 */
// @ts-ignore
const Page: Page = (function (): Page {
    return {
        init: function (): void {
            dF.Board.init();
            cF.table.initSort();
            dF.Board.initDraggable();
        },
    }
})();

document.addEventListener("DOMContentLoaded", function (): void {
    Page.init();
});
