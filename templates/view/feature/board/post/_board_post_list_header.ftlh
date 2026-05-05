<#--
 * 게시판(board_post) > 목록 툴바 Header
 * @included_in : board_post_list
-->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="board" id="board" value="${board!}"/>      <!-- 게시판 검색에서 항상 필요하다. -->
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
            <@component.header_input_categoryCode searchParam POST_CTGR_CD![] />
        </div>
    </#if>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="<@spring.message 'txt.title'/>" ${fn.selected(searchParam.searchType!, "title")} />
            <option value="nickname" label="?묒꽦?? ${fn.selected(searchParam.searchType!, "nickname")} />
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "window.dispatchEvent(new CustomEvent('board-post:list-search'));" />
    </div>
    <div>
        <#--헤더:: 버튼: 내 글 보기-->
        <#if MY_PAPR!false>
            <@component.header_btn_mypapr "window.dispatchEvent(new CustomEvent('board-post:list-all'));" MY_PAPR!false />
        <#else>
            <@component.header_btn_mypapr "window.dispatchEvent(new CustomEvent('board-post:list-mypapr'));" MY_PAPR!false />
        </#if>
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동-->
        <@component.header_btn_reg_form "window.dispatchEvent(new CustomEvent('board-post:open-reg-form'));" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드-->
        <@component.header_btn_xlsx_download "window.dispatchEvent(new CustomEvent('board-post:list-xlsx-download'));" />
    </div>
</form>
