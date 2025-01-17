/**
 * jrnl_sumry_aside_module.ts
 * 저널 결산 사이드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlSumryAside = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlSumryAside.initialized) return;

            dF.JrnlSumryAside.initialized = true;
            console.log("'dF.JrnlSumryAside' module initialized.");
        },

        yyMnth: function(obj: HTMLInputElement): void {
            // 쿠키 설정하기
            const id: string = $(obj).attr("id");
            const cookieOptions = {
                path: "/jrnl/sumry/",
                expires: cF.date.getCurrDateAddDay(36135)
            };
            $.cookie("jrnl_" + id, $(obj).val(), cookieOptions);
            // 목록 조회
            dF.JrnlSumry.listAjax();
        },
    }
})();