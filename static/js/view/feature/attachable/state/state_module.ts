/**
 * state_module.ts
 * 상태 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.State = (function(): dfModule {
    return {
        initialized: false,
        swappable: null,

        /**
         * State 객체 초기화
         * @param {Record<string, any>} options - 초기화 옵션 객체.
         * @param {Function} [options.refreshFunc] - 섹션 새로 고침에 사용할 함수 (선택적).
         */
        init: function ({refreshFunc}: { refreshFunc?: Function } = {}): void {
            if (dF.State.initialized) return;

            if (refreshFunc != null) dF.State.refreshFunc = refreshFunc;

            dF.State.initialized = true;
            console.log("'dF.State' module initialized.");
        },

        resolveJournalCacheContext: function(item?: HTMLElement | null): Record<string, any> {
            const itemYy: string = item?.dataset?.yy ?? "";
            const itemMnth: string = item?.dataset?.mnth ?? "";
            const dayElement: HTMLElement = item?.closest?.(".journal-day") as HTMLElement;
            const stdrdDt: string = item?.dataset?.stdrdDt
                ?? dayElement?.dataset?.stdrdDt
                ?? cF.util.getUrlParam("stdrdDt")
                ?? "";

            const resolvedYy: string = cF.util.isNotEmpty(itemYy)
                ? itemYy
                : (cF.util.isNotEmpty(stdrdDt) ? stdrdDt.substring(0, 4) : (cF.util.getUrlParam("yy") ?? ""));
            const resolvedMnth: string = cF.util.isNotEmpty(itemMnth)
                ? itemMnth
                : (cF.util.isNotEmpty(stdrdDt) ? String(parseInt(stdrdDt.substring(5, 7), 10)) : (cF.util.getUrlParam("mnth") ?? ""));
            const weekStartDt: string = cF.util.isNotEmpty((window as any).Page?.weekStartDt)
                ? (window as any).Page.weekStartDt
                : (cF.util.isNotEmpty(stdrdDt) ? (cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? "") : "");

            const cacheContext: Record<string, any> = {};
            if (cF.util.isNotEmpty(resolvedYy)) cacheContext.yy = Number(resolvedYy);
            if (cF.util.isNotEmpty(resolvedMnth)) cacheContext.mnth = Number(resolvedMnth);
            if (cF.util.isNotEmpty(weekStartDt)) cacheContext.weekStartDt = weekStartDt;

            return cacheContext;
        },

        /**
         * 상태 변경 처리. (Ajax)
         * @param {id: string|number, contentType: string, stateKey: string, cacheContext?: Record<string, any>} payload
         * @param {Function} [callback]
         */
        toggleAjax: function (
            payload: { id: string|number, contentType: string, stateKey: string, cacheContext?: Record<string, any> },
            callback: Function
        ): void {

            const url: string = Url.STATES;
            cF.$ajax.postJson(url, payload, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return cF.ui.swalOrAlert(res.message);
                }

                if (typeof callback === "function") callback?.(res);
            }, "block");
        },
    }
})();

