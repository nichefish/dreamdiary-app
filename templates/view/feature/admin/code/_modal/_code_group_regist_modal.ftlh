<#--
 * 코드 그룹 등록 Modal
 * @included_in : code_admin_page
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "분류 코드 저장" />
<@modal.layout id="code_group_regist" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <form name="codeGroupRegistForm" id="codeGroupRegistForm">
            <div id="code_group_regist_div">
                <#-- Vue(CodeAdminApp)가 폼을 렌더링 -->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "window.dispatchEvent(new CustomEvent('code-admin:request-submit-group-regist'));" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

