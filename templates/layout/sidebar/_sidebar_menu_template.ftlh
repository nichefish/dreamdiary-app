<!--
 * 레이아웃 > 메뉴 그리기 템플릿
 * @included_in : aside/_aside_menu_portal
 * @included_in : aside/_aside_menu_admin
 -->

<#macro noSubMenu topMenuNo siteMenu>
    <div class="menu-item">
        <div class="menu-content mt-5 pb-2">
            <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${siteMenu.topMenuLabel}</span>
        </div>
    </div>
    <#assign isNotTopMenu = (topMenuNo != siteMenu.topMenuNo) />
    <div class="menu-item" id="${siteMenu.menuNo!}">
        <a href="${siteMenu.url!}" class="menu-link <#if isNotTopMenu>text-inverse-light</#if>">
            <span class="menu-icon">
                <span class="svg-icon svg-icon-3 svg-icon-gray-600">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                         class="bi bi-bell-fill" viewBox="0 0 16 16">
                        <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z"/>
                    </svg>
                </span>
            </span>
            <span class="menu-title">${siteMenu.menuNm!}</span>
            <!-- TODO: 새 글 갯수 표시. 1개 이상이면 표시. 5개 이상이면 깜빡임. -->
            <#if noticeUnreadCnt! gt 0>
                <span class="badge badge-success bg-noti <#if noticeUnreadCnt! gt 5>blink</#if>">
                    ${noticeUnreadCnt!}
                </span>
            </#if>
        </a>
    </div>
</#macro>

<#macro withSubMenu topMenuNo siteMenu>
    <div class="menu-item">
        <div class="menu-content mt-5 pb-2">
            <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${siteMenu.topMenuLabel}</span>
        </div>
    </div>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (topMenuNo != siteMenu.topMenuNo!)!>
    <div data-kt-menu-trigger="click" class="menu-item menu-accordion hover show" id="${siteMenu.topMenuNo}">
        <span class="menu-link <#if isNotTopMenu>text-inverse-light</#if> mb-1">
            <span class="menu-icon">
                <#--begin::Svg Icon | path: icons/duotune/communication/com013.svg-->
                <span class="svg-icon svg-icon-3">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                         class="bi bi-calendar-date-fill" viewBox="0 0 16 16">
                        <path d="M4 .5a.5.5 0 0 0-1 0V1H2a2 2 0 0 0-2 2v1h16V3a2 2 0 0 0-2-2h-1V.5a.5.5 0 0 0-1 0V1H4V.5zm5.402 9.746c.625 0 1.184-.484 1.184-1.18 0-.832-.527-1.23-1.16-1.23-.586 0-1.168.387-1.168 1.21 0 .817.543 1.2 1.144 1.2z"/>
                        <path d="M16 14V5H0v9a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2zm-6.664-1.21c-1.11 0-1.656-.767-1.703-1.407h.683c.043.37.387.82 1.051.82.844 0 1.301-.848 1.305-2.164h-.027c-.153.414-.637.79-1.383.79-.852 0-1.676-.61-1.676-1.77 0-1.137.871-1.809 1.797-1.809 1.172 0 1.953.734 1.953 2.668 0 1.805-.742 2.871-2 2.871zm-2.89-5.435v5.332H5.77V8.079h-.012c-.29.156-.883.52-1.258.777V8.16a12.6 12.6 0 0 1 1.313-.805h.632z"/>
                    </svg>
                </span>
                <#--end::Svg Icon-->
            </span>
            <span class="menu-title">${siteMenu.menuNm!}</span>
            <span class="menu-arrow"></span>
        </span>
        <#list siteMenu.subMenuList![] as subMenu>
            <div class="menu-sub menu-sub-accordion menu-active-bg">
                <!-- 일정 > 전체 일정 확인 -->
                <div class="menu-item" id="${subMenu.menuNo!}">
                    <a class="menu-link" href="${subMenu.url!}">
                        <span class="menu-bullet">
                            <span class="bullet bullet-dot"></span>
                        </span>
                        <span class="menu-title">${subMenu.menuNm!}</span>
                    </a>
                </div>
            </div>
        </#list>
    </div>
</#macro>

<#macro extendedSubMenu topMenuNo siteMenu>
    <div class="menu-item">
        <div class="menu-content mt-5 pb-2">
            <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${siteMenu.topMenuLabel}</span>
        </div>
    </div>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (topMenuNo != siteMenu.topMenuNo!)! />

    <div data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="right-start"
         class="menu-item menu-lg-down-accordion menu-sub-lg-down-indention" id="${siteMenu.topMenuNo}">

        <span class="menu-link <#if isNotTopMenu>text-inverse-light</#if>">
            <span class="menu-icon">
                <#--begin::Svg Icon | path: icons/duotune/communication/com013.svg-->
                <span class="svg-icon svg-icon-3">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                         class="bi bi-calendar-date-fill" viewBox="0 0 16 16">
                        <path d="M4 .5a.5.5 0 0 0-1 0V1H2a2 2 0 0 0-2 2v1h16V3a2 2 0 0 0-2-2h-1V.5a.5.5 0 0 0-1 0V1H4V.5zm5.402 9.746c.625 0 1.184-.484 1.184-1.18 0-.832-.527-1.23-1.16-1.23-.586 0-1.168.387-1.168 1.21 0 .817.543 1.2 1.144 1.2z"/>
                        <path d="M16 14V5H0v9a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2zm-6.664-1.21c-1.11 0-1.656-.767-1.703-1.407h.683c.043.37.387.82 1.051.82.844 0 1.301-.848 1.305-2.164h-.027c-.153.414-.637.79-1.383.79-.852 0-1.676-.61-1.676-1.77 0-1.137.871-1.809 1.797-1.809 1.172 0 1.953.734 1.953 2.668 0 1.805-.742 2.871-2 2.871zm-2.89-5.435v5.332H5.77V8.079h-.012c-.29.156-.883.52-1.258.777V8.16a12.6 12.6 0 0 1 1.313-.805h.632z"/>
                    </svg>
                </span>
                <#--end::Svg Icon-->
            </span>
            <span class="menu-title">${siteMenu.menuNm!}</span>
            <span class="menu-arrow"></span>
        </span>
        <div class="menu-sub menu-sub-lg-down-accordion menu-sub-lg-dropdown px-2 py-4 w-200px mh-75 overflow-auto">
            <#list siteMenu.subMenuList![] as subMenu>
                <div class="menu-item" id="${subMenu.menuNo!}">
                    <a class="menu-link" href="${subMenu.url!}">
                        <span class="menu-bullet">
                            <span class="bullet bullet-dot"></span>
                        </span>
                        <span class="menu-title">${subMenu.menuNm!}</span>
                    </a>
                </div>
            </#list>
        </div>
    </div>
</#macro>


<#macro outer_menu siteMenu>
    <!--begin:Menu item-->
    <div class="menu-item" id="${siteMenu.menuNo!}">
        <!--begin:Menu link-->
        <a class="menu-link" href="${siteMenu.url!}">
            <span class="menu-bullet">
                <span class="bullet bullet-dot"></span>
            </span>
            <span class="menu-title">${siteMenu.menuNm!}</span>
        </a>
        <!--end:Menu link-->
    </div>
</#macro>


<#macro inner_menu siteMenu>
    <!--end:Menu item-->
    <div class="menu-inner flex-column collapse" id="kt_app_sidebar_menu_dashboards_collapse">
        <!--begin:Menu item-->
        <div class="menu-item" id="${siteMenu.menuNo!}">
            <!--begin:Menu link-->
            <a class="menu-link" href="${siteMenu.url!}">
                <span class="menu-bullet">
                    <span class="bullet bullet-dot"></span>
                </span>
                <span class="menu-title">${siteMenu.menuNm!}</span>
            </a>
            <!--end:Menu link-->
        </div>
        <!--end:Menu item-->
    </div>
</#macro>