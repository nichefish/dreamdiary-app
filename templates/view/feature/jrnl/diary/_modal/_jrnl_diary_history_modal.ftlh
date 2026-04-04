<#--
 * 저널 일기(jrnl_diary) > 히스토리 Modal
 * @included_in : _jrnl_diary_reg_modal.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("view.jrnl.diary.history.title") />
<@modal.layout id="jrnl_diary_history" size="xl">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="jrnl_diary_history_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#include "_jrnl_diary_history_modal_template.hbs">
