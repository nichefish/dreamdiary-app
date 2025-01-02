<#--
 * 저널 일기(jrnl_diary) : 태그 스크립트 모듈 분리
 -->

<script>
    const JrnlDiaryTag = (function() {
        return {
            /**
             * 목록에 따른 일기 태그 조회 (Ajax)
             */
            listAjax: function() {
                const url = "${Url.JRNL_DIARY_TAG_LIST_AJAX!}";
                const ajaxData = { "yy": $("#jrnl_aside #yy").val(), "mnth": $("#jrnl_aside #mnth").val() };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltList, "jrnl_diary_tag_list");
                });
            },

            /**
             * 목록에 따른 일기 태그 (전체) 조회 (Ajax)
             */
            listAllAjax: function() {
                const url = "${Url.JRNL_DIARY_TAG_LIST_AJAX!}";
                const ajaxData = { "yy": 9999, "mnth":99 };
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
                    cF.handlebars.template(ctgrSet, "jrnl_tag_ctgr");
                    cF.handlebars.template(res.rsltList, "jrnl_tag_list", "show");
                    $("#jrnl_tag_dtl_modal").modal("hide");
                });
            },

            /**
             * 상세 모달 호출
             * @param {string|number} tagNo - 조회할 태그 번호.
             */
            dtlModal: function(tagNo, tagNm) {
                event.stopPropagation();
                if (isNaN(tagNo)) return;

                const url = "${Url.JRNL_DIARY_TAG_DTL_AJAX!}";
                const ajaxData = { "tagNo": tagNo };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltList, "jrnl_diary_tag_dtl", "show");
                });
            },
        }
    })();
</script>