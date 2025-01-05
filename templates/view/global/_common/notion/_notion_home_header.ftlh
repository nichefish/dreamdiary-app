<#--
 * 활동 로그 목록 > 검색 영역 Header
 * @included_in : log_sys_list_header
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <div class="position-relative w-md-200px me-md-2">
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div class="d-flex align-items-center me-md-2">
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "Page.search();" />
    </div>
</form>

