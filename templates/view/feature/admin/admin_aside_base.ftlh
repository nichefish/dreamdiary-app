<#--
 * 레이아웃 > Footer
 * @included_in : layout
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

        <#--begin::Aside1-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">시스템 정보</h3>
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
            <div class="card-body">
                <#--begin::Card widget 20-->
                <table class="table align-middle table-row-dashed fs-small gy-4 table-fixed mb-3">
                    <#--begin::Table head-->
                    <thead>
                        <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                            <th class="col-4 text-center wb-keepall">권한 코드</th>
                            <th class="col-8 text-center wb-keepall">권한명 (level)</th>
                        </tr>
                    </thead>
                    <#--begin::Table body-->
                    <tbody>
                        <#list authRoleList![] as authRole>
                            <tr>
                                <td class="text-center">
                                    <div class="text-muted fw-bold">${authRole.authCd!}</div>
                                </td>
                                <td class="text-center">
                                    <div class="d-flex-center">
                                        <#if (authRole.authCd! == Constant.AUTH_MNGR)>
                                            <!-- 관리자 -->
                                            <i class="bi bi-person-lines-fill text-info fs-2 me-2 opacity-75"
                                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="${authRole.authNm!"-"}"></i>
                                            <div class="text-info fw-bold">${authRole.authNm!}</div>
                                            <div class="badge badge-info ms-3">${authRole.authLevel!}</div>
                                        <#elseif (authRole.authCd! == Constant.AUTH_USER)>
                                            <!-- 사용자 -->
                                            <i class="bi bi-people-fill me-2 fs-2"
                                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="${authRole.authNm!"-"}"></i>
                                            <div class="text-muted fw-bold">${authRole.authNm!}</div>
                                            <div class="badge badge-dark ms-3 opacity-50">${authRole.authLevel!}</div>
                                        <#elseif (authRole.authCd! == Constant.AUTH_DEV)>
                                            <!-- 개발자 -->
                                            <i class="bi bi-person-fill-gear text-info fs-2 me-2 opacity-75"
                                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="${authRole.authNm!"-"}"></i>
                                            <div class="text-info fw-bold">${authRole.authNm!}</div>
                                            <div class="badge badge-info ms-3">${authRole.authLevel!}</div>
                                        </#if>
                                    </div>
                                </td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan=8" class="text-center"><@spring.message 'view.list.empty'/></td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
                <!--end::Body-->
            </div>
        </div>
        <!--end::Aside1-->
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->