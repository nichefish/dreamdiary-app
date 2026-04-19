<#--
 * Board > list
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
                  title="현재 게시판 상위 메뉴 번호입니다.">
                현재 게시판 상위메뉴 번호 :
            </span>
            <span class="text-noti ls-1 text-decoration-underline">${SiteMenu.BOARD.id!}</span>
        </div>
    </div>
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed mb-3">
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="col-1 text-center wb-keepall w-5 hidden-table">번호</th>
                    <th class="col-lg-3 col-8 text-center wb-keepall">게시판 코드</th>
                    <th class="col-1 text-center wb-keepall w-15">글분류 코드</th>
                    <th class="col-4 text-center wb-keepall">게시판 설명</th>
                    <th class="col-1 text-center wb-keepall w-10">글 개수</th>
                    <th class="col-1 text-center wb-keepall w-5">사용 여부</th>
                    <th class="col-1 text-center wb-keepall w-5">-</th>
                </tr>
            </thead>
            <tbody class="draggable-zone">
                <#list boardList![] as board>
                    <tr id="board-${board.id!}" data-id="${board.id!}" class="sortable-item draggable">
                        <td class="text-center hidden-table">
                            <div class="draggable-handle d-flex-center cursor-move pt-2"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여 순서를 이동합니다.">
                                <i class="ki-duotone ki-abstract-14 fs-2x">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            </div>
                        </td>
                        <td class="text-start ps-15 fw-bold text-gray-700">
                            <span class="fw-bold">${board.boardName!}</span>
                            <span class="ms-1">(${board.boardKey!})</span>
                            <a class="badge badge-secondary ms-2 p-2 btn-primary blank blink-slow float-end"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                               title="항목 수정 팝업을 엽니다.">
                                <i class="bi bi-pencil-square fs-5 text-noti" onclick="dF.Board.mdfModal(${board.id!});"></i>
                            </a>
                        </td>
                        <td class="text-center hidden-table text-gray-700">
                            ${board.categoryGroupCode!}
                        </td>
                        <td class="text-center hidden-table">
                            <div class="w-100 text-truncate">${board.description!}</div>
                        </td>
                        <td class="text-center hidden-table">
                            -
                        </td>
                        <td class="text-center hidden-table">
                            <#if (board.useYn!"Y") == "Y">
                                <div class="badge badge-success cursor-pointer" onclick="dF.Board.unuseAjax(${board.id!});"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="사용 상태를 변경합니다.">
                                    <i class="bi bi-check text-light blink-slow"></i><@spring.message 'txt.status.use'/>
                                </div>
                            <#else>
                                <div class="badge badge-secondary cursor-pointer" onclick="dF.Board.useAjax(${board.id!});"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="사용 상태를 변경합니다.">
                                    <i class="bi bi-x blink-slow"></i><@spring.message 'txt.status.unuse'/>
                                </div>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            <div class="badge badge-danger badge-outlined cursor-pointer" onclick="dF.Board.delAjax(${board.id!});"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.del'/>">
                                <i class="bi bi-trash text-danger"></i>삭제
                            </div>
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan="8" class="text-center"><@spring.message 'view.list.empty'/></td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value=""/>
</form>

<#include "/view/feature/board/group/_board_aside_base.ftlh">

<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>

<#include "/view/feature/board/group/_modal/_board_reg_modal.ftlh">
<script type="text/javascript" src="/js/view/feature/board/group/board_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/board/group/board_list.js?${releaseDate!}"></script>

</@layout.layout>
