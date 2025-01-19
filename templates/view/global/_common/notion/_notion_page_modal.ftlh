<#--
 * 활동 로그 > 상세 Modal
 * @included_in : log_actvty_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="notion_page" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "NOTION 페이지"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="react_notion_modal_root">
                <!-- contents -->
            </div>
            <div id="notion_page_div">
                <!-- contents -->
            </div>
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
