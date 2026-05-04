<#--
 * 코드 그룹 상세 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#-- 상세 모달 제목: 하드코딩「분류 코드」대신 메시지 번들(메뉴「코드 관리」 등과 용어 통일). -->
<#assign modal_title><@spring.message 'txt.admin.code.group.detail.modal-title' /></#assign>
<@modal.layout id="code_group_detail" size="xl">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div id="code_group_detail_div">
            <#-- Vue(CodeAdminApp)가 상세를 렌더링 -->
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_modify "window.dispatchEvent(new CustomEvent('code-admin:request-open-group-modify-current'));" />
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

