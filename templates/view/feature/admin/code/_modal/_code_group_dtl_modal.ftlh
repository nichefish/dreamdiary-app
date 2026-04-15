<#--
 * 코드 그룹 상세 Modal
 * @included_in : code_group_list.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "분류 코드 조회" />
<@modal.layout id="code_group_dtl" size="xl">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="code_group_dtl_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_modify "dF.CodeGroup.mdfModal(dF.CodeGroup.key);" />
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#include "_code_group_dtl_modal_template.hbs">
