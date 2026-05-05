/**
 * 사이트 관리 화면 액션 (API·캐시 모듈 브리지)
 *
 * 동작: 휴일/노션 API Ajax, 캐시 모달(`dF.Cache` + `_cache.js`) 연동.
 */

export type AdminPageActions = {
    holydayAjax: (yy: string) => void;
    notionAjax: (dataType: string, dataId: string) => void;
    cacheActiveListModal: () => void;
    cacheClearAllAjax: () => void;
    initCacheModule: () => void;
};

function postAjaxThenSwal(url: string, data: Record<string, unknown>): void {
    cF.$ajax.post(url, data, (res: AjaxResponse): void => {
        if (res.rslt) Swal.fire(JSON.stringify(res));
        Swal.fire({ text: res.message }).then((): void => {
            if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
            if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
        });
    });
}

export default function createAdminPageActions(): AdminPageActions {
    return {
        holydayAjax(yy: string): void {
            const url: string = (Url as { API_HOLYDAY_GET?: string }).API_HOLYDAY_GET || "";
            if (!url) return;
            postAjaxThenSwal(url, { yy });
        },

        notionAjax(dataType: string, dataId: string): void {
            const url: string = (Url as { API_NOTION_GET?: string }).API_NOTION_GET || "";
            if (!url) return;
            cF.ajax.get(url, { dataType, dataId }, (res: AjaxResponse): void => {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message });
            });
        },

        cacheActiveListModal(): void {
            const dFRef = (globalThis as { dF?: { Cache?: { activeListModal?: () => void; clearAllAjax?: () => void; init?: () => void } } }).dF;
            const cache = dFRef?.Cache;
            if (cache && typeof cache.activeListModal === "function") {
                cache.activeListModal();
                return;
            }
            console.warn("[AdminPageApp] dF.Cache.activeListModal unavailable (load _cache.js?)");
        },

        cacheClearAllAjax(): void {
            const dFRef = (globalThis as { dF?: { Cache?: { clearAllAjax?: () => void } } }).dF;
            const cache = dFRef?.Cache;
            if (cache && typeof cache.clearAllAjax === "function") {
                cache.clearAllAjax();
                return;
            }
            console.warn("[AdminPageApp] dF.Cache.clearAllAjax unavailable (load _cache.js?)");
        },

        initCacheModule(): void {
            const dFRef = (globalThis as { dF?: { Cache?: { init?: () => void } } }).dF;
            const cache = dFRef?.Cache;
            if (cache && typeof cache.init === "function") {
                cache.init();
            }
        },
    };
}
