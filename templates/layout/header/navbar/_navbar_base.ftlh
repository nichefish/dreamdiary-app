<!--
 * 레이아웃 > 헤더 > 네비게이션 바 (base)
 * @included_in : layout/header/_header_base.ftlh
 -->

<#--begin::Navbar-->
<div class="app-navbar flex-shrink-0">
    <#--include::Notion-->
    <#include "/layout/header/navbar/_navbar_btn_notion.ftlh">

    <#--include::Flsys-->
    <#include "/layout/header/navbar/_navbar_btn_flsys.ftlh">

    <span class="my-6 border-gray-300 border-start ms-3 mx-2"></span>

    <#--include::테스트 페이지 -->
    <#include "/layout/header/navbar/_navbar_btn_test_page.ftlh">

    <#--include::태그클라우드 -->
    <#include "/layout/header/navbar/_navbar_btn_tag_cloud.ftlh">

    <#--include::사이트맵 -->
    <#include "/layout/header/navbar/_navbar_btn_sitemap.ftlh">

    <#--include::검색 -->
    <#include "/layout/header/navbar/_navbar_btn_search.ftlh">

    <!--begin::Chat-->
    <div class="app-navbar-item ms-1 ms-md-4">
        <!--begin::Menu wrapper-->
        <div class="btn btn-icon btn-custom btn-icon-muted btn-active-light btn-active-color-primary w-35px h-35px position-relative" id="kt_drawer_chat_toggle">
            <i class="ki-duotone ki-message-text-2 fs-2">
                <span class="path1"></span>
                <span class="path2"></span>
                <span class="path3"></span>
            </i>
            <span class="bullet bullet-dot bg-success h-6px w-6px position-absolute translate-middle top-0 start-50 animation-blink"></span>
        </div>
        <!--end::Menu wrapper-->
    </div>

    <span class="my-6 border-gray-300 border-start ms-2 me-6"></span>

    <!--begin::Header menu toggle-->
    <div class="app-navbar-item align-items-center me-2">
        <#if isMngr!>
            <#--begin::사용자/관리자 모드 선택-->
            <div class="position-relative w-md-115px me-3"
                 data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="사용자/관리자 권한별로&#10;화면을 전환합니다.">
                <select name="userMode" id="userMode" class="form-select form-select-solid fs-small fw-bolder form-select-sm opacity-hover mt-1 ps-3"
                    style="border-color:#86b7fe; outline: 0; box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);">
                    <option value="${(Constant.AUTH_USER)!}" <#if !isMngrMode>selected</#if>>사용자 모드</option>
                    <option value="${(Constant.AUTH_MNGR)!}" <#if isMngrMode>selected</#if>>관리자 모드</option>
                </select>
            </div>
        </#if>

        <#--begin::Users-->
        <div class="d-flex align-items-center ms-1 mt-1">
            <#--begin::Menu wrapper-->
            <!--begin::User menu-->
            <div class="app-navbar-item"
                 id="kt_header_user_menu_toggle"
                 data-kt-menu-trigger="{default: 'click', lg: 'hover'}"
                 data-kt-menu-attach="parent"
                 data-kt-menu-placement="bottom-end">
                <!--begin::Menu wrapper-->
                <div class="cursor-pointer symbol symbol-35px">
                    <#if (authInfo.proflImgUrl)??>
                        <img src="${authInfo.proflImgUrl!}" class="img-thumbnail p-0 w-100" onerror="this.src='/metronic/assets/media/avatars/avatar_blank.png';" />
                    <#else>
                        <span class="svg-icon svg-icon-1">
                            <i class="fas fa-user-circle fs-2"></i>
                        </span>
                    </#if>
                </div>
                <div class="cursor-pointer mt-2">
                    <div class="w-75px ms-2 text-truncate">
                        ${(authInfo.nickNm)!}
                        <#if isMngr>
                            <i class="bi bi-person-lines-fill fs-=5 text-info ms-1 opacity-75"
                               data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="관리자"></i>   <!-- 관리자 -->
                        <#else>
                            <i class="bi bi-people-fill fs-5 ms-1"
                               data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="사용자"></i>   <!-- 관리자 -->
                        </#if>
                    </div>
                    <div class="w-75px ms-2 mb-1 fs-7 text-muted text-truncate">
                        (${(authInfo.userId)!})
                    </div>
                </div>
                <!--begin::User account menu-->
                <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg menu-state-color fw-semibold py-4 fs-6 w-275px" data-kt-menu="true">
                    <#--include:: User-My-Drawer-->
                    <#include "/layout/header/navbar/_navbar_user_toggle.ftlh">
                </div>
                <!--end::User account menu-->
                <!--end::Menu wrapper-->
            </div>
            <!--end::User menu-->
        </div>
        <#--end::Users-->
    </div>

    <#--end::Header menu toggle-->
</div>
<#--end::Navbar-->