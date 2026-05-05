<#--
 * 사이트 관리 (Vue: AdminPageApp)
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#assign currYy = DateUtils.getCurrYyStr()! />

<script defer="defer" src="/static/node_modules/sass/sass.js"></script>

<@layout.layout toolbar=toolbar!"">

<script id="admin_page_meta_data" type="application/json">
{
    "authMngrKey": "${Code.AUTH_MNGR?js_string}",
    "authUserKey": "${Code.AUTH_USER?js_string}",
    "authDevKey": "${Code.AUTH_DEV?js_string}",
    "currYy": ${currYy?number}
}
</script>

<script id="admin_page_role_list_data" type="application/json">
[
<#list roleList![] as r>
    {
        "id": ${r.id!0},
        "roleKey": "${(r.roleKey!'')?js_string}",
        "roleName": "${(r.roleName!'')?js_string}",
        "authLevel": <#if r.authLevel??>${r.authLevel}<#else>null</#if>,
        "parentRoleId": <#if r.parentRoleId??>${r.parentRoleId}<#else>null</#if>,
        "sortOrder": <#if r.sortOrder??>${r.sortOrder}<#else>null</#if>,
        "useYn": "${(r.useYn!'N')?js_string}"
    }<#if r_has_next>,</#if>
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

<div id="admin_page_app"></div>

<form name="procForm" id="procForm" method="get">

</form>

<#include "/view/feature/admin/admin_aside_base.ftlh">

<#include "/view/feature/admin/cache/_modal/_cache_list_modal.ftlh">
<#include "/view/feature/admin/cache/_modal/_cache_dtl_modal.ftlh">

<script type="text/javascript" src="/js/view/feature/admin/cache/_cache.js?${releaseDate!}"></script>

<script type="module" src="/vue/admin/site/AdminPageApp.js?${releaseDate!}"></script>

</@layout.layout>
