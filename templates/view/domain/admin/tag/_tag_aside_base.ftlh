<#--
 * 태그 관리 > ASIDE (우측 사이드바)
 * @included_in : jrnl_day_page.ftlh
 -->

<#--begin::aside-->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <#--begin::Aside wrapper-->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y px-5 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <#--begin::Aside-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">Filters</h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="#" class="btn btn-sm btn-icon btn-color-gray-500 btn-light">
                        <i class="ki-duotone ki-magnifier fs-3">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div class="card-body" id="jrnl_aside">
                <input type="hidden" name="sort" id="sort">
                <#--begin::row-->
                <div class="mb-4">
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="dF.JrnlDayAside.today();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                        오늘 일자 조회하기<i class="bi bi-search ps-1 pe-0"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="dF.JrnlDayAside.sort();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="날짜 정렬을 변경합니다.">
                        <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
                    </button>
                </div>
                <#--begin::row-->
                <div class="mb-4">
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="dF.JrnlDayAside.pinpoint();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                        현재 년월 저장<i class="bi bi-pin-map ps-1 pe-0"></i>
                    </button>
                    <span class="px-2">
                        <span id="pinYy" class="text-muted text-underline-dotted">----</span>
                        <span> / </span>
                        <span id="pinMnth" class="text-muted text-underline-dotted">--</span>
                    </span>
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="dF.JrnlDayAside.turnback();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="저장된 년월로 이동합니다.">
                        <i class="bi bi-reply-all"></i>
                    </button>
                </div>
                <#--begin::row-->
                <div class="mb-7 d-flex-between gap-4">
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">년도</span>
                        <select name="yy" id="yy" class="form-select" aria-label="Select example"
                                onchange="dF.JrnlDayAside.yy();">
                            <option value="">----</option>
                            <#assign currYy = DateUtils.getCurrYy()?number />
                            <#assign startYy = 2010 />
                            <#list currYy..startYy as year>
                                <option value="${year}"><#if year == 2010>~</#if>${year}년</option>
                            </#list>
                        </select>
                    </div>
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">월</span>
                        <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                                onchange="dF.JrnlDayAside.mnth();">
                            <option value="">--</option>
                            <#list 1..12 as mnth>
                                <option value="${mnth}">${mnth}</option>
                            </#list>
                            <option value="99">전체 월</option>
                        </select>
                    </div>
                </div>
                <#--begin::row-->
                <div class="mb-6">
                    <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">꿈 키워드 검색</span>
                    <div class="d-flex gap-1">
                        <input type="text" name="dreamKeyword" id="dreamKeyword" class="form-control"
                               value="" placeholder="꿈 키워드 검색" maxlength="200"/>
                        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                                onclick="dF.JrnlDream.keywordListAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                            <i class="bi bi-search pe-0"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <!--end::Aside-->
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->