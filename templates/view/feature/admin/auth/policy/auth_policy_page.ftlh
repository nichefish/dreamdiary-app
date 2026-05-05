<#--
 * 인증 정책(auth_policy) > 관리 화면 — Vue(AuthPolicyApp)
 -->

<#import "/layout/layout_default.ftlh" as layout >

<script defer="defer" src="/static/node_modules/sass/sass.js"></script>

<@layout.layout toolbar=toolbar!"">

<script id="auth_policy_page_data" type="application/json">
{
    "id": <#if authPolicy.id??>${authPolicy.id}<#else>null</#if>,
    "inactiveLockDays": <#if authPolicy.inactiveLockDays??>${authPolicy.inactiveLockDays}<#else>null</#if>,
    "loginAttemptLimit": <#if authPolicy.loginAttemptLimit??>${authPolicy.loginAttemptLimit}<#else>null</#if>,
    "loginAttemptWindowMinutes": <#if authPolicy.loginAttemptWindowMinutes??>${authPolicy.loginAttemptWindowMinutes}<#else>null</#if>,
    "accountLockDurationMinutes": <#if authPolicy.accountLockDurationMinutes??>${authPolicy.accountLockDurationMinutes}<#else>null</#if>,
    "passwordChangeCycleDays": <#if authPolicy.passwordChangeCycleDays??>${authPolicy.passwordChangeCycleDays}<#else>null</#if>,
    "passwordResetTokenExpiryMinutes": <#if authPolicy.passwordResetTokenExpiryMinutes??>${authPolicy.passwordResetTokenExpiryMinutes}<#else>null</#if>
}
</script>

<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>

<div class="card post">
    <div id="auth_policy_app"></div>
</div>

<script type="module" src="/vue/admin/auth/policy/AuthPolicyApp.js?${releaseDate!}"></script>

</@layout.layout>
