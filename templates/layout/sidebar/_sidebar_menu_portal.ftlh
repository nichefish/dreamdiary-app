<!--
 * 레이아웃 > 사이드바 메뉴 (사용자)
 * @included_in : aside/_aside_base
 -->
<#import "/layout/sidebar/_sidebar_menu_template.ftlh" as menu >

<#assign upperMenuNo = (siteAcsInfo.upperMenuNo)!>

<!--begin::sidebar menu-->
<div class="app-sidebar-menu overflow-hidden flex-column-fluid">
    <!--begin::Menu wrapper-->
    <div id="kt_app_sidebar_menu_wrapper" class="app-sidebar-wrapper">
        <!--begin::Scroll wrapper-->
        <div id="kt_app_sidebar_menu_scroll" class="scroll-y my-5 mx-3"
            data-kt-scroll="true"
            data-kt-scroll-activate="true"
            data-kt-scroll-height="auto"
            data-kt-scroll-dependencies="#kt_app_sidebar_logo, #kt_app_sidebar_footer"
            data-kt-scroll-wrappers="#kt_app_sidebar_menu"
            data-kt-scroll-offset="5px"
            data-kt-scroll-save-state="true">

            <!--begin::Menu-->
            <div id="#kt_app_sidebar_menu" class="menu menu-column menu-rounded menu-sub-indention fw-semibold fs-6"
                    data-kt-menu="true"
                    data-kt-menu-expand="false">

                <#list userMenuList![] as userMenu>
                    <#list userMenu.subMenuList![] as subMenu>
                        <#if subMenu.menuSubExtendTyCd == "NO_SUB">
                            <@menu.noSubMenu upperMenuNo=upperMenuNo menu=subMenu />
                        </#if>
                        <#if subMenu.menuSubExtendTyCd == "LIST">
                            <@menu.listSubMenu upperMenuNo=upperMenuNo menu=subMenu />
                        </#if>
                        <#if subMenu.menuSubExtendTyCd == "EXTEND">
                            <div>${menu.menuNm!}</div>
                            <#list subMenu.subMenuList![] as subSubMenu>
                                <div>- ${subSubMenu.menuNm!}</div>
                            </#list>
                        </#if>
                        <#if subMenu.menuSubExtendTyCd == "COLLAPSE">
                            <@menu.collapseSubMenu upperMenuNo=upperMenuNo menu=subMenu />
                        </#if>
                        <#if subMenu.menuSubExtendTyCd == "BOARD">
                            <!-- BOARD -->
                            <@menu.boardMenu upperMenuNo=upperMenuNo menu=subMenu />
                        </#if>
                    </#list>
                </#list>
            </div>
            <!--end::Menu-->
        </div>
        <!--end::Scroll wrapper-->

    </div>
    <!--end::Menu wrapper-->

</div>
<!--end::sidebar menu-->

