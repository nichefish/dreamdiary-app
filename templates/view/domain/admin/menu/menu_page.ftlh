<#--
 -- 메뉴(menu) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/menu/_menu_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<script type="text/javascript" src="/lib/jstree/js/jstree.min.js"></script>
<script type="text/javascript" src="/js/common/helper/jstree.js"></script>
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<#include "/view/domain/admin/menu/_menu_scripts.ftlh">

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

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                Page.updtTree();

                /* init : Draggable */
                Menu.initDraggable();
            },
            /**
             * 트리 구조 데이터 리로드 및 새로 그리기
             */
            updtTree: function() {
                // 메인 메뉴 그리기
                const url = "${Url.MENU_MAIN_LIST_AJAX!}";
                cF.util.blockUIAjax(url, 'GET', null, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const mainMenuList = res.rsltList;
                    if (!Array.isArray(mainMenuList) || mainMenuList.length < 1) return;
                    mainMenuList.forEach(function(mainMenu) {
                        cF.handlebars.append(mainMenu, "menu_main_card");
                        Page.drawSubMenu(mainMenu);
                    });
                });
            },
            /** 서브메뉴 처리 :: 자기 자신을 그리고 각각의 서브메뉴에 대해서 재귀호출 */
            drawSubMenu: function(upperMenu) {
                // 서브메뉴 돌면서 재귀호출
                const subMenuList = upperMenu.subMenuList;
                if (!Array.isArray(subMenuList) || subMenuList.length <1) return;
                const menuNo = upperMenu.menuNo;
                subMenuList.forEach(function(subMenu) {
                    cF.handlebars.appendTo(subMenu, "menu_sub", "menu_sub_"+menuNo);
                    Page.drawSubMenu(subMenu);
                });
            },
            drawTree: function() {
                const jstreeContainer = Page.jstreeContainer;
                cF.jstree.reset(jstreeContainer);
                cF.jstree.init(jstreeContainer);     // 조직도 트리구조 세팅
                // 부서 조회
                // let deptUrl = "/manage/user/totalOrgnztChartAjax";
                // cF.util.blockUIAjax(deptUrl, "get", {}, function(res) {
                //     if (!res.rslt) return;
                //     $(jstreeContainer).jstree(true).settings.core.data = res.rsltList;
                //     $(jstreeContainer).jstree(true).settings.core.expand_selected_onload = true;
                //     $(jstreeContainer).jstree(true).refresh(false, true);
                // });
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<div class="row mt-2 mb-6 px-10 draggable-zone-main" id="menu_main_card_div">
    <#--handlebars template goes in here-->
</div>
<!--end::Card-->

<form name="procForm" id="procForm" method="get">

</form>

<#include "/view/domain/admin/menu/_menu_main_card_template.hbs">
<#include "/view/domain/admin/menu/_menu_sub_template.hbs">
<#include "/view/domain/admin/menu/_menu_reg_modal.ftlh">

</@layout.layout>