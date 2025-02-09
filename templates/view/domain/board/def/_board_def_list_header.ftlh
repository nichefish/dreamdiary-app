<#--
 * 게시판 정의(board_def) > 목록 툴바 Header
 * @included_in : board_def_list
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <div>
        <#--헤더:: 버튼: 등록 모달 호출하기-->
        <@component.header_btn_reg_modal "dF.BoardDef.regModal();" />
    </div>
</form>
