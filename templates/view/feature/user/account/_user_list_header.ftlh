<#--
  -- 사용자 관리 > 계정 및 권한 관리 목록 > 검색 영역 Header
  -- @included_in : user_list
  -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: 권한 코드-->
        <select name="roleKey" id="roleKey" class="form-select form-select-sm form-select-solid">
            <option value=""><@spring.message 'txt.user.list.header.all-roles'/></option>
            <#list AUTH_CD![] as auth>
                <option value="${auth.code!}" id="${auth.code!}">${auth.codeName!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 직급 코드-->
        <select name="rankCd" id="rankCd" class="form-select form-select-sm form-select-solid">
            <option value=""><@spring.message 'txt.user.list.header.rank'/></option>
            <#list RANK_CD![] as rank>
                <option value="${rank.code!}" id="${rank.code!}">${rank.codeName!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 팀 코드-->
        <select name="teamCd" id="teamCd" class="form-select form-select-sm form-select-solid">
            <option value=""><@spring.message 'txt.user.list.header.team'/></option>
            <#list TEAM_CD![] as team>
                <option value="${team.code!}" id="${team.code!}">${team.codeName!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 회사 코드-->
        <select name="cmpyCd" id="cmpyCd" class="form-select form-select-sm form-select-solid">
            <option value=""><@spring.message 'txt.user.list.header.company'/></option>
            <#list CMPY_CD![] as cmpy>
                <option value="${cmpy.code!}" id="${cmpy.code!}">${cmpy.codeName!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="nickname"><@spring.message 'txt.user.list.header.name'/></option>
            <option value="username"><@spring.message 'txt.user.form.username'/></option>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.User.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동하기-->
        <@component.header_btn_reg_form "dF.User.registForm();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "dF.User.xlsxDownload();" />
    </div>
</form>


