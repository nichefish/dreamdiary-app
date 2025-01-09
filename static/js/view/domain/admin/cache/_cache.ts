// @ts-ignore
// @ts-ignore
/**
 * _cache.ts
 * 캐시 : 스크립트 모듈 분리
 *
 * @author nichefish
 */
// @ts-ignore
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Cache = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'dF.Cache' module initialized.");
        },

        /**
         * url, data 받아서 ajax 호출
         */
        activeListModal: function(): void {
            const url: string = Url.CACHE_ACTIVE_LIST_AJAX;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                cF.handlebars.template(res.rsltMap, "cache_list", "show");
            });
        },

        /**
         * 캐시 무효화 처리 (Ajax)
         * @param {string} cacheName - 캐시 이름.
         * @param {string} key - 무효화할 캐시의 키.
         */
        evictAjax: function(cacheName: string, key: string): void {
            const url: string = Url.CACHE_EVICT_AJAX;
            const ajaxData: Record<string, any> = { "cacheName": cacheName, "key": key };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                        if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                    });
            });
        },

        /**
         * 전체 캐시 무효화 (Ajax)
         */
        clearAllAjax: function(): void {
            const url: string = Url.CACHE_CLEAR_AJAX;
            cF.$ajax.post(url, null, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                        if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                    });
            });
        },
    }
})();