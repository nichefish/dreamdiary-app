<#--
 * 분류 코드(CL_CD) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/cd/_cl_cd_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<#include "/view/domain/admin/cd/_cl_cd_scripts.ftlh">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
                commons.util.initSortTable();
                /* init : Draggable */
                ClCd.initDraggable();
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <!--begin::Card Body-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="col-1 text-center wb-keepall cursor-help w-8 hidden-table"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                        정렬
                        <i class="bi bi-question-circle"></i>
                    </th>
                    <th class="col-3 text-center wb-keepall">분류 코드</th>
                    <th class="col-4 text-center wb-keepall">분류 코드 설명</th>
                    <th class="col-1 text-center wb-keepall">하위 상세 코드 수</th>
                    <th class="col-1 text-center wb-keepall">사용 여부</th>
                    <th class="col-1 text-center wb-keepall">관리</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="draggable-zone">
                <#--begin::목록-->
                <#list clCdList![] as clCd>
                    <tr class="sortable-item draggable bg-hover-secondary" id="${clCd.clCd!}"
                        onclick="ClCd.dtl('${clCd.clCd!}');">
                        <td class="text-center hidden-table">
                            <div class="draggable-handle d-flex-center cursor-move pt-2"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                                <i class="ki-duotone ki-abstract-14 fs-2x">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            </div>
                        </td>
                        <td class="text-start px-6 cursor-pointer fw-bold text-gray-700">
                            <#if clCd.clCtgrNm??>
                                <span class="fw-bolder fs-2 ctgr-span ctgr-imprtc">
                                    ${clCd.clCtgrNm!}
                                </span>
                            </#if>
                            <span>${clCd.clCdNm!}</span>
                            <span class="ms-1">(${clCd.clCd!})</span>
                            <a class="badge badge-secondary ms-2 p-2 btn-white blank blink-slow float-end"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="모달로 보기">
                                <i class="bi bi-stickies fs-5 text-noti opacity-hover"
                                   onclick="ClCd.dtlModal('${clCd.clCd!}');"></i>
                            </a>
                        </td>
                        <td class="text-start ps-6 hidden-table">
                            ${clCd.dc!}
                        </td>
                        <td class="text-center hidden-table">
                            ${clCd.dtlCdCnt!0}
                        </td>
                        <td class="text-center hidden-table">
                            <#if ((clCd.state.useYn)!"N") == "Y">
                                <div class="badge badge-success cursor-pointer"
                                     onclick="ClCd.unuseAjax('${clCd.clCd}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                     title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-check text-light blink-slow"></i>사용
                                </div>
                            <#else>
                                <div class="badge badge-secondary btn-white cursor-pointer"
                                     onclick="ClCd.useAjax('${clCd.clCd}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                     title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-x blink-slow"></i>미사용
                                </div>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            <div class="badge badge-danger badge-outlined cursor-pointer"
                                 onclick="ClCd.delAjax('${clCd.clCd}');"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                 title="<@spring.message "bs.tooltip.del"/>">
                                <i class="bi bi-trash text-danger fs-7 blink-slow"></i>삭제
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
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="clCd" id="clCd" />
</form>

<#--include::modals-->
<#include "/view/domain/admin/cd/_cl_cd_reg_modal.ftlh">
<#include "/view/domain/admin/cd/_cl_cd_dtl_modal.ftlh">

</@layout.layout>