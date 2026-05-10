import { CacheActions, CacheDetail, CacheMap } from "../types.js";

type CacheActionHooks = {
    setCacheMap: (cacheMap: CacheMap) => void;
    setDetail: (detail: CacheDetail) => void;
    removeCache: (cacheName: string) => void;
    removeEntry: (cacheName: string, cacheKey: string) => void;
};

function showModal(modalId: string): void {
    const modalEl = document.getElementById(modalId);
    if (!modalEl) return;
    const bootstrapRef = (window as any).bootstrap;
    if (bootstrapRef?.Modal?.getOrCreateInstance) {
        bootstrapRef.Modal.getOrCreateInstance(modalEl).show();
        return;
    }
    $("#" + modalId).modal("show");
}

function hideOpenModals(): void {
    document.querySelectorAll(".modal.show").forEach(function(modal: Element): void {
        const bootstrapRef = (window as any).bootstrap;
        if (bootstrapRef?.Modal?.getOrCreateInstance) {
            bootstrapRef.Modal.getOrCreateInstance(modal).hide();
            return;
        }
        $(modal).modal("hide");
    });
}

function pushModalHistory(owner: CacheActions, func: string, args: unknown[]): void {
    const history = (window as Window & { ModalHistory?: { push?: (owner: unknown, func: string, args: unknown[]) => void } }).ModalHistory;
    history?.push?.(owner, func, args);
}

export default function createCacheAdminActions(hooks: CacheActionHooks): CacheActions {
    const actions = {
        init(): void {
            console.log("'dF.Cache' module initialized.");
        },

        activeListModal(): void {
            const url: string = Url.CACHE_ACTIVE_MAP_AJAX;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                hooks.setCacheMap((res.rsltMap || {}) as CacheMap);
                requestAnimationFrame(function(): void {
                    showModal("cache_list_modal");
                    pushModalHistory(actions, "activeListModal", []);
                });
            });
        },

        detailModal(cacheName: string, cacheKey: string | number): void {
            hideOpenModals();
            const url: string = Url.CACHE_ACTIVE_DTL_AJAX;
            const ajaxData: Record<string, unknown> = { cacheName, cacheKey };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                hooks.setDetail((res.rsltObj ?? null) as CacheDetail);
                requestAnimationFrame(function(): void {
                    showModal("cache_detail_modal");
                    pushModalHistory(actions, "detailModal", [cacheName, cacheKey]);
                });
            });
        },

        clearByNmAjax(cacheName: string): void {
            const url: string = Url.CACHE_CLEAR_BY_NM_AJAX;
            const ajaxData: Record<string, unknown> = { cacheName };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) hooks.removeCache(cacheName);
                    });
            });
        },

        evictAjax(cacheName: string, cacheKey: string): void {
            const url: string = Url.CACHE_EVICT_AJAX;
            const ajaxData: Record<string, unknown> = { cacheName, cacheKey };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) hooks.removeEntry(cacheName, cacheKey);
                    });
            });
        },

        clearAllAjax(): void {
            const url: string = Url.CACHE_CLEAR_AJAX;
            cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (!res.rslt) {
                            cF.ui.swalOrAlert(res.message);
                            return;
                        }
                        cF.ui.blockUIReload();
                    });
            });
        },

        closeModal(): void {
            const history = (window as Window & { ModalHistory?: { prev?: () => void } }).ModalHistory;
            history?.prev?.();
        },
    } as CacheActions;

    return actions;
}
