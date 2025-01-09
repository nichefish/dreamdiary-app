/**
 * log_sys_module.ts
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