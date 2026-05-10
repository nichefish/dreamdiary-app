<#--
 * 사용자 관리 > 계정 신청 화면 — Vue(UserSignupApp)
 * 변경 전: Handlebars + user_reqst_module.js / user_module.js 등 레거시 스크립트 다중 로드.
 * 변경 후: 부트스트랩 JSON + 단일 Vue 모듈. 뷰 파일명·경로는 `signup` 계열로 통일(user_signup_page.ftlh).
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<style>
    @media (max-width: 768px) {
        #idDupChckBtn{
            font-size:12px;
        }
    }
</style>

<script id="user_signup_page_data" type="application/json">
{
    "staffRankCd": "${Code.RANK_STAFF?js_string}",
    "authUserRoleKey": "${Code.AUTH_USER?js_string}",
    "isReg": ${(isReg!false)?c},
    "siteAcs": {
        "upperMenuNm": "${(siteAcsInfo.upperMenuNm!'')?js_string}",
        "menuName": "${(siteAcsInfo.menuName!'')?js_string}",
        "pageName": "${(siteAcsInfo.pageName!'')?js_string}"
    },
    "codeLists": {
        "CMPY_CD": [
            <#list CMPY_CD![] as row>
            {"code": "${row.code!''?js_string}", "codeName": "${row.codeName!''?js_string}"}<#if row_has_next>,</#if>
            </#list>
        ],
        "TEAM_CD": [
            <#list TEAM_CD![] as row>
            {"code": "${row.code!''?js_string}", "codeName": "${row.codeName!''?js_string}"}<#if row_has_next>,</#if>
            </#list>
        ],
        "EMPLYM_CD": [
            <#list EMPLYM_CD![] as row>
            {"code": "${row.code!''?js_string}", "codeName": "${row.codeName!''?js_string}"}<#if row_has_next>,</#if>
            </#list>
        ],
        "JOB_TITLE_CD": [
            <#list JOB_TITLE_CD![] as row>
            {"code": "${row.code!''?js_string}", "codeName": "${row.codeName!''?js_string}"}<#if row_has_next>,</#if>
            </#list>
        ]
    },
    "userDefaults": {
        "id": <#if user.id??>${user.id?c}<#else>null</#if>,
        "fileGroupId": "${(user.fileGroupId!'')?js_string}",
        "nickname": "${(user.nickname!'')?js_string}",
        "username": "${(user.username!'')?js_string}",
        "emailId": "${(user.emailId!'')?js_string}",
        "emailDomain": "<#if isReg!false>gmail.com<#else>${(user.emailDomain!'')?js_string}</#if>",
        "phoneNumber": "${(user.phoneNumber!'')?js_string}",
        "content": "${(user.content!'')?js_string}"
    }
}
</script>

<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>

<div id="user_signup_app"></div>

<script type="module" src="/vue/feature/user/signup/UserSignupApp.js?${releaseDate!}"></script>

</@layout.layout>
