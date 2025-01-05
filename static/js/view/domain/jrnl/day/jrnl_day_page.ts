/**
 * jrnl_day_page.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page = (function(): Module {
    return {
        /**
         * Page 객체 초기화
         */
        init: function() {
            Page.setYyMnthCookie();
            dF.JrnlDayAside.init();
            // 목록 조회
            dF.JrnlDay.yyMnthListAjax();
            // 태그 조회
            dF.JrnlDayTag.listAjax();
            JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            // 일기/꿈 키워드 검색에 엔터키 처리
            cF.util.enterKey("#diaryKeyword", dF.JrnlDiary.keywordListAjax);
            cF.util.enterKey("#dreamKeyword", dF.JrnlDream.keywordListAjax);
        },

        /**
         * 페이지에 조회년월 쿠키 세팅
         */
        setYyMnthCookie: function() {
            // 년도 쿠키 설정
            const yyCookie = $.cookie("jrnl_yy");
            if (yyCookie !== undefined) document.querySelector("#jrnl_aside #yy").value = yyCookie;
            // 월 쿠키 설정
            const mnthCookie = $.cookie("jrnl_mnth");
            if (yyCookie !== undefined) document.querySelector("#jrnl_aside #mnth").value = mnthCookie;
            // 정렬 쿠키 설정
            const sortCookie = $.cookie("jrnl_day_sort");
            if (sortCookie !== undefined) document.querySelector("#jrnl_aside #sort").value = sortCookie;
            // 아무 쿠키도 없을경우 전체 데이터 로딩을 막기 위해 올해 년도 세팅
            if (yyCookie === undefined && mnthCookie === undefined) {
                $("#jrnl_aside #yy").val(cF.date.getCurrYyStr());
            }
        },
        /**
         * 달력 화면으로 이동
         */
        calPage: function(): void {
            cF.util.blockUIReplace(Url.JRNL_DAY_CAL);
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
    // 모달 댓글 모듈 수동 init (refreshFunc 주입(
    dF.Comment.modal.init({
        "refreshFunc": dF.JrnlDay.yyMnthListAjax
    });
});