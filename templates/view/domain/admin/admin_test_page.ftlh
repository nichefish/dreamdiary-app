<#--
 * 테스트 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<script defer="defer" src="/static/node_modules/sass/sass.js"></script>

<@layout.layout toolbar=toolbar!"">

<!--begin::Card-->
<div class="card post">
    <#--begin::Tab-->
    <ul class="nav nav-tabs nav-tabs-line ps-5 my-5">
        <li class="nav-item">
            <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_1" onclick="Page.adminPage();">
                <span class="nav-icon">
                    <i class="bi bi-sort-down-alt"></i>
                </span>
                <span class="nav-text">사이트 관리</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_2" onclick="Page.cdList();">
                <span class="nav-icon">
                    <i class="bi bi-people"></i>
                </span>
                <span class="nav-text">코드 관리</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_3" onclick="Page.logStatsUserList();">
                <span class="nav-icon">
                    <i class="bi bi-grid-1x2"></i>
                </span>
                <span class="nav-text">기능별</span>
            </a>
        </li>
    </ul>

    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Row-->
        <div class="row mb-8">
            <div class="col-xl-2">
                <div class="fs-6 fw-bold col-form-label"><label>사이트 캐시 관리</label></div>
            </div>
            <div class="col-xl-3">
                <div class="mt-1">
                    <button type="button" class="btn btn-sm btn-primary" onclick="Page.ajax('${Url.CACHE_ACTIVE_LIST_AJAX!}');">
                        현재 캐시 조회
                    </button>
                    <button type="button" class="btn btn-sm btn-primary" onclick="Page.ajax('${Url.CACHE_CLEAR_AJAX!}');">
                        사이트 캐시 초기화
                    </button>
                </div>
            </div>
            <div class="col-xl-6 text-noti">
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
                <button type="button" class="btn btn-sm btn-primary" onclick="Page.hldyAjax();">
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
                <button type="button" class="btn btn-sm btn-primary" onclick="Page.notionAjax();">
                    NOTION 데이터 조회
                </button>
            </div>
        </div>
    </div>
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">
    <!-- -->
</form>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/admin/admin_test_page.js?${releaseDate!}"></script>

</@layout.layout>