<#--
 * 활동 로그 목록 > 검색 영역 Header
 * @included_in : log_actvty_list_header
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchEndDt" DateUtils.parser.eDateParseStr(DateUtils.getCurrDate()) />
    </div>
    <div>
        <#--헤더:: 인풋: 작업결과-->
        <select name="rslt" id="rslt" class="form-select form-select-solid fs-7 fw-bolder me-md-2 w-125px" data-hide-search="true">
            <option value="">작업결과</option>
            <option value="true" label="성공" ${fn.selected(searchParam.rslt!, "true")} />
            <option value="false" label="실패" ${fn.selected(searchParam.rslt!, "false")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-solid fs-7 fw-bolder">
            <option value="nickNm">이름</option>
            <option value="userId">아이디</option>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "Page.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "Page.xlsxDownload();" />
    </div>
</form>

