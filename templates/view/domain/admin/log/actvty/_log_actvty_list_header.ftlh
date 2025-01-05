<#--
 * 활동 로그(log_actvty) > 목록 검색 영역 Header
 * @included_in : log_actvty_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
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
        <select name="rslt" id="rslt" class="form-select form-select-sm form-select-solid" data-hide-search="true">
            <option value="">작업결과</option>
            <option value="true" label="성공" ${fn.selected(searchParam.rslt!, "true")} />
            <option value="false" label="실패" ${fn.selected(searchParam.rslt!, "false")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="nickNm" label="이름" ${fn.selected(searchParam.searchType!, "nickNm")} />
            <option value="userId" label="아이디" ${fn.selected(searchParam.searchType!, "userId")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.LogActvty.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "dF.LogActvty.xlsxDownload();" />
    </div>
</form>

