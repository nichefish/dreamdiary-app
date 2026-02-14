<#--
 * 저널 일기(jrnl_diary) > 목록 툴바 Header
 * @included_in : jrnl_diary_search.ftlh
 -->

<div class="row w-100 mb-5">
    <div class="col-1 d-flex-center">
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
    </div>
    <div class="col-1 d-flex-align-center">
        <button type="button" class="btn btn-sm btn-light-secondary text-dark"
                onclick="dF.JrnlDiarySearch.resetSearch();">
            초기화
        </button>
        <button type="submit" class="btn btn-sm btn-primary"
                onclick="dF.JrnlDiarySearch.search();">
            검색
        </button>
    </div>
    <div class="col">
        <span id="msgDiv"></span>
    </div>
</div>
<div class="collapse mb-5 px-10 py-4 bg-light w-100" id="schdul_advanced_search_div">
    <!--begin::Advance form-->
    <form name="listForm" id="listForm" class="d-md-flex align-items-center flex-nowrap gap-2" method="get">
        <div class="d-flex align-items-center justify-content-between mb-2">
            <label class="fw-bold fs-7 text-gray-700">검색 키워드</label>
            <button type="button" class="btn btn-sm btn-light-primary"
                    onclick="dF.JrnlDiarySearch.addKeywordField();">
                + 추가
            </button>
        </div>
        <div id="jrnlKeywordContainer" class="d-flex flex-column gap-2">
            <!-- 동적으로 input 추가 -->
        </div>
    </form>
    <!--end::Advance form-->
</div>

