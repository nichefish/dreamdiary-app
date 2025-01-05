/**
 * admin_page.ts
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
            console.log("Page scripts initialized.");
        },

        /**
         * 휴일정보 : url, data 받아서 ajax 호출
         */
        hldyAjax: function(): void {
            const url: string = Url.API_HLDY_GET;
            const ajaxData: Record<string, any> = { "yy" : $("#hldyYy option:selected").val() };
            Page.ajax(url, ajaxData);
        },

        /**
         * NOTION : url, data 받아서 ajax 호출
         */
        notionAjax: function(): void {
            const url: string = Url.API_NOTION_GET;
            const ajaxData: Record<string, any> = { "dataType": $("#dataType").val(), "dataId": $("#dataId").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message });
            });
        },

        /**
         * url, data 받아서 ajax 호출
         * @param {string} url - 요청을 보낼 대상 URL.
         * @param {Record<string, any>} data - 서버로 전송할 데이터 객체.
         */
        ajax: function(url: string, data: Record<string, any>): void {
            cF.ajax.post(url, data, function(res: AjaxResponse): void {
                if (res.rslt) Swal.fire(JSON.stringify(res));
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                        if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                    });
            });
        },

        /**
         * 사이트 관리 페이지로 이동
         */
        adminPage: function(): void {
            cF.util.blockUISubmit("#procForm", Url.ADMIN_PAGE);
        },

        /**
         * 코드 관리 페이지로 이동
         */
        cdList: function(): void {
            cF.util.blockUISubmit("#procForm", Url.CL_CD_LIST);
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});