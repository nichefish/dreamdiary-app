/**
 * lifecycle_module.ts
 * 부착 가능 컨텐츠의 단일 현재 라이프사이클 처리 모듈.
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Lifecycle = (function(): dfModule {
    return {
        initialized: false,

        /**
         * 페이지에서 라이프사이클 모듈을 한 번만 초기화한다.
         */
        init: function(): void {
            if (dF.Lifecycle.initialized) return;

            dF.Lifecycle.initialized = true;
            console.log("'dF.Lifecycle' module initialized.");
        },

        /**
         * 라이프사이클 변경에 필요한 저널 월간/주간 캐시 컨텍스트를 계산한다.
         *
         * 라이프사이클은 state와 같은 저널 캐시 주소 체계를 사용한다. 캐시 컨텍스트 helper가
         * 부착 가능 컨텐츠 공통으로 완전히 이동하기 전까지는 state 모듈의 helper를 재사용한다.
         *
         * @param item 날짜 메타데이터를 가진 journal item 엘리먼트
         * @returns 백엔드 캐시 updater가 사용할 캐시 컨텍스트
         */
        resolveJournalCacheContext: function(item?: HTMLElement | null): Record<string, any> {
            return dF.State.resolveJournalCacheContext(item);
        },

        /**
         * 부착 가능 컨텐츠 하나의 단일 현재 라이프사이클 값을 설정한다.
         *
         * @param payload 컨텐츠 ID/타입, 라이프사이클 키, 선택 캐시 컨텍스트
         * @param callback ajax 응답을 받을 선택 callback
         */
        setAjax: function(
            payload: { id: string|number, contentType: string, lifecycleKey: string, cacheContext?: Record<string, any> },
            callback?: Function
        ): void {
            const url: string = Url.LIFECYCLES;
            cF.$ajax.put(url, payload, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return cF.ui.swalOrAlert(res.message);
                }

                if (typeof callback === "function") callback?.(res);
            }, "block");
        },
    }
})();
