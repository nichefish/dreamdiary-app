
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

                const metaId: string = metaElmt.getAttribute("id").replace("meta-id-", "");
                dF.JrnlDayMeta.modal(metaId);
            });

            dF.JrnlDayMeta.initialized = true;
            console.log("'dF.JrnlDayMeta' module initialized.");
        },

        /**
         * 태그 카테고리 맵 조회
         */
        getCtgrMap: function(): void {
            const url: string = Url.JRNL_DAY_META_CTGR_MAP;
            cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JrnlDayMeta.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 년도 선택 처리
         * @param {string|number} yy
         */
        getSelectedYy: function(yy?: string|number): string {
            if (yy != null && cF.util.isNotEmpty(String(yy))) return String(yy);

            const currentSearchYy: string = dF.JrnlDay?.currentSearchParams?.yy;
            if (cF.util.isNotEmpty(currentSearchYy)) return currentSearchYy;

            const urlYy: string = cF.util.getUrlParam("yy");
            if (cF.util.isNotEmpty(urlYy)) return urlYy;

            return cF.date.getCurrYyStr();
        },

        /**
         * 선택된 년도 정규화 처리
         * @param {string} selectedYy
         * @param {(string|number)[]} yyList
         */
        normalizeSelectedYy: function(selectedYy: string, yyList: (string|number)[]): string {
            if (yyList.length === 0) return selectedYy;

            const matchedYy = yyList.find((yy: string|number): boolean => String(yy) === String(selectedYy));
            if (matchedYy != null) return String(matchedYy);

            return String(yyList[0]);
        },

        /**
         * 년도 옵션 처리
         * @param {string} selectedYy
         * @param {(string|number)[]} yyList
         */
        getYearOptions: function(selectedYy: string, yyList: (string|number)[]): Record<string, any>[] {
            return yyList.map((yy: string|number): Record<string, any> => ({
                value: yy,
                label: yy,
                selected: String(yy) === String(selectedYy),
            }));
        },

        getYyListAjax: function(metaId: string|number, callback: (yyList: any[]) => void): void {
            const url: string = cF.util.bindUrl(Url.JRNL_DAY_META_YYS, { id: metaId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                callback(Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        /**
         * 목록에 따른 일자 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.JRNL_DAY_METAS;
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "jrnl_day_meta_list");
            });
        },

        /**
         * 메타 모달 호출
         * @param {string|number} metaId - 조회할 메타 ID.
         */
        modal: function(metaId: string|number, yy?: string|number): void {
            if (isNaN(Number(metaId))) return;

            ModalHistory.reset();

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const preferredYy: string = dF.JrnlDayMeta.getSelectedYy(yy);
            dF.JrnlDayMeta.getYyListAjax(metaId, function(yyList: any[]): void {
                const selectedYy: string = dF.JrnlDayMeta.normalizeSelectedYy(preferredYy, yyList);
                const url: string = cF.util.bindUrl(Url.JRNL_DAYS);
                const ajaxData: Record<string, any> = { viewType: "SEARCH", metaId, yy: selectedYy };
                cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    cF.handlebars.modal({
                        metaId,
                        yy: selectedYy,
                        yearOptions: dF.JrnlDayMeta.getYearOptions(selectedYy, yyList),
                        list: res.rsltList
                    }, "jrnl_day_meta");

                    /* modal history push */
                    ModalHistory.push(self, func, args);
                });
            });
        },

        changeYy: function(metaId: string|number, yy: string|number): void {
            if (isNaN(Number(metaId))) return;
            dF.JrnlDayMeta.modal(metaId, yy);
        },


        select: function(metaId: string|number): void {
            const url: string = cF.util.bindUrl(Url.JRNL_DAY_META, { id: metaId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltObj, "jrnl_day_meta_config");
            });
        }
    }
})();
