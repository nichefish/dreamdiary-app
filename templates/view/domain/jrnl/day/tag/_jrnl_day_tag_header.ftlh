<#--
 * 저널 일자(jrnl_day) : 태그 헤더
 -->

<#--begin::Tags-->
<div class="mb-6 ms-4">
    <div class="d-flex align-items-center mb-4 ms-4">
        <div class="col-1 ms-4 me-6 text-center fs-6">
            <b>일자 태그 : </b>
        </div>
        <div class="col" id="jrnl_day_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-1 ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 ms-4 me-20 text-center fs-6 w-auto gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="Tag.hideSingleTag('#jrnl_day_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="JrnlTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
    <div class="separator mx-20 mb-4"></div>
    <div class="d-flex align-items-center mb-4 ms-4">
        <div class="col-1 ms-4 me-6 text-center fs-6">
            <b>일기 태그 : </b>
        </div>
        <div class="col" id="jrnl_diary_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-1 ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 ms-4 me-20 text-center fs-6 w-auto gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="Tag.hideSingleTag('#jrnl_diary_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="JrnlTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
    <div class="separator mx-20 mb-4"></div>
    <div class="d-flex align-items-center mb-6 ms-4">
        <div class="col-1 ms-4 me-6 text-center fs-6">
            <b>꿈 태그 : </b>
        </div>
        <div class="col" id="jrnl_dream_tag_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div class="col-1 ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
        <div class="col-1 ms-4 me-20 text-center fs-6 w-auto gap-3">
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="Tag.hideSingleTag('#jrnl_dream_tag_list_div');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="1개짜리 태그를 숨기고 보입니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                    onclick="JrnlDreamTag.listAllAjax();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="헤당 년도 전체 태그 모달 팝업을 호출합니다.">
                <i class="bi bi-tag pe-0"></i>
            </button>
        </div>
    </div>
</div>