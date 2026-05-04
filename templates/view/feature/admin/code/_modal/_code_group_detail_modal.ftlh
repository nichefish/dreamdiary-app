<#--
 * 코드 그룹 상세 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "분류 코드 조회" />
<@modal.layout id="code_group_detail" size="xl">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="code_group_detail_div">
            <#-- Vue(CodeAdminApp)가 상세를 렌더링 -->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_modify "window.dispatchEvent(new CustomEvent("code-admin:request-open-group-modify-current"));" />
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

