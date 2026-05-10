<#--
 * 저널 스레드(journal_thread) > 목록 툴바 Header
 * @included_in : journal_thread_list.ftlh
 -->

<form name="listForm" id="listForm" class="d-none d-md-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}" method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!}">
    <input type="hidden" name="tags" id="tags">

    <div>
        <@component.header_datepicker searchParam "searchStartDt" "" />
        <@component.header_datepicker searchParam "searchEndDt" "today" />
    </div>
    <div>
        <@component.header_input_categoryCode searchParam JOURNAL_THREAD_CTGR_CD![] />
    </div>
    <div>
        <select name="searchType" id="searchType" class="form-select form-select-sm form-select-solid">
            <option value="title" label="<@spring.message 'txt.title'/>" ${fn.selected(searchParam.searchType!, "title")} />
            <option value="nickname" label="작성자" ${fn.selected(searchParam.searchType!, "nickname")} />
        </select>
    </div>
    <div>
        <@component.header_input_searchKeyword searchParam />
    </div>
    <div>
        <#-- 변경: 저널 스레드 목록 Vue 브리지 — `JournalThreadListApp` 가 CustomEvent 로 액션 서비스에 연결한다(공지 목록 패턴). -->
        <@component.header_btn_search "window.dispatchEvent(new CustomEvent('journal-thread:list-search'));" />
        <@component.header_btn_mypapr "window.dispatchEvent(new CustomEvent('journal-thread:list-mypapr'));" MY_PAPR!false />
    </div>
    <div>
        <@component.header_btn_reg_form "window.dispatchEvent(new CustomEvent('journal-thread:open-regist-form'));" />
    </div>
    <div>
        <@component.header_btn_xlsx_download "window.dispatchEvent(new CustomEvent('journal-thread:list-xlsx-download'));" />
    </div>
</form>
