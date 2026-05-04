<#--
 * 코드 그룹 목록 툴바 Header
 * @included_in : code_group_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="groupCode" ${fn.selected(searchParam.searchType!, "groupCode")}><@spring.message 'txt.admin.code.group.form.group-code'/></option>
            <option value="groupName" ${fn.selected(searchParam.searchType!, "groupName")}><@spring.message 'txt.admin.code.group.form.group-name'/></option>
            <option value="description" ${fn.selected(searchParam.searchType!, "description")}><@spring.message 'txt.admin.code.group.form.group-description'/></option>
        </select>
    </div>
    <div>
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <@component.header_btn_search "window.dispatchEvent(new CustomEvent("code-admin:request-search"));" />
    </div>
    <div>
        <@component.header_btn_reg_modal "window.dispatchEvent(new CustomEvent("code-admin:request-open-group-regist"));" />
    </div>
</form>
