<#--
 * 게시판(board_post) > 목록 툴바 Header
 * @included_in : board_post_list
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="boardDef" id="boardDef" value="${boardDef!}"/>      <!-- 게시판 검색에선 꼭 있어야 한다. -->
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">
    <input type="hidden" name="tags" id="tags">

    <div>
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <#--헤더:: 인풋: datepicker-->
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <#if POST_CTGR_CD??>
        <div>
            <#--헤더:: 인풋: 글분류 코드-->
            <@component.header_input_ctgrCd searchParam POST_CTGR_CD![] />
        </div>
    </#if>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="제목" ${fn.selected(searchParam.searchType!, "제목")} />
            <option value="nickNm" label="작성자" ${fn.selected(searchParam.searchType!, "작성자")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.BoardPost.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 내 글 보기-->
        <@component.header_btn_mypapr "dF.BoardPost.myPaprList();" MY_PAPR!false />
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동하기-->
        <@component.header_btn_reg_form "dF.BoardPost.regForm();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "dF.BoardPost.xlsxDownload();" />
    </div>
</form>
