<!--
 * 레이아웃 > 툴바
 * @included_in : layout
 -->

<!--begin::Toolbar-->
<div id="kt_app_toolbar" class="app-toolbar py-3 py-lg-6">
    <!--begin::Toolbar container-->
    <div id="kt_app_toolbar_container" class="app-container container-fluid d-flex flex-stack">
        <!--begin::Page title-->
        <div class="page-title d-flex flex-column justify-content-center flex-wrap me-3">
            <#--begin::title-->
            <h1 class="page-heading d-flex text-gray-900 fw-bold fs-3 flex-column justify-content-center my-0">${(siteAcsInfo.menuNm)!}</h1>
            <!--end::Title-->
            <!--begin::Breadcrumb-->
            <ul class="breadcrumb fw-semibold fs-7 my-0 pt-1">
                <!--begin::Item-->
                <li class="breadcrumb-item text-muted">
                    <a href="<#if isMngrMode>${Url.ADMIN_MAIN}<#else>${Url.MAIN}</#if>" class="text-muted text-hover-primary"
                       data-bs-toggle="tooltip" data-bs-placement="right" data-bs-dismiss="click" title="메인 화면으로 이동합니다.">
                        <i class="bi bi-house-<#if isMngrMode>gear-</#if>fill fs-4 me-1 text-primary"></i>
                    </a>
                </li>
                <!--end::Item-->
                <!--begin::Item-->
                <#if ((siteAcsInfo.upperMenuTyCd)! != "MAIN")>
                    <li class="breadcrumb-item text-muted">${(siteAcsInfo.upperMenuNm)!}</li>
                </#if>
                <!--end::Item-->
                <li class="breadcrumb-item text-muted">${(siteAcsInfo.menuNm)!}</li>
                <!--begin::Item-->
                <li class="breadcrumb-item text-muted text-underline-dotted" style=" text-underline-offset:3px;">
                    ${(siteAcsInfo.pageNm)!}&nbsp;
                </li>
                <!--end::Item-->
            </ul>
            <!--end::Breadcrumb-->
        </div>
        <!--end::Page title-->
        <!--begin::Actions-->
        <div class="d-flex flex align-items-center gap-2 gap-lg-3 flex-nowrap">

            <!-- 각 화면에서 정의한 상단 버튼 추가 -->
            <#if toolbar?? && toolbar?has_content>
                ${toolbar}
            </#if>
        </div>
        <!--end::Actions-->
    </div>
    <!--end::Toolbar container-->
</div>