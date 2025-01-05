/**
 * menu_page.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         */
        init: function(): void {
            Page.updtTree();

            /* init : Draggable */
            dF.Menu.initDraggable();

            console.log("Page scripts initialized.");
        },
        /**
         * 트리 구조 데이터 리로드 및 새로 그리기
         */
        updtTree: function(): void {
            // 메인 메뉴 그리기
            const url: string = Url.MENU_MAIN_LIST_AJAX;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const mainMenuList = res.rsltList;
                if (!Array.isArray(mainMenuList) || mainMenuList.length < 1) return;

                mainMenuList.forEach(function(mainMenu): void {
                    cF.handlebars.append(mainMenu, "menu_main_card");
                    Page.drawSubMenu(mainMenu);
                });
            });
        },
        /**
         * 서브메뉴 처리 :: 자기 자신을 그리고 각각의 서브메뉴에 대해서 재귀호출
         */
        drawSubMenu: function(upperMenu: any): void {
            // 서브메뉴 돌면서 재귀호출
            const subMenuList = upperMenu.subMenuList;
            if (!Array.isArray(subMenuList) || subMenuList.length <1) return;

            const menuNo = upperMenu.menuNo;
            subMenuList.forEach(function(subMenu): void {
                cF.handlebars.appendTo(subMenu, "menu_sub", "menu_sub_"+menuNo);
                Page.drawSubMenu(subMenu);
            });
        },
        drawTree: function(): void {
            const jstreeContainer = Page.jstreeContainer;
            cF.jstree.reset(jstreeContainer);
            cF.jstree.init(jstreeContainer);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});