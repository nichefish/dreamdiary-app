<#--
  -- 사용자 관리 > 계정 및 권한 관리 목록 > 검색 영역 Header
  -- @included_in : user_list
  -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: 권한 코드-->
        <select name="authCd" id="authCd" class="form-select form-select-sm form-select-solid">
            <option value="">전체 권한</option>
            <#list AUTH_CD![] as auth>
                <option value="${auth.dtlCd!}" id="${auth.dtlCd!}">${auth.dtlCdNm!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 직급 코드-->
        <select name="rankCd" id="rankCd" class="form-select form-select-sm form-select-solid">
            <option value="">직급</option>
            <#list RANK_CD![] as rank>
                <option value="${rank.dtlCd!}" id="${rank.dtlCd!}">${rank.dtlCdNm!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 팀 코드-->
        <select name="teamCd" id="teamCd" class="form-select form-select-sm form-select-solid">
            <option value="">팀</option>
            <#list TEAM_CD![] as team>
                <option value="${team.dtlCd!}" id="${team.dtlCd!}">${team.dtlCdNm!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: 회사 코드-->
        <select name="cmpyCd" id="cmpyCd" class="form-select form-select-sm form-select-solid">
            <option value="">회사</option>
            <#list CMPY_CD![] as cmpy>
                <option value="${cmpy.dtlCd!}" id="${cmpy.dtlCd!}">${cmpy.dtlCdNm!}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="nickNm">이름</option>
            <option value="userId">아이디</option>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "User.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동하기-->
        <@component.header_btn_reg_form "User.regForm();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "User.xlsxDownload();" />
    </div>
</form>


