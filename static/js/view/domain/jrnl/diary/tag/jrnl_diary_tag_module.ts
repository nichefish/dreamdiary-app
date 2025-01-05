/**
 * jrnl_diary_tag_module.ts
 * 
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
const JrnlDiaryTag = (function(): Module {
    return {
        /**
         * initializes module.
         */
        init: function(): void {
            console.log("'JrnlDiaryTag' module initialized.");
        },
        
        /**
         * 목록에 따른 일기 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.JRNL_DIARY_TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "yy": $("#jrnl_aside #yy").val(), "mnth": $("#jrnl_aside #mnth").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "jrnl_diary_tag_list");
            });
        },

        /**
         * 목록에 따른 일기 태그 (전체) 조회 (Ajax)
         */
        listAllAjax: function(): void {
            const url: string = Url.JRNL_DIARY_TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "yy": 9999, "mnth":99 };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 상단에 태그 카테고리 메뉴 생성
                const ctgrSet = new Set();
                res.rsltList.forEach(item => {
                    if (item.ctgr) ctgrSet.add(item.ctgr);
                });
                cF.handlebars.template(ctgrSet, "jrnl_tag_ctgr");
                cF.handlebars.template(res.rsltList, "jrnl_tag_list", "show");
                $("#jrnl_tag_dtl_modal").modal("hide");
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} tagNo - 조회할 태그 번호.
         */
        dtlModal: function(tagNo: string|number, tagNm): void {
            event.stopPropagation();
            if (isNaN(Number(tagNo))) return;

            const url: string = Url.JRNL_DIARY_TAG_DTL_AJAX;
            const ajaxData: Record<string, any> = { "tagNo": tagNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "jrnl_diary_tag_dtl", "show");
            });
        },
    }
})();