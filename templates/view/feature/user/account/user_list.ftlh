<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 목록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/user/account/_user_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table"><@spring.message 'txt.user.list.column.number'/></th>
                    <th class="text-center wb-keepall hidden-table"><@spring.message 'txt.user.list.column.role'/></th>
                    <th class="col-2 text-center wb-keepall"><@spring.message 'txt.user.list.column.name-username'/></th>
                    <th class="col-1 text-center wb-keepall hidden-table"><@spring.message 'txt.user.list.column.affiliation'/></th>
                    <th class="col-1 text-center wb-keepall hidden-table"><@spring.message 'txt.user.list.header.team'/></th>
                    <th class="col-1 text-center wb-keepall"><@spring.message 'txt.user.list.header.rank'/></th>
                    <th class="col-3 text-center wb-keepall hidden-table">E-MAIL</th>
                    <th class="text-center wb-keepall hidden-table"><@spring.message 'txt.user.list.column.locked-yn'/></th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody id="user_list_div">
                <#-- Vue(UserListApp)가 행을 렌더링 -->
            </tbody>
        </table>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <div id="user_pagination_div"></div>
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" />
</form>

<script id="user_list_data" type="application/json">
[
<#list userList![] as user>
    <#assign divideRetireYn = prevRetireYn?? && user.retireYn! != prevRetireYn>
    <#assign divide = divideRetireYn>
    {
        "rnum": ${user.rnum!0},
        "id": ${user.id!0},
        "isMe": ${(user.isMe!false)?c},
        "userRoles": [
            <#list user.userRoles![] as role>
                {
                    "roleKey": "${(role.roleKey!'')?js_string}",
                    "roleName": "${(role.roleName!'')?js_string}"
                }<#if role_has_next>,</#if>
            </#list>
        ],
        "profileImageUrl": "${(user.profileImageUrl!'')?js_string}",
        "userNm": "${(user.userNm!'')?js_string}",
        "username": "${(user.username!'')?js_string}",
        "userProflYn": "${(user.userProflYn!'Y')?js_string}",
        "retireYn": "${(user.retireYn!'N')?js_string}",
        "cmpyNm": "${(user.cmpyNm!'')?js_string}",
        "teamNm": "${(user.teamNm!'')?js_string}",
        "rankNm": "${(user.rankNm!'')?js_string}",
        "apntcYn": "${(user.apntcYn!'N')?js_string}",
        "email": "${(user.email!'')?js_string}",
        "isLocked": ${(user.isLocked!false)?c},
        "divide": ${(divide!false)?c}
    }<#if user_has_next>,</#if>
    <#-- 계정 승인여부 상태 저장 -->
    <#assign prevRetireYn = user.retireYn! />
</#list>
]
</script>

<script id="user_pagination_data" type="application/json">
{
    "currPageNo": ${paginationInfo.currPageNo!1},
    "lastPageNo": ${paginationInfo.lastPageNo!1},
    "totalCnt": ${paginationInfo.totalCnt!0},
    "pageSize": ${searchParam.pageSize!10},
    "isFirstPage": ${(paginationInfo.isFirstPage!false)?c},
    "isLastPage": ${(paginationInfo.isLastPage!false)?c},
    "prevPageNo": ${paginationInfo.prevPageNo!0},
    "nextPageNo": ${paginationInfo.nextPageNo!0}
}
</script>

<script id="user_label_data" type="application/json">
{
    "noProfile": "${springMacroRequestContext.getMessage('bs.tooltip.user.list.no-profile')?js_string}",
    "retired": "${springMacroRequestContext.getMessage('bs.tooltip.user.list.retired')?js_string}",
    "activeEmployee": "${springMacroRequestContext.getMessage('bs.tooltip.user.list.active-employee')?js_string}",
    "probation": "${springMacroRequestContext.getMessage('txt.user.list.probation')?js_string}",
    "locked": "${springMacroRequestContext.getMessage('txt.user.list.locked')?js_string}",
    "use": "${springMacroRequestContext.getMessage('txt.comm.use')?js_string}",
    "emptyList": "${springMacroRequestContext.getMessage('view.list.empty')?js_string}",
    "totalPrefix": "Total"
}
</script>

<div id="user_app" class="d-none"></div>
<script type="module" src="/vue/feature/user/account/UserListApp.js?${releaseDate!}"></script>

</@layout.layout>
