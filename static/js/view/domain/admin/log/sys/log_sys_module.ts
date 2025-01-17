/**
 * log_sys_module.ts
 * 시스템 로그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.LogSys = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.LogSys.initialized) return;

            dF.LogSys.initialized = true;
            console.log("'dF.LogSys' module initialized.");
        },
        // TODO:
    }
})();