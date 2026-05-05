<#--
 * 사용자 관리 > 계정 신청 승인관리 화면 (Vue)
 *
 * 변경 전: 계정관리(user_list)에 승인 대기 카드가 인라인으로 포함되어 화면 역할이 혼재.
 * 변경 후: 승인관리 전용 Vue 엔트리로 분리.
 * -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
</#assign>

<@layout.layout toolbar=toolbar!"">

<script id="user_signup_approval_data" type="application/json">
{
    "siteAcs": {
        "upperMenuNm": "${(siteAcsInfo.upperMenuNm!'')?js_string}",
        "menuName": "${(siteAcsInfo.menuName!'')?js_string}",
        "pageName": "${(siteAcsInfo.pageName!'')?js_string}"
    },
    "messages": {
        "emptyList": "${springMacroRequestContext.getMessage('view.list.empty')?js_string}",
        "pendingTitle": "${springMacroRequestContext.getMessage('txt.user.list.pending-signup-requests')?js_string}",
        "actionApprove": "${springMacroRequestContext.getMessage('txt.user.list.action.approve')?js_string}",
        "actionReject": "${springMacroRequestContext.getMessage('txt.user.list.action.reject')?js_string}",
        "statusPending": "PENDING",
        "statusApproved": "APPROVED",
        "statusRejected": "REJECTED"
    },
    "pendingList": [
    <#list pendingSignupReqList![] as req>
        {
            "id": ${req.id!0},
            "username": "${(req.username!'')?js_string}",
            "nickname": "${(req.nickname!'')?js_string}",
            "email": "${(req.email!'')?js_string}",
            "status": "${(req.status!'')?js_string}",
            "createdAt": "${(req.createdAt?string('yyyy-MM-dd HH:mm')!'')?js_string}"
        }<#if req_has_next>,</#if>
    </#list>
    ],
    "recentList": [
    <#list recentSignupReqList![] as req>
        {
            "id": ${req.id!0},
            "username": "${(req.username!'')?js_string}",
            "nickname": "${(req.nickname!'')?js_string}",
            "email": "${(req.email!'')?js_string}",
            "status": "${(req.status!'')?js_string}",
            "createdAt": "${(req.createdAt?string('yyyy-MM-dd HH:mm')!'')?js_string}"
        }<#if req_has_next>,</#if>
    </#list>
    ]
}
</script>

<div id="user_signup_approval_app"></div>

<script type="module" src="/vue/feature/user/signup/UserSignupApprovalApp.js?${releaseDate!}"></script>

</@layout.layout>
