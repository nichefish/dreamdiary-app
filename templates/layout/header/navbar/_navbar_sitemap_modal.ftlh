<!--
 * 레이아웃 > 사이트맵 Modal
 * @included_in : layout/topbar
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="sitemap" size="xl" module="">
    <#--begin::Modal Header-->
    <@component.modal_header "사이트맵" module! />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="sitemap_div">
            <div class="row mt-2 mb-6">
                <div class="col-4">
                    <h2 class="fw-bolder text-center text-gray-900 mb-0 me-1">
                        사용자<i class="bi bi-people-fill fs-3 ms-1"></i>
                    </h2>
                </div>
                <div class="col-4">
                    <h2 class="fw-bolder text-center text-gray-900 mb-0 me-1">
                        관리자<i class="bi bi-person-lines-fill fs-3 text-info ms-1 opacity-75"></i>
                    </h2>
                </div>
                <div class="col-4">
                    <h2 class="fw-bolder text-center text-gray-900 mb-0 me-1">
                        -
                    </h2>
                </div>
            </div>
            <div class="row mt-2 mb-6">
                <div class="col-4 px-5">
                    <div class="separator"></div>
                </div>
                <div class="col-4 px-5">
                    <div class="separator"></div>
                </div>
                <div class="col-4 px-5">
                    <div class="separator"></div>
                </div>
            </div>
            <div class="row mt-2 mb-6">
                <!-- 사용자 -->
                <div class="col-4 ps-5">
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                                    <span class="svg-icon svg-icon-3 svg-icon-gray-600">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell-fill" viewBox="0 0 16 16">
                                            <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z"></path>
                                        </svg>
                                    </span>
                        </div>
                        <div class="cursor-pointer <#if (listUrl! == Url.NOTICE_LIST!)>fw-bold border-bottom</#if>"
                             onclick="location.replace('${Url.NOTICE_LIST!}')">
                            공지사항
                        </div>
                        <#if (listUrl! == Url.NOTICE_LIST!)>
                            <div class="ms-2 cursor-help blink-fast"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="현재 위치입니다.">
                                <i class="bi bi-pin-map-fill blink-fast"></i>
                            </div>
                        </#if>
                    </div>
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                                    <span class="svg-icon svg-icon-3">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chat-square-text-fill" viewBox="0 0 16 16">
                                            <path d="M0 2a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-2.5a1 1 0 0 0-.8.4l-1.9 2.533a1 1 0 0 1-1.6 0L5.3 12.4a1 1 0 0 0-.8-.4H2a2 2 0 0 1-2-2V2zm3.5 1a.5.5 0 0 0 0 1h9a.5.5 0 0 0 0-1h-9zm0 2.5a.5.5 0 0 0 0 1h9a.5.5 0 0 0 0-1h-9zm0 2.5a.5.5 0 0 0 0 1h5a.5.5 0 0 0 0-1h-5z"></path>
                                        </svg>
                                    </span>
                        </div>
                        <div class="<#if (listUrl! == Url.BOARD_POST_LIST!)>fw-bold border-bottom</#if>">
                            게시판
                        </div>
                    </div>
                    <#list boardDefList![] as boardDef>
                        <div class="menu-item">
                            <a class="menu-link" href="${Url.BOARD_POST_LIST!}?boardDef=${boardDef.boardDef!}">
                                        <span class="menu-bullet">
                                            <span class="bullet bullet-dot"></span>
                                        </span>
                                <span class="menu-title">${boardDef.boardNm!}</span>
                            </a>
                        </div>
                    </#list>
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                                    <span class="svg-icon svg-icon-3">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-date-fill" viewBox="0 0 16 16">
                                            <path d="M4 .5a.5.5 0 0 0-1 0V1H2a2 2 0 0 0-2 2v1h16V3a2 2 0 0 0-2-2h-1V.5a.5.5 0 0 0-1 0V1H4V.5zm5.402 9.746c.625 0 1.184-.484 1.184-1.18 0-.832-.527-1.23-1.16-1.23-.586 0-1.168.387-1.168 1.21 0 .817.543 1.2 1.144 1.2z"></path>
                                            <path d="M16 14V5H0v9a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2zm-6.664-1.21c-1.11 0-1.656-.767-1.703-1.407h.683c.043.37.387.82 1.051.82.844 0 1.301-.848 1.305-2.164h-.027c-.153.414-.637.79-1.383.79-.852 0-1.676-.61-1.676-1.77 0-1.137.871-1.809 1.797-1.809 1.172 0 1.953.734 1.953 2.668 0 1.805-.742 2.871-2 2.871zm-2.89-5.435v5.332H5.77V8.079h-.012c-.29.156-.883.52-1.258.777V8.16a12.6 12.6 0 0 1 1.313-.805h.632z"></path>
                                        </svg>
                                    </span>
                        </div>
                        <div>
                            일정
                        </div>
                    </div>
                    <!-- 일정 > 전체 일정 확인 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_SCHDUL_CAL[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_SCHDUL_CAL[2]!}</span>
                        </a>
                    </div>
                    <!-- 일정 > 휴가계획서 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_VCATN_PAPR[3]!}">
                            <span class="menu-bullet">
                                <span class="bullet bullet-dot"></span>
                            </span>
                            <span class="menu-title">${SiteMenu.MENU_VCATN_PAPR[2]!}</span>
                        </a>
                    </div>
                    </div>
                </div>
                <!-- 관리자 -->
                <div class="col-4 ps-5">
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                            <i class="fas fa-user-cog text-gray-600 fs-3"></i>
                        </div>
                        <div>
                            사용자 관리
                        </div>
                    </div>
                    <!-- 사용자 관리 > 계정 및 권한 관리 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_user_profl[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_user_profl[2]!}</span>
                        </a>
                    </div>
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                            <i class="fas fa-umbrella-beach text-gray-600 fs-3"></i>
                        </div>
                        <div>
                            휴가 및 일정관리
                        </div>
                    </div>
                    <!-- 휴가관리 > 년도별 휴가관리 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_VCATN_STATS_YY[3]!}">
                            <span class="menu-bullet">
                                <span class="bullet bullet-dot"></span>
                            </span>
                            <span class="menu-title">${SiteMenu.MENU_VCATN_STATS_YY[2]!}</span>
                        </a>
                    </div>
                    <!-- 휴가관리 > 휴가사용일자 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_VCATN_SCHDUL[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_VCATN_SCHDUL[2]!}</span>
                        </a>
                    </div>
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                            <span class="svg-icon svg-icon-3">
                                <i class="fas fa-globe text-gray-600 fs-3"></i>
                            </span>
                        </div>
                        <div>
                            사이트 관리
                        </div>
                    </div>
                    <!-- 사이트 관리 > 사이트 관리 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_SITE_ADMIN[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_SITE_ADMIN[2]!}</span>
                        </a>
                    </div>
                    <!-- 사이트 관리 > 게시판 관리 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_BOARD_DEF[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_BOARD_DEF[2]!}</span>
                        </a>
                    </div>
                    <!-- 사이트 관리 > 로그인 정책 관리 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_LGN_POLICY[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_LGN_POLICY[2]!}</span>
                        </a>
                    </div>
                    <#if isDev!false>
                        <!-- 사이트 관리 > 템플릿 관리 -->
                        <div class="menu-item">
                            <a class="menu-link" href="${SiteMenu.MENU_TMPLAT[3]!}">
                                        <span class="menu-bullet">
                                            <span class="bullet bullet-dot"></span>
                                        </span>
                                <span class="menu-title">${SiteMenu.MENU_TMPLAT[2]!} (작업중)</span>
                            </a>
                        </div>
                    </#if>
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                                    <span class="svg-icon svg-icon-3">
                                        <i class="fa-solid fa-arrow-down-short-wide text-gray-600 fs-3"></i>
                                    </span>
                        </div>
                        <div>
                            로그 조회
                        </div>
                    </div>
                    <!-- 사이트 관리 > 활동 로그 조회 -->
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_LOG_ACTVTY[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_LOG_ACTVTY[2]!}</span>
                        </a>
                    </div>
                    <div class="menu-item">
                        <a class="menu-link" href="${SiteMenu.MENU_LOG_SYS[3]!}">
                                    <span class="menu-bullet">
                                        <span class="bullet bullet-dot"></span>
                                    </span>
                            <span class="menu-title">${SiteMenu.MENU_LOG_SYS[2]!}</span>
                        </a>
                    </div>
                </div>
                <!-- 공통 -->
                <div class="col-4 ps-5">
                    <div class="d-flex align-items-center">
                        <div class="btn btn-icon btn-active-light-primary position-relative w-30px h-30px w-md-40px h-md-40px">
                            <span class="svg-icon svg-icon-muted svg-icon-2qx">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path opacity="0.3" d="M7 20.5L2 17.6V11.8L7 8.90002L12 11.8V17.6L7 20.5ZM21 20.8V18.5L19 17.3L17 18.5V20.8L19 22L21 20.8Z" fill="currentColor"></path>
                                    <path d="M22 14.1V6L15 2L8 6V14.1L15 18.2L22 14.1Z" fill="currentColor"></path>
                                </svg>
                            </span>
                        </div>
                        <div>
                            파일 시스템
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>
