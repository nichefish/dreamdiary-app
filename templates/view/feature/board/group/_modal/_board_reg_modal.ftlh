<#--
 * Board > register modal (Vue: BoardRegistForm)
 * @included_in : board_admin_page
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title><@spring.message 'txt.board.group.modal.save' /></#assign>
<@modal.layout id="board_reg" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <form name="boardRegForm" id="boardRegForm">
            <div id="board_reg_div">
                <#-- Vue(BoardAdminApp)가 폼을 렌더링 -->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "window.dispatchEvent(new CustomEvent('board-admin:request-submit-board-regist'));" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>
