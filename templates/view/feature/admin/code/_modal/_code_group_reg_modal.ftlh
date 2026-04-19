<#--
 * 코드 그룹 등록 Modal
 * @included_in : code_group_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "분류 코드 저장" />
<@modal.layout id="code_group_reg" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <form name="codeGroupRegForm" id="codeGroupRegForm">
            <div id="code_group_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "dF.CodeGroup.submit();" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#include "_code_group_reg_modal_template.hbs">
