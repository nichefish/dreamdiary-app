/**
 * jrnl_sumry_aside_module.ts
 *
 * @author nichefish
 */
const JrnlSumryAside = (function() {
    return {
        yyMnth: function(obj) {
            // 쿠키 설정하기
            const id = $(obj).attr("id");
            const cookieOptions = {
                path: "/jrnl/sumry/",
                expires: cF.date.getCurrDateAddDay(36135)
            };
            $.cookie("jrnl_" + id, $(obj).val(), cookieOptions);
            // 목록 조회
            JrnlSumry.listAjax();
        },
    }
})();