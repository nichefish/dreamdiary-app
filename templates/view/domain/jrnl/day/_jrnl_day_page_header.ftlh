<#--
 * 저널 일자(jrnl_day) > 목록 툴바 Header
 * @included_in : jrnl_day_page.ftlh
 -->

<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <div class="col-1 w-auto">
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                onclick="JrnlTag.tagCtgrSyncAjax();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="태그 카테고리 정보를&#10;동기화합니다.">
            <i class="bi bi-arrow-repeat fs-2 pe-0"></i>
        </button>
    </div>
    <div class="col-1 ms-2 me-4 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
    <div>
        <#--헤더:: 버튼: 등록 모달 호출하기-->
        <@component.header_btn_reg_modal "dF.JrnlDay.regModal();" />
    </div>
</form>
