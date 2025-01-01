<#--
 * 저널 태그(jrnl_tag) : 스크립트 모듈 분리
 -->

<script>
    const JrnlTag = (function() {
        return {
            /**
             * 태그 카테고리 파일을 DB로부터 새로고침한다. (Ajax)
             */
            tagCtgrSyncAjax: function() {
                const url = "${Url.JRNL_TAG_CTGR_SYNC_AJAX!}";
                cF.util.blockUIAjax(url, 'POST', {}, function(res) {
                    Swal.fire({ text: res.message });
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                });
            }
        }
    })();
</script>