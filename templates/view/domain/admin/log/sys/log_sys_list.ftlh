<#--
  -- 시스템 로그(log_sys) > 목록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/log/sys/_log_sys_list_header.ftlh">
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
                    <th class="col-xl-2 col-4 text-center wb-keepall w-10">작업일시</th>
                    <th class="col-xl-2 col-3 min-w-125px text-center wb-keepall hidden-table">작업자</th>
                    <th class="col-xl-2 col-1 text-center wb-keepall hidden-table">작업IP</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업URL</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업 파라미터</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업내용</th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table">작업결과</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="text-gray-600">
                <#list logSysList![] as log>
                    <tr <#if log.isJobUser!false>class="bg-fxd-list"</#if>>
                        <td class="text-center hidden-table">${log.rnum!}</td>
                        <td class="text-center">
                            ${log.logDt!}
                        </td>
                        <td class="text-center hidden-table">
                            <div>
                                ${log.logUserNm!"-"}
                                <i class="bi bi-gear s-1 opacity-75"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="시스템 계정입니다."></i>
                            </div>
                            <div>(${log.userId!})</div>
                            <#if log.isRegstr!false>
                                <span class="badge badge-secondary opacity-75 mx-1 fs-9">나</span>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            -
                        </td>
                        <td class="text-center">
                            ${log.url!}
                        </td>
                        <td class="text-center">
                            -
                        </td>
                        <td class="text-center">
                            ${log.cn!"-"}
                        </td>
                        <!--begin::actionTyCd=-->
                        <td class="text-center text-end hidden-table">
                            <#if (log.rslt!"false") == "true">
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
            </tbody>
            <!--end::Table body-->
        </table>
        <!--end::Table-->
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
    <input type="hidden" name="logSysNo" id="logSysNo" />
</form>

<#--include::log_actvty_module-->
<script type="text/javascript" src="/js/view/domain/admin/log/sys/log_sys_module.js"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/admin/log/sys/log_sys_list.js"></script>

</@layout.layout>