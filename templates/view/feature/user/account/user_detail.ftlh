<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 상세 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div id="user_detail_div">
        <#-- Vue(UserDetailApp)가 상세 본문을 렌더링 -->
    </div>
    <#--페이지:: 사용자 프로필 정보 영역-->
    <#--페이지:: 사용자 인사정보 영역-->
    <div id="user_detail_profile_emplym_div">
        <#-- Vue(UserDetailApp)가 기존 _user_profile_detail_area.ftlh/_user_emplym_detail_area.ftlh를 대체 -->
    </div>
    <#--begin::Card footer-->
    <div id="user_detail_footer_div">
        <#-- Vue(UserDetailApp)가 상세 footer 버튼을 렌더링 -->
    </div>
    <!--end::Card footer-->
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" value="${user.id!}">
    <#if user.profile??>
        <input type="hidden" name="userProfileId" value="${user.profile.userProfileId!}">
    </#if>
</form>

<script id="user_detail_data" type="application/json">
{
    "id": ${user.id!0},
    "username": "${(user.username!'')?js_string}",
    "profileImageUrl": "${(user.profileImageUrl!'')?js_string}",
    "createdBy": "${(user.createdBy!'')?js_string}",
    "createdAt": "${(user.createdAt!'')?js_string}",
    "userRoles": [
        <#list user.userRoles![] as role>
            {
                "roleKey": "${(role.roleKey!'')?js_string}",
                "roleName": "${(role.roleName!'')?js_string}"
            }<#if role_has_next>,</#if>
        </#list>
    ],
    "nickname": "${(user.nickname!'')?js_string}",
    "email": "${(user.email!'')?js_string}",
    "phoneNumber": "${(user.phoneNumber!'')?js_string}",
    "isLocked": ${(user.isLocked!false)?c},
    "useAllowedIp": ${(user.useAllowedIp!false)?c},
    "allowedIpList": [
        <#list user.allowedIpList![] as item>
            {
                "id": ${item.id!0},
                "userId": ${item.userId!0},
                "allowedIp": "${(item.allowedIp!'')?js_string}"
            }<#if item_has_next>,</#if>
        </#list>
    ],
    "content": "${(user.content!'')?js_string}",
    "userProfileId": <#if user.profile??>${user.profile.userProfileId!0}<#else>null</#if>,
    "profile": <#if user.profile??>{
        "userProfileId": <#if user.profile.userProfileId??>${user.profile.userProfileId}<#else>null</#if>,
        "addr": "${(user.profile.addr!'')?js_string}",
        "zipcode": "${(user.profile.zipcode!'')?js_string}",
        "brthdy": "${(user.profile.brthdy!'')?js_string}",
        "lunarYn": "${(user.profile.lunarYn!'N')?js_string}",
        "proflCn": "${(user.profile.proflCn!'')?js_string}"
    }<#else>null</#if>,
    "emplym": <#if user.emplym??>{
        "userNm": "${(user.emplym.userNm!'')?js_string}",
        "emplymEmail": "${(user.emplym.emplymEmail!'')?js_string}",
        "emplymPhoneNumber": "${(user.emplym.emplymPhoneNumber!'')?js_string}",
        "cmpyNm": "${(user.emplym.cmpyNm!'')?js_string}",
        "teamNm": "${(user.emplym.teamNm!'')?js_string}",
        "emplymNm": "${(user.emplym.emplymNm!'')?js_string}",
        "rankNm": "${(user.emplym.rankNm!'')?js_string}",
        "ecnyDt": "${(user.emplym.ecnyDt!'')?js_string}",
        "retireYn": "${(user.emplym.retireYn!'')?js_string}",
        "retireDt": "${(user.emplym.retireDt!'')?js_string}",
        "acntBank": "${(user.emplym.acntBank!'')?js_string}",
        "acntNo": "${(user.emplym.acntNo!'')?js_string}"
    }<#else>null</#if>
}
</script>

<script id="user_detail_label_data" type="application/json">
{
    "locked": "${springMacroRequestContext.getMessage('txt.user.list.locked')?js_string}",
    "use": "${springMacroRequestContext.getMessage('txt.status.use')?js_string}",
    "unuse": "${springMacroRequestContext.getMessage('txt.status.unuse')?js_string}",
    "modifyTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.page.mdf')?js_string}",
    "deleteTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.del')?js_string}",
    "listTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.page.list')?js_string}",
    "passwordResetTooltip": "비밀번호를 초기화합니다.\n인증 정책 관리 메뉴에서\n초기화 값을 설정할 수\n있습니다.",
    "profileAddress": "${springMacroRequestContext.getMessage('txt.user.profile.address')?js_string}",
    "profileBirthDate": "${springMacroRequestContext.getMessage('txt.user.profile.birth-date')?js_string}",
    "profileLunar": "${springMacroRequestContext.getMessage('txt.user.profile.lunar')?js_string}",
    "profileProfile": "${springMacroRequestContext.getMessage('txt.user.profile.profile')?js_string}",
    "emplymUserName": "${springMacroRequestContext.getMessage('txt.user.emplym.user-name')?js_string}",
    "emplymEmail": "${springMacroRequestContext.getMessage('txt.user.emplym.email')?js_string}",
    "emplymPhoneNumber": "${springMacroRequestContext.getMessage('txt.user.emplym.phone-number')?js_string}",
    "emplymAffiliation": "${springMacroRequestContext.getMessage('txt.user.emplym.affiliation')?js_string}",
    "emplymRank": "${springMacroRequestContext.getMessage('txt.user.emplym.rank')?js_string}",
    "emplymJoinDate": "${springMacroRequestContext.getMessage('txt.user.emplym.join-date')?js_string}",
    "emplymPayrollAccount": "${springMacroRequestContext.getMessage('txt.user.emplym.payroll-account')?js_string}"
}
</script>

<div id="user_detail_app" class="d-none"></div>
<#--include::script modules-->
<#-- Vue(UserDetailApp)가 기존 user_module.js의 상세 화면 동작을 대체 -->
<!--include::page scripts-->
<script type="module" src="/vue/feature/user/account/UserDetailApp.js?${releaseDate!}"></script>

</@layout.layout>
