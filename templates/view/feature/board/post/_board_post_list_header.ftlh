<#--
 * 寃뚯떆??board_post) > 紐⑸줉 ?대컮 Header
 * @included_in : board_post_list
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="board" id="board" value="${board!}"/>      <!-- 寃뚯떆??寃?됱뿉??瑗??덉뼱???쒕떎. -->
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">
    <input type="hidden" name="tags" id="tags">

    <div>
        <#--?ㅻ뜑:: ?명뭼: datepicker-->
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <#--?ㅻ뜑:: ?명뭼: datepicker-->
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <#if POST_CTGR_CD??>
        <div>
            <#--?ㅻ뜑:: ?명뭼: 湲遺꾨쪟 肄붾뱶-->
            <@component.header_input_categoryCode searchParam POST_CTGR_CD![] />
        </div>
    </#if>
    <div>
        <#--?ㅻ뜑:: ?명뭼: searchType-->
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="<@spring.message 'txt.title'/>" ${fn.selected(searchParam.searchType!, "title")} />
            <option value="nickname" label="?묒꽦?? ${fn.selected(searchParam.searchType!, "nickname")} />
        </select>
    </div>
    <div>
        <#--?ㅻ뜑:: ?명뭼: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#--?ㅻ뜑:: 踰꾪듉: 紐⑸줉 寃??->
        <@component.header_btn_search "dF.BoardPost.search();" />
    </div>
    <div>
        <#--?ㅻ뜑:: 踰꾪듉: ??湲 蹂닿린-->
        <@component.header_btn_mypapr "dF.BoardPost.myPaprList();" MY_PAPR!false />
    </div>
    <div>
        <#--?ㅻ뜑:: 踰꾪듉: ?깅줉 ?붾㈃ ?대룞?섍린-->
        <@component.header_btn_reg_form "dF.BoardPost.regForm();" />
    </div>
    <div>
        <#--?ㅻ뜑:: 踰꾪듉: ?묒? ?ㅼ슫濡쒕뱶?섍린-->
        <@component.header_btn_xlsx_download "dF.BoardPost.xlsxDownload();" />
    </div>
</form>
