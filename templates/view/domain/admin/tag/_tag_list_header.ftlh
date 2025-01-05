<#--
 * 태그 관리 > 목록 툴바 Header
 * @included_in : tag_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="clCd" ${fn.selected(searchParam.searchType!, "clCd")}>분류 코드</option>
            <option value="clCdNm" ${fn.selected(searchParam.searchType!, "clCdNm")}>분류 코드명</option>
            <option value="dc" ${fn.selected(searchParam.searchType!, "dc")}>분류 코드 설명</option>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.ClCd.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 등록 모달 호출하기-->
        <@component.header_btn_reg_modal "dF.ClCd.regModal();" />
    </div>
</form>
