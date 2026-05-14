<#--
 * 저널 일자(journal_day) : 태그 헤더
 -->

<#--begin::Tags-->
<div id="journal_tag_header" class="mb-6 ms-4 w-100">
    <div id="journal_day_tag_header" class="row align-items-center mb-4 ms-4 min-h-42px">
        <div class="col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
            <b><@spring.message 'txt.day.tag'/> : </b>
        </div>
        <div class="col flex-grow-1" id="journal_day_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.TagService.hideSingleTag('#journal_day_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.JournalTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
    <div class="separator"></div>
    <div id="journal_diary_tag_header" class="row align-items-center mb-4 ms-4 min-h-42px">
        <div class="col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
            <b><@spring.message 'txt.diary.tag'/> : </b>
        </div>
        <div class="col flex-grow-1" id="journal_diary_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.TagService.hideSingleTag('#journal_diary_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.JournalTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
    <div class="separator"></div>
    <div id="journal_dream_tag_header" class="row align-items-center mb-6 ms-4 min-h-42px">
        <div class="col-auto d-none d-md-flex ms-4 me-6 text-center fs-6">
            <b><@spring.message 'txt.dream.tag'/> : </b>
        </div>
        <div class="col flex-grow-1" id="journal_dream_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.TagService.hideSingleTag('#journal_dream_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="dF.JournalEntryTag.get('JOURNAL_DREAM').listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
</div>
<div id="journal_day_tag_panel_app" class="d-none"></div>
