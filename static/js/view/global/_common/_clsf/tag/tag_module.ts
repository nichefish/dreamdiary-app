/**
 * tag_module.ts
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Tag = (function(): dfModule {
    return {
        initialized: false,

        /**
         * Tag 모듈 초기화
         */
        init: function(): void {
            if (dF.Tag.initialized) return;

            dF.Tag.initialized = true;
            console.log("'dF.Tag' module initialized.");
        },

        /**
         * 상세 모달 호출
         * @param {string|number} tagNo - 조회할 태그 번호.
         */
        dtlModal: function(tagNo: string|number): void {
            event.stopPropagation();
            if (isNaN(Number(tagNo))) return;

            const url: string = Url.TAG_DTL_AJAX;
            const ajaxData: Record<string, any> = { "tagNo": tagNo };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 태그 상세 정보
                cF.handlebars.template(res.rsltObj, "tag_dtl", "show");
                // 태그 글 목록
                cF.handlebars.template(res.rsltList, "tag_dtl_post_list");
                $("#board_tag_list_modal").modal("hide");
            });
        },

        /**
         * 태그를 태그 카테고리별로 그룹화
         * @param {object[]} tagList - 태그 목록.
         */
        groupTagsByCategory: function(tagList) {
            // 카테고리별로 그룹화
            const groupedTags = tagList.reduce((acc, tag) => {
                const category: string = tag.ctgr || '';
                if (!acc[category]) {
                    acc[category] = [];
                }
                acc[category].push(tag);
                return acc;
            }, {});

            // 그룹을 순서대로 정렬 (Unknown을 마지막으로)
            return Object.keys(groupedTags)
                .sort((a, b) => {
                    if (a === 'Unknown') return 1;
                    if (b === 'Unknown') return -1;
                    return a.localeCompare(b);
                })
                .reduce((acc, key) => {
                    acc[key] = groupedTags[key];
                    return acc;
                }, {});
        },

        /**
         * '전체 카테고리'로 글 목록 필터링
         * @param {HTMLElement} obj - 클릭한 요소를 나타내는 HTML 요소.
         */
        ctgrFilterAll: function(obj): void {
            const isActivate: boolean = !$(obj).hasClass("active");
            const $spanCtgr = $("span.ctgr");
            const $iconCtgr = $("i.ctgr");
            const $divCtgr = $("div.ctgr");
            if (isActivate) {
                $spanCtgr.show();
                $iconCtgr.show();
                $divCtgr.addClass("active").addClass("btn-outlined").removeClass("btn-light");
            } else {
                $spanCtgr.hide();
                $iconCtgr.hide();
                $divCtgr.removeClass("active").removeClass("btn-outlined").addClass("btn-light");
            }
        },

        /**
         * 태그 카테고리로 글 목록 필터링
         * @param {string} tagCtgr - 필터링할 태그 카테고리.
         */
        ctgrFilter: function(tagCtgr: string): void {
            if (cF.util.isEmpty(tagCtgr)) return;

            $("span."+tagCtgr).toggle();
            $("i."+tagCtgr).toggle();
            $("div."+tagCtgr).toggleClass("active").toggleClass("btn-outlined").toggleClass("btn-light");
        },

        /**
         * 태그로 글 목록 필터링
         * @param {string|number} tagNo - 필터링할 태그 번호.
         */
        filter: function(tagNo: string|number): void {
            if (isNaN(Number(tagNo))) return;

            const pageNoElement: HTMLInputElement = document.querySelector("#listForm #pageNo") as HTMLInputElement;
            const tagsElement: HTMLInputElement = document.querySelector("#listForm #tags") as HTMLInputElement;
            if (!pageNoElement || !tagsElement) return;

            pageNoElement.value = "1";
            tagsElement.value = tagNo.toString();

            const listUrl: string = $("#listForm").data("url");
            const url: string = `${listUrl!}?actionTyCd=SEARCH`;
            cF.util.blockUISubmit("#listForm", url);
        },

        /**
         * 글 1개짜리 태그 숨김
         * @param {HTMLElement} selectorDiv - 토글을 적용할 범위 selector
         */
        hideSingleTag(selectorDiv: HTMLElement): void {
            $(selectorDiv + " span.ts-1").parent().toggle();
        },

        /**
         * 글 목록 태그 필터링 리셋
         */
        resetFilter: function(): void {
            dF.Tag.filter("");
        },

        /**
         * 전체 태그 목록 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.TAG_LIST_AJAX;
            const ajaxData: Record<string, any> = { "contentType": $("#contentType").val() };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 태그 상세 정보
                cF.handlebars.template(res.rsltList, "board_tag_list", "show");
                $("#board_tag_dtl_modal").modal("hide");
            });
        },
    }
})();
document.addEventListener("DOMContentLoaded", function(): void {
    dF.Tag.init();
});