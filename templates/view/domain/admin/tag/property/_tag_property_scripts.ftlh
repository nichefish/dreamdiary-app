<#--
 * 태그 속성 : 스크립트 모듈 분리
 -->

<script>
    const TagProperty = (function() {
        return {
            /**
             * 태그 속성 등록 모달 호출
             * @param {string|number} tagNo - 조회할 태그 번호.
             */
            regModal: function(tagNo) {
                if (isNaN(tagNo)) return;

                const url = "${Url.TAG_DTL_AJAX!}";
                const ajaxData = { "tagNo": tagNo };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    // 태그 상세 정보
                    cF.handlebars.template(res.rsltObj, "tag_admin_dtl", "show");
                });
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>