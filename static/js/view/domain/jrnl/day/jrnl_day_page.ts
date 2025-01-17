/**
 * jrnl_day_page.ts
 * 저널 일자 페이지 스크립트
 *
 * @author nichefish
 */
// @ts-ignore
const Page: Page = (function(): Page {
    return {
        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* initialize modules. */
            dF.JrnlDay.init();
            dF.JrnlDiary.init();
            dF.JrnlDream.init();
            dF.Comment.modal.init({
                "refreshFunc": dF.JrnlDay.yyMnthListAjax
            });

            Page.setYyMnthCookie();
            dF.JrnlDayAside.init();
            // 목록 조회
            dF.JrnlDay.yyMnthListAjax();
            // 태그 조회
            dF.JrnlDayTag.listAjax();
            dF.JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            // 일기/꿈 키워드 검색에 엔터키 처리
            cF.util.enterKey("#diaryKeyword", dF.JrnlDiary.keywordListAjax);
            cF.util.enterKey("#dreamKeyword", dF.JrnlDream.keywordListAjax);
        },

        /**
         * 페이지에 조회년월 쿠키 세팅
         */
        setYyMnthCookie: function(): void {
            // 년도 쿠키 설정
            const yyCookie = $.cookie("jrnl_yy");
            const yyElement = document.querySelector("#jrnl_aside #yy") as HTMLInputElement | null;
            if (yyCookie !== undefined && yyElement !== null) {
                yyElement.value = yyCookie;
            }
            // 월 쿠키 설정
            const mnthCookie = $.cookie("jrnl_mnth");
            const mnthElement = document.querySelector("#jrnl_aside #mnth") as HTMLInputElement | null;
            if (mnthCookie !== undefined && mnthElement !== null) {
                mnthElement.value = mnthCookie;
            }
            // 정렬 쿠키 설정
            const sortCookie = $.cookie("jrnl_day_sort");
            const sortElement = document.querySelector("#jrnl_aside #sort") as HTMLInputElement | null;
            if (sortCookie !== undefined && sortElement !== null) {
                sortElement.value = sortCookie;
            }
            // 아무 쿠키도 없을경우 전체 데이터 로딩을 막기 위해 올해 년도 세팅
            if (yyCookie === undefined && mnthCookie === undefined) {
                $("#jrnl_aside #yy").val(cF.date.getCurrYyStr());
            }
        },
        /**
         * 달력 화면으로 이동
         */
        calPage: function(): void {
            cF.ui.blockUIReplace(Url.JRNL_DAY_CAL);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});