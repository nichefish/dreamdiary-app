<#--
 * 로그 > 상세 Modal
 * @included_in : log_list.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "로그 조회" />
<@modal.layout id="log_dtl" size="xl">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="log_dtl_div">
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#include "_log_dtl_modal_template.hbs">
