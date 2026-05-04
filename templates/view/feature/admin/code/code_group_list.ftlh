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
                    <th class="col-1 text-center wb-keepall cursor-help w-8 hidden-table"><@spring.message 'txt.admin.code.group.detail.number'/></th>
                    <th class="col-3 text-center wb-keepall"><@spring.message 'txt.admin.code.group.form.group-code'/></th>
                    <th class="col-4 text-center wb-keepall"><@spring.message 'txt.admin.code.group.form.group-description'/></th>
                    <th class="col-1 text-center wb-keepall"><@spring.message 'txt.admin.code.group.list.item-count'/></th>
                    <th class="col-1 text-center wb-keepall"><@spring.message 'txt.admin.code.group.form.use-yn'/></th>
                    <th class="col-1 text-center wb-keepall"><@spring.message 'txt.admin.code.group.detail.manage'/></th>
                </tr>
            </thead>
            <tbody id="code_group_list_div" class="draggable-zone">
                <#-- Vue(CodeAdminApp)가 행을 렌더링 -->
            </tbody>
        </table>
    </div>

    <div class="card-footer">
        <div id="code_group_pagination_div"></div>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <#-- id=groupCode 는 등록 모달(CodeGroupRegistForm) 내 입력과 중복되면 document.querySelector("#groupCode") 가 잘못된 노드를 가리킨다. -->
    <input type="hidden" name="groupCode" id="code_proc_groupCode" />
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

<script id="code_group_pagination_data" type="application/json">
{
    "currPageNo": ${paginationInfo.currPageNo!1},
    "lastPageNo": ${paginationInfo.lastPageNo!1},
    "totalCnt": ${paginationInfo.totalCnt!0},
    "pageSize": ${searchParam.pageSize!10},
    <#-- ! 가 ?c 보다 낮아서 묶지 않으면 isFirstPage!(false?c)로 해석되고, 값이 있으면 boolean이 ${}에 그대로 들어가 formatBoolean 오류가 남 -->
    "isFirstPage": ${(paginationInfo.isFirstPage!false)?c},
    "isLastPage": ${(paginationInfo.isLastPage!false)?c},
    "prevPageNo": ${paginationInfo.prevPageNo!0},
    "nextPageNo": ${paginationInfo.nextPageNo!0}
}
</script>

<#--include::draggable-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#--include::code_group_modules-->
<#include "/view/feature/admin/code/_modal/_code_group_regist_modal.ftlh">
<#include "/view/feature/admin/code/_modal/_code_group_detail_modal.ftlh">

<#--include::code_item_modules-->
<#include "/view/feature/admin/code/_modal/_code_item_regist_modal.ftlh">
<#-- LoginFormApp 등과 동일: ES module(CodeAdminApp)이 i18n용 로케일을 읽을 수 있게 window 에 노출(var Model). -->
<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>
<div id="code_admin_app" class="d-none"></div>
<script type="module" src="/vue/admin/code/CodeAdminApp.js?${releaseDate!}"></script>

</@layout.layout>
