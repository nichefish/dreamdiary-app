<#--
 * 로그 > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/admin/log/_log_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">
<script id="log_list_data" type="application/json">
[
<#list logList![] as log>
    {
        "id": ${log.id!0},
        "rnum": ${log.rnum!0},
        "logDt": "${(log.logDt!'-')?js_string}",
        "logUserNm": "${(log.logUserNm!'-')?js_string}",
        "username": "${(log.username!'-')?js_string}",
        "ipAddr": "${(log.ipAddr!'-')?js_string}",
        "actionTyNm": "${(log.actionTyNm!'-')?js_string}",
        "requestUri": "${(log.requestUri!'-')?js_string}",
        "rsltMsg": "${(log.rsltMsg!'-')?js_string}",
        "rslt": ${(log.rslt!false)?c},
        "isJobUser": ${(log.isJobUser!false)?c}
    }<#if log_has_next>,</#if>
</#list>
]
</script>

<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>

<div class="card post">
    <div class="card-body">
        <table class="table table-hover align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <thead>
                <tr class="text-start text-muted fs-7 text-uppercase gs-0">
                    <th class="col-xl-1 text-center wb-keepall w-10 hidden-table"><@spring.message 'txt.admin.log.list.number'/></th>
                    <th class="col-xl-2 col-4 text-center wb-keepall w-10"><@spring.message 'txt.admin.log.list.action-datetime'/></th>
                    <th class="col-xl-2 col-3 min-w-125px text-center wb-keepall"><@spring.message 'txt.admin.log.list.actor'/></th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table"><@spring.message 'txt.admin.log.list.action-ip'/></th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table"><@spring.message 'txt.admin.log.list.action-type'/></th>
                    <th class="col-xl-3 col-5 min-w-80px text-center wb-keepall"><@spring.message 'txt.admin.log.list.action-url'/></th>
                    <th class="col-xl-3 col-5 min-w-80px text-center wb-keepall"><@spring.message 'txt.admin.log.list.result-message'/></th>
                    <th class="col-xl-1 col-1 text-center wb-keepall"><@spring.message 'txt.admin.log.list.action-result'/></th>
                </tr>
            </thead>
            <tbody id="log_list_body" class="text-gray-600">
                <#-- Vue(LogAdminApp)가 행을 렌더링 -->
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

<#include "/view/feature/admin/log/_modal/_log_detail_modal.ftlh">
<div id="log_admin_app" class="d-none"></div>
<script type="module" src="/vue/feature/admin/log/LogAdminApp.js?${releaseDate!}"></script>
</@layout.layout>
