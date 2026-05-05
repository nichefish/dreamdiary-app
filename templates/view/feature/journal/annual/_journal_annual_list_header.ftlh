<#--
 * 저널 결산(journal_annual) > 목록 툴바 Header
 * @included_in : journal_annual_page.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div>
        <#--헤더:: 인풋: searchType-->
        <select name="yy" id="yy" class="form-select" aria-label="Select example">
            <option value=""><@spring.message 'txt.journal.annual.all-years'/></option>
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
        <#-- 변경(A-5-β-1) NOTE: `dF.JournalAnnual.search()` 본체는 현재 미정의(가시 dead). -->
        <#-- 클릭 시 `Uncaught TypeError` 발생. 본 phase 에서는 `listAjax` 별칭화 등 정상 가장 우회를 추가하지 않고 -->
        <#-- 미구현 상태를 가시화한 채로 보존한다(룰: '비정상 상태를 임시 우회로 감추지 마라'). -->
        <@component.header_btn_search "dF.JournalAnnual.search();" />
    </div>
    <div>
        <#--헤더:: 버튼: 결산 동기화-->
        <button type="button" class="btn btn-sm btn-primary"
                onclick="dF.JournalAnnual.makeTotalAnnualAjax();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="전체 년도에 대한 결산 정보를 갱신합니다.">
            <i class="bi bi-arrow-repeat"></i>전체 결산 갱신
        </button>
    </div>
</form>
