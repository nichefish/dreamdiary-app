/**
 * tmplat_def_list.ts
 *
 * @author nichefish
 */
// @ts-ignore
const Page = (function() {
    return {
        /**
         * Page 객체 초기화
         */
        init: function(): void {
            /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
            cF.util.initSortTable();
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.util.blockUISubmit("#listForm", `${Url.TMPLAT_DEF_LIST!}?actionTyCd=SEARCH`);
        },

        /**
         * 내가 작성한 글 목록 보기
         */
        myPaprList: function(): void {
            const url: string = Url.TMPLAT_DEF_LIST;
            const param: string = `?searchType=nickNm&searchKeyword=${AuthInfo.nickNm!}&regstrId=${AuthInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR`;
            cF.util.blockUIReplace(url + param);
        },

        /**
         * 등록 화면으로 이동
         */
        regForm: function(): void {
            cF.util.blockUIRequest();
            cF.handlebars.template({}, "tmplat_def_reg", "show");
        },

        /**
         * 등록 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.reg"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                $("#clCdRegForm #regYn").val("Y");
                const url: string = Url.TMPLAT_DEF_REG_AJAX;
                const ajaxData: Record<string, any> = $("#clCdRegForm").serializeArray();
                cF.ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.util.blockUIReplace(Url.CL_CD_LIST);
                        });
                });
            });
        },

        /**
         * 상세 화면으로 이동
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtl: function(postNo: string|number): void {
            if (isNaN(Number(postNo))) return;

            $("#procForm #postNo").val(postNo);
            cF.util.blockUISubmit("#procForm", Url.TMPLAT_DEF_DTL);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} postNo - 조회할 글 번호.
         */
        dtlModal: function(postNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(postNo))) return;

            const url: string = Url.TMPLAT_DEF_DTL_AJAX;
            const ajaxData: Record<string, any> = { "postNo": postNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltObj, "tmplat_dtl", "show");
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function() {
    Page.init();
});