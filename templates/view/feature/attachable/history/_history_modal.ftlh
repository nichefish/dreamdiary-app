<#--
 * common history modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign history_modal_id = "attachable_history" />
<#assign history_modal_title = history_modal_title!springMacroRequestContext.getMessage("txt.history") />

<@modal.layout id=history_modal_id size="xl">
    <@component.modal_header history_modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="history_modal_app"></div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--begin::JSON injection-->
<script type="application/json" id="history_modal_labels">{"triggeredAt":"${springMacroRequestContext.getMessage('txt.history.triggered-at')?js_string}","typeRestore":"${springMacroRequestContext.getMessage('txt.history.restore')?js_string}","typeChange":"${springMacroRequestContext.getMessage('txt.history.change')?js_string}","regDt":"${springMacroRequestContext.getMessage('txt.reg-dt')?js_string}","createdBy":"${springMacroRequestContext.getMessage('txt.createdBy')?js_string}","fromNo":"${springMacroRequestContext.getMessage('txt.history.from-no')?js_string}","restore":"${springMacroRequestContext.getMessage('txt.comm.restore')?js_string}","del":"${springMacroRequestContext.getMessage('txt.comm.del')?js_string}","clear":"${springMacroRequestContext.getMessage('txt.history.clear')?js_string}","summary":"${springMacroRequestContext.getMessage('txt.history.summary')?js_string}","detail":"${springMacroRequestContext.getMessage('txt.history.detail')?js_string}","empty":"${springMacroRequestContext.getMessage('txt.history.empty')?js_string}","tooltipRestore":"${springMacroRequestContext.getMessage('bs.tooltip.history.restore')?js_string}","tooltipDel":"${springMacroRequestContext.getMessage('bs.tooltip.history.delete')?js_string}","tooltipClear":"${springMacroRequestContext.getMessage('bs.tooltip.history.clear')?js_string}"}</script>

<script type="module" src="/vue/feature/attachable/history/HistoryModal.js?${releaseDate!}"></script>