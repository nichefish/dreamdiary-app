<!--
 * 레이아웃 > 메뉴 그리기 템플릿
 * @included_in : aside/_aside_menu_portal
 * @included_in : aside/_aside_menu_admin
 -->

<#macro noSubMenu upperMenuNo menu>
    <!-- 라벨 -->
    <#if (menu.menuLabel?? && menu.menuLabel! != "")>
        <div class="menu-item">
            <div class="menu-content mt-5 pb-2">
                <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${menu.menuLabel!}</span>
            </div>
        </div>
    </#if>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (upperMenuNo != menu.upperMenuNo!?string) />
    <div class="menu-item" id="${menu.menuNo!}">
        <a href="${menu.url!}" class="menu-link <#if isNotTopMenu>text-inverse-light</#if>">
            ${menu.icon?no_esc}
            <span class="menu-title">${menu.menuNm!}</span>
            <!-- TODO: 새 글 갯수 표시. 1개 이상이면 표시. 5개 이상이면 깜빡임. -->
            <#if menu.unreadCntNm?? && menu.unreadCntNm != ''>
                <#assign unreadCount = (menu.unreadCntNm?eval)!0>
                <#if unreadCount gt 0>
                    <span class="badge badge-success bg-noti <#if unreadCount! gt 5>blink</#if>">
                        ${unreadCount!}
                    </span>
                </#if>
            </#if>
        </a>
    </div>
</#macro>

<!-- 하단에 하위메뉴 표시 -->
<#macro listSubMenu upperMenuNo menu>
    <!-- 라벨 -->
    <#if (menu.menuLabel?? && menu.menuLabel! != "")>
        <div class="menu-item">
            <div class="menu-content mt-5 pb-2">
                <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${menu.menuLabel!}</span>
            </div>
        </div>
    </#if>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (upperMenuNo != menu.upperMenuNo!?string) />
    <div data-kt-menu-trigger="click" class="menu-item menu-accordion hover show" id="${menu.upperMenuNo!}">
        <span class="menu-link <#if isNotTopMenu>text-inverse-light</#if> mb-1">
            ${menu.icon!?no_esc}
            <span class="menu-title">${menu.menuNm!}</span>
            <span class="menu-arrow"></span>
        </span>
        <!-- TODO: 새 글 갯수 표시. 1개 이상이면 표시. 5개 이상이면 깜빡임. -->
        <#if menu.unreadCntNm?? && menu.unreadCntNm != ''>
            <#assign unreadCount = (menu.unreadCntNm?eval)!0>
            <#if unreadCount gt 0>
                <span class="badge badge-success bg-noti <#if unreadCount! gt 5>blink</#if>">
                    ${unreadCount!}
                </span>
            </#if>
        </#if>
        <#list menu.subMenuList![] as subMenu>
            <div class="menu-sub menu-sub-accordion menu-active-bg">
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

<!-- 하위메뉴 접기 표시 -->
<#macro collapseSubMenu upperMenuNo menu>
    <!-- 라벨 -->
    <#if (menu.menuLabel?? && menu.menuLabel! != "")>
        <div class="menu-item">
            <div class="menu-content mt-5 pb-2">
                <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${menu.menuLabel!}</span>
            </div>
        </div>
    </#if>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (upperMenuNo != menu.upperMenuNo!?string) />
    <div data-kt-menu-trigger="click" class="menu-item menu-accordion here hover show" id="${menu.upperMenuNo!}">
        <span class="menu-link <#if isNotTopMenu>text-inverse-light</#if> mb-1">
            ${menu.icon!?no_esc}
            <span class="menu-title">${menu.menuNm!}</span>
            <span class="menu-arrow"></span>
        </span>
        <div class="menu-sub menu-sub-accordion">
            <#list menu.subMenuList![] as subMenu>
                <!--end:Menu item-->
                <div class="menu-inner flex-column <#if subMenu?index gt 1>collapse" id="kt_app_sidebar_menu_${menu.menuNo!}_collapse</#if>">
                    <!--begin:Menu item-->
                    <div class="menu-item" id="${subMenu.menuNo!}">
                        <!--begin:Menu link-->
                        <a class="menu-link" href="${subMenu.url!}">
                            <span class="menu-bullet">
                                <span class="bullet bullet-dot"></span>
                            </span>
                            <span class="menu-title">${subMenu.menuNm!}</span>
                        </a>
                        <!--end:Menu link-->
                    </div>
                    <!--end:Menu item-->
                </div>
            </#list>
            <#if menu.subMenuList?size gt 2>
                <div class="menu-item pt-0">
                    <div class="menu-content">
                        <a class="btn btn-flex btn-color-primary d-flex flex-stack fs-base p-0 ms-2 mb-2 toggle collapsible collapsed"
                           data-bs-toggle="collapse" href="#kt_app_sidebar_menu_${menu.menuNo!}_collapse"
                           data-kt-toggle-text="[ 접기 - ]">
                            <span data-kt-toggle-text-target="true">[ 더보기 + ]</span>
                            <i class="ki-duotone ki-minus-square toggle-on fs-2 me-0">
                                <span class="path1"></span>
                                <span class="path2"></span>
                            </i>
                            <i class="ki-duotone ki-plus-square toggle-off fs-2 me-0">
                                <span class="path1"></span>
                                <span class="path2"></span>
                                <span class="path3"></span>
                            </i>
                        </a>
                    </div>
                </div>
            </#if>
        </div>
    </div>
</#macro>

<!-- 일반게시판 메뉴 -->
<#macro boardMenu upperMenuNo menu>
    <!-- 라벨 -->
    <div class="menu-item">
        <div class="menu-content mt-5 pb-2">
            <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${menu.menuLabel!}</span>
        </div>
    </div>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (upperMenuNo != menu.upperMenuNo!?string)!>
    <div data-kt-menu-trigger="click" class="menu-item menu-accordion hover show" id="${menu.upperMenuNo!}">
        <span class="menu-link <#if isNotTopMenu>text-inverse-light</#if> mb-1">
            ${menu.icon!?no_esc}
            <span class="menu-title">${menu.menuNm!}</span>
            <span class="menu-arrow"></span>
        </span>
        <#list boardDefList![] as boardDef>
            <div class="menu-item">
                <a class="menu-link" href="${Url.BOARD_POST_LIST!}?boardDef=${boardDef.boardDef!}">
                    <span class="menu-bullet">
                        <span class="bullet bullet-dot"></span>
                    </span>
                    <span class="menu-title">${boardDef.menuNm!}</span>
                </a>
            </div>
        </#list>
    </div>
</#macro>

<#macro extendedSubMenu upperMenuNo siteMenu>
    <div class="menu-item">
        <div class="menu-content mt-5 pb-2">
            <span class="menu-heading fw-bold text-uppercase fs-8 ls-1">${siteMenu.topMenuLabel!}</span>
        </div>
    </div>
    <!-- 메뉴 시작 -->
    <#assign isNotTopMenu = (upperMenuNo != siteMenu.upperMenuNo!?string)! />

    <div data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="right-start"
         class="menu-item menu-lg-down-accordion menu-sub-lg-down-indention" id="${siteMenu.upperMenuNo}">

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