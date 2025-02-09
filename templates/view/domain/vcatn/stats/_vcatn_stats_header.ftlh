<#--
 * 휴가관리 > 년도별 휴가관리 화면 Header
 * @included_in : vcatn_stats.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get" >
    <div>
        <#--헤더:: 인풋: 년도 선택-->
        <select id="statsYy_hidden" class="form-select form-select-sm form-select-solid"
                onchange="Page.chgYy(this);"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="조회 년도를 변경합니다.">
            <#list yyList![] as yy>
                <option value="${yy.statsYy!}" <#if yy.statsYy == vcatnYy.statsYy>selected="selected"</#if>>${yy.statsYy!}</option>
            </#list>
        </select>
    </div>
    <div>
        년도 휴가관리
    </div>
    <div>
        <span class="text-gray-600 align-center">
            ■ 연차 사용 기간 : ${(vcatnYy.bgnDt)!} ~ ${(vcatnYy.endDt)!}
        </span>
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "Page.xlsxDownload();" />
    </div>
</form>