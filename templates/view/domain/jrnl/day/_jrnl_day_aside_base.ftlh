<#--
 * 저널 일자(jrnl_day) : ASIDE (우측 사이드바)
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
            <div id="jrnl_aside_header" class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">Filters</h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="javascript:void(0);" class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
                       onclick="dF.JrnlDayAside.sort();"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-sort'/>">
                        <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div class="card-body" id="jrnl_aside">
                <input type="hidden" name="sort" id="sort">

                <#--begin::row-->
                <div class="mb-4 d-flex-between gap-4">
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-1"><@spring.message 'yy'/></span>
                        <div class="d-flex">
                            <div class="d-flex align-items-center me-2">
                                <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-prev'/>"></i>
                            </div>
                            <select name="yy" id="yy" class="form-select" aria-label="Select example"
                                    onchange="dF.JrnlDayAside.yy();">
                                <option value="">----</option>
                                <#assign currYy = DateUtils.getCurrYy()?number />
                                <#assign startYy = 2010 />
                                <#list currYy..startYy as year>
                                    <option value="${year}"><#if year == 2010>~</#if>${year}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-2 mb-1">월</span>
                        <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                                onclick="dF.JrnlDayAside.today();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-today'/>">
                            TODAY
                            <i class="bi bi-box-arrow-in-up-right"></i>
                        </button>
                        <div class="d-flex">
                            <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                                    onchange="dF.JrnlDayAside.mnth();">
                                <option value="">--</option>
                                <#list 1..12 as mnth>
                                    <option value="${mnth}">${mnth}</option>
                                </#list>
                                <option value="99"><@spring.message 'mmth-all'/></option>
                            </select>
                            <div class="d-flex align-items-center ms-2">
                                <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-next'/>"></i>
                            </div>
                        </div>
                    </div>
                </div>
                <#--begin::row-->
                <div class="px-8 mb-4 gap-1">
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                            onclick="dF.JrnlDayAside.pinpoint();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-pin'/>">
                        <i class="bi bi-bookmarks pe-0"></i>
                    </button>
                    <span class="mx-2">|</span>
                    <span class="px-1 me-1">
                        <span id="pinYy" class="text-muted text-underline-dotted">----</span>
                        <span> / </span>
                        <span id="pinMnth" class="text-muted text-underline-dotted">--</span>
                        <i class="bi bi-pin-map fs-7"></i>
                    </span>
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                            onclick="dF.JrnlDayAside.turnback();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-turnback'/>">
                        <i class="bi bi-reply-all pe-0"></i>
                    </button>
                </div>
                <div class="separator my-6"></div>
                <#--begin::row-->
                <div class="mb-5">
                    <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">
                        <@spring.message 'jrnl.diary.keyword.search'/>
                    </span>
                    <div class="d-flex gap-1">
                        <input type="text" name="diaryKeyword" id="diaryKeyword" class="form-control"
                               value="" placeholder="<@spring.message 'jrnl.diary.keyword.search'/>" maxlength="200"/>
                        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                                onclick="dF.JrnlDiary.keywordListAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.diary.keyword.search'/>">
                            <i class="bi bi-search pe-0"></i>
                        </button>
                    </div>
                </div>
                <#--begin::row-->
                <div class="mb-5">
                    <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">
                        <@spring.message 'jrnl.dream.keyword.search'/>
                    </span>
                    <div class="d-flex gap-1">
                        <input type="text" name="dreamKeyword" id="dreamKeyword" class="form-control"
                               value="" placeholder="<@spring.message 'jrnl.dream.keyword.search'/>" maxlength="200"/>
                        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                                onclick="dF.JrnlDream.keywordListAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.dream.keyword.search'/>">
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

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/jrnl/day/jrnl_day_aside_module.js?${releaseDate!}"></script>