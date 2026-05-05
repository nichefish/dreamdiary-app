<#--
 -- 메뉴(menu) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/admin/menu/_menu_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/lib/jstree/css/style.min.css" />

<style>
    .draggable-handle-sub * {
    pointer-events: none;
}
    div.card > ul > li {
        padding-top: 1.2em;
        padding-bottom: 1.2em;
    }
    div.card > ul > li:not(:first-child) {
        border-top: 2px solid #e0e0e0;
    }
</style>

<ul class="row mt-2 mb-6 px-10 draggable-zone-main" id="menu_main_card_div">
    <#-- Vue(MenuAdminApp > MenuTreeNode)가 메뉴 트리를 렌더링 -->
</ul>
<!--end::Card-->

<#--include::draggable-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#--include::jstree-->
<script type="text/javascript" src="/lib/jstree/js/jstree.min.js"></script>
<script type="text/javascript" src="/js/common/helper/jstree.js"></script>

<#include "/view/feature/admin/menu/_modal/_menu_reg_modal.ftlh">

<#-- LoginFormApp 등과 동일: ES module(MenuAdminApp)이 i18n용 로케일을 읽을 수 있게 window 에 노출(var Model). -->
<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>
<script id="menu_submenu_expand_types_data" type="application/json">
[
<#list SUBMENU_EXPAND_TYPES![] as row>
    { "code": "${(row.code!'')?js_string}", "codeName": "${(row.codeName!'')?js_string}" }<#if row_has_next>,</#if>
</#list>
]
</script>
<div id="menu_admin_app" class="d-none"></div>
<script type="module" src="/vue/feature/admin/menu/MenuAdminApp.js?${releaseDate!}"></script>

</@layout.layout>
