<#--
 * 목록 > 첨부파일 조회 Modal
 -->

<script>
    const FileGroupList = (function() {
        return {
            /**
             * 첨부파일 목록 모달 호출
             * @param {string|number} fileGroupId - 첨부파일 번호.
             */
            modal: function(fileGroupId) {
                event.stopPropagation();
                if (isNaN(fileGroupId)) return;

                const url = "${Url.FILE_INFO_LIST_AJAX!}";
                const ajaxData = { "fileGroupId": fileGroupId };
                cF.ajax.get(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltList = [] } = res;
                    cF.handlebars.modal(rsltList, "file_group_list");
                });
            },
        }
    })();
</script>

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "첨부파일 목록 조회" />
<@modal.layout id="file_group_list" size="lg">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="file_group_list_div">
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
<#include "_file_list_modal_template.hbs">
