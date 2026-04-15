<#--
 * 코드 그룹 목록 툴바 Header
 * @included_in : code_group_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="clCd" ${fn.selected(searchParam.searchType!, "clCd")}>분류 코드</option>
            <option value="clCdNm" ${fn.selected(searchParam.searchType!, "clCdNm")}>분류 코드명</option>
            <option value="dc" ${fn.selected(searchParam.searchType!, "dc")}>분류 코드 설명</option>
        </select>
    </div>
    <div>
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <@component.header_btn_search "dF.CodeGroup.search();" />
    </div>
    <div>
        <@component.header_btn_reg_modal "dF.CodeGroup.regModal();" />
    </div>
</form>
