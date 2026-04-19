<#--
 * 저널 주제(journal_sbjct) > 목록 툴바 Header
 * @included_in : journal_sbjct_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!}">
    <input type="hidden" name="tags" id="tags">

    <div>
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <div>
        <@component.header_input_categoryCode searchParam JOURNAL_SBJCT_CTGR_CD![] />
    </div>
    <div>
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="<@spring.message 'txt.title'/>" ${fn.selected(searchParam.searchType!, "title")} />
            <option value="nickname" label="작성자" ${fn.selected(searchParam.searchType!, "nickname")} />
        </select>
    </div>
    <div>
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <@component.header_btn_search "dF.JournalSbjct.search();" />
        <@component.header_btn_mypapr "dF.JournalSbjct.myPaprList();" MY_PAPR!false />
    </div>
    <div>
        <@component.header_btn_reg_form "dF.JournalSbjct.regForm();" />
    </div>
    <div>
        <@component.header_btn_xlsx_download "dF.JournalSbjct.xlsxDownload();" />
    </div>
</form>
