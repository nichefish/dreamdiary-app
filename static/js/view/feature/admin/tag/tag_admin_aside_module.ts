/**
 * tag_aside_module.ts
 * 태그 관리 사이드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.TagAdminAside = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.TagAdminAside.initialized) return;

            dF.TagAdminAside.initialized = true;
            console.log("'dF.TagAdminAside' module initialized.");
        },
    }
})();