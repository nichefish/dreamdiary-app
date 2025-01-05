<#--
 * 휴가일자(vcatn_schdul) > 휴가사용일자 화면
 * @included_in : vcatn_schdul_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get" >
    <div>
        <#--헤더:: 인풋: 년도 선택-->
        <select name="statsYy" id="statsYy" class="form-select form-select-solid w-90px mx-1"
                onchange="dF.VcatnSchdul.chgYy(this);"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="조회 년도를 변경합니다.">
            <#list yyList![] as yy>
                <option value="${yy.statsYy!}"<#if yy.statsYy == vcatnYy.statsYy>selected="selected"</#if>>${yy.statsYy!}</option>
            </#list>
        </select>
    </div>
    <div>
        년도 휴가사용일자
    </div>
    <div>
        <span class="text-gray-600 align-center">■ 연차 사용 기간 : ${(vcatnYy.bgnDt)!} ~ ${(vcatnYy.endDt)!}</span>
    </div>
    <div>
        <#--헤더:: 버튼: 등록 화면 이동하기-->
        <@component.header_btn_reg_form "dF.VcatnSchdul.regModal();" />
    </div>
    <div>
        <#--헤더:: 버튼: 엑셀 다운로드하기-->
        <@component.header_btn_xlsx_download "dF.VcatnSchdul.xlsxDownload();" />
    </div>
</form>

