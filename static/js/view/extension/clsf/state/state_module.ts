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

        /**
         * 상태 변경 처리. (Ajax)
         * @param {postNo: string|number, contentType: string, stateCd: string} payload
         * @param {Function} [callback]
         */
        toggleAjax: function (payload: { postNo: string|number, contentType: string, stateCd: string }, callback: Function): void {

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