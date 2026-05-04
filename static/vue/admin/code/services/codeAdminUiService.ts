/**
 * codeAdminUiService.ts
 * 코드 관리 Vue 화면 UI 유틸
 */

function bootstrapAny(): any {
    return (window as any).bootstrap;
}

let nestedModalReturnBound = false;

export default {
    isYn(value: string): boolean {
        return String(value || "N").toUpperCase() === "Y";
    },
    openModal(modalId: string): void {
        const modalEl: HTMLElement | null = document.getElementById(modalId);
        if (!modalEl) return;
        const bs: any = bootstrapAny();
        if (!bs?.Modal) return;
        const existing = bs.Modal.getInstance(modalEl);
        (existing || new bs.Modal(modalEl)).show();
    },
    hideModal(modalId: string): void {
        const modalEl: HTMLElement | null = document.getElementById(modalId);
        if (!modalEl) return;
        const bs: any = bootstrapAny();
        if (!bs?.Modal) return;
        const existing = bs.Modal.getInstance(modalEl);
        if (existing) existing.hide();
    },
    syncTooltips(rootSelector: string): void {
        const root: HTMLElement | null = document.querySelector(rootSelector);
        if (!root) return;
        const bs: any = bootstrapAny();
        if (!bs?.Tooltip) return;
        root.querySelectorAll("[data-bs-toggle='tooltip']").forEach((el: Element): void => {
            const target = el as HTMLElement;
            const existed = bs.Tooltip.getInstance(target);
            if (existed) existed.dispose();
            new bs.Tooltip(target);
        });
    },
    initCodeItemDraggable(): void {
        const hasZone: boolean = !!document.querySelector(".draggable-zone-code-item");
        if (!hasZone) return;
        const keyExtractor: Function = (item: HTMLElement) => ({ id: Number(item.dataset.id || item.id) });
        cF.draggable.init("-code-item", keyExtractor, Url.CODE_ITEMS_SORT_ORDERS);
    },
    /**
     * 상세 모달에서 항목/그룹 등록 모달로 들어간 뒤, 자식이 닫히면 상세를 다시 연다.
     * 저장 성공 시에는 호출부에서 modalReturnTarget 을 먼저 비울 것.
     */
    bindNestedModalReturn(state: { modalReturnTarget: string | null; detail: { id?: number } }): void {
        if (nestedModalReturnBound) return;
        nestedModalReturnBound = true;
        const childIds = ["code_item_regist_modal", "code_group_regist_modal"];
        const self = this;
        childIds.forEach((modalId: string): void => {
            const el: HTMLElement | null = document.getElementById(modalId);
            if (!el) return;
            el.addEventListener("hidden.bs.modal", (): void => {
                const targetId = state.modalReturnTarget;
                if (!targetId) return;
                state.modalReturnTarget = null;
                if (targetId === "code_group_detail_modal" && state.detail.id) {
                    self.openModal(targetId);
                    setTimeout((): void => self.initCodeItemDraggable(), 0);
                }
            });
        });
    },
};

