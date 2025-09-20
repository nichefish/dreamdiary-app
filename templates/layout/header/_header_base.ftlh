<!--
 * 레이아웃 > 헤더 (base)
 * @included_in : layout.ftlh
 -->

<#--include::script modules-->
<#include "/layout/header/_header_scripts.ftlh">

<#--begin::Header-->
<div id="kt_app_header"
     class="app-header shadow-sm"
     data-kt-sticky="true"
     data-kt-sticky-activate="{default: true, lg: true}"
     data-kt-sticky-name="app-header-minimize"
     data-kt-sticky-offset="{default: '200px', lg: '0'}"
     data-kt-sticky-animation="false" >

    <#--begin::Header container-->
    <div class="app-container container-fluid d-flex align-items-stretch justify-content-between" id="kt_app_header_container">

        <#--begin::Web&Mobile logo-->
        <#include "/layout/header/_header_logo.ftlh">
        <#--end::Web&Mobile logo-->

        <#--begin::Header wrapper-->
        <div class="d-flex align-items-stretch justify-content-between flex-lg-grow-1" id="kt_app_header_wrapper">

            <#include "/layout/header/menu/_header_menu.ftlh">

            <#include "/layout/header/navbar/_navbar_base.ftlh">

            <!-- 모바일 저널 -->
            <#include "/layout/header/jrnl/_jrnl_navbar_base.ftlh">
        </div>
        <#--end::Header wrapper-->

    </div>
    <#--end::Header container-->

</div>
<#--end::Header-->
