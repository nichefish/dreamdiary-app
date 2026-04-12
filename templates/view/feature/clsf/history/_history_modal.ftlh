<#--
 * common history modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign history_modal_id = "clsf_history" />
<#assign history_modal_title = history_modal_title!springMacroRequestContext.getMessage("txt.history") />

<@modal.layout id=history_modal_id size="xl">
    <@component.modal_header history_modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="${history_modal_id}_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#include "_history_modal_template.hbs">
<script type="text/javascript" src="/js/view/feature/clsf/history/history_modal_module.js?${releaseDate!}"></script>
