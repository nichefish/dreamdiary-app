/**
 * journalDayTagContextMenuShell.ts
 * 저널 일자 태그 컨텍스트 메뉴 서비스 — 전역 <code>dF.JournalDayTagContextMenu</code>.
 *
 * 변경(P7): <code>static/js/view/feature/journal/day/tag/journal_day_tag_context_menu_service.ts</code> 본문을 Vue 축으로 이전.
 * 로드: <code>registerJournalDayShellServices</code> side-effect import.
 */
const __journalDayGlobal: any = typeof globalThis !== "undefined" ? globalThis : (window as any);
if (__journalDayGlobal.dF == null) {
    __journalDayGlobal.dF = {};
}
const dfNs: any = __journalDayGlobal.dF;
dfNs.JournalDayTagContextMenu = (function(): Module {
    let contextMenuEl: HTMLElement | null = null;
    let contextMenuState: Record<string, any> | null = null;
    let contextMenuBound: boolean = false;
    let contextAnchorEl: HTMLElement | null = null;
    let contextAnchorPrevStyle: string | null = null;

    function getCurrentClickEvent(): Event | null {
        return (typeof event !== "undefined" && event) ? event as Event : null;
    }

    function stopCurrentClickEvent(): void {
        const currentEvent: Event | null = getCurrentClickEvent();
        if (!currentEvent) return;
        currentEvent.stopPropagation();
        if (typeof currentEvent.preventDefault === "function") currentEvent.preventDefault();
    }

    function getContextAnchorEl(): HTMLElement | null {
        const currentEvent: Event | null = getCurrentClickEvent();
        if (!(currentEvent?.target instanceof HTMLElement)) return null;
        return currentEvent.target.closest("[onclick]") as HTMLElement | null;
    }

    function resetContextAnchor(): void {
        const anchorEl: HTMLElement | null = contextAnchorEl;
        if (!(anchorEl instanceof HTMLElement)) return;

        if (contextAnchorPrevStyle == null || contextAnchorPrevStyle === "") anchorEl.removeAttribute("style");
        else anchorEl.setAttribute("style", contextAnchorPrevStyle);

        contextAnchorEl = null;
        contextAnchorPrevStyle = null;
    }

    function highlightContextAnchor(anchorEl?: HTMLElement | null): void {
        resetContextAnchor();
        if (!(anchorEl instanceof HTMLElement)) return;

        contextAnchorEl = anchorEl;
        contextAnchorPrevStyle = anchorEl.getAttribute("style");
        anchorEl.style.background = "#eef6ff";
        anchorEl.style.color = "#0b63ce";
        anchorEl.style.borderRadius = "10px";
        anchorEl.style.boxShadow = "0 0 0 1px rgba(11, 99, 206, 0.18), 0 10px 24px rgba(11, 99, 206, 0.12)";
        anchorEl.style.transition = "background-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease";
    }

    function getContextTooltipSelector(): string {
        return [
            "[onclick*='dF.JournalDayTagService.select(']",
            "[onclick*='dF.JournalEntryTag.get(']",
        ].join(", ");
    }

    function applyTooltipText(targetEl: HTMLElement, messageKey: string): void {
        const message: string = Message.get(messageKey);
        targetEl.setAttribute("title", message);
        targetEl.setAttribute("data-bs-original-title", message);
        targetEl.setAttribute("aria-label", message);
    }

    function getContextMenuPosition(menuEl: HTMLElement): Record<string, number> {
        const currentEvent: Event | null = getCurrentClickEvent();
        let left: number = 16;
        let top: number = 16;

        if (currentEvent instanceof MouseEvent) {
            left = currentEvent.clientX;
            top = currentEvent.clientY + 8;
        } else if (currentEvent?.target instanceof HTMLElement) {
            const rect: DOMRect = currentEvent.target.getBoundingClientRect();
            left = rect.left;
            top = rect.bottom + 8;
        }

        const menuWidth: number = menuEl.offsetWidth || 164;
        const menuHeight: number = menuEl.offsetHeight || 110;
        left = Math.min(Math.max(left, 8), Math.max(window.innerWidth - menuWidth - 8, 8));
        top = Math.min(Math.max(top, 8), Math.max(window.innerHeight - menuHeight - 8, 8));
        return { left, top };
    }

    function hideContextMenu(): void {
        if (contextMenuEl instanceof HTMLElement) contextMenuEl.style.display = "none";
        contextMenuState = null;
        resetContextAnchor();
    }

    function runContextMenuSearch(): void {
        const currentState: Record<string, any> | null = contextMenuState;
        hideContextMenu();
        if (currentState?.onSearch) currentState.onSearch();
    }

    function runContextMenuConfigure(): void {
        const currentState: Record<string, any> | null = contextMenuState;
        hideContextMenu();
        if (currentState?.onConfigure) currentState.onConfigure();
    }

    function ensureContextMenu(): HTMLElement {
        if (contextMenuEl instanceof HTMLElement) return contextMenuEl;
        const menuEl: HTMLDivElement = document.createElement("div");
        menuEl.id = "journal_day_tag_context_menu";
        menuEl.style.cssText = [
            "position: fixed",
            "display: none",
            "min-width: 176px",
            "padding: 8px",
            "background: rgba(255, 255, 255, 0.96)",
            "backdrop-filter: blur(10px)",
            "border: 1px solid rgba(11, 99, 206, 0.12)",
            "border-radius: 14px",
            "box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18)",
            "z-index: 2000",
        ].join("; ");

        const createMenuButton = function(config: { action: "search" | "configure"; iconClass: string; label: string; styleText: string }): HTMLButtonElement {
            const buttonEl: HTMLButtonElement = document.createElement("button");
            buttonEl.type = "button";
            buttonEl.setAttribute("data-action", config.action);
            buttonEl.style.cssText = config.styleText;

            const iconEl: HTMLElement = document.createElement("i");
            iconEl.className = config.iconClass;
            iconEl.style.fontSize = "13px";
            buttonEl.appendChild(iconEl);

            const labelEl: HTMLSpanElement = document.createElement("span");
            labelEl.textContent = config.label;
            buttonEl.appendChild(labelEl);
            return buttonEl;
        };

        const searchButtonEl = createMenuButton({
            action: "search",
            iconClass: "bi bi-search",
            label: "검색",
            styleText: "display:flex;align-items:center;gap:10px;width:100%;padding:10px 12px;border:0;background:#edf5ff;color:#0b63ce;text-align:left;border-radius:10px;font-weight:600",
        });
        const configureButtonEl = createMenuButton({
            action: "configure",
            iconClass: "bi bi-sliders2",
            label: "태그 설정",
            styleText: "display:flex;align-items:center;gap:10px;width:100%;padding:10px 12px;border:0;background:transparent;color:#7c2d12;text-align:left;border-radius:10px;cursor:pointer;margin-top:4px",
        });
        menuEl.appendChild(searchButtonEl);
        menuEl.appendChild(configureButtonEl);
        applyTooltipText(searchButtonEl, "view.tag.content-list");

        menuEl.addEventListener("click", function(evt: Event): void {
            evt.stopPropagation();
            let target: HTMLElement | null = evt.target as HTMLElement | null;
            while (target && target !== menuEl && !target.hasAttribute("data-action")) target = target.parentElement;
            if (!target || target === menuEl) return;
            if (target.getAttribute("data-action") === "search") runContextMenuSearch();
            if (target.getAttribute("data-action") === "configure") runContextMenuConfigure();
        });

        menuEl.querySelectorAll("button[data-action]").forEach(function(button: Element): void {
            const btn: HTMLElement = button as HTMLElement;
            const baseBg: string = btn.getAttribute("data-action") === "search" ? "#edf5ff" : "transparent";
            const hoverBg: string = btn.getAttribute("data-action") === "search" ? "#dbeafe" : "#ffedd5";
            btn.addEventListener("mouseenter", function(): void { btn.style.background = hoverBg; });
            btn.addEventListener("mouseleave", function(): void { btn.style.background = baseBg; });
        });

        document.body.appendChild(menuEl);
        contextMenuEl = menuEl;
        return menuEl;
    }

    function bindContextMenuEvents(): void {
        if (contextMenuBound) return;

        document.addEventListener("click", function(evt: Event): void {
            if (!(contextMenuEl instanceof HTMLElement)) return;
            if (contextMenuEl.contains(evt.target as Node)) return;
            hideContextMenu();
        });
        document.addEventListener("mouseover", function(evt: Event): void {
            if (!(evt.target instanceof HTMLElement)) return;
            const tooltipTarget: HTMLElement | null = evt.target.closest(getContextTooltipSelector()) as HTMLElement | null;
            if (!tooltipTarget) return;
            applyTooltipText(tooltipTarget, "bs.tooltip.context.menu.show");
        });
        document.addEventListener("keydown", function(evt: KeyboardEvent): void {
            if (evt.key === "Escape") hideContextMenu();
        });
        window.addEventListener("resize", function(): void { hideContextMenu(); });
        window.addEventListener("scroll", function(): void { hideContextMenu(); }, true);
        contextMenuBound = true;
    }

    return {
        bindContextMenuEvents: bindContextMenuEvents,
        syncContextTooltipTargets: function(rootEl?: ParentNode): void {
            const queryRoot: ParentNode = rootEl ?? document;
            queryRoot.querySelectorAll(getContextTooltipSelector()).forEach(function(el: Element): void {
                applyTooltipText(el as HTMLElement, "bs.tooltip.context.menu.show");
            });
        },
        openContextMenu: function(args: {
            tagId: string|number;
            tagNm: string;
            ctgr: string;
            contentType: string;
            onSearch: () => void;
            onConfigure: () => void;
        }): void {
            const anchorEl: HTMLElement | null = getContextAnchorEl();
            stopCurrentClickEvent();
            highlightContextAnchor(anchorEl);

            const menuEl: HTMLElement = ensureContextMenu();
            contextMenuState = {
                tagId: args.tagId,
                tagNm: args.tagNm,
                ctgr: args.ctgr,
                contentType: args.contentType,
                onSearch: args.onSearch,
                onConfigure: args.onConfigure,
            };
            bindContextMenuEvents();

            menuEl.style.display = "block";
            const position: Record<string, number> = getContextMenuPosition(menuEl);
            menuEl.style.left = `${position.left}px`;
            menuEl.style.top = `${position.top}px`;
        },
        hideContextMenu: hideContextMenu,
    };
})();

export {};
