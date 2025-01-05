<#--
 * 저널 주제(jrnl_sbjct) > 목록 툴바 Header
 * @included_in : notice_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!}">
    <input type="hidden" name="tags" id="tags">

    <div>
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <div>
        <#--헤더:: 인풋: 글분류 코드-->
        <@component.header_input_ctgrCd searchParam NOTICE_CTGR_CD![] />
    </div>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="제목" ${fn.selected(searchParam.searchType!, "title")} />
            <option value="nickNm" label="작성자" ${fn.selected(searchParam.searchType!, "nickNm")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.JrnlSbjct.search();" />
        <#--헤더:: 버튼: 내 글 보기-->
        <@component.header_btn_mypapr "dF.JrnlSbjct.myPaprList();" MY_PAPR!false />
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동하기-->
        <@component.header_btn_reg_form "dF.JrnlSbjct.regForm();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "dF.JrnlSbjct.xlsxDownload();" />
    </div>
</form>
