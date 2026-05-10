export type CacheMap = Record<string, Record<string, unknown>>;

export type CacheDetail = Record<string, unknown> | unknown[] | string | number | boolean | null;

export type CacheActions = {
    init: () => void;
    activeListModal: () => void;
    detailModal: (cacheName: string, cacheKey: string | number) => void;
    clearByNmAjax: (cacheName: string) => void;
    evictAjax: (cacheName: string, cacheKey: string) => void;
    clearAllAjax: () => void;
    closeModal: () => void;
};
