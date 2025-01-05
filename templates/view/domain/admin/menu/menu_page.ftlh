<#--
 -- 메뉴(menu) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/menu/_menu_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/lib/jstree/css/style.min.css" />

<style>
    div.card > ul > li {
        padding-top: 1.2em;
        padding-bottom: 1.2em;
    }
    div.card > ul > li:not(:first-child) {
        border-top: 2px solid #e0e0e0;
    }
</style>

<div class="row mt-2 mb-6 px-10 draggable-zone-main" id="menu_main_card_div">
    <#--handlebars template goes in here-->
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">

</form>

<#include "/view/domain/admin/menu/_menu_main_card_template.hbs">
<#include "/view/domain/admin/menu/_menu_sub_template.hbs">
<#include "/view/domain/admin/menu/_modal/_menu_reg_modal.ftlh">
<#--include::script modules-->
<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/lib/jstree/js/jstree.min.js"></script>
<script type="text/javascript" src="/js/common/helper/jstree.js"></script>
<script type="text/javascript" src="/js/view/domain/admin/menu/menu_module.js"></script>
<script type="text/javascript" src="/js/view/domain/admin/menu/menu_page.js"></script>

</@layout.layout>