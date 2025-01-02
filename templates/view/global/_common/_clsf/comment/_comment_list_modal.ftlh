<#--
 * 댓글 목록 Modal
 * @included_in : 댓글 미리보기 기능이 있는 페이지들
 -->

<script>
    const CommentList = (function() {
        return {
            /**
             * 댓글 목록 모달 호출
             * @param {string|number} refPostNo - 참조할 게시물 번호.
             * @param {string} refContentType - 참조할 콘텐츠 타입.
             */
            modal: function(refPostNo, refContentType) {
                event.stopPropagation();
                if (isNaN(refPostNo) || !refContentType) return;

                document.querySelector(`#commentProcForm [name="refPostNo"]`).value = refPostNo;
                if (cF.util.isNotEmpty(refContentType)) {
                    document.querySelector(`#commentProcForm [name="refContentType"]`).value = refContentType;
                }
                const url = "${Url.COMMENT_LIST_AJAX!}";
                const ajaxData = $("#commentProcForm").serializeArray();
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltList = [] } = res;
                    cF.handlebars.template(rsltList, "comment_list", "show");
                });
            },
        }
    })();
</script>

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="comment_list" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "댓글 목록 조회" />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="comment_list_div">
            <#--handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<form name="procForm" id="commentProcForm" method="get">
    <input type="hidden" name="refPostNo" value="" />
    <input type="hidden" name="refContentType" value="${contentType!}" />
    <input type="hidden" name="actvtyCtgrCd" value="${actvtyCtgrCd!}" />
</form>

<#--include:: Handlebars template-->
<#include "/view/global/_common/_clsf/comment/_comment_list_template.hbs">