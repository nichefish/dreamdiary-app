<#--
 * 게시판 정의(board_def) > 목록 화면
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/board/def/_board_def_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card header-->
    <div class="card-header">
        <div class="mt-3 fs-5 fw-bold">
            <span class="opacity-75 me-5 cursor-help"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                  title="사용자 포탈에 표시되는&#10;'게시판' 대메뉴 번호입니다.&#10;게시판 메뉴 번호 작성시&#10;해당 번호 아래에 둘 것을&#10;권장합니다.">
                ※ 현재 게시판 상위메뉴 번호 : </span>
            <span class="text-noti ls-1 text-decoration-underline">${SiteMenu.BOARD.menuNo!}</span>
        </div>
    </div>
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="col-1 text-center wb-keepall w-5 hidden-table">번호</th>
                    <th class="col-lg-3 col-8 text-center wb-keepall">게시판 코드</th>
                    <th class="col-1 text-center wb-keepall w-15">글분류 코드</th>
                    <th class="col-4 text-center wb-keepall">게시판 설명</th>
                    <th class="col-1 text-center wb-keepall w-10">글 갯수</th>
                    <th class="col-1 text-center wb-keepall w-5">사용 여부</th>
                    <th class="col-1 text-center wb-keepall w-5">-</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="draggable-zone">
                <#list boardDefMngList![] as boardDef>
                    <tr id="${boardDef.boardCd!}" class="sortable-item draggable">
                        <td class="text-center hidden-table">
                            <div class="draggable-handle d-flex-center cursor-move pt-2"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                                <i class="ki-duotone ki-abstract-14 fs-2x">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            </div>
                        </td>
                        <td class="text-start ps-15 fw-bold text-gray-700">
                            <span class="fw-bold">${boardDef.boardNm!}</span>
                            <span class="ms-1">(${boardDef.boardCd!})</span>
                            <a class="badge badge-secondary ms-2 p-2 btn-primary blank blink-slow float-end"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                               title="항목 수정 팝업을 호출합니다.">
                                <i class="bi bi-pencil-square fs-5 text-noti" onclick="dF.BoardDef.mdfModal('${boardDef.boardCd!}');"></i>
                            </a>
                        </td>
                        <td class="text-center hidden-table text-gray-700">
                            ${boardDef.ctgrClCd!}
                        </td>
                        <td class="text-center hidden-table">
                            <div class="w-100 text-truncate">${boardDef.dc!}</div>
                        </td>
                        <td class="text-center hidden-table">
                            -
                        </td>
                        <td class="text-center hidden-table">
                            <#if (boardDef.state.useYn!"Y") == "Y">
                                <div class="badge badge-success cursor-pointer" onclick="dF.BoardDef.unuseAjax('${boardDef.boardCd!}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-check text-light blink-slow "></i>사용
                                </div>
                            <#else>
                                <div class="badge badge-secondary cursor-pointer" onclick="dF.BoardDef.useAjax('${boardDef.boardCd!}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-x blink-slow "></i>미사용
                                </div>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            <div class="badge badge-danger badge-outlined cursor-pointer" onclick="dF.BoardDef.delAjax('${boardDef.boardCd!}');"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.del"/>">
                                <i class="bi bi-trash text-danger"></i>삭제
                            </div>
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan=8" class="text-center"><@spring.message "view.list.empty"/></td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value=""/>
    <input type="hidden" name="boardCd" id="boardCd" value="${boardCd!}"/>
</form>

<#--include:: Aside-->
<#include "/view/domain/board/def/_board_def_aside_base.ftlh">
<#--include::modals-->
<#include "/view/domain/board/def/_modal/_board_def_reg_modal.ftlh">
<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/board/def/board_def_moodule.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/board/def/board_def_list.js?${releaseDate!}"></script>

</@layout.layout>

