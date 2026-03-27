/**
 * log_stats_user_list.ts
 * 로그 통계 - 사용자별 로그 목록 페이지 스크립트
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
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.table.initSort();
        },

        /**
         * 전체 로그 페이지로 이동
         */
        logActvtyList: function(): void {
            cF.form.blockUISubmit("#listForm", Url.LOG_ACTVTY_LIST);
        },

        /**
         * 전체 로그 페이지로 이동
         */
        logStatsUserList: function(): void {
            cF.form.blockUISubmit("#listForm", Url.LOG_STATS_USER_LIST);
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            const url: string = `${Url.LOG_STATS_USER_LIST!}?actionTyCd=SEARCH`;
            cF.form.blockUISubmit("#listForm", url);
        },

        /**
         * 엑셀 다운로드
         */
        xlsxDownload: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.download"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.util.blockUIFileDownload();
                const url: string = Url.LOG_ACTVTY_LIST_XLSX_DOWNLOAD;
                $("#listForm").attr("action", url).submit();
            });
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});