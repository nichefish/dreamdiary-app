<!--
 * 레이아웃 > 탑바 > 사용자 정보 Drawer
 * @included_in : layout
 -->

<#--begin::user drawer-->
<div>
    <!--begin::Menu item-->
    <div class="menu-item px-3">
        <div class="menu-content d-flex align-items-center px-3">
            <!--begin::Avatar-->
            <div class="d-flex flex-column align-items-center symbol symbol-50px me-5">
                <#if (authInfo.proflImgUrl)??>
                    <img src="${authInfo.proflImgUrl!}" class="img-thumbnail p-0 w-100" onerror="this.src='/metronic/assets/media/avatars/avatar_blank.png';" />
                <#else>
                    <span class="svg-icon svg-icon-1">
                        <i class="fas fa-user-circle fs-2"></i>
                    </span>
                </#if>
                <button type="button" class="badge badge-outlined badge-light-primary mt-2 fw-normal"
                        onclick="Layout.myInfoDtl();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="내 정보 화면으로 이동합니다.">
                    내 정보
                </button>
            </div>
            <!--end::Avatar-->
            <!--begin::Username-->
            <div class="d-flex flex-column">
                <div class="fw-bold d-flex align-items-center fs-5">
                    ${(authInfo.nickNm)!}
                    <span class="badge badge-light-success fw-bold fs-8 px-2 py-1 ms-2">
                        <#if isMngr>
                            <i class="bi bi-person-lines-fill fs-3 text-info me-1 opacity-75"
                               data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="관리자"></i>   <!-- 관리자 -->
                            관리자
                        <#else>
                            <i class="bi bi-people-fill fs-3 mx-1"
                               data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click" title="사용자"></i>   <!-- 사용자 -->
                            사용자
                        </#if>
                        <#if ((authInfo.authCd)! == Constant.AUTH_MNGR)>
                            <i class="bi bi-person-lines-fill text-info opacity-75"></i>   <!-- 관리자 -->
                        <#elseif ((authInfo.authCd)! == Constant.AUTH_USER)>
                            <i class="bi bi-people-fill ms-1"></i>        <!-- 사용자 -->
                        </#if>
                    </span>
                </div>
                <a href="#" class="fw-semibold text-muted text-hover-primary fs-7">${(authInfo.email)!}</a>
                <a href="#" class="fw-semibold text-muted text-hover-primary fs-7">(${(authInfo.userId)!})</a>
                <a href="#" class="fw-semibold text-muted text-hover-primary fs-7">(${acsIp!})</a>
            </div>
            <!--end::Username-->
        </div>
    </div>
    <!--end::Menu item-->

    <!--begin::Menu separator-->
    <div class="separator my-2"></div>
    <!--end::Menu separator-->
    <!--begin::Menu item-->
    <div class="menu-item px-5">
        <a href="account/overview.html" class="menu-link px-5">My Profile</a>
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="menu-item px-5">
        <a href="apps/projects/list.html" class="menu-link px-5">
            <span class="menu-text">My Projects</span>
            <span class="menu-badge">
                <span class="badge badge-light-danger badge-circle fw-bold fs-7">3</span>
            </span>
        </a>
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="menu-item px-5" data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="left-start" data-kt-menu-offset="-15px, 0">
        <a href="#" class="menu-link px-5">
            <span class="menu-title">My Subscription</span>
            <span class="menu-arrow"></span>
        </a>
        <!--begin::Menu sub-->
        <div class="menu-sub menu-sub-dropdown w-175px py-4">
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/referrals.html" class="menu-link px-5">Referrals</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/billing.html" class="menu-link px-5">Billing</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/statements.html" class="menu-link px-5">Payments</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/statements.html" class="menu-link d-flex flex-stack px-5">Statements
                    <span class="ms-2 lh-0" data-bs-toggle="tooltip" title="View your statements">
                        <i class="ki-duotone ki-information-5 fs-5">
                            <span class="path1"></span>
                            <span class="path2"></span>
                            <span class="path3"></span>
                        </i>
                    </span>
                </a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu separator-->
            <div class="separator my-2"></div>
            <!--end::Menu separator-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid">
                        <input class="form-check-input w-30px h-20px" type="checkbox" value="1" checked="checked" name="notifications" />
                        <span class="form-check-label text-muted fs-7">Notifications</span>
                    </label>
                </div>
            </div>
            <!--end::Menu item-->
        </div>
        <!--end::Menu sub-->
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="menu-item px-5">
        <a href="account/statements.html" class="menu-link px-5">My Statements</a>
    </div>
    <!--end::Menu item-->
    <!--begin::Menu separator-->
    <div class="separator my-2"></div>
    <!--end::Menu separator-->
    <!--begin::Menu item-->
    <div class="menu-item px-5" data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="left-start" data-kt-menu-offset="-15px, 0">
        <a href="#" class="menu-link px-5">
            <span class="menu-title position-relative">Mode
                <span class="ms-5 position-absolute translate-middle-y top-50 end-0">
                    <i class="ki-duotone ki-night-day theme-light-show fs-2">
                        <span class="path1"></span>
                        <span class="path2"></span>
                        <span class="path3"></span>
                        <span class="path4"></span>
                        <span class="path5"></span>
                        <span class="path6"></span>
                        <span class="path7"></span>
                        <span class="path8"></span>
                        <span class="path9"></span>
                        <span class="path10"></span>
                    </i>
                    <i class="ki-duotone ki-moon theme-dark-show fs-2">
                        <span class="path1"></span>
                        <span class="path2"></span>
                    </i>
                </span>
            </span>
        </a>
        <!--begin::Menu-->
        <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-title-gray-700 menu-icon-gray-500 menu-active-bg menu-state-color fw-semibold py-4 fs-base w-150px" data-kt-menu="true" data-kt-element="theme-mode-menu">
            <!--begin::Menu item-->
            <div class="menu-item px-3 my-0">
                <a href="#" class="menu-link px-3 py-2" data-kt-element="mode" data-kt-value="light">
                    <span class="menu-icon" data-kt-element="icon">
                        <i class="ki-duotone ki-night-day fs-2">
                            <span class="path1"></span>
                            <span class="path2"></span>
                            <span class="path3"></span>
                            <span class="path4"></span>
                            <span class="path5"></span>
                            <span class="path6"></span>
                            <span class="path7"></span>
                            <span class="path8"></span>
                            <span class="path9"></span>
                            <span class="path10"></span>
                        </i>
                    </span>
                    <span class="menu-title">Light</span>
                </a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3 my-0">
                <a href="#" class="menu-link px-3 py-2" data-kt-element="mode" data-kt-value="dark">
                    <span class="menu-icon" data-kt-element="icon">
                        <i class="ki-duotone ki-moon fs-2">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                    </span>
                    <span class="menu-title">Dark</span>
                </a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3 my-0">
                <a href="#" class="menu-link px-3 py-2" data-kt-element="mode" data-kt-value="system">
                    <span class="menu-icon" data-kt-element="icon">
                        <i class="ki-duotone ki-screen fs-2">
                            <span class="path1"></span>
                            <span class="path2"></span>
                            <span class="path3"></span>
                            <span class="path4"></span>
                        </i>
                    </span>
                    <span class="menu-title">System</span>
                </a>
            </div>
            <!--end::Menu item-->
        </div>
        <!--end::Menu-->
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="menu-item px-5" data-kt-menu-trigger="{default: 'click', lg: 'hover'}" data-kt-menu-placement="left-start" data-kt-menu-offset="-15px, 0">
        <a href="#" class="menu-link px-5">
            <span class="menu-title position-relative">
                Language
                <span class="fs-8 rounded bg-light px-3 py-2 position-absolute translate-middle-y top-50 end-0">
                    English
                    <img class="w-15px h-15px rounded-1 ms-2" src="assets/media/flags/united-states.svg" alt="" />
                </span>
            </span>
        </a>
        <!--begin::Menu sub-->
        <div class="menu-sub menu-sub-dropdown w-175px py-4">
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/settings.html" class="menu-link d-flex px-5 active">
                <span class="symbol symbol-20px me-4">
                    <img class="rounded-1" src="assets/media/flags/united-states.svg" alt="" />
                </span>English</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/settings.html" class="menu-link d-flex px-5">
                <span class="symbol symbol-20px me-4">
                    <img class="rounded-1" src="assets/media/flags/spain.svg" alt="" />
                </span>Spanish</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/settings.html" class="menu-link d-flex px-5">
                <span class="symbol symbol-20px me-4">
                    <img class="rounded-1" src="assets/media/flags/germany.svg" alt="" />
                </span>German</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/settings.html" class="menu-link d-flex px-5">
                <span class="symbol symbol-20px me-4">
                    <img class="rounded-1" src="assets/media/flags/japan.svg" alt="" />
                </span>Japanese</a>
            </div>
            <!--end::Menu item-->
            <!--begin::Menu item-->
            <div class="menu-item px-3">
                <a href="account/settings.html" class="menu-link d-flex px-5">
                <span class="symbol symbol-20px me-4">
                    <img class="rounded-1" src="assets/media/flags/france.svg" alt="" />
                </span>French</a>
            </div>
            <!--end::Menu item-->
        </div>
        <!--end::Menu sub-->
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="menu-item px-5 my-1">
        <a href="account/settings.html" class="menu-link px-5">Account Settings</a>
    </div>
    <!--end::Menu item-->
    <!--begin::Menu item-->
    <div class="d-flex justify-content-center">
        <a href="javascript:void(0)" class="btn btn-light btn-light-primary fs-7 mt-2 w-85"
           onclick="Layout.lgout();"
           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="로그아웃합니다.">
            <#--begin::Svg Icon | path: assets/media/icons/duotune/arrows/arr076.svg-->
            <span class="svg-icon svg-icon-muted svg-icon-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <rect opacity="0.7" width="12" height="2" rx="1" transform="matrix(-1 0 0 1 15.5 11)" fill="black"/>
                    <path d="M13.6313 11.6927L11.8756 10.2297C11.4054 9.83785 11.3732 9.12683 11.806 8.69401C12.1957 8.3043 12.8216 8.28591 13.2336 8.65206L16.1592 11.2526C16.6067 11.6504 16.6067 12.3496 16.1592 12.7474L13.2336 15.3479C12.8216 15.7141 12.1957 15.6957 11.806 15.306C11.3732 14.8732 11.4054 14.1621 11.8756 13.7703L13.6313 12.3073C13.8232 12.1474 13.8232 11.8526 13.6313 11.6927Z" fill="black"/>
                    <path d="M8 5V6C8 6.55228 8.44772 7 9 7C9.55228 7 10 6.55228 10 6C10 5.44772 10.4477 5 11 5H18C18.5523 5 19 5.44772 19 6V18C19 18.5523 18.5523 19 18 19H11C10.4477 19 10 18.5523 10 18C10 17.4477 9.55228 17 9 17C8.44772 17 8 17.4477 8 18V19C8 20.1046 8.89543 21 10 21H19C20.1046 21 21 20.1046 21 19V5C21 3.89543 20.1046 3 19 3H10C8.89543 3 8 3.89543 8 5Z" fill="#C4C4C4"/>
                </svg>
            </span>
            <#--end::Svg Icon-->
            <span class="fw-bolder" style="letter-spacing :0.6px;">로그아웃</span>
        </a>
    </div>
    <!--end::Menu item-->
</div>
<#--end::user drawer-->