/**
 * journal_annual_aside_module.ts
 * 저널 결산 사이드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JournalAnnualAside = (function(): dfModule {
    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JournalAnnualAside.initialized) return;

            dF.JournalAnnualAside.initialized = true;
            console.log("'dF.JournalAnnualAside' module initialized.");
        },

        yyMnth: function(obj: HTMLInputElement): void {
            // 쿠키 설정하기
            const id: string = $(obj).attr("id");
            const cookieOptions = {
                path: "/journal/annual/",
                expires: cF.date.getCurrDateAddDay(36135)
            };
            $.cookie("journal_" + id, $(obj).val(), cookieOptions);
            // 목록 조회
            dF.JournalAnnual.listAjax();
        },
    }
})();
