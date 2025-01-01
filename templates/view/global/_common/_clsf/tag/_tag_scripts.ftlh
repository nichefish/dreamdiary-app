<#--
 * 태그 관련파일 묶음
 * @included_in : board_post_list
 -->

<script>
    const Tag = (function() {
        return {
            /**
             * Tag 객체 초기화
             */
            init: function() {
                //
            },

            /**
             * 상세 모달 호출
             * @param {string|number} tagNo - 조회할 태그 번호.
             */
            dtlModal: function(tagNo) {
                event.stopPropagation();
                if (isNaN(tagNo)) return;

                const url = "${Url.TAG_DTL_AJAX!}";
                const ajaxData = { "tagNo": tagNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
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
             * @param {string|number} tagNo - 태그 목록.
             */
            groupTagsByCategory: function(tagList) {
                // 카테고리별로 그룹화
                const groupedTags = tagList.reduce((acc, tag) => {
                    const category = tag.ctgr || '';
                    if (!acc[category]) {
                        acc[category] = [];
                    }
                    acc[category].push(tag);
                    return acc;
                }, {});

                // 그룹을 순서대로 정렬 (Unknown을 마지막으로)
                const sortedGroupedTags = Object.keys(groupedTags)
                    .sort((a, b) => {
                        if (a === 'Unknown') return 1;
                        if (b === 'Unknown') return -1;
                        return a.localeCompare(b);
                    })
                    .reduce((acc, key) => {
                        acc[key] = groupedTags[key];
                        return acc;
                    }, {});

                return sortedGroupedTags;
            },

            /**
             * '전체 카테고리'로 글 목록 필터링
             * @param {HTMLElement} obj - 클릭한 요소를 나타내는 HTML 요소.
             */
            ctgrFilterAll: function(obj) {
                const isActivate = !$(obj).hasClass("active");
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
             * @param {string|number} tagCtgr - 필터링할 태그 카테고리.
             */
            ctgrFilter: function(tagCtgr) {
                if (cF.util.isEmpty(tagCtgr)) return;
                $("span."+tagCtgr).toggle();
                $("i."+tagCtgr).toggle();
                $("div."+tagCtgr).toggleClass("active").toggleClass("btn-outlined").toggleClass("btn-light");
            },

            /**
             * 태그로 글 목록 필터링
             * @param {string|number} tagNo - 필터링할 태그 번호.
             */
            filter: function(tagNo) {
                if (isNaN(tagNo)) return;

                document.querySelector("#listForm #pageNo").value = 1;
                document.querySelector("#listForm #tags").value = tagNo;
                const url = "${listUrl!}?actionTyCd=SEARCH";
                cF.util.blockUISubmit("#listForm", url);
            },

            /**
             * 글 1개짜리 태그 숨김
             * @param {HTMLElement} obj - 토글을 적용할 범위 selector
             */
            hideSingleTag(selectorDiv) {
                $(selectorDiv + " span.ts-1").parent().toggle();
            },

            /**
             * 글 목록 태그 필터링 리셋
             */
            resetFilter: function() {
                Tag.filter("");
            },

            /**
             * 전체 태그 목록 조회 (Ajax)
             */
            listAjax: function() {
                const url = "${Url.TAG_LIST_AJAX!}";
                const ajaxData = { "contentType": $("#contentType").val() };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    // 태그 상세 정보
                    cF.handlebars.template(res.rsltList, "board_tag_list", "show");
                    $("#board_tag_dtl_modal").modal("hide");
                });
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Tag.init();
    });
</script>

<#include "/view/global/_common/_clsf/tag/_tag_dtl_modal.ftlh">
<#include "/view/global/_common/_clsf/tag/_tag_dtl_post_list_template.hbs">
<#include "/view/global/_common/_clsf/tag/_tag_list_modal.ftlh">