<#--
 * Board > list header
 * @included_in : board_list
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <div>
        <@component.header_btn_reg_modal "dF.Board.regModal();" />
    </div>
</form>
