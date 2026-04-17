<#--
 * Board > register modal
 * @included_in : board_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "게시판 관리" />
<@modal.layout id="board_reg" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <form name="boardRegForm" id="boardRegForm">
            <div id="board_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_save "dF.Board.submit();" />
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#include "_board_reg_modal_template.hbs">
