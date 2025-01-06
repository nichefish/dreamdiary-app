<#--
 * 저널 결산(jrnl_sumry) > 목록 툴바 Header
 * @included_in : jrnl_sumry_page.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="yy" id="yy" class="form-select" aria-label="Select example">
            <option value="">모든 년도</option>
            <#assign currYy = DateUtils.getCurrYy()?number />
            <#assign startYy = 2011 />
            <#list currYy..startYy as year>
                <option value="${year}">${year}</option>
            </#list>
        </select>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--헤더:: 버튼: 목록 검색-->
        <@component.header_btn_search "dF.JrnlSumry.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 결산 동기화-->
        <button type="button" class="btn btn-sm btn-primary"
                onclick="dF.JrnlSumry.makeTotalSumryAjax();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="전체 년도에 대한 결산 정보를 갱신합니다.">
            <i class="bi bi-arrow-repeat"></i>전체 결산 갱신
        </button>
    </div>
</form>
