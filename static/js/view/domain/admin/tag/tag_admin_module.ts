/**
 * tag_admin_module.ts
 * 태그 관리 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.TagAdmin = (function(): dfModule {
    const self: dfModule = this;

    return {
        initialized: false,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.TagAdmin.initialized) return;

            /* initialize submodules. */
            dF.TagAdminAside.init();

            dF.TagAdmin.initialized = true;
            console.log("'dF.TagAdmin' module initialized.");
        },

        /**
         * 전체 태그 목록 조회 (Ajax)
         * @param {string} refContentType - 조회할 태그의 참조 콘텐츠 유형.
         */
        tagListAjax: function(refContentType: string): void {
            const url: string = Url.TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "contentType": refContentType };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 상단에 태그 카테고리 메뉴 생성
                const ctgrSet: Set<string> = new Set();
                res.rsltList.forEach((item: Record<string, string>): void => {
                    if (item.ctgr) ctgrSet.add(item.ctgr);
                });
                cF.handlebars.template(ctgrSet, "tag_ctgr");
                cF.handlebars.template(res.rsltList, "tag_list");
            });
        },

        /**
         * 관리 모달 호출
         * @param {string|number} tagNo - 조회할 태그 번호.
         */
        dtlModal: function(tagNo: string|number): void {
            if (isNaN(Number(tagNo))) return;

            const url: string = Url.TAG_DTL_AJAX;
            const ajaxData: Record<string, any> = { "tagNo": tagNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 태그 상세 정보
                cF.handlebars.modal(res.rsltObj, "tag_admin_dtl");
            });
        },

        /**
         * 속성 추가 모달 호출
         * @param {string|number} tagNo - 속성을 추가할 태그 번호.
         */
        addPropertyModal: function(tagNo: string|number): void {
            if (isNaN(Number(tagNo))) return;

            cF.handlebars.modal({}, "tag_property_reg");
        }
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    Page.init();
});