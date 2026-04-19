<#--
 * 로그 > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/admin/log/_log_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">
<div class="card post">
    <div class="card-body">
        <table class="table table-hover align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <thead>
                <tr class="text-start text-muted fs-7 text-uppercase gs-0">
                    <th class="col-xl-1 text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-xl-2 col-4 text-center wb-keepall w-10">작업일시</th>
                    <th class="col-xl-2 col-3 min-w-125px text-center wb-keepall">작업자</th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table">작업IP</th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table">활동 유형</th>
                    <th class="col-xl-3 col-5 min-w-80px text-center wb-keepall">작업URL</th>
                    <th class="col-xl-3 col-5 min-w-80px text-center wb-keepall">작업결과 메세지</th>
                    <th class="col-xl-1 col-1 text-center wb-keepall">작업결과</th>
                </tr>
            </thead>
            <tbody class="text-gray-600">
                <#list logList![] as log>
                    <tr class="cursor-pointer <#if log.isJobUser!false>bg-fxd-list</#if>" onclick="dF.Log.dtlModal('${log.id!}');">
                        <td class="text-center hidden-table">${log.rnum!}</td>
                        <td class="text-center">${log.logDt!"-"}</td>
                        <td class="text-center hidden-table"><div>${log.logUserNm!"-"}</div><div>(${log.username!})</div></td>
                        <td class="text-center hidden-table">${log.ipAddr!"-"}</td>
                        <td class="text-center hidden-table">${log.actionTyNm!"-"}</td>
                        <td class="text-start ps-4">${log.requestUri!}</td>
                        <td class="text-center">${log.rsltMsg!"-"}</td>
                        <td class="text-center text-end hidden-table"><#if (log.rslt!"false") == "true"><span class="text-success">성공</span><#else><span class="text-danger">실패</span></#if></td>
                    </tr>
                <#else>
                    <tr><td colspan="8" class="text-center"><@spring.message 'view.list.empty'/></td></tr>
                </#list>
            </tbody>
        </table>
    </div>
    <div class="card-footer">
        <#include "/view/global/_pagination.ftlh">
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="logId" id="logId" />
</form>

<#include "/view/feature/admin/log/_modal/_log_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/admin/log/log_module.js"></script>
<script type="text/javascript" src="/js/view/feature/admin/log/log_list.js"></script>
</@layout.layout>
