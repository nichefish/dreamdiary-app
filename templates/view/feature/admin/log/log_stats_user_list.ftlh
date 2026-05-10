<#--
 * 로그 > 사용자별 통계 placeholder 화면
 * 아직 서버 기능이 완성되지 않은 화면이므로, 전달된 데이터가 있으면 Vue가 그대로 표시한다.
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/admin/log/_log_stats_user_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<script id="log_stats_user_list_data" type="application/json">
[
<#list logStatsUserList![] as user>
    {
        "rnum": ${user.rnum!0},
        "userNm": "${(user.userNm!'-')?js_string}",
        "username": "${(user.username!'-')?js_string}",
        "userProflYn": "${(user.userProflYn!'Y')?js_string}",
        "retireYn": "${(user.retireYn!'N')?js_string}",
        "roleKey": "${(user.roleKey!'')?js_string}",
        "roleName": "${(user.roleName!'-')?js_string}",
        "actvtyCnt": ${user.actvtyCnt!0},
        "url": "${(user.url!'-')?js_string}",
        "param": "${(user.param!'-')?js_string}",
        "content": "${(user.content!'-')?js_string}",
        "rslt": "${(user.rslt!'false')?js_string}"
    }<#if user_has_next>,</#if>
</#list>
]
</script>

<script id="log_stats_not_user_list_data" type="application/json">
[
<#list logStatsNotUserList![] as user>
    {
        "rnum": ${user.rnum!0},
        "userNm": "${(user.userNm!'-')?js_string}",
        "username": "${(user.username!'-')?js_string}",
        "userProflYn": "${(user.userProflYn!'Y')?js_string}",
        "retireYn": "${(user.retireYn!'N')?js_string}",
        "roleKey": "${(user.roleKey!'')?js_string}",
        "roleName": "${(user.roleName!'-')?js_string}",
        "actvtyCnt": ${user.actvtyCnt!0},
        "url": "${(user.url!'-')?js_string}",
        "param": "${(user.param!'-')?js_string}",
        "content": "${(user.content!'-')?js_string}",
        "rslt": "${(user.rslt!'false')?js_string}"
    }<#if user_has_next>,</#if>
</#list>
]
</script>

<div class="card post">
    <div class="card-body">
        <div class="alert alert-secondary py-4 mb-6">
            사용자별 로그 통계는 아직 구현 중입니다. 현재 서버에서 내려오는 데이터만 임시 표로 표시합니다.
        </div>
        <table class="table table-hover align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <thead>
                <tr class="text-start text-muted fs-7 text-uppercase gs-0">
                    <th class="col-xl-1 col-4 text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-3 min-w-125px text-center wb-keepall hidden-table">작업자</th>
                    <th class="col-xl-2 col-5 min-w-80px text-center wb-keepall">권한</th>
                    <th class="col-xl-2 col-1 text-center wb-keepall hidden-table">작업 건수</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업 URL</th>
                    <th class="col-xl-4 col-5 min-w-80px text-center wb-keepall">작업 내용</th>
                    <th class="col-xl-1 text-center wb-keepall hidden-table">작업 결과</th>
                </tr>
            </thead>
            <tbody id="log_stats_user_list_body" class="text-gray-600">
                <#-- Vue(LogStatsUserPlaceholderApp)가 placeholder 행을 렌더링 -->
            </tbody>
        </table>
    </div>
    <div class="card-footer">
        <#include "/view/global/_pagination.ftlh">
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="logActvtyId" id="logActvtyId" />
</form>

<div id="log_stats_user_app" class="d-none"></div>
<script type="module" src="/vue/feature/admin/log/LogStatsUserPlaceholderApp.js?${releaseDate!}"></script>

</@layout.layout>
