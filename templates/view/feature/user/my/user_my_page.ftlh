<#--
 * 내 정보(user_my) > 상세 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<#-- Model 값을 객체에 담아 typescript로 전달 -->
<script id="user_my_page_data" type="application/json">
{
    "errorMsg": "${(errorMsg!'')?js_string}",
    "user": {
        "id": <#if user.id??>${user.id}<#else>null</#if>,
        "username": "${(user.username!'')?js_string}",
        "nickname": "${(user.nickname!'')?js_string}",
        "email": "${(user.email!'')?js_string}",
        "phoneNumber": "${(user.phoneNumber!'')?js_string}",
        "profileImageUrl": "${(user.profileImageUrl!'')?js_string}",
        "userRoles": [
            <#list user.userRoles![] as role>
                {
                    "roleKey": "${(role.roleKey!'')?js_string}",
                    "roleName": "${(role.roleName!'')?js_string}"
                }<#if role_has_next>,</#if>
            </#list>
        ],
        "isAllowedIpY": ${(user.isAllowedIpY!false)?c},
        "allowedIpList": [
            <#list user.allowedIpList![] as item>
                {
                    "allowedIp": "${(item.allowedIp!'')?js_string}"
                }<#if item_has_next>,</#if>
            </#list>
        ],
        "userInfo": <#if user.userInfo??>{
            "userProfileId": <#if user.userInfo.userProfileId??>${user.userInfo.userProfileId}<#else>null</#if>,
            "cmpyNm": "${(user.userInfo.cmpyNm!'')?js_string}",
            "teamNm": "${(user.userInfo.teamNm!'')?js_string}",
            "emplymNm": "${(user.userInfo.emplymNm!'')?js_string}",
            "rankNm": "${(user.userInfo.rankNm!'')?js_string}",
            "rankCd": "${(user.userInfo.rankCd!'')?js_string}",
            "apntcYn": "${(user.userInfo.apntcYn!'N')?js_string}",
            "ecnyDt": "${(user.userInfo.ecnyDt!'')?js_string}",
            "retireYn": "${(user.userInfo.retireYn!'N')?js_string}",
            "retireDt": "${(user.userInfo.retireDt!'')?js_string}",
            "brthdy": "${(user.userInfo.brthdy!'')?js_string}",
            "acntBank": "${(user.userInfo.acntBank!'')?js_string}",
            "acntNo": "${(user.userInfo.acntNo!'')?js_string}",
            "itemList": [
                <#list user.userInfo.itemList![] as item>
                    {
                        "itemNm": "${(item.itemNm!'')?js_string}",
                        "itemCn": "${(item.itemCn!'')?js_string}",
                        "itemDc": "${(item.itemDc!'')?js_string}"
                    }<#if item_has_next>,</#if>
                </#list>
            ]
        }<#else>null</#if>
    },
    "vacation": {
        "visible": ${(authInfo.hasEcnyDt!false)?c},
        "statsYy": "${((vcatnYy.statsYy)!'')?js_string}",
        "bgnDt": "${((vcatnYy.bgnDt)!'')?js_string}",
        "endDt": "${((vcatnYy.endDt)!'')?js_string}",
        "total": "${(((vcatnStats.totYryc)!0)?string)?js_string}",
        "used": "${(((vcatnStats.exhsYryc)!0)?string)?js_string}",
        "remains": "${(((vcatnStats.remndrYryc)!0)?string)?js_string}",
        "tooltip": "기본연차: ${(((vcatnStats.bsYryc)!0)?string)?js_string}<#if ((vcatnStats.isNewbie)!false)>(+${(((vcatnStats.newbieYryc)!0)?string)?js_string})</#if>일<#if ((vcatnStats.cnwkYryc)!0) gt 0>, \n근속 추가연차: ${(((vcatnStats.cnwkYryc)!0)?string)?js_string}일</#if><#if ((vcatnStats.prjctYryc)!0) gt 0>, \n프로젝트 추가연차: ${(((vcatnStats.prjctYryc)!0)?string)?js_string}일</#if>"
    },
    "labels": {
        "username": "${springMacroRequestContext.getMessage('txt.user.my.username')?js_string}",
        "password": "${springMacroRequestContext.getMessage('txt.user.my.password')?js_string}",
        "passwordChange": "${springMacroRequestContext.getMessage('txt.user.my.password-change')?js_string}",
        "role": "${springMacroRequestContext.getMessage('txt.user.my.role')?js_string}",
        "nickname": "${springMacroRequestContext.getMessage('txt.user.my.nickname')?js_string}",
        "allowedIp": "${springMacroRequestContext.getMessage('txt.user.my.allowed-ip')?js_string}",
        "use": "${springMacroRequestContext.getMessage('txt.status.use')?js_string}",
        "unuse": "${springMacroRequestContext.getMessage('txt.status.unuse')?js_string}",
        "department": "${springMacroRequestContext.getMessage('txt.user.my.department')?js_string}",
        "rank": "${springMacroRequestContext.getMessage('txt.user.my.rank')?js_string}",
        "probation": "${springMacroRequestContext.getMessage('txt.user.my.probation')?js_string}",
        "joinDate": "${springMacroRequestContext.getMessage('txt.user.my.join-date')?js_string}",
        "retireDate": "${springMacroRequestContext.getMessage('txt.user.my.retire-date')?js_string}",
        "phoneNumber": "${springMacroRequestContext.getMessage('txt.user.my.phone-number')?js_string}",
        "email": "${springMacroRequestContext.getMessage('txt.user.my.email')?js_string}",
        "birthDate": "${springMacroRequestContext.getMessage('txt.user.my.birth-date')?js_string}",
        "accountNumber": "${springMacroRequestContext.getMessage('txt.user.my.account-number')?js_string}",
        "additionalInfo": "${springMacroRequestContext.getMessage('txt.user.my.additional-info')?js_string}",
        "vacationTitleSuffix": "${springMacroRequestContext.getMessage('txt.user.my.vacation-title-suffix')?js_string}",
        "totalVacation": "${springMacroRequestContext.getMessage('txt.user.my.total-vacation')?js_string}",
        "usedVacation": "${springMacroRequestContext.getMessage('txt.user.my.used-vacation')?js_string}",
        "remainsVacation": "${springMacroRequestContext.getMessage('txt.user.my.remains-vacation')?js_string}",
        "currentPassword": "${springMacroRequestContext.getMessage('txt.user.my.current-password')?js_string}",
        "newPassword": "${springMacroRequestContext.getMessage('txt.user.my.new-password')?js_string}",
        "newPasswordConfirm": "${springMacroRequestContext.getMessage('txt.user.my.new-password-confirm')?js_string}",
        "passwordReq": "${springMacroRequestContext.getMessage('txt.req.password')?js_string}",
        "save": "저장",
        "close": "닫기",
        "uploadProfileImageTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.user.my.upload-profile-image')?js_string}",
        "removeProfileImageTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.user.my.remove-profile-image')?js_string}",
        "passwordChangeTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.user.my.password-change')?js_string}",
        "tooltipSave": "${springMacroRequestContext.getMessage('bs.tooltip.save')?js_string}",
        "tooltipClose": "${springMacroRequestContext.getMessage('bs.tooltip.modal.close')?js_string}",
        "changedProfileNotice": "${springMacroRequestContext.getMessage('msg.user.my.changed-profile-notice')?js_string}"
    }
}
</script>

<div id="user_my_app"></div>
<div id="user_my_page_div">
    <#-- Vue(UserMyPageApp)가 내 정보 화면을 렌더링 -->
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" value="${user.id!}">
    <#if user.userInfo??>
        <input type="hidden" name="userProfileId" value="${user.userInfo.userProfileId!}">
    </#if>
</form>
<div id="proflImageTemplate" hidden>
    <#--begin::Form-->
    <form name="profllImgForm" id="profllImgForm" class="form" enctype="multipart/form-data">
        <input type="file" name="fileGroup0" id="fileGroup0" class="file"
               onchange="return false;"
               accept=".png, .jpg, .jpeg" />
    </form>
</div>

<#--include::modals-->
<#include "/view/feature/user/my/_modal/_user_my_pw_chg_modal.ftlh">
<#-- Vue(UserMyPageApp)가 기존 user_my_module.js/user_my_pw_chg_module.js/user_my_page.js를 대체 -->
<script type="module" src="/vue/feature/user/my/UserMyPageApp.js?${releaseDate!}"></script>

</@layout.layout>
