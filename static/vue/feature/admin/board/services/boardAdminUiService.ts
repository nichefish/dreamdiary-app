/**
 * 게시판 관리 Vue 화면 UI 유틸
 */

function bootstrapAny(): any {
    return (window as any).bootstrap;
}

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
};
