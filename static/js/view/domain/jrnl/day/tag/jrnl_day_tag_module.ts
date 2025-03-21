/**
 * jrnl_day_tag_module.ts
 * 저널 일자 태그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDayTag = (function(): dfModule {
    return {
        initialized: false,
        ctgrMap: new Map(),

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDayTag.initialized) return;

            dF.JrnlDayTag.getCtgrMap();

            dF.JrnlDayTag.initialized = true;
            console.log("'dF.JrnlDayTag' module initialized.");
        },

        /**
         * 태그 카테고리 정보 조회
         */
        getCtgrMap: function(): void {
            const url: string = Url.JRNL_DAY_TAG_CTGR_MAP_AJAX;
            cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JrnlDayTag.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 목록에 따른 일자 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.JRNL_DAY_TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "yy": $("#jrnl_aside #yy").val(), "mnth": $("#jrnl_aside #mnth").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "jrnl_day_tag_list");
            });
        },

        /**
         * 목록에 따른 일자 태그 (전체) 조회 (Ajax)
         */
        listAllAjax: function(): void {
            const url: string = Url.JRNL_DAY_TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "yy": 9999, "mnth":99 };
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
                cF.handlebars.template(ctgrSet, "jrnl_tag_ctgr");
                cF.handlebars.modal(res.rsltList, "jrnl_tag_list");
                $("#jrnl_tag_dtl_modal").modal("hide");
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} tagNo - 조회할 태그 번호.
         * @param tagNm 태그 이름
         */
        dtlModal: function(tagNo: string|number, tagNm: string): void {
            event.stopPropagation();
            if (isNaN(Number(tagNo))) return;

            ModalHistory.reset();

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            const url: string = Url.JRNL_DAY_TAG_DTL_AJAX;
            const ajaxData: Record<string, any> = { "tagNo": tagNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.modal(res.rsltList, "jrnl_day_tag_dtl");
                document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_nm").innerHTML = tagNm;
                document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_cnt").innerHTML = (res.rsltList?.length ?? 0).toString();

                /* modal history push */
                ModalHistory.push(self, func, args);
            });
        },

        /**
         * 모달 닫기 시 수행할 로직
         */
        closeModal: function(): void {
            /* modal history pop */
            ModalHistory.prev();
        },

        expand: function(obj: HTMLElement): void {
            $(obj).prev(".cn").toggleClass("expanded");
        }
    }
})();