<#--
 * 일정 > 전체 일정 화면 (달력) 헤더
 * @included_in : schdul_cal
 -->

<style>
    @media (max-width: 768px) {
        .card-header {
            padding-left: 1rem !important;
            padding-right: 1rem !important;
        }
    }
</style>
<#--begin::Card header-->
<div class="card-toolbar d-flex align-items-center flex-nowrap gap-2">
    <div>
        <label class="col-form-label text-center fw-bold text-gray-700 me-2">조회일시: </label>
    </div>
    <div>
        <span class="menu-icon me-md-2 vertical-middle">
            <label for="calDt"><i class="bi bi-calendar3 fs-2 cursor-pointer"></i></label>
        </span>
        <input type="text" name="calDt" id="calDt" class="form-control form-control-solid w-sm-150px w-90px fs-7 me-md-2 fw-bolder d-inline"
               placeholder="Pick date" readonly/>
    </div>

    <span class="h-25px border-gray-500 border-start mx-4"></span>

    <div>
        <label class="col-form-label text-center fw-bold text-gray-700">키워드: </label>
    </div>
    <div>
        <#--헤더:: 인풋: searchKeyword-->
        <@component.header_input_searchKeyword searchParam />
    </div>
    <#--헤더:: 버튼: 목록 검색-->
    <@component.header_btn_search "Page.search();" />
    <div class="cursor-pointer ms-1 me-5" data-bs-toggle="collapse" href="#schdul_advanced_search_div">
        <a class="btn btn-sm btn-light-primary h-100 d-flex align-items-center text-gray-700 fs-7 fw-bold">
        <span class="svg-icon svg-icon-muted svg-icon-2 svg-icon-gray-700"
              data-bs-toggle="tooltip" data-bs-placement="top" title="고급 필터 영역을 열고 닫습니다.">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19.0759 3H4.72777C3.95892 3 3.47768 3.83148 3.86067 4.49814L8.56967 12.6949C9.17923 13.7559 9.5 14.9582 9.5 16.1819V19.5072C9.5 20.2189 10.2223 20.7028 10.8805 20.432L13.8805 19.1977C14.2553 19.0435 14.5 18.6783 14.5 18.273V13.8372C14.5 12.8089 14.8171 11.8056 15.408 10.964L19.8943 4.57465C20.3596 3.912 19.8856 3 19.0759 3Z" fill="rgba(0, 158, 247, 0.5)"/>
            </svg>
        </span>
            <label class="blink-slow cursor-pointer"
                   data-bs-toggle="tooltip" data-bs-placement="top" title="고급 필터 영역을 열고 닫습니다.">
                고급필터
            </label>
        </a>
    </div>
    <a href="${Url.VCATN_PAPR_REG_FORM!}" class="btn btn-sm btn-primary"
       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가계획서 등록 화면으로&#10;이동합니다.">
        <i class="fas fa-plus"></i>휴가 등록
    </a>
    <a href="javascript:void(0);" class="btn btn-sm btn-primary"
       onclick="dF.Schdul.regModal();"
       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="일정 등록 모달을 호출합니다.">
        <i class="fas fa-plus"></i>일정 등록
    </a>
    <a href="javascript:void(0);" class="btn btn-sm btn-light-primary btn-outlined"
       onclick="dF.Schdul.prvtRegModal();"
       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="개인용 일정 등록 모달을&#10;호출합니다.&#10;(다른 사람에겐 보이지 않는&#10;일정입니다.)">
        <i class="fas fa-plus"></i>개인용 일정<span class="d-none d-lg-inline"> 등록</span>
    </a>
</div>
<!--end::Card header-->

