/**
 * _cache.ts
 * 캐시 스크립트 모듈
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
            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = Url.CACHE_ACTIVE_MAP_AJAX;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                cF.handlebars.modal(res.rsltMap, "cache_list");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * url, data 받아서 ajax 호출
         */
        dtlModal: function(cacheName: string, cacheKey: string|number): void {
            // 기존에 열린 모달이 있으면 닫기
            const openModals: NodeList = document.querySelectorAll('.modal.show'); // 열린 모달을 찾기
            openModals.forEach((modal: Node): void => {
                $(modal).modal('hide');  // 각각의 모달을 닫기
            });

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = Url.CACHE_ACTIVE_DTL_AJAX;
            const ajaxData: Record<string, any> = { cacheName: cacheName, cacheKey: cacheKey };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                cF.handlebars.modal(res.rsltObj, "cache_dtl");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 캐시 무효화 처리 (Ajax)
         * 해당 CacheName 전체 무효화
         * @param {string} cacheName - 캐시 이름.
         */
        clearByNmAjax: function(cacheName: string): void {
            const url: string = Url.CACHE_CLEAR_BY_NM_AJAX;
            const ajaxData: Record<string, any> = { cacheName: cacheName };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) {
                            document.querySelector(`div.row[data-cache-name="${cacheName}"]`)?.remove();
                        }
                    });
            });
        },

        /**
         * 캐시 무효화 처리 (Ajax)
         * 해당 CacheName 전체 무효화
         * @param {string} cacheName - 캐시 이름.
         * @param cacheKey
         */
        evictAjax: function(cacheName: string, cacheKey: string): void {
            const url: string = Url.CACHE_EVICT_AJAX;
            const ajaxData: Record<string, any> = { cacheName: cacheName, cacheKey: cacheKey };
            cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rslt) {
                            const parentRow: HTMLDivElement = document.querySelector(`.row[data-cache-name="${cacheName}"]`);
                            parentRow.querySelector(`div.row[data-cache-key="${cacheKey}"]`)?.remove();

                            if (parentRow && parentRow.querySelectorAll(`.row[data-cache-key]`).length === 0) {
                                const nextSeparator: Element = parentRow.nextElementSibling;
                                if (nextSeparator && nextSeparator.matches('.separator.my-5')) {
                                    nextSeparator.remove();
                                }

                                parentRow.remove();

                                const prevSeparator: Element = parentRow.previousElementSibling;
                                if (prevSeparator && prevSeparator.matches('.separator.my-5')) {
                                    prevSeparator.remove();
                                }
                            }
                        }
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
                        if (!res.rslt) cF.ui.swalOrAlert(res.message);
                    });
            });
        },

        /**
         * 모달 닫기 시 수행할 로직
         */
        closeModal: function(): void {
            /* modal history pop */
            ModalHistory.prev();
        }
    }
})();