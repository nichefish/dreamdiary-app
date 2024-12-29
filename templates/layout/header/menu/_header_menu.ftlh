<!--
 -- 레이아웃 > 헤더 > 메뉴
 -- @included_in : layout/header/_header_menu.ftlh
 -->
<!--begin::Menu wrapper-->
<div
    class="app-header-menu app-header-mobile-drawer align-items-stretch ms-5"
    data-kt-drawer="true"
    data-kt-drawer-name="app-header-menu"
    data-kt-drawer-activate="{default: true, lg: false}"
    data-kt-drawer-overlay="true"
    data-kt-drawer-width="250px"
    data-kt-drawer-direction="end"
    data-kt-drawer-toggle="#kt_app_header_menu_toggle"
    data-kt-swapper="true"
    data-kt-swapper-mode="{default: 'append', lg: 'prepend'}"
    data-kt-swapper-parent="{default: '#kt_app_body', lg: '#kt_app_header_wrapper'}">
    <!--begin::Menu-->
    <div
        class="menu menu-rounded menu-column menu-lg-row my-5 my-lg-0 align-items-stretch fw-semibold px-2 px-lg-0"
        id="kt_app_header_menu"
        data-kt-menu="true">

        <!--begin:Menu item-->
        <!--show/here class-->
        <div data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="bottom-start"  class="menu-item menu-here-bg menu-lg-down-accordion me-0 me-lg-2" >
            <!--begin:Menu link-->
            <span class="menu-link">
                <span  class="menu-title" >Dashboards</span>
                <span  class="menu-arrow d-lg-none" ></span>
            </span><!--end:Menu link-->
            <!--begin:Menu sub-->
            <div  class="menu-sub menu-sub-lg-down-accordion menu-sub-lg-dropdown p-0 w-100 w-lg-850px" >
                <#include "/layout/header/menu/__dashboards.html">
            </div><!--end:Menu sub-->
        </div>
        <!--end:Menu item-->
        <!--begin:Menu item-->
        <div data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="bottom-start"  class="menu-item menu-lg-down-accordion me-0 me-lg-2" >
            <!--begin:Menu link-->
            <span class="menu-link"  >
                <span  class="menu-title" >Pages</span>
                <span  class="menu-arrow d-lg-none" ></span>
            </span>
            <!--end:Menu link-->
            <!--begin:Menu sub-->
            <div  class="menu-sub menu-sub-lg-down-accordion menu-sub-lg-dropdown p-0" >
                <#include "/layout/header/menu/__pages.html">
            </div>
            <!--end:Menu sub-->
        </div>
        <!--end:Menu item-->
        <!--begin:Menu item-->
        <div data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="bottom-start"  class="menu-item menu-lg-down-accordion menu-sub-lg-down-indention me-0 me-lg-2" >
            <!--begin:Menu link-->
            <span class="menu-link"  >
                <span  class="menu-title" >Apps</span>
                <span  class="menu-arrow d-lg-none" ></span>
            </span>
            <!--end:Menu link-->
            <!--begin:Menu sub-->
            <div  class="menu-sub menu-sub-lg-down-accordion menu-sub-lg-dropdown px-lg-2 py-lg-4 w-lg-250px" >
                <!--begin:Menu item-->
                <div  data-kt-menu-trigger="{default:'click', lg: 'hover'}" data-kt-menu-placement="right-start"  class="menu-item menu-lg-down-accordion" >
                    <!--begin:Menu link-->
                    <span class="menu-link"  >
                        <span  class="menu-icon" >
                            <i class="ki-duotone ki-rocket fs-2"><span class="path1"></span><span class="path2"></span></i>
                        </span>
                        <span  class="menu-title" >Projects</span>
                        <span  class="menu-arrow" ></span>
                    </span>
                    <!--end:Menu link-->
                    <!--begin:Menu sub-->
                    <div  class="menu-sub menu-sub-lg-down-accordion menu-sub-lg-dropdown menu-active-bg px-lg-2 py-lg-4 w-lg-225px" >
                        <!--begin:Menu item-->
                        <div  class="menu-item" >
                            <!--begin:Menu link-->
                            <a class="menu-link"  href="?page=apps/projects/list"  >
                                <span  class="menu-bullet" >
                                    <span class="bullet bullet-dot"></span>
                                </span>
                                <span  class="menu-title" >My Projects</span>
                            </a>
                            <!--end:Menu link-->
                        </div>
                        <!--end:Menu item-->
                    </div>
                    <!--end:Menu sub-->
                </div>
                <!--end:Menu item-->
            </div>
            <!--end:Menu item-->
        </div>
        <!--end:Menu item-->
    </div>
    <!--end::Menu-->

</div>
<!--end::Menu wrapper-->