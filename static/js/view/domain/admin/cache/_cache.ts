/**
 * _cache.ts
 * 캐시 : 스크립트 모듈 분리
 *
 * @author nichefish
 */
const Cache = (function() {
    return {
        /**
         * url, data 받아서 ajax 호출
         */
        activeListModal: function() {
            const url = Url.CACHE_ACTIVE_LIST_AJAX;
            commons.util.blockUIAjax(url, 'GET', null, function(res: AjaxResponse) {
                commons.util.handlebarsTemplate(res.rsltMap, "cache_list", "show");
            });
        },

        /**
         * 캐시 무효화 처리 (Ajax)
         * @param {string} cacheName - 캐시 이름.
         * @param {string} key - 무효화할 캐시의 키.
         */
        evictAjax: function(cacheName: string, key: string) {
            const url = Url.CACHE_EVICT_AJAX;
            const ajaxData = { "cacheName": cacheName, "key": key };
            commons.util.blockUIAjax(url, 'POST', ajaxData, function(res: AjaxResponse) {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                        if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                    });
            });
        },

        /**
         * 전체 캐시 무효화 (Ajax)
         */
        clearAllAjax: function() {
            const url = Url.CACHE_CLEAR_AJAX;
            commons.util.blockUIAjax(url, 'POST', null, function(res: AjaxResponse) {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function() {
                        if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                        if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                    });
            });
        },
    }
})();