<#--
 * Board > list (Vue: BoardAdminApp)
 -->

<#import "/layout/layout_with_aside.ftlh" as layout>

<#assign toolbar>
    <#include "/view/feature/board/group/_board_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <div class="card-header">
        <div class="mt-3 fs-5 fw-bold">
            <span class="opacity-75 me-5 cursor-help"
                  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                  title="<@spring.message 'txt.board.group.card.menu-id-tooltip'/>">
                <@spring.message 'txt.board.group.card.menu-id-label'/>
            </span>
            <span class="text-noti ls-1 text-decoration-underline">${SiteMenu.BOARD.id!}</span>
        </div>
    </div>
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed mb-3">
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="col-1 text-center wb-keepall w-5 hidden-table"><@spring.message 'txt.board.group.list.number'/></th>
                    <th class="col-lg-3 col-8 text-center wb-keepall"><@spring.message 'txt.board.group.list.board-code'/></th>
                    <th class="col-1 text-center wb-keepall w-15"><@spring.message 'txt.board.group.list.category-code'/></th>
                    <th class="col-4 text-center wb-keepall"><@spring.message 'txt.board.group.list.description'/></th>
                    <th class="col-1 text-center wb-keepall w-10"><@spring.message 'txt.board.group.list.post-count'/></th>
                    <th class="col-1 text-center wb-keepall w-5"><@spring.message 'txt.board.group.list.use-yn'/></th>
                    <th class="col-1 text-center wb-keepall w-5"><@spring.message 'txt.board.group.list.manage'/></th>
                </tr>
            </thead>
            <tbody id="board_group_list_div" class="draggable-zone">
                <#-- Vue(BoardAdminApp)가 행을 렌더링 -->
            </tbody>
        </table>
    </div>

    <div class="card-footer">
        <div id="board_group_pagination_div"></div>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value=""/>
</form>

<#include "/view/feature/board/group/_board_aside_base.ftlh">

<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#include "/view/feature/board/group/_modal/_board_reg_modal.ftlh">

<#assign _pgCurr = paginationInfo.currPageNo!1 />
<#assign _pgSize = searchParam.pageSize!10 />
<script id="board_group_list_data" type="application/json">
[
<#list boardList![] as board>
    {
        "rnum": ${(_pgCurr - 1) * _pgSize + board_index + 1},
        "id": ${board.id!0},
        "boardKey": "${(board.boardKey!'')?js_string}",
        "boardName": "${(board.boardName!'')?js_string}",
        "categoryGroupCode": "${(board.categoryGroupCode!'')?js_string}",
        "description": "${(board.description!'')?js_string}",
        "useYn": "${(board.useYn!'N')?js_string}",
        "postCount": ${board.postCount!0}
    }<#if board_has_next>,</#if>
</#list>
]
</script>

<script id="board_group_pagination_data" type="application/json">
{
    "currPageNo": ${paginationInfo.currPageNo!1},
    "lastPageNo": ${paginationInfo.lastPageNo!1},
    "totalCnt": ${paginationInfo.totalCnt!0},
    "pageSize": ${searchParam.pageSize!10},
    "isFirstPage": ${(paginationInfo.isFirstPage!false)?c},
    "isLastPage": ${(paginationInfo.isLastPage!false)?c},
    "prevPageNo": ${paginationInfo.prevPageNo!0},
    "nextPageNo": ${paginationInfo.nextPageNo!0}
}
</script>

<script>
    var Model = (function() {
        return {
            locale: "${.locale?replace('_', '-')?js_string}",
        };
    })();
</script>
<div id="board_admin_app" class="d-none"></div>
<script type="module" src="/vue/feature/admin/board/BoardAdminApp.js?${releaseDate!}"></script>

</@layout.layout>
