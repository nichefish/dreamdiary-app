
/**
 * jrnl_day_meta_module.ts
 * 저널 일자 태그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDayMeta = (function(): dfModule {
    return {
        initialized: false,
        ctgrMap: new Map(),

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDayMeta.initialized) return;

            dF.JrnlDayMeta.getCtgrMap();

            document.addEventListener('click', function (e: MouseEvent): void {
                const target: EventTarget = e.target;
                if (!(target instanceof HTMLElement)) return;
                const metaElmt: HTMLElement = target.closest('.meta-item');
                if (!metaElmt) return;

                e.preventDefault();

                const metaNo: string = metaElmt.getAttribute("id").replace("meta-no-", "");
                dF.JrnlDayMeta.showMeta(metaNo);
            });

            dF.JrnlDayMeta.initialized = true;
            console.log("'dF.JrnlDayMeta' module initialized.");
        },

        /**
         * 태그 카테고리 정보 조회
         */
        getCtgrMap: function(): void {
            const url: string = Url.JRNL_DAY_META_CTGR_MAP;
            cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JrnlDayMeta.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 메타 모달 호출
         * @param {string|number} metaNo - 조회할 메타 번호.
         */
        showMeta: function(metaNo: string|number): void {
            if (isNaN(Number(metaNo))) return;

            ModalHistory.reset();

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = cF.util.bindUrl(Url.JRNL_DAYS);
            const ajaxData: Record<string, any> = { viewType: "SEARCH", metaNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal({ metaNo, list: res.rsltList }, "jrnl_day_meta");

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        }
    }
})();