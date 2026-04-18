<#--
 * 코드 그룹 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign toolbar>
    <#include "/view/feature/admin/code/_code_group_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="col-1 text-center wb-keepall cursor-help w-8 hidden-table">순번</th>
                    <th class="col-3 text-center wb-keepall">분류 코드</th>
                    <th class="col-4 text-center wb-keepall">분류 코드 설명</th>
                    <th class="col-1 text-center wb-keepall">하위 상세 코드 수</th>
                    <th class="col-1 text-center wb-keepall">사용 여부</th>
                    <th class="col-1 text-center wb-keepall">관리</th>
                </tr>
            </thead>
            <tbody id="code_group_list_div" class="draggable-zone">
                <#-- Handlebars template renders here -->
            </tbody>
        </table>
    </div>

    <div class="card-footer">
        <#include "/view/global/_pagination.ftlh">
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="groupCode" id="groupCode" />
</form>

<script id="code_group_list_data" type="application/json">
[
<#list codeGroupList![] as codeGroup>
    {
        "rnum": ${codeGroup.rnum!0},
        "id": ${codeGroup.id!0},
        "groupCode": "${(codeGroup.groupCode!'')?js_string}",
        "groupName": "${(codeGroup.groupName!'')?js_string}",
        "description": "${(codeGroup.description!'')?js_string}",
        "codeItemCnt": ${codeGroup.codeItemCnt!0},
        "useYn": "${(codeGroup.useYn!'N')?js_string}"
    }<#if codeGroup_has_next>,</#if>
</#list>
]
</script>

<#include "/view/feature/admin/code/_code_group_list_template.hbs">

<#--include::draggable-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#--include::code_group_modules-->
<#include "/view/feature/admin/code/_modal/_code_group_reg_modal.ftlh">
<#include "/view/feature/admin/code/_modal/_code_group_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/admin/code/code_group_module.js"></script>

<#--include::code_item_modules-->
<#include "/view/feature/admin/code/_modal/_code_item_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/admin/code/code_item_module.js"></script>

<script type="text/javascript" src="/js/view/feature/admin/code/code_group_list.js"></script>

</@layout.layout>
