<#--
 * 목록 > 첨부파일 조회 Modal
 -->

<script>
    const AtchFileList = (function() {
        return {
            /**
             * 첨부파일 목록 모달 호출
             * @param {string|number} atchFileNo - 첨부파일 번호.
             */
            modal: function(atchFileNo) {
                event.stopPropagation();
                if (isNaN(atchFileNo)) return;

                const url = "${Url.FILE_INFO_LIST_AJAX!}";
                const ajaxData = { "atchFileNo": atchFileNo };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltList = [] } = res;
                    cF.handlebars.modal(rsltList, "atch_file_list");
                });
            },
        }
    })();
</script>

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="atch_file_list" size="lg">
    <#--begin::Modal Header-->
    <@component.modal_header "첨부파일 목록 조회"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="atch_file_list_div">
            <#--Handlebars template goes in here-->
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

<#--include:: Handlebars template-->
<#include "/view/extension/file/_modal/_atch_file_list_modal_template.hbs">