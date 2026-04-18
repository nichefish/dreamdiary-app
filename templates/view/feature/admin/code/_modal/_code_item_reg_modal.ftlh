<#--
 -- 코드 아이템 등록/수정 Modal
 -- @included_in : code_group_dtl
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "상세 코드 저장" />
<@modal.layout id="code_item_reg" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <form name="codeItemRegForm" id="codeItemRegForm">
            <div id="code_item_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "dF.CodeItem.submit();" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#include "_code_item_reg_modal_template.hbs">
