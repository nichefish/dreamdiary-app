<#--
 * tag profile modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "태그 프로필 설정" />
<@modal.layout id="tag_profile" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="tag_profile_div">
            <#--Handlebars template goes in here-->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-between w-100">
            <button type="button" class="btn btn-sm btn-light-danger d-none" id="tag_profile_del_btn"
                    onclick="dF.JournalDayTag.deleteProfileAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.del'/>">
                <i class="bi bi-trash"></i>삭제
            </button>
            <div class="d-flex justify-content-end ms-auto">
                <@component.modal_btn_save "dF.JournalDayTag.submitProfile();" />
                <@component.modal_btn_close />
            </div>
        </div>
    </div>
</@modal.layout>

<#include "_tag_profile_modal_template.hbs">
