<#--
 * 로그 > 목록 검색 영역 Header
 * @included_in : log_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <@component.header_datepicker searchParam "searchEndDt" DateUtils.parser.eDateParseStr(DateUtils.getCurrDate()) />
    </div>
    <div>
        <select name="rslt" id="rslt" class="form-select form-select-sm form-select-solid" data-hide-search="true">
            <option value=""><@spring.message 'txt.admin.log.list.action-result'/></option>
            <option value="true" label="<@spring.message 'txt.admin.log.list.result.success'/>" ${fn.selected(searchParam.rslt!, "true")} />
            <option value="false" label="<@spring.message 'txt.admin.log.list.result.failure'/>" ${fn.selected(searchParam.rslt!, "false")} />
        </select>
    </div>
    <div>
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="nickname" label="<@spring.message 'txt.admin.log.list.search-type.nickname'/>" ${fn.selected(searchParam.searchType!, "nickname")} />
            <option value="username" label="<@spring.message 'txt.admin.log.list.search-type.username'/>" ${fn.selected(searchParam.searchType!, "username")} />
        </select>
    </div>
    <div>
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <@component.header_btn_search "window.LogAdminBridge && window.LogAdminBridge.searchLogs();" />
    </div>
    <div>
        <@component.header_btn_xlsx_download "window.LogAdminBridge && window.LogAdminBridge.downloadLogsAsExcel();" />
    </div>
</form>
