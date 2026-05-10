import { CacheActions } from "../types.js";

declare global {
    interface Window {
        dF?: Record<string, any>;
    }
}

export default function bindCacheAdminEventBridge(actions: CacheActions): void {
    const win = window as Window & { dF?: Record<string, any> };
    if (typeof win.dF === "undefined") win.dF = {};
    win.dF.Cache = {
        init: actions.init,
        activeListModal: actions.activeListModal,
        detailModal: actions.detailModal,
        clearByNmAjax: actions.clearByNmAjax,
        evictAjax: actions.evictAjax,
        clearAllAjax: actions.clearAllAjax,
        closeModal: actions.closeModal,
    };
}
