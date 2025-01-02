/**
 * tag_admin_module.ts
 *
 * @author nichefish
 */
const TagAdmin = (function() {
    return {
        /**
         * 전체 태그 목록 조회 (Ajax)
         * @param {string} refContentType - 조회할 태그의 참조 콘텐츠 유형.
         */
        tagListAjax: function(refContentType) {
            const url: string = Url.TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "contentType": refContentType };
            cF.ajax.get(url, ajaxData, function(res) {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return false;
                }
                // 상단에 태그 카테고리 메뉴 생성
                const ctgrSet = new Set();
                res.rsltList.forEach(item => {
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
        dtlModal: function(tagNo) {
            if (isNaN(tagNo)) return;

            const url: string = Url.TAG_DTL_AJAX;
            const ajaxData: Record<string, any> = { "tagNo": tagNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 태그 상세 정보
                cF.handlebars.template(res.rsltObj, "tag_admin_dtl", "show");
            });
        },

        /**
         * 속성 추가 모달 호출
         * @param {string|number} tagNo - 속성을 추가할 태그 번호.
         */
        addPropertyModal: function(tagNo) {
            if (isNaN(tagNo)) return;

            cF.handlebars.template({}, "tag_property_reg", "show");
        }
    }
})();
document.addEventListener("DOMContentLoaded", function() {
    Page.init();
});