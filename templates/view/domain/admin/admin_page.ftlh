<#--
 * 사이트 관리
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<script defer="defer" src="/static/node_modules/sass/sass.js"></script>

<@layout.layout toolbar=toolbar!"">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                //
            },

            /**
             * 휴일정보 : url, data 받아서 ajax 호출
             */
            hldyAjax: function() {
                const url = "${Url.API_HLDY_GET!}";
                const ajaxData = { "yy" : $("#hldyYy option:selected").val() };
                Page.ajax(url, ajaxData);
            },

            /**
             * NOTION : url, data 받아서 ajax 호출
             */
            notionAjax: function() {
                const url = "${Url.API_NOTION_GET!}";
                const ajaxData = { "dataType": $("#dataType").val(), "dataId": $("#dataId").val() };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (res.rslt) Swal.fire(JSON.stringify(res));
                    Swal.fire({ text: res.message });
                });
            },

            /**
             * url, data 받아서 ajax 호출
             * @param {string} url - 요청을 보낼 대상 URL.
             * @param {Object} data - 서버로 전송할 데이터 객체.
             */
            ajax: function(url, data) {
                cF.util.blockUIAjax(url, 'POST', data, function(res) {
                    if (res.rslt) Swal.fire(JSON.stringify(res));
                    Swal.fire({ text: res.message })
                        .then(function() {
                            if (res.rsltList) Swal.fire(JSON.stringify(res.rsltList));
                            if (res.rsltObj) Swal.fire(JSON.stringify(res.rsltObj));
                        });
                });
            },

            /**
             * 사이트 관리 페이지로 이동
             */
            adminPage: function() {
                cF.util.blockUISubmit("#procForm", "${Url.ADMIN_PAGE!}");
            },

            /**
             * 코드 관리 페이지로 이동
             */
            cdList: function() {
                cF.util.blockUISubmit("#procForm", "${Url.CL_CD_LIST!}");
            },
        }
    })();
</script>

<#--begin::Tab-->
<ul class="nav nav-tabs nav-tabs-line ps-5 my-5">
    <li class="nav-item">
        <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_1"
           onclick="Page.adminPage();">
        <span class="nav-icon">
            <i class="bi bi-sort-down-alt"></i>
        </span>
            <span class="nav-text">사이트 관리</span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_2"
           onclick="Page.cdList();">
        <span class="nav-icon">
            <i class="bi bi-people"></i>
        </span>
            <span class="nav-text">코드 관리</span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_3"
           onclick="Page.logStatsUserList();">
        <span class="nav-icon">
            <i class="bi bi-grid-1x2"></i>
        </span>
            <span class="nav-text">기능별</span>
        </a>
    </li>
</ul>
<!--begin::Card-->
<div class="row g-5 gx-xl-10 mb-5 mb-xl-10">
    <!--begin::Col-->
    <div class="col-md-6 col-lg-6 col-xl-6 col-xxl-7 mb-md-5 mb-xl-10">
        <div class="card post">
            <#--begin::Row-->
            <div class="row mb-8">
                <div class="col-xl-2">
                    <div class="fs-6 fw-bold col-form-label"><label>사이트 캐시 관리</label></div>
                </div>
                <div class="col-xl-3">
                    <div class="mt-1">
                        <button type="button" class="btn btn-sm btn-primary"
                                onclick="Cache.activeListModal();">
                            현재 캐시 조회
                        </button>
                        <button type="button" class="btn btn-sm btn-primary"
                                onclick="Cache.clearAllAjax();">
                            사이트 캐시 초기화
                        </button>
                    </div>
                </div>
                <div class="col-xl-7 text-noti">
                    <p class="mb-0">※ 캐시 처리되는 정보들(ex.코드 및 메뉴 등) 중</p>
                    <p class="mb-0 ps-4">시스템을 통하지 않은(ex.DB 직접입력) 변동사항이 발생했을 경우 수동 업데이트</p>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-8">
                <div class="col-xl-2">
                    <div class="fs-6 fw-bold col-form-label"><label for="hldyYy">한국천문연구원 특일정보 API</label></div>
                </div>
                <div class="col-xl-2">
                    <select name="hldyYy" id="hldyYy" class="form-select form-select-solid">
                        <#assign currYy = DateUtils.getCurrYyStr()!>
                        <#assign prevYy = ((currYy?number)-1)?c>
                        <#assign nextYy = ((currYy?number)+1)?c>
                        <option value="${prevYy!}">${prevYy!}</option>
                        <option value="${currYy!}" selected="selected">${currYy!}</option>
                        <option value="${nextYy!}">${nextYy!}</option>
                    </select>
                </div>
                <div class="col-xl-2 mt-1">
                    <button type="button" class="btn btn-sm btn-primary"
                            onclick="Page.hldyAjax();">
                        휴일정보 조회+저장
                    </button>
                </div>
                <div class="col-xl-6 col-form-label text-noti">
                    ※ 해당 년도 공휴일 정보 API 조회 :: 시스템에서 누락된 경우 수동 업데이트
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-8">
                <div class="col-xl-2">
                    <div class="fs-6 fw-bold col-form-label"><label for="hldyYy">NOTION API 테스트</label></div>
                </div>
                <div class="col-xl-2">
                    <select name="dataType" id="dataType" class="form-select form-select-solid">
                        <option value="PAGE" selected>PAGE</option>
                        <option value="BLOCK">BLOCK</option>
                        <option value="BLOCKS">BLOCKS</option>
                        <option value="DATABASE">DATABASE</option>
                    </select>
                </div>
                <div class="col-xl-3">
                    <input type="text" name="dataId" id="dataId" class="form-control form-control-solid no-space"
                           value="" maxlength="64" />
                </div>
                <div class="col-xl-2 mt-1">
                    <button type="button" class="btn btn-sm btn-primary"
                            onclick="Page.notionAjax();">
                        NOTION 데이터 조회
                    </button>
                </div>
            </div>
        </div>
    </div>
    <!--begin::Col-->
    <div class="col-md-6 col-lg-6 col-xl-6 col-xxl-5 mb-md-5 mb-xl-10">
        <div class="card post">
            <!--begin::Card widget 20-->
            <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed mb-3">
                <#--begin::Table head-->
                <thead>
                    <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                        <th class="col-2 text-center wb-keepall">권한 코드</th>
                        <th class="col-2 text-center wb-keepall">권한명 (level)</th>
                        <th class="col-3 text-center wb-keepall">상위 권한</th>
                        <th class="col-2 text-center wb-keepall">정렬 순서</th>
                        <th class="col-2 text-center wb-keepall">사용 여부</th>
                    </tr>
                </thead>
                <#--begin::Table body-->
                <tbody>
                    <#list authRoleList![] as authRole>
                        <tr>
                            <td class="text-center hidden-table">
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
                                        <!-- 사용자 -->
                                        <i class="bi bi-person-fill-gear text-info fs-2 me-2 opacity-75"
                                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="${authRole.authNm!"-"}"></i>
                                        <div class="text-info fw-bold">${authRole.authNm!}</div>
                                        <div class="badge badge-info ms-3">${authRole.authLevel!}</div>
                                    </#if>
                                </div>
                            </td>
                            <td class="text-start ps-10 hidden-table">
                                ${authRole.topAuthCd!}
                            </td>
                            <td class="text-center hidden-table">
                                ${authRole.state.sortOrdr!}
                            </td>
                            <td class="text-center hidden-table">
                                ${authRole.state.useYn!}
                            </td>
                        </tr>
                    <#else>
                        <tr>
                            <td colspan=8" class="text-center"><@spring.message "view.list.empty"/></td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">

</form>

<#--include:: Dream - Aside-->
<#include "/view/domain/admin/admin_aside_base.ftlh">

<#--include::modals-->
<#include "/view/domain/admin/cache/_cache_list_modal.ftlh">

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/admin/cache/_cache.js?${releaseDate!}"></script>

</@layout.layout>