<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 등록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<style>
    @media (max-width: 768px) {
        #idDupChckBtn{
            font-size:12px;
        }
    }
</style>

<div id="user_form_app">
    <#-- Vue(UserFormApp)가 사용자 등록/수정 폼을 렌더링 -->
</div>

<script id="user_form_data" type="application/json">
{
    "id": <#if user.id??>${user.id}<#else>null</#if>,
    "fileGroupId": <#if user.fileGroupId??>${user.fileGroupId}<#else>null</#if>,
    "mode": "${(formMode!'')?js_string}",
    "username": "${(user.username!'')?js_string}",
    "nickname": "${(user.nickname!'')?js_string}",
    "emailId": "${(user.emailId!'')?js_string}",
    "emailDomain": "<#if isReg!false>gmail.com<#else>${(user.emailDomain!'')?js_string}</#if>",
    "phoneNumber": "${(user.phoneNumber!'')?js_string}",
    "roleKeyList": [
        <#list user.roleKeyList![] as roleKey>
            "${(roleKey!'')?js_string}"<#if roleKey_has_next>,</#if>
        </#list>
    ],
    "useAllowedIp": ${(user.useAllowedIp!false)?c},
    "allowedIpListStr": "${(user.allowedIpListStr!'')?js_string}",
    "content": "${(user.content!'')?js_string}",
    "hasProfile": ${(user.profile??)?c},
    "hasEmplym": ${(user.emplym??)?c},
    "profile": {
        "brthdy": "${((user.profile.brthdy)!'')?js_string}",
        "lunarYn": "${((user.profile.lunarYn)!'N')?js_string}",
        "proflCn": "${((user.profile.proflCn)!'')?js_string}"
    },
    "emplym": {
        "userNm": "${((user.emplym.userNm)!'')?js_string}",
        "emplymEmailId": "${((user.emplym.emplymEmailId)!'')?js_string}",
        "emplymEmailDomain": "${((user.emplym.emplymEmailDomain)!'')?js_string}",
        "emplymPhoneNumber": "${((user.emplym.emplymPhoneNumber)!'')?js_string}",
        "cmpyCd": "${((user.emplym.cmpyCd)!'')?js_string}",
        "teamCd": "${((user.emplym.teamCd)!'')?js_string}",
        "emplymCd": "${((user.emplym.emplymCd)!'')?js_string}",
        "rankCd": "${((user.emplym.rankCd)!'')?js_string}",
        "apntcYn": "${((user.emplym.apntcYn)!'N')?js_string}",
        "ecnyDt": "${((user.emplym.ecnyDt)!'')?js_string}",
        "retireYn": "${((user.emplym.retireYn)!'N')?js_string}",
        "retireDt": "${((user.emplym.retireDt)!'')?js_string}",
        "acntBank": "${((user.emplym.acntBank)!'')?js_string}",
        "acntNo": "${((user.emplym.acntNo)!'')?js_string}"
    }
}
</script>

<script id="user_form_role_data" type="application/json">
[
<#list roleList![] as auth>
    {
        "roleKey": "${(auth.roleKey!'')?js_string}",
        "roleName": "${(auth.roleName!'')?js_string}"
    }<#if auth_has_next>,</#if>
</#list>
]
</script>

<script id="user_form_cmpy_data" type="application/json">
[
<#list CMPY_CD![] as row>
    {
        "code": "${(row.code!'')?js_string}",
        "codeName": "${(row.codeName!'')?js_string}"
    }<#if row_has_next>,</#if>
</#list>
]
</script>

<script id="user_form_team_data" type="application/json">
[
<#list TEAM_CD![] as row>
    {
        "code": "${(row.code!'')?js_string}",
        "codeName": "${(row.codeName!'')?js_string}"
    }<#if row_has_next>,</#if>
</#list>
]
</script>

<script id="user_form_emplym_data" type="application/json">
[
<#list EMPLYM_CD![] as row>
    {
        "code": "${(row.code!'')?js_string}",
        "codeName": "${(row.codeName!'')?js_string}"
    }<#if row_has_next>,</#if>
</#list>
]
</script>

<script id="user_form_rank_data" type="application/json">
[
<#list RANK_CD![] as row>
    {
        "code": "${(row.code!'')?js_string}",
        "codeName": "${(row.codeName!'')?js_string}"
    }<#if row_has_next>,</#if>
</#list>
]
</script>

<script id="user_form_label_data" type="application/json">
{
    "username": "${springMacroRequestContext.getMessage('txt.user.form.username')?js_string}",
    "usernameReq": "${springMacroRequestContext.getMessage('txt.req.username')?js_string}",
    "password": "${springMacroRequestContext.getMessage('txt.user.form.password')?js_string}",
    "passwordReq": "${springMacroRequestContext.getMessage('txt.req.password')?js_string}",
    "passwordConfirm": "${springMacroRequestContext.getMessage('txt.user.form.password-confirm')?js_string}",
    "role": "${springMacroRequestContext.getMessage('txt.user.form.role')?js_string}",
    "nickname": "${springMacroRequestContext.getMessage('txt.user.form.nickname')?js_string}",
    "nicknameReq": "${springMacroRequestContext.getMessage('txt.req.nicknm')?js_string}",
    "emailIdPlaceholder": "${springMacroRequestContext.getMessage('txt.user.form.email-id-placeholder')?js_string}",
    "customInput": "${springMacroRequestContext.getMessage('txt.user.form.custom-input')?js_string}",
    "dupCheck": "${springMacroRequestContext.getMessage('txt.user.form.dup-check')?js_string}",
    "phoneNumber": "${springMacroRequestContext.getMessage('txt.user.form.phone-number')?js_string}",
    "phoneReq": "${springMacroRequestContext.getMessage('txt.req.phoneNumber')?js_string}",
    "allowedIp": "${springMacroRequestContext.getMessage('txt.user.form.allowed-ip')?js_string}",
    "allowedIpReq": "${springMacroRequestContext.getMessage('txt.req.acsip')?js_string}",
    "accountDescription": "${springMacroRequestContext.getMessage('txt.user.form.account-description')?js_string}",
    "accountDescriptionPlaceholder": "${springMacroRequestContext.getMessage('txt.user.form.account-description-placeholder')?js_string}",
    "use": "${springMacroRequestContext.getMessage('txt.status.use')?js_string}",
    "unuse": "${springMacroRequestContext.getMessage('txt.status.unuse')?js_string}",
    "addProfile": "${springMacroRequestContext.getMessage('txt.user.form.add-profile')?js_string}",
    "addEmployment": "${springMacroRequestContext.getMessage('txt.user.form.add-employment')?js_string}",
    "userEmplymReq": "${springMacroRequestContext.getMessage('txt.req.user.emplym')?js_string}",
    "removeProfile": "프로필 정보 삭제-",
    "removeEmployment": "직원 인사정보 삭제-",
    "profileAddress": "${springMacroRequestContext.getMessage('txt.user.profile.address')?js_string}",
    "profileBirthDate": "${springMacroRequestContext.getMessage('txt.user.profile.birth-date')?js_string}",
    "profileLunarYn": "${springMacroRequestContext.getMessage('txt.user.profile.lunar-yn')?js_string}",
    "profileLunar": "${springMacroRequestContext.getMessage('txt.user.profile.lunar')?js_string}",
    "profileSolar": "${springMacroRequestContext.getMessage('txt.user.profile.solar')?js_string}",
    "profileProfile": "${springMacroRequestContext.getMessage('txt.user.profile.profile')?js_string}",
    "emplymUserName": "${springMacroRequestContext.getMessage('txt.user.emplym.user-name')?js_string}",
    "emplymNamePlaceholder": "${springMacroRequestContext.getMessage('txt.user.emplym.name-placeholder')?js_string}",
    "emplymEmail": "${springMacroRequestContext.getMessage('txt.user.emplym.email')?js_string}",
    "emplymPhoneNumber": "${springMacroRequestContext.getMessage('txt.user.emplym.phone-number')?js_string}",
    "emplymAffiliation": "${springMacroRequestContext.getMessage('txt.user.emplym.affiliation')?js_string}",
    "emplymCompanyOption": "${springMacroRequestContext.getMessage('txt.user.emplym.company-option')?js_string}",
    "emplymTeamOption": "${springMacroRequestContext.getMessage('txt.user.emplym.team-option')?js_string}",
    "emplymEmploymentTypeOption": "${springMacroRequestContext.getMessage('txt.user.emplym.employment-type-option')?js_string}",
    "emplymRank": "${springMacroRequestContext.getMessage('txt.user.emplym.rank')?js_string}",
    "emplymSelectOption": "${springMacroRequestContext.getMessage('txt.user.emplym.select-option')?js_string}",
    "emplymProbation": "${springMacroRequestContext.getMessage('txt.user.emplym.probation')?js_string}",
    "emplymProbationActive": "${springMacroRequestContext.getMessage('txt.user.emplym.probation.active')?js_string}",
    "emplymNotApplicable": "${springMacroRequestContext.getMessage('txt.user.emplym.not-applicable')?js_string}",
    "emplymJoinDate": "${springMacroRequestContext.getMessage('txt.user.emplym.join-date')?js_string}",
    "emplymJoinDateGuide": "${springMacroRequestContext.getMessage('txt.user.emplym.join-date-guide')?js_string}",
    "emplymRetiredYn": "${springMacroRequestContext.getMessage('txt.user.emplym.retired-yn')?js_string}",
    "emplymRetired": "${springMacroRequestContext.getMessage('txt.user.emplym.retired')?js_string}",
    "emplymRetiredDate": "${springMacroRequestContext.getMessage('txt.user.emplym.retired-date')?js_string}",
    "emplymRetiredDateGuide": "${springMacroRequestContext.getMessage('txt.user.emplym.retired-date-guide')?js_string}",
    "emplymPayrollAccount": "${springMacroRequestContext.getMessage('txt.user.emplym.payroll-account')?js_string}",
    "emplymBank": "${springMacroRequestContext.getMessage('txt.user.emplym.bank')?js_string}",
    "emplymAccountNumber": "${springMacroRequestContext.getMessage('txt.user.emplym.account-number')?js_string}",
    "save": "저장",
    "list": "목록",
    "tooltipUsernameDupCheck": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.username-dup-check')?js_string}",
    "tooltipRole": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.role')?js_string}",
    "tooltipNickname": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.nickname')?js_string}",
    "tooltipEmail": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.email')?js_string}",
    "tooltipEmailDupCheck": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.email-dup-check')?js_string}",
    "tooltipAllowedIp": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.allowed-ip')?js_string}",
    "tooltipAccountDescription": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.account-description')?js_string}",
    "tooltipAddProfile": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.add-profile')?js_string}",
    "tooltipAddEmployment": "${springMacroRequestContext.getMessage('bs.tooltip.user.form.add-employment')?js_string}",
    "tooltipSave": "${springMacroRequestContext.getMessage('bs.tooltip.save')?js_string}",
    "tooltipList": "${springMacroRequestContext.getMessage('bs.tooltip.page.list')?js_string}"
}
</script>

<#--include::user_emplym_modules-->
<#-- Vue(UserFormApp)가 기존 _user_emplym_regist_template.hbs/user_emplym_module.js를 대체 -->
<#--include::user_profile_modules-->
<#-- Vue(UserFormApp)가 기존 _user_profile_regist_template.hbs/user_profile_module.js를 대체 -->

<!--include::page scripts-->
<script type="module" src="/vue/feature/user/account/UserFormApp.js?${releaseDate!}"></script>

</@layout.layout>
