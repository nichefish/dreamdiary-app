/**
 * menuAdminActionService.ts
 * 메뉴 관리 액션 서비스 (기존 dF.Menu 로직 이관)
 *
 * 변경(D): `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
 *
 * @author nichefish
 */
import { resolveMessage } from "../../../../common/messageHelper.js";

type MenuActions = {
    initialized: boolean;
    mainSwappable: any;
    subSwappable: any;
    init: () => void;
    initForm: (obj?: Record<string, any>) => void;
    initMainDraggable: () => void;
    initSubDraggable: () => void;
    regModal: (menuType: string, parentMenuId: number, upperMenuNm: string) => void;
    refreshIcon: () => void;
    toggleUrlSpan: (obj: object) => void;
    submit: () => void;
    registAjax: () => void;
    modifyModal: (id: string | number) => void;
    toggleUseAjax: (id: string | number) => void;
    deleteAjax: (id: number) => void;
};

type MenuAdminActionHooks = {
    renderForm: (obj: Record<string, any>) => void;
    afterFormRendered?: () => void;
};

export default function createMenuAdminActions(hooks: MenuAdminActionHooks): MenuActions {
    const actions: MenuActions = {
        initialized: false,
        mainSwappable: null,
        subSwappable: null,
        init(): void {
            if (actions.initialized)
                return;
            actions.initialized = true;
            console.log("'MenuAdminActions' initialized.");
        },
        initForm(obj: Record<string, any> = {}): void {
            hooks.renderForm(obj);
            if (typeof hooks.afterFormRendered === "function")
                hooks.afterFormRendered();
        },
        initMainDraggable(): void {
            const keyExtractor: (item: HTMLElement) => { id: number } = (item: HTMLElement) => ({ id: Number(item.dataset.id) });
            actions.mainSwappable = cF.draggable.init("-main", keyExtractor, Url.MENUS_IDX);
        },
        initSubDraggable(): void {
            const zoneSelector = ".draggable-zone-sub";
            const itemSelector = ".sortable-item.draggable-sub";
            const handleSelector = ".draggable-handle-sub";
            const containers = document.querySelectorAll(zoneSelector);
            if (containers.length === 0)
                return;

            let sourceContainer: HTMLElement = null as any;
            let targetContainer: HTMLElement = null as any;
            let sourceOrder: string[] = [];
            let sortedOrder: string[] = [];

            const getSortableOrder = function(container: HTMLElement): string[] {
                if (!container)
                    return [];
                return Array.from(container.children)
                    .filter((el: Element): boolean => el.matches(itemSelector))
                    .map((item: Element): string => String((item as HTMLElement).dataset.id || ""));
            };

            const isSameOrder = function(beforeOrder: string[], afterOrder: string[]): boolean {
                if (beforeOrder.length !== afterOrder.length)
                    return false;
                return beforeOrder.every((id: string, index: number): boolean => id === afterOrder[index]);
            };

            const sortSubTreeByOrder = function(
                movedItem: HTMLElement,
                source: HTMLElement,
                target: HTMLElement,
                order: string[]
            ): void {
                const movedId = Number(movedItem.dataset.id);
                const sourceParentMenuId = Number(movedItem.dataset.parentMenuId);
                const targetParentMenuId = Number(target?.dataset.parentMenuId);
                if (isNaN(movedId) || isNaN(sourceParentMenuId) || isNaN(targetParentMenuId))
                    return;

                const groups: Record<string, any>[] = [];
                const targetItems = order.map((id: string, idx: number): Record<string, number> => ({
                    id: Number(id),
                    sortOrder: idx,
                }));
                groups.push({ parentMenuId: targetParentMenuId, items: targetItems });

                if (source !== target) {
                    const sourceItems = Array.from(source.children)
                        .filter((el: Element): boolean => el.matches(itemSelector))
                        .map((el: Element): HTMLElement => el as HTMLElement)
                        .filter((el: HTMLElement): boolean => Number(el.dataset.id) !== movedId)
                        .map((el: HTMLElement, idx: number): Record<string, number> => ({
                            id: Number(el.dataset.id),
                            sortOrder: idx,
                        }));
                    groups.push({ parentMenuId: sourceParentMenuId, items: sourceItems });
                }

                const ajaxData = {
                    movedId,
                    sourceParentMenuId,
                    targetParentMenuId,
                    groups,
                };

                cF.$ajax.put(Url.MENUS_TREE, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt && cF.util.isNotEmpty(res.message))
                        return Swal.fire({ text: res.message });
                    cF.ui.blockUIReload();
                }, "block");
            };

            const onDragStart = function(event: any): void {
                const source = event.data.source as HTMLElement;
                sourceContainer = source.closest(zoneSelector) as HTMLElement;
                targetContainer = sourceContainer;
                sourceOrder = getSortableOrder(sourceContainer);
                sortedOrder = [...sourceOrder];
                source.classList.add("dragging");
            };

            const onSorted = function(event: any): void {
                const newContainer = event.data.newContainer as HTMLElement;
                const source = event.data.dragEvent.data.source as HTMLElement;
                const movedId = String(source.dataset.id || "");
                const currentOrder = getSortableOrder(newContainer).filter((id: string): boolean => id !== movedId);
                currentOrder.splice(event.data.newIndex, 0, movedId);
                sortedOrder = currentOrder;
                targetContainer = newContainer;
            };

            const onDragStop = function(event: any): void {
                const source = event.data.source as HTMLElement;
                const movedAcrossParent = sourceContainer !== targetContainer;
                const isChanged = movedAcrossParent || !isSameOrder(sourceOrder, sortedOrder);
                source.classList.remove("dragging");
                if (!isChanged)
                    return;
                sortSubTreeByOrder(source, sourceContainer, targetContainer, sortedOrder);
            };

            actions.subSwappable = new Draggable.Sortable(containers, {
                draggable: `${zoneSelector} ${itemSelector}`,
                handle: `${zoneSelector} ${itemSelector} ${handleSelector}`,
                mirror: {
                    appendTo: "body",
                    constrainDimensions: true,
                },
            })
                .on("drag:start", onDragStart)
                .on("sortable:sorted", onSorted)
                .on("drag:stop", onDragStop);
        },
        regModal(menuType: string, parentMenuId: number, upperMenuNm: string): void {
            actions.initForm({ menuType, parentMenuId, upperMenuNm });
        },
        refreshIcon(): void {
            const iconElmt = document.querySelector("#menuRegistForm #icon") as HTMLInputElement | null;
            if (!iconElmt)
                return;
            const menuIconDiv = document.querySelector("#menuRegistForm #menu_icon_div") as HTMLElement | null;
            if (menuIconDiv)
                menuIconDiv.innerHTML = iconElmt.value;
        },
        toggleUrlSpan(obj: object): void {
            const submenuExpandType = $(obj).val();
            if (submenuExpandType !== "NO_SUB") {
                $("#url_div").addClass("d-none");
            } else {
                $("#url_div").removeClass("d-none");
            }
        },
        submit(): void {
            $("#menuRegistForm").submit();
        },
        registAjax(): void {
            Swal.fire({
                text: resolveMessage("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value)
                    return;

                const id = cF.util.getInputValue("#menuRegistForm #id");
                const isModify = cF.util.isNotEmpty(id);
                const url = isModify ? cF.util.bindUrl(Url.MENU, { id }) : Url.MENUS;
                const ajaxData = cF.util.getJsonFormData("#menuRegistForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt)
                                cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },
        modifyModal(id: string | number): void {
            if (isNaN(Number(id)))
                return;
            const url = cF.util.bindUrl(Url.MENU, { id });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message))
                        Swal.fire({ text: res.message });
                    return;
                }
                actions.initForm(res.rsltObj);
            });
        },
        toggleUseAjax(id: string | number): void {
            if (isNaN(Number(id)))
                return;
            const item = document.querySelector(`li.menu-item[data-id='${id}']`) as HTMLElement | null;
            if (!item)
                return;
            const currentUseYn = String(item.dataset.useYn || "N");
            const nextUseYn = currentUseYn === "Y" ? "N" : "Y";
            const url = cF.util.bindUrl(Url.MENU, { id });
            cF.$ajax.patch(url, { useYn: nextUseYn }, function(res: AjaxResponse): void {
                if (!res.rslt && cF.util.isNotEmpty(res.message))
                    return Swal.fire({ text: res.message });
                cF.ui.blockUIReload();
            });
        },
        deleteAjax(id: number): void {
            if (isNaN(Number(id)))
                return;
            Swal.fire({
                text: resolveMessage("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value)
                    return;
                const url = cF.util.bindUrl(Url.MENU, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt)
                                cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },
    };

    return actions;
}
