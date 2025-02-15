<#--
  -- 활동 로그 목록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/log/stats/_log_stats_user_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table table-hover align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start text-muted fs-7 text-uppercase gs-0">
                    <th class="col-xl-1 col-4 text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-3 min-w-125px text-center wb-keepall hidden-table">작업자</th>
                    <th class="col-xl-2 col-5 min-w-80px text-center wb-keepall">권한</th>
                    <th class="col-xl-2 col-1 text-center wb-keepall hidden-table">기간내 작업 건수</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업URL</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업내용</th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table">작업결과</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="text-gray-600">
                <#list logStatsUserList![] as user>
                    <tr >
                        <td class="text-center hidden-table">${user.rnum!}</td>
                        <td class="text-start ps-10 hidden-table">
                            <span class="fw-bold">${user.userNm!"-"}</span>
                            <span class="ms-1">(${user.userId!})</span>
                            <#if (user.userProflYn!"Y") == "N">
                                <i class="bi bi-person-dash vertical-middle text-muted"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="직원정보가 없습니다."></i>
                            <#elseif (user.retireYn!"N") == "Y">
                                <i class="bi bi-person-dash vertical-middle text-danger"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="퇴사한 인원입니다."></i>
                            <#else>
                                <i class="bi bi-person-check vertical-middle text-success"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="소속된 직원입니다."></i>
                            </#if>
                        </td>
                        <td class="text-center">
                            ${user.authNm!"-"}
                            <#if (user.authCd! == Constant.AUTH_MNGR)>
                                <i class="bi bi-person-lines-fill text-info ms-1 opacity-75"></i>   <!-- 관리자 -->
                            <#elseif (user.authCd! == Constant.AUTH_USER)>
                                <i class="bi bi-people-fill ms-1"></i>        <!-- 사용자 -->
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            총 ${user.actvtyCnt!0?string.number}건
                        </td>
                        <td class="text-center">
                            ${user.url!}
                        </td>
                        <td class="text-center">
                            ${user.cn!"-"}
                        </td>
                        <!--begin::actionTyCd=-->
                        <td class="text-center text-end hidden-table">
                            <#if (user.rslt!"false") == "true">
                                <span class="text-success">
                                    성공<i class="bi bi-check text-success fs-8"></i>
                                </span>
                            <#else>
                                <span class="text-danger">
                                    실패<i class="bi bi-x text-danger fs-8"></i>
                                </span>
                            </#if>
                        </td>
                        <!--end::actionTyCd=-->
                    </tr>
                <#else>
                    <tr>
                        <td colspan="7" class="text-center"><@spring.message 'view.list.empty'/></td>
                    </tr>
                </#list>
                <#if logStatsNotUserList?has_content>
                    <tr class="border-top-5"></tr>
                </#if>
                <#list logStatsNotUserList![] as user>
                    <tr >
                        <td class="text-center hidden-table">${user.rnum!}</td>
                        <td class="text-start ps-10 hidden-table">
                            <span class="fw-bold">${user.userNm!"-"}</span>
                            <span class="ms-1">(${user.userId!"-"})</span>
                        </td>
                        <td class="text-center hidden-table">
                            총 ${user.actvtyCnt!0?string.number}건
                        </td>
                        <td class="text-center">
                            ${user.url!}
                        </td>
                        <td class="text-center">
                            ${user.param!"-"}
                        </td>
                        <td class="text-center">
                            ${user.cn!"-"}
                        </td>
                        <!--begin::actionTyCd=-->
                        <td class="text-center text-end hidden-table">
                            <#if (user.rslt!"false") == "true">
                                <span class="text-success">
                                    성공<i class="bi bi-check text-success fs-8"></i>
                                </span>
                            <#else>
                                <span class="text-danger">
                                    실패<i class="bi bi-x text-danger fs-8"></i>
                                </span>
                            </#if>
                        </td>
                        <!--end::actionTyCd=-->
                    </tr>
                </#list>
            </tbody>
            <!--end::Table body-->
        </table>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="logActvtyNo" id="logActvtyNo" />
</form>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/admin/log/stats/log_stats_user_list.js?${releaseDate!}"></script>

</@layout.layout>