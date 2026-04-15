<#--
 * journal_day: aside (right sidebar)
 * @included_in : journal_day_page.ftlh
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
    <div id="kt_app_aside_wrapper" class="hover-scroll-y ps-5 pe-2 mx-5 my-5"
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
            <div id="journal_aside_header" class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">
                    <i class="bi bi-filter fs-2 me-1"></i> <@spring.message 'txt.journal.day.filter'/>
                </h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="javascript:void(0);" class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
                       onclick="dF.JournalDayAside.sort();"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-sort'/>">
                        <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div id="journal_aside" class="card-body">
                <input type="hidden" name="sort" id="sort">

                <div class="d-flex flex-column gap-4">
                    <!-- 년월 필터 부분 분리 -->
                    <#include "/view/feature/journal/day/_aside/_journal_day_aside_yy_mnth_section.ftlh">
                    <!-- separator -->
                    <div class="separator"></div>
                    <!-- 태그클라우드 필터 -->
                    <div class="d-flex flex-column gap-2 px-2">
                        <div class="d-flex align-items-center justify-content-between"
                             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-tagcloud-toggle'/>">
                            <label for="toggleTagCloud" class="text-muted cursor-help mb-0">TAGCLOUD</label>
                            <input type="checkbox" id="toggleTagCloud" class="form-check-input cursor-pointer m-0" onclick="dF.JournalDay.toggleParam();" checked>
                        </div>
                    </div>
                    <!-- separator -->
                    <div class="separator"></div>
                    <!-- 일기 필터 -->
                    <#include "/view/feature/journal/day/_aside/_journal_day_aside_diary_filter.ftlh">
                    <!-- separator -->
                    <div class="separator"></div>
                    <!-- 꿈 필터 -->
                    <#include "/view/feature/journal/day/_aside/_journal_day_aside_dream_filter.ftlh">
                    <!-- separator -->
                    <div class="separator"></div>
                    <!-- 필터 -->
                    <div class="accordion accordion-flush" id="journal_day_filter_accordion">
                        <div class="accordion-item">
                            <h2 class="accordion-header" id="journal_day_filter_heading_advanced">
                                <button class="accordion-button collapsed fw-semibold" type="button"
                                        data-bs-toggle="collapse" data-bs-target="#journal_day_filter_advanced"
                                        aria-expanded="false" aria-controls="journal_day_filter_advanced">
                                    <@spring.message 'txt.journal.day.filter.advanced'/>
                                </button>
                            </h2>
                            <div id="journal_day_filter_advanced" class="accordion-collapse collapse"
                                 aria-labelledby="journal_day_filter_heading_advanced"
                                 data-bs-parent="#journal_day_filter_accordion">
                                <div class="accordion-body pt-4">
                                    <div class="text-muted fs-7">
                                        <@spring.message 'txt.journal.day.filter.more'/>
                                    </div>
                                    <div class="mt-4" data-filter-slot="advanced"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--end::Aside-->

        <div class="separator my-8"></div>

        <#--begin::Aside-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div id="journal_aside_header" class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">
                    <i class="bi bi-list-task fs-2 me-1"></i> TODO List
                </h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="javascript:void(0);" class="btn btn-sm btn-icon btn-primary"
                       onclick="dF.JournalTodo.regModal();"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-sort'/>">
                        <i class="bi bi-plus fs-2 pe-0" id="sortIcon"></i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <div id="journal_todo_list_div">
                <!-- Handlebars template goes in here -->
            </div>
        </div>
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/journal/day/journal_day_aside_module.js?${releaseDate!}"></script>

<#--include::journal_todo_module-->
<#include "/view/feature/journal/todo/_modal/_journal_todo_reg_modal.ftlh">
<#include "/view/feature/journal/todo/_journal_todo_list_template.hbs">
<script type="text/javascript" src="/js/view/feature/journal/todo/journal_todo_module.js?${releaseDate!}"></script>
